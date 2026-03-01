/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.checks.net;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.CombinedConfig;
import fr.neatmonster.nocheatplus.checks.combined.EvidenceFusionProfile;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;
import fr.neatmonster.nocheatplus.players.IPlayerData;
import fr.neatmonster.nocheatplus.utilities.TickTask;

/**
 * Fall-back check for pre 1.9: Limit the overall packet frequency, aiming at
 * crash exploits only. Thus the default actions should be kicking, as this
 * won't distinguish type of packets with the global packet count.
 * 
 * @author asofold
 *
 */
public class PacketFrequency extends Check {

    private static final long EVIDENCE_COOLDOWN_MS = 120L;
    private static final double EVIDENCE_STAGE2_EXCESS = 15.0;
    private static final double EVIDENCE_STAGE3_EXCESS = 60.0;

    public PacketFrequency() {
        super(CheckType.NET_PACKETFREQUENCY);
    }

    /**
     * Actual state.
     * 
     * @param player
     * @param data
     *           
     * @param cc
     *            
     * @return If to cancel a packet event.
     */
    public boolean check(final Player player, final NetData data, final NetConfig cc, final IPlayerData pData) {
        data.packetFrequency.add(System.currentTimeMillis(), 1f);
        final long fDur = data.packetFrequency.bucketDuration() * data.packetFrequency.numberOfBuckets();
        double amount = data.packetFrequency.score(1f) * 1000f / (float) fDur;
        //        if (data.debug) {
        //            debug(player, "Basic amount: " + amount);
        //        }
        if (amount > cc.packetFrequencyPacketsPerSecond) {
            amount /= TickTask.getLag(fDur);
            if (amount > cc.packetFrequencyPacketsPerSecond) {
                final double violation = amount - cc.packetFrequencyPacketsPerSecond;
                final boolean cancel = executeActions(player, violation, 1.0, cc.packetFrequencyActions).willCancel();
                if (cancel || applyEvidenceFusion(player, data, pData, violation)) {
                    return true;
                }
            }
        }
        return false; // Cancel state.
    }

    private boolean applyEvidenceFusion(final Player player,
                                        final NetData data,
                                        final IPlayerData pData,
                                        final double violation) {
        if (!pData.isCheckActive(CheckType.COMBINED_IMPROBABLE, player)) {
            return false;
        }
        final long now = System.currentTimeMillis();
        if (now - data.lastNetPacketEvidenceTime < EVIDENCE_COOLDOWN_MS) {
            return false;
        }
        data.lastNetPacketEvidenceTime = now;

        final CombinedConfig combinedConfig = pData.getGenericInstance(CombinedConfig.class);
        final String overrideProfile = combinedConfig == null
                ? EvidenceFusionProfile.PROFILE_INHERIT
                : combinedConfig.evidenceProfileNetPacketFrequency;
        final float base = (float) Math.max(0.35, Math.min(15.0, 0.8 + violation * 0.12));
        final double stage2Threshold = EvidenceFusionProfile.stage2Threshold(EVIDENCE_STAGE2_EXCESS, combinedConfig, overrideProfile);
        final double stage3Threshold = EvidenceFusionProfile.stage3Threshold(EVIDENCE_STAGE3_EXCESS, combinedConfig, overrideProfile);
        if (violation < stage2Threshold) {
            EvidenceFusionProfile.debugProfile(player, pData, type, combinedConfig, overrideProfile,
                    "net.packetfrequency", violation, base, "feed");
            Improbable.feed(player, EvidenceFusionProfile.feedWeight(base * 0.50f, combinedConfig, overrideProfile), now, pData);
            return false;
        }
        if (violation >= stage3Threshold) {
            EvidenceFusionProfile.debugProfile(player, pData, type, combinedConfig, overrideProfile,
                    "net.packetfrequency", violation, base, "stage3");
            return Improbable.check(player,
                    EvidenceFusionProfile.stage3Weight(base * 1.25f, combinedConfig, overrideProfile),
                    now,
                    "net.packetfrequency.stage3",
                    pData);
        }
        EvidenceFusionProfile.debugProfile(player, pData, type, combinedConfig, overrideProfile,
                "net.packetfrequency", violation, base, "stage2");
        return Improbable.check(player,
                EvidenceFusionProfile.stage2Weight(base * 0.90f, combinedConfig, overrideProfile),
                now,
                "net.packetfrequency.stage2",
                pData);
    }

    /**
     * Allow to relax the count by 1, e.g. with outgoing teleport.
     * 
     * @param player
     * @param data
     * 
     * @param cc
     * 
     * @return
     */
    public void relax(final Player player, final NetData data, final NetConfig cc) {
        // TODO: Concept (not more locking, instead a counter (optimistic)).
    }

}
