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
package fr.neatmonster.nocheatplus.checks.blockplace;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * The Reach check will find out if a player places a block too far away.
 */
public class Reach extends Check {

    /** Fallback defaults (can be overridden in config). */
    public static final double CREATIVE_DISTANCE = 5.6D;
    public static final double SURVIVAL_DISTANCE = 5.1D;

    /** For temporary use: LocUtil.clone before passing deeply, call setWorld(null) after use. */
    private final Location useLoc = new Location(null, 0, 0, 0);

    /**
     * Instantiates a new reach check.
     */
    public Reach() {
        super(CheckType.BLOCKPLACE_REACH);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param eyeHeight 
     * @param block 
     * @param data
     * @param cc
     * @return true, if successful
     */
    public boolean check(final Player player, final double eyeHeight, final Block block, final BlockPlaceData data, final BlockPlaceConfig cc) {
        boolean cancel = false;

        final double baseLimit = player.getGameMode() == GameMode.CREATIVE
                ? (cc.reachCreativeDistance > 0.0 ? cc.reachCreativeDistance : CREATIVE_DISTANCE)
                : (cc.reachSurvivalDistance > 0.0 ? cc.reachSurvivalDistance : SURVIVAL_DISTANCE);
        final double distanceLimit = baseLimit + Math.max(0.0, cc.reachMovementSlack);

        // Grim-inspired approach: use shortest eye->block-AABB distance (not center point distance).
        final Location eyeLoc = player.getLocation(useLoc);
        eyeLoc.setY(eyeLoc.getY() + eyeHeight);

        final double minX = block.getX();
        final double minY = block.getY();
        final double minZ = block.getZ();
        final double maxX = minX + 1.0;
        final double maxY = minY + 1.0;
        final double maxZ = minZ + 1.0;

        final double closestX = clamp(eyeLoc.getX(), minX, maxX);
        final double closestY = clamp(eyeLoc.getY(), minY, maxY);
        final double closestZ = clamp(eyeLoc.getZ(), minZ, maxZ);

        final double dx = eyeLoc.getX() - closestX;
        final double dy = eyeLoc.getY() - closestY;
        final double dz = eyeLoc.getZ() - closestZ;
        final double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        final double violation = distance - distanceLimit;

        if (violation > 0.0) {
            // They failed, increment violation level.
            data.reachVL += violation;
            final ViolationData vd = new ViolationData(this, player, data.reachVL, violation, cc.reachActions);
            vd.setParameter(ParameterName.REACH_DISTANCE, StringUtil.fdec3.format(distance));
            cancel = executeActions(vd).willCancel();
        }
        // Player passed the check, reward them
        else {
            data.reachVL *= 0.9D;
        }

        // Cleanup.
        useLoc.setWorld(null);
        return cancel;
    }

    private static double clamp(final double v, final double min, final double max) {
        return Math.max(min, Math.min(max, v));
    }
}