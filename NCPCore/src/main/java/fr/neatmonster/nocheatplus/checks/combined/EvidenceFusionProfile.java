/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 */
package fr.neatmonster.nocheatplus.checks.combined;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.players.IPlayerData;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * Centralized profile tuning for staged evidence fusion.
 * <p>
 * Global profiles:
 * <ul>
 *   <li><b>balanced</b>: default, conservative multipliers.</li>
 *   <li><b>strict</b>: escalates faster (lower stage thresholds, higher weights).</li>
 * </ul>
 * <p>
 * Per-check override supports: {@code inherit|balanced|strict}.
 */
public final class EvidenceFusionProfile {

    public static final String PROFILE_INHERIT = "inherit";
    public static final String PROFILE_BALANCED = "balanced";
    public static final String PROFILE_STRICT = "strict";

    private static final long DEFAULT_DEBUG_MIN_INTERVAL_MS = 1000L;
    private static final int DEBUG_RATE_LIMIT_MAX_ENTRIES = 20000;
    private static final Map<String, Long> debugRateLimitMap = new ConcurrentHashMap<String, Long>();

    private EvidenceFusionProfile() {}

    public static String normalizeProfile(final String rawProfile) {
        if (rawProfile == null) {
            return PROFILE_BALANCED;
        }
        final String profile = rawProfile.trim().toLowerCase();
        if (PROFILE_STRICT.equals(profile)) {
            return PROFILE_STRICT;
        }
        return PROFILE_BALANCED;
    }

    public static String normalizeOverride(final String rawProfileOverride) {
        if (rawProfileOverride == null) {
            return PROFILE_INHERIT;
        }
        final String profile = rawProfileOverride.trim().toLowerCase();
        if (PROFILE_STRICT.equals(profile) || PROFILE_BALANCED.equals(profile)) {
            return profile;
        }
        return PROFILE_INHERIT;
    }

    private static String resolveProfile(final CombinedConfig cc, final String overrideProfile) {
        final String normalizedOverride = normalizeOverride(overrideProfile);
        if (!PROFILE_INHERIT.equals(normalizedOverride)) {
            return normalizedOverride;
        }
        return cc == null ? PROFILE_BALANCED : normalizeProfile(cc.evidenceProfile);
    }

    public static String effectiveProfile(final CombinedConfig cc, final String overrideProfile) {
        return resolveProfile(cc, overrideProfile);
    }

    public static void debugProfile(final Player player,
                                    final IPlayerData pData,
                                    final CheckType checkType,
                                    final CombinedConfig cc,
                                    final String overrideProfile,
                                    final String source,
                                    final double vl,
                                    final float base,
                                    final String stage) {
        if (pData == null || !pData.isDebugActive(checkType)) {
            return;
        }
        if (cc != null && !cc.evidenceDebugActive) {
            return;
        }
        final long minInterval = cc == null ? DEFAULT_DEBUG_MIN_INTERVAL_MS
                : Math.max(0L, cc.evidenceDebugMinIntervalMs);
        if (player != null && minInterval > 0L && shouldSkipDebug(player, source, minInterval)) {
            return;
        }

        final String effective = effectiveProfile(cc, overrideProfile);
        CheckUtils.debug(player, checkType,
                "[EvidenceProfile] source=" + source
                        + " effective=" + effective
                        + " override=" + normalizeOverride(overrideProfile)
                        + " stage=" + stage
                        + " vl=" + String.format("%.3f", vl)
                        + " base=" + String.format("%.3f", base)
                        + " minIntMs=" + minInterval);
    }

    private static boolean shouldSkipDebug(final Player player, final String source, final long minInterval) {
        final long now = System.currentTimeMillis();
        if (debugRateLimitMap.size() > DEBUG_RATE_LIMIT_MAX_ENTRIES) {
            debugRateLimitMap.clear();
        }
        final String key = player.getUniqueId().toString() + ':' + source;
        final Long last = debugRateLimitMap.get(key);
        if (last != null && now - last.longValue() < minInterval) {
            return true;
        }
        debugRateLimitMap.put(key, now);
        return false;
    }

