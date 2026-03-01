/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 */
package fr.neatmonster.nocheatplus.checks.combined;

/**
 * Centralized profile tuning for staged evidence fusion.
 * <p>
 * Profiles:
 * <ul>
 *   <li><b>balanced</b>: default, conservative multipliers.</li>
 *   <li><b>strict</b>: escalates faster (lower stage thresholds, higher weights).</li>
 * </ul>
 */
public final class EvidenceFusionProfile {

    public static final String PROFILE_BALANCED = "balanced";
    public static final String PROFILE_STRICT = "strict";

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

    public static boolean isStrict(final CombinedConfig cc) {
        return cc != null && PROFILE_STRICT.equals(cc.evidenceProfile);
    }

    public static double stage2Threshold(final double baseThreshold, final CombinedConfig cc) {
        return baseThreshold * (isStrict(cc) ? 0.80 : 1.0);
    }

    public static double stage3Threshold(final double baseThreshold, final CombinedConfig cc) {
        return baseThreshold * (isStrict(cc) ? 0.80 : 1.0);
    }

    public static float feedWeight(final float baseWeight, final CombinedConfig cc) {
        return (float) (baseWeight * (isStrict(cc) ? 1.30 : 1.0));
    }

    public static float stage2Weight(final float baseWeight, final CombinedConfig cc) {
        return (float) (baseWeight * (isStrict(cc) ? 1.18 : 1.0));
    }

    public static float stage3Weight(final float baseWeight, final CombinedConfig cc) {
        return (float) (baseWeight * (isStrict(cc) ? 1.28 : 1.0));
    }
}
