/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 */
package fr.neatmonster.nocheatplus.checks.moving.player;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.combined.CombinedConfig;
import fr.neatmonster.nocheatplus.checks.combined.EvidenceFusionProfile;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.players.IPlayerData;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * Grim-inspired lightweight timer check based on movement cadence.
 */
public class Timer extends Check {

    private static final double EVIDENCE_STAGE2_VL = 8.0;
    private static final double EVIDENCE_STAGE3_VL = 24.0;

    private final List<String> tags = new ArrayList<String>();

    public Timer() {
        super(CheckType.MOVING_TIMER);
    }

    public boolean check(final Player player,
                         final double avgDt,
                         final double lowRatio,
                         final int samples,
                         final double moved,
                         final double effectiveMinMoveDtMs,
                         final double effectiveMaxLowDtRatio,
                         final int pingMs,
                         final double jitterMs,
                         final MovingData data,
                         final MovingConfig cc,
                         final IPlayerData pData) {

        final double severity = Math.max(0.0,
                ((effectiveMinMoveDtMs - avgDt) / Math.max(1.0, effectiveMinMoveDtMs))
                        + Math.max(0.0, lowRatio - effectiveMaxLowDtRatio));

        data.timerVL += Math.max(0.25, severity * 4.0);

        tags.clear();
        tags.add("avgdt=" + StringUtil.fdec3.format(avgDt));
        tags.add("low=" + StringUtil.fdec3.format(lowRatio));
        tags.add("n=" + samples);
        tags.add("dist=" + StringUtil.fdec3.format(moved));
        tags.add("thr_dt=" + StringUtil.fdec3.format(effectiveMinMoveDtMs));
        tags.add("thr_low=" + StringUtil.fdec3.format(effectiveMaxLowDtRatio));
        tags.add("ping=" + pingMs);
        tags.add("jitter=" + StringUtil.fdec3.format(jitterMs));

        final ViolationData vd = new ViolationData(this, player, data.timerVL, severity, cc.timerActions);
        if (vd.needsParameters() || pData.isDebugActive(type)) {
            vd.setParameter(ParameterName.TAGS, StringUtil.join(tags, ","));
        }
        final boolean cancel = executeActions(vd).willCancel();
        final boolean evidenceCancel = applyEvidenceFusion(player, severity, data, pData);
        return cancel || evidenceCancel;
    }

    private boolean applyEvidenceFusion(final Player player,
                                        final double severity,
                                        final MovingData data,
                                        final IPlayerData pData) {
        if (!pData.isCheckActive(CheckType.COMBINED_IMPROBABLE, player)) {
            return false;
        }
        final long now = System.currentTimeMillis();
        final CombinedConfig combinedConfig = pData.getGenericInstance(CombinedConfig.class);
        final float base = (float) Math.max(0.4, Math.min(7.0, 0.7 + severity * 5.0));
        final double stage2Threshold = EvidenceFusionProfile.stage2Threshold(EVIDENCE_STAGE2_VL, combinedConfig, combinedConfig.evidenceProfileMovingTimer);
        final double stage3Threshold = EvidenceFusionProfile.stage3Threshold(EVIDENCE_STAGE3_VL, combinedConfig, combinedConfig.evidenceProfileMovingTimer);

        if (data.timerVL < stage2Threshold) {
            Improbable.feed(player, EvidenceFusionProfile.feedWeight(base * 0.5f, combinedConfig, combinedConfig.evidenceProfileMovingTimer), now, pData);
            return false;
        }
        if (data.timerVL >= stage3Threshold) {
            return Improbable.check(player,
                    EvidenceFusionProfile.stage3Weight(base * 1.20f, combinedConfig, combinedConfig.evidenceProfileMovingTimer),
                    now,
                    "moving.timer.stage3",
                    pData);
        }
        return Improbable.check(player,
                EvidenceFusionProfile.stage2Weight(base * 0.80f, combinedConfig, combinedConfig.evidenceProfileMovingTimer),
                now,
                "moving.timer.stage2",
                pData);
    }
}
