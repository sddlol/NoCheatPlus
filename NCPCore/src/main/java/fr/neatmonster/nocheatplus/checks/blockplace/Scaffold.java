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

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.combined.CombinedConfig;
import fr.neatmonster.nocheatplus.checks.combined.EvidenceFusionProfile;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;
import fr.neatmonster.nocheatplus.components.registry.feature.TickListener;
import fr.neatmonster.nocheatplus.players.IPlayerData;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import fr.neatmonster.nocheatplus.utilities.math.TrigUtil;


/**
 * Check for common behavior from a client using the "Scaffold" cheat.
 * Each sub-check should cover areas that one sub-check may not cover.
 * If the cheat does not flag any other sub-checks, the time sub-check
 * Should enforce realistic time between block placements.
 * 
 * @author CaptainObvious0
 */
public class Scaffold extends Check {

    private static final double EVIDENCE_STAGE2_VL = 6.0;
    private static final double EVIDENCE_STAGE3_VL = 16.0;
    private static final long EVIDENCE_COOLDOWN_MS = 120L;

    final static double MAX_ANGLE = Math.toRadians(90);
    public List<String> tags = new LinkedList<>();
    
   /*
    * Instantiates a new Scaffold check
    */
    public Scaffold() {
        super(CheckType.BLOCKPLACE_SCAFFOLD);
    }

