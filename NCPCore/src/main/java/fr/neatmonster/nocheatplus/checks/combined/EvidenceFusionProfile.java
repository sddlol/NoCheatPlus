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