    public static boolean shouldEscalateStage3(final CombinedConfig cc,
                                               final long now,
                                               final long lastStage3CandidateTime) {
        if (cc == null || !cc.evidenceGuardrailsEnabled || !cc.evidenceGuardrailsRequireRepeatForStage3) {
            return true;
        }
        if (lastStage3CandidateTime <= 0L) {
            return false;
        }
        return now - lastStage3CandidateTime <= Math.max(1L, cc.evidenceGuardrailsMinRepeatWindowMs);
    }

    public static void snapshotStage(final Player player,
                                     final CombinedConfig cc,
                                     final String source,
                                     final String stage,
                                     final double vl,
                                     final String overrideProfile,
                                     final Integer pingMs,
                                     final Double jitterMs) {
        if (player == null || (cc != null && !cc.evidenceDebugSnapshotActive)) {
            return;
        }
        final String effective = effectiveProfile(cc, overrideProfile);
        final Location loc = player.getLocation();
        final String xyz = loc == null ? "na" : (StringUtil.fdec3.format(loc.getX()) + ","
                + StringUtil.fdec3.format(loc.getY()) + ","
                + StringUtil.fdec3.format(loc.getZ()));
        StaticLog.logInfo("[EvidenceSnapshot]"
                + " player=" + player.getName()
                + " source=" + source
                + " stage=" + stage
                + " vl=" + StringUtil.fdec3.format(vl)
                + " effective=" + effective
                + " override=" + normalizeOverride(overrideProfile)
                + " ping=" + (pingMs == null ? "na" : Integer.toString(pingMs.intValue()))
                + " jitter=" + (jitterMs == null ? "na" : StringUtil.fdec3.format(jitterMs.doubleValue()))
                + " xyz=" + xyz);
    }

    public static boolean isStrict(final CombinedConfig cc) {
        return PROFILE_STRICT.equals(resolveProfile(cc, PROFILE_INHERIT));
    }

    public static boolean isStrict(final CombinedConfig cc, final String overrideProfile) {
        return PROFILE_STRICT.equals(resolveProfile(cc, overrideProfile));
    }

    public static double stage2Threshold(final double baseThreshold, final CombinedConfig cc) {
        return stage2Threshold(baseThreshold, cc, PROFILE_INHERIT);
    }

    public static double stage2Threshold(final double baseThreshold, final CombinedConfig cc, final String overrideProfile) {
        return baseThreshold * (isStrict(cc, overrideProfile) ? 0.80 : 1.0);
    }

    public static double stage3Threshold(final double baseThreshold, final CombinedConfig cc) {
        return stage3Threshold(baseThreshold, cc, PROFILE_INHERIT);
    }

    public static double stage3Threshold(final double baseThreshold, final CombinedConfig cc, final String overrideProfile) {
        return baseThreshold * (isStrict(cc, overrideProfile) ? 0.80 : 1.0);
    }

    public static float feedWeight(final float baseWeight, final CombinedConfig cc) {
        return feedWeight(baseWeight, cc, PROFILE_INHERIT);
    }

    public static float feedWeight(final float baseWeight, final CombinedConfig cc, final String overrideProfile) {
        return (float) (baseWeight * (isStrict(cc, overrideProfile) ? 1.30 : 1.0));
    }

    public static float stage2Weight(final float baseWeight, final CombinedConfig cc) {
        return stage2Weight(baseWeight, cc, PROFILE_INHERIT);
    }

    public static float stage2Weight(final float baseWeight, final CombinedConfig cc, final String overrideProfile) {
        return (float) (baseWeight * (isStrict(cc, overrideProfile) ? 1.18 : 1.0));
    }

    public static float stage3Weight(final float baseWeight, final CombinedConfig cc) {
        return stage3Weight(baseWeight, cc, PROFILE_INHERIT);
    }

    public static float stage3Weight(final float baseWeight, final CombinedConfig cc, final String overrideProfile) {
        return (float) (baseWeight * (isStrict(cc, overrideProfile) ? 1.28 : 1.0));
    }
}
