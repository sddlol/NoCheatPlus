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
import fr.neatmonster.nocheatplus.checks.combined.Improbable;
import fr.neatmonster.nocheatplus.players.IPlayerData;

/**
 * The WrongTurn check will detect players who send improbable rotations 
 */
public class WrongTurn extends Check {

    private static final long EVIDENCE_COOLDOWN_MS = 120L;
    private static final double EVIDENCE_STAGE2_VL = 3.0;
    private static final double EVIDENCE_STAGE3_VL = 10.0;
    
    public WrongTurn() {
        super(CheckType.NET_WRONGTURN);
    }

    /**
     * Checks a player
     * @param player
     * @param pitch
     * @param data
     * @param cc
     * @return true if successful.
     * 
     */
    public boolean check(final Player player, final float pitch, final NetData data, final NetConfig cc, final IPlayerData pData) {
        boolean cancel = false;
        // Currently, only impossible pitch is detected, later, we might throw in actual pattern-based checks.
        if (pitch > 90.0 || pitch < -90.0) {
            data.wrongTurnVL++;
            cancel = executeActions(player, data.wrongTurnVL, 1, cc.wrongTurnActions).willCancel();
            if (applyEvidenceFusion(player, data, pData, pitch)) {
                cancel = true;
            }
        }
        return cancel;
    }

    private boolean applyEvidenceFusion(final Player player,
                                        final NetData data,
                                        final IPlayerData pData,
                                        final float pitch) {
        if (!pData.isCheckActive(CheckType.COMBINED_IMPROBABLE, player)) {
            return false;
        }
        final long now = System.currentTimeMillis();
        if (now - data.lastNetWrongTurnEvidenceTime < EVIDENCE_COOLDOWN_MS) {
            return false;
        }
        data.lastNetWrongTurnEvidenceTime = now;

        final float pitchExcess = (float) Math.max(0.0, Math.max(pitch - 90.0f, -90.0f - pitch));
        final float base = Math.max(0.5f, Math.min(12.0f, 0.8f + pitchExcess * 0.12f));
        if (data.wrongTurnVL < EVIDENCE_STAGE2_VL) {
            Improbable.feed(player, base * 0.55f, now, pData);
            return false;
        }
        if (data.wrongTurnVL >= EVIDENCE_STAGE3_VL) {
            return Improbable.check(player, base * 1.25f, now, "net.wrongturn.stage3", pData);
        }
        return Improbable.check(player, base * 0.90f, now, "net.wrongturn.stage2", pData);
    }
}