    /**
     * Check the player for Scaffold cheats
     *
     * @param player the player
     * @param placedFace blockface player placed against
     * @param pData player data
     * @param data block place data
     * @param cc block place config
     * @param isCancelled is the event cancelled
     * @param yDistance players current yDistance for this move
     * @param jumpPhase players jump phase
     * @return
     */
    public boolean check(final Player player, final Block placedBlock, final BlockFace placedFace, final IPlayerData pData,
                         final BlockPlaceData data, final BlockPlaceConfig cc, final boolean isCancelled,
                         final double yDistance, final int jumpPhase) {
        // NOTE: Looks like there's a legit fast-scaffolding technique called 'god-bridge'
        // Would be useful to compare the speed with a skilled player god-bridging VS scaffold cheats.
        // TODO: ^ Need testing + debug log + someone who can actually replicate such... :)

        // TODO: Not too sure about the usefulness of this check these days:
        // Angle: like reported in the comment below, this is already covered by BlockInteract.Direction. Why not simply sharpen that one instead of bloating NCP with redundant code?
        // Time: Possibly rethink it. Could be moved to FastPlace as a sub-check
        //       ...Or just remove it. We already have a FastPlace check (which is ALSO
        //       feeding Improbable with too fast block placements on longer time periods...)
        // Sprint: Would be best to have our own sprinting handling, and catch the cheat with SurvivalFly instead.
        //        (If the player sprints while placing a block: sprinting = false -> cause Sf to enforce walking speed -> the player will then trigger a speed violation)
        // ToolSwitch: matter of taste... it may catch badly implemented scaffold cheats but not really much else. 
        //             Also clashes with NCP's principle (having long-lasting checks based on deterministic protection, not detecting specific cheat implementations)  
        // PitchRotation: this is the only one that I can see being kept. Combined with yawrate, they can nerf the cheat decently.
        //                (At that point it should be moved as a subcheck into something else. Wrongturn? A new Combined.PitchRate check? Wouldn't make sense keeping it into the BlockPlace category)      
        boolean cancel = false;

        final long nowTick = TickTask.getTick();
        if (Math.abs(nowTick - data.currentTick) > 20) {
            data.scaffoldRayBuffer = Math.max(0.0, data.scaffoldRayBuffer - 1.0);
        }

        // Update sneakTime since the player may have unsneaked after the last move.
        if (player.isSneaking()) {
            data.sneakTime = data.currentTick;
        }
        data.currentTick = nowTick;

        // Angle Check - Check if the player is looking at the block (Should already be covered by BlockInteract.Direction)
        if (cc.scaffoldAngle) {
            final Vector placedVector = new Vector(placedFace.getModX(), placedFace.getModY(), placedFace.getModZ());
            double placedAngle = TrigUtil.angle(player.getLocation().getDirection(), placedVector);
            if (placedAngle > MAX_ANGLE) {
                cancel = violation("Angle", Math.min(Math.max(1, (int) (placedAngle - MAX_ANGLE) * 10), 10), player, data, pData);
            }
        }

        // Time Check - A FastPlace check but for Scaffold type block placements. If all other sub-checks fail to detect the cheat this
        // Should ensure the player cannot quickly place blocks below themselves.
        if (cc.scaffoldTime && !isCancelled && Math.abs(player.getLocation().getPitch()) > 70
            && (data.currentTick - data.sneakTime) > 3 
            && !player.hasPotionEffect(PotionEffectType.SPEED)) {
            
            // TODO: Switch to ActionAccumulator based check.
            data.placeTick.add(data.currentTick);
            if (data.placeTick.size() > 2) {
                long sum = 0;
                long lastTick = 0;
                for (int i = 0; i < data.placeTick.size(); i++) {
                    long tick = data.placeTick.get(i);

                    if (lastTick != 0) {
                        sum += (tick - lastTick);
                    }
                    lastTick = tick;

                }

                double avg = sum / data.placeTick.size();
                if (avg < cc.scaffoldTimeAvg) {
                    cancel = violation("Time", Math.min((cc.scaffoldTimeAvg - (int) avg), 5), player, data, pData);
                    if (data.placeTick.size() > 20) data.placeTick.clear(); // Clear if it gets too big to prevent players being unable to place blocks
                } 
                else {
                    data.placeTick.clear();
                }
            }
        }

        // Sprint check - Prevent players from sprinting while placing blocks below them
        long diff = data.currentTick - data.sprintTime;
        if (cc.scaffoldSprint && Math.abs(player.getLocation().getPitch()) > 70
            && diff < 8 && yDistance < 0.1 && jumpPhase < 4) { 

            cancel = violation("Sprint", 1, player, data, pData);
        }

        // Rotate Check - Check for large changes in rotation between block placements
        // Note: Yaw speed change is also monitored. (See listener...)
        if (cc.scaffoldRotate) {
            data.lastYaw = player.getLocation().getYaw();
            TickListener yawTick = new TickListener() {
                @Override
                public void onTick(int tick, long timeLast) {
                    // Needs to be run on the next tick
                    // Most likely better way to to this with TickTask but this works as well
                    if (TickTask.getTick() != data.currentTick) {
                        float diff = Math.abs(data.lastYaw - player.getLocation().getYaw());

                        if (diff > cc.scaffoldRotateDiff) {
                            data.cancelNextPlace = violation("Rotate", Math.min((int) (diff - cc.scaffoldRotateDiff) / 10, 5), player, data, pData);
                            tags.clear();
                        }
                        TickTask.removeTickListener(this);
                    }
                }
            };
            TickTask.addTickListener(yawTick);
        }

        // Tool Switch - Check if the player is quickly switching inventory slots between block placements
        if (cc.scaffoldToolSwitch) {

            data.lastSlot = player.getInventory().getHeldItemSlot();
            TickListener toolSwitchTick = new TickListener() {
                @Override
                public void onTick(int tick, long timeLast) {
                    // Needs to be run on the next tick
                    // Most likely better way to to this with TickTask but this works as well
                    if (data.currentTick != TickTask.getTick()) {
                        if (data.lastSlot != player.getInventory().getHeldItemSlot()) {
                            data.cancelNextPlace = violation("ToolSwitch", 1, player, data, pData);
                            tags.clear();
                        }
                        TickTask.removeTickListener(this);
                    }
                }
            };
            TickTask.addTickListener(toolSwitchTick);
        }

        // Grim-inspired FarPlace style: nearest eye->block-box distance.
        if (cc.scaffoldFar && placedBlock != null) {
            final double dist = minDistanceToBlock(player, placedBlock);
            if (dist > cc.scaffoldFarDistance) {
                final int weight = Math.max(1, Math.min(8, (int) Math.ceil((dist - cc.scaffoldFarDistance) * 6.0)));
                cancel = violation("Far", weight, player, data, pData) || cancel;
            }
        }

        // Grim-inspired RotationPlace style: look ray should intersect the placed block.
        if (cc.scaffoldRotateRaytrace && placedBlock != null) {
            if (!doesLookRayHitBlock(player, placedBlock, cc.scaffoldFarDistance + 0.8)) {
                data.scaffoldRayBuffer = Math.min(6.0, data.scaffoldRayBuffer + 1.0);
            } else {
                data.scaffoldRayBuffer = Math.max(0.0, data.scaffoldRayBuffer - cc.scaffoldRotateRayBufferDecay);
            }
            if (data.scaffoldRayBuffer >= cc.scaffoldRotateRayBufferMin) {
                cancel = violation("Ray", 1, player, data, pData) || cancel;
                data.scaffoldRayBuffer = Math.max(0.0, data.scaffoldRayBuffer - 0.75);
            }
        }

        tags.clear();
        return cancel;
    }

