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

public class KeepAliveFrequency extends Check {

    private static final long EVIDENCE_COOLDOWN_MS = 120L;
    private static final double EVIDENCE_STAGE2_EXCESS = 1.5;
    private static final double EVIDENCE_STAGE3_EXCESS = 4.0;

    public KeepAliveFrequency() {
        super(CheckType.NET_KEEPALIVEFREQUENCY);
    }
    
    /**
     * Checks hasBypass on violation only.
     * @param player
     * @param time
     * @param data
     * @param cc
     * @return If to cancel.
     */
    public boolean check(final Player player, final long time, final NetData data, final NetConfig cc, final IPlayerData pData) {
        data.keepAliveFreq.add(time, 1f);
        final float first = data.keepAliveFreq.bucketScore(0);
        final long now = System.currentTimeMillis();
        
        if (now > pData.getLastJoinTime() && pData.getLastJoinTime() + cc.keepAliveFrequencyStartupDelay * 1000 < now) {
            return false;
        }
        
        if (first > 1f) {
            // Trigger a violation.
            final double vl = Math.max(first - 1f, data.keepAliveFreq.score(1f) - data.keepAliveFreq.numberOfBuckets());
            final boolean cancel = executeActions(player, vl, 1.0, cc.keepAliveFrequencyActions).willCancel();
            if (cancel || applyEvidenceFusion(player, data, pData, vl)) {
                return true;
            }
        }
        return false;
    }

    private boolean applyEvidenceFusion(final Player player,
                                        final NetData data,
                                        final IPlayerData pData,
                                        final double violation) {
        if (!pData.isCheckActive(CheckType.COMBINED_IMPROBABLE, player)) {
            return false;
        }
        final long now = System.currentTimeMillis();
        if (now - data.lastNetKeepAliveEvidenceTime < EVIDENCE_COOLDOWN_MS) {
            return false;
        }
        data.lastNetKeepAliveEvidenceTime = now;

        final CombinedConfig combinedConfig = pData.getGenericInstance(CombinedConfig.class);
        final String overrideProfile = combinedConfig == null
                ? EvidenceFusionProfile.PROFILE_INHERIT
                : combinedConfig.evidenceProfileNetKeepAliveFrequency;
        final float base = (float) Math.max(0.25, Math.min(8.0, 0.7 + violation * 0.9));
        final double stage2Threshold = EvidenceFusionProfile.stage2Threshold(EVIDENCE_STAGE2_EXCESS, combinedConfig, overrideProfile);
        final double stage3Threshold = EvidenceFusionProfile.stage3Threshold(EVIDENCE_STAGE3_EXCESS, combinedConfig, overrideProfile);
        if (violation < stage2Threshold) {
            EvidenceFusionProfile.debugProfile(player, pData, type, combinedConfig, overrideProfile,
                    "net.keepalivefrequency", violation, base, "feed");
            Improbable.feed(player, EvidenceFusionProfile.feedWeight(base * 0.50f, combinedConfig, overrideProfile), now, pData);
            return false;
        }
        if (violation >= stage3Threshold) {
            EvidenceFusionProfile.debugProfile(player, pData, type, combinedConfig, overrideProfile,
                    "net.keepalivefrequency", violation, base, "stage3");
            return Improbable.check(player,
                    EvidenceFusionProfile.stage3Weight(base * 1.20f, combinedConfig, overrideProfile),
                    now,
                    "net.keepalivefrequency.stage3",
                    pData);
        }
        EvidenceFusionProfile.debugProfile(player, pData, type, combinedConfig, overrideProfile,
                "net.keepalivefrequency", violation, base, "stage2");
        return Improbable.check(player,
                EvidenceFusionProfile.stage2Weight(base * 0.85f, combinedConfig, overrideProfile),
                now,
                "net.keepalivefrequency.stage2",
                pData);
    }
}
