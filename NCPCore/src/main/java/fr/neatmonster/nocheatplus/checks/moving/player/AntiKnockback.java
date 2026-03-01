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
 * Grim-inspired lightweight AntiKB check:
 * compare expected knockback vs actually taken movement (horizontal + vertical),
 * then use buffering + VL/actions.
 */
public class AntiKnockback extends Check {

    private static final double EVIDENCE_STAGE2_VL = 6.0;
    private static final double EVIDENCE_STAGE3_VL = 20.0;

    private final List<String> tags = new ArrayList<String>();

    public AntiKnockback() {
        super(CheckType.MOVING_VELOCITY);
    }

    public boolean check(final Player player,
                         final double ratioH,
                         final double ratioV,
                         final double expectedH,
                         final double movedH,
                         final double expectedV,
                         final double movedV,
                         final double minExpectedH,
                         final double minExpectedV,
                         final double minTakeH,
                         final double minTakeV,
                         final int pingMs,
                         final double jitterMs,
                         final MovingData data,
                         final MovingConfig cc,
                         final IPlayerData pData) {

        final boolean badH = expectedH >= minExpectedH && ratioH < minTakeH;
        final boolean badV = expectedV >= minExpectedV && ratioV < minTakeV;

        if (!badH && !badV) {
            data.velocityVL *= 0.98;
            return false;
        }

        final double deficitH = badH ? (minTakeH - ratioH) : 0.0;
        final double deficitV = badV ? (minTakeV - ratioV) : 0.0;
        final double severity = Math.max(deficitH, deficitV) + (badH && badV ? 0.05 : 0.0);
        final double addedVl = Math.max(0.25, severity * 5.0);
        data.velocityVL += addedVl;

        tags.clear();
        if (badH) tags.add("h");
        if (badV) tags.add("v");

        final ViolationData vd = new ViolationData(this, player, data.velocityVL, severity, cc.velocityActions);
        if (vd.needsParameters() || pData.isDebugActive(type)) {
            vd.setParameter(ParameterName.TAGS,
                    StringUtil.join(tags, "+")
                            + " expH=" + StringUtil.fdec3.format(expectedH)
                            + " gotH=" + StringUtil.fdec3.format(movedH)
                            + " rH=" + StringUtil.fdec3.format(ratioH)
                            + " minH=" + StringUtil.fdec3.format(minTakeH)
                            + " expV=" + StringUtil.fdec3.format(expectedV)
                            + " gotV=" + StringUtil.fdec3.format(movedV)
                            + " rV=" + StringUtil.fdec3.format(ratioV)
                            + " minV=" + StringUtil.fdec3.format(minTakeV)
                            + " ping=" + pingMs
                            + " jitter=" + StringUtil.fdec3.format(jitterMs));
        }
        final boolean cancel = executeActions(vd).willCancel();
        final boolean evidenceCancel = applyEvidenceFusion(player, severity, badH, badV, data, pData);
        return cancel || evidenceCancel;
    }

    private boolean applyEvidenceFusion(final Player player,
                                        final double severity,
                                        final boolean badH,
                                        final boolean badV,
                                        final MovingData data,
                                        final IPlayerData pData) {
        if (!pData.isCheckActive(CheckType.COMBINED_IMPROBABLE, player)) {
            return false;
        }
        final long now = System.currentTimeMillis();
        final CombinedConfig combinedConfig = pData.getGenericInstance(CombinedConfig.class);
        final float base = (float) Math.max(0.5, Math.min(8.0, 0.8 + severity * 6.0 + (badH && badV ? 0.6 : 0.0)));
        final double stage2Threshold = EvidenceFusionProfile.stage2Threshold(EVIDENCE_STAGE2_VL, combinedConfig);
        final double stage3Threshold = EvidenceFusionProfile.stage3Threshold(EVIDENCE_STAGE3_VL, combinedConfig);

        if (data.velocityVL < stage2Threshold) {
            Improbable.feed(player, EvidenceFusionProfile.feedWeight(base * 0.55f, combinedConfig), now, pData);
            return false;
        }
        if (data.velocityVL >= stage3Threshold) {
            return Improbable.check(player,
                    EvidenceFusionProfile.stage3Weight(base * 1.25f, combinedConfig),
                    now,
                    "moving.velocity.stage3",
                    pData);
        }
        return Improbable.check(player,
                EvidenceFusionProfile.stage2Weight(base * 0.85f, combinedConfig),
                now,
                "moving.velocity.stage2",
                pData);
    }
}