    private static double minDistanceToBlock(final Player player, final Block block) {
        final Location eye = player.getEyeLocation();
        final double minX = block.getX();
        final double minY = block.getY();
        final double minZ = block.getZ();
        final double maxX = minX + 1.0;
        final double maxY = minY + 1.0;
        final double maxZ = minZ + 1.0;

        final double cx = clamp(eye.getX(), minX, maxX);
        final double cy = clamp(eye.getY(), minY, maxY);
        final double cz = clamp(eye.getZ(), minZ, maxZ);

        final double dx = eye.getX() - cx;
        final double dy = eye.getY() - cy;
        final double dz = eye.getZ() - cz;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static boolean doesLookRayHitBlock(final Player player, final Block block, final double maxDistance) {
        final Location eye = player.getEyeLocation();
        final Vector dir = eye.getDirection();
        if (dir == null || dir.lengthSquared() < 1.0E-6) return true;

        final Vector nd = dir.clone().normalize();
        final double minX = block.getX();
        final double minY = block.getY();
        final double minZ = block.getZ();
        final double maxX = minX + 1.0;
        final double maxY = minY + 1.0;
        final double maxZ = minZ + 1.0;

        // If eye is already inside the placed block, treat as hit.
        if (eye.getX() > minX && eye.getX() < maxX && eye.getY() > minY && eye.getY() < maxY && eye.getZ() > minZ && eye.getZ() < maxZ) {
            return true;
        }

        return rayAabbIntersection(
                eye.getX(), eye.getY(), eye.getZ(),
                nd.getX(), nd.getY(), nd.getZ(),
                minX, minY, minZ, maxX, maxY, maxZ,
                maxDistance
        );
    }

    private static boolean rayAabbIntersection(
            final double ox, final double oy, final double oz,
            final double dx, final double dy, final double dz,
            final double minX, final double minY, final double minZ,
            final double maxX, final double maxY, final double maxZ,
            final double maxDistance) {

        double tMin = 0.0;
        double tMax = maxDistance;

        // X slab
        if (Math.abs(dx) < 1.0E-8) {
            if (ox < minX || ox > maxX) return false;
        } else {
            double tx1 = (minX - ox) / dx;
            double tx2 = (maxX - ox) / dx;
            if (tx1 > tx2) {
                final double t = tx1;
                tx1 = tx2;
                tx2 = t;
            }
            tMin = Math.max(tMin, tx1);
            tMax = Math.min(tMax, tx2);
            if (tMin > tMax) return false;
        }

        // Y slab
        if (Math.abs(dy) < 1.0E-8) {
            if (oy < minY || oy > maxY) return false;
        } else {
            double ty1 = (minY - oy) / dy;
            double ty2 = (maxY - oy) / dy;
            if (ty1 > ty2) {
                final double t = ty1;
                ty1 = ty2;
                ty2 = t;
            }
            tMin = Math.max(tMin, ty1);
            tMax = Math.min(tMax, ty2);
            if (tMin > tMax) return false;
        }

        // Z slab
        if (Math.abs(dz) < 1.0E-8) {
            if (oz < minZ || oz > maxZ) return false;
        } else {
            double tz1 = (minZ - oz) / dz;
            double tz2 = (maxZ - oz) / dz;
            if (tz1 > tz2) {
                final double t = tz1;
                tz1 = tz2;
                tz2 = t;
            }
            tMin = Math.max(tMin, tz1);
            tMax = Math.min(tMax, tz2);
            if (tMin > tMax) return false;
        }

        return tMax >= 0.0 && tMin <= maxDistance;
    }

    private static double clamp(final double v, final double min, final double max) {
        return Math.max(min, Math.min(max, v));
    }

    /**
     * Create a violation for Scaffold
     *
     * @param addTags
     * @param weight
     * @param player
     * @param data
     * @param pData
     * @return
     */
    private boolean violation(final String addTags, final int weight, final Player player,
                              final BlockPlaceData data, final IPlayerData pData) {
        final BlockPlaceConfig cc = pData.getGenericInstance(BlockPlaceConfig.class);
        ViolationData vd = new ViolationData(this, player, data.scaffoldVL, weight, cc.scaffoldActions);
        tags.add(addTags);
        if (vd.needsParameters()) vd.setParameter(ParameterName.TAGS, StringUtil.join(tags, "+"));
        data.scaffoldVL += weight;

        final boolean cancel = executeActions(vd).willCancel();
        final boolean evidenceCancel = applyScaffoldEvidence(player, data, cc, pData, weight);
        return cancel || evidenceCancel;
    }

    private boolean applyScaffoldEvidence(final Player player,
                                          final BlockPlaceData data,
                                          final BlockPlaceConfig cc,
                                          final IPlayerData pData,
                                          final int weight) {
        if (cc.scaffoldImprobableWeight <= 0.0f || !pData.isCheckActive(CheckType.COMBINED_IMPROBABLE, player)) {
            return false;
        }
        final long now = System.currentTimeMillis();
        if (now - data.scaffoldEvidenceTime < EVIDENCE_COOLDOWN_MS) {
            return false;
        }
        data.scaffoldEvidenceTime = now;

        final CombinedConfig combinedConfig = pData.getGenericInstance(CombinedConfig.class);
        final String overrideProfile = combinedConfig == null
                ? EvidenceFusionProfile.PROFILE_INHERIT
                : combinedConfig.evidenceProfileBlockplaceScaffold;
        final float base = (float) Math.max(0.25, Math.min(8.0, weight * cc.scaffoldImprobableWeight));
        final double stage2Threshold = EvidenceFusionProfile.stage2Threshold(EVIDENCE_STAGE2_VL, combinedConfig, overrideProfile);
        final double stage3Threshold = EvidenceFusionProfile.stage3Threshold(EVIDENCE_STAGE3_VL, combinedConfig, overrideProfile);
        if (data.scaffoldVL < stage2Threshold) {
            EvidenceFusionProfile.debugProfile(player, pData, type, combinedConfig, overrideProfile,
                    "blockplace.scaffold", data.scaffoldVL, base, "feed");
            Improbable.feed(player, EvidenceFusionProfile.feedWeight(base * 0.55f, combinedConfig, overrideProfile), now, pData);
            return false;
        }
        final boolean stage3 = data.scaffoldVL >= stage3Threshold;
        if (cc.scaffoldImprobableFeedOnly) {
            EvidenceFusionProfile.debugProfile(player, pData, type, combinedConfig, overrideProfile,
                    "blockplace.scaffold", data.scaffoldVL, base, stage3 ? "stage3-feed-only" : "stage2-feed-only");
            Improbable.feed(player,
                    stage3
                            ? EvidenceFusionProfile.stage3Weight(base * 1.15f, combinedConfig, overrideProfile)
                            : EvidenceFusionProfile.stage2Weight(base * 0.85f, combinedConfig, overrideProfile),
                    now,
                    pData);
            return false;
        }
        if (stage3) {
            EvidenceFusionProfile.debugProfile(player, pData, type, combinedConfig, overrideProfile,
                    "blockplace.scaffold", data.scaffoldVL, base, "stage3");
            return Improbable.check(player,
                    EvidenceFusionProfile.stage3Weight(base * 1.20f, combinedConfig, overrideProfile),
                    now,
                    "blockplace.scaffold.stage3",
                    pData);
        }
        EvidenceFusionProfile.debugProfile(player, pData, type, combinedConfig, overrideProfile,
                "blockplace.scaffold", data.scaffoldVL, base, "stage2");
        return Improbable.check(player,
                EvidenceFusionProfile.stage2Weight(base * 0.85f, combinedConfig, overrideProfile),
                now,
                "blockplace.scaffold.stage2",
                pData);
    }
}
