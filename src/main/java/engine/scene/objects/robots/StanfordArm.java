package engine.scene.objects.robots;

import engine.Engine;
import engine.InteractionType;
import engine.scene.objects.SceneObject;
import engine.scene.objects.composite.SceneRectangle;
import math.Vector3;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StanfordArm {

    // ---- Gelenke
    public float baseYawDeg = 15f;
    public float slideY;          // aktueller Hub relativ zur Säulenmitte (wird im ctor gesetzt)
    public float slideMin;        // unteres Limit (negativ)
    public float slideMax;        // oberes  Limit (positiv)
    public float shoulderYawDeg = 0f;
    public float elbowPitchDeg  = 10f;
    public float wristYawDeg    = 0f;

    // ---- Maße
    private static final float BASE_W=1.2f,  BASE_D=1.2f,  BASE_H=0.40f;
    private static final float COLUMN_W=0.50f, COLUMN_D=0.50f, COLUMN_H=0.80f;
    private static final float SLIDER_W=0.60f, SLIDER_D=0.60f, SLIDER_H=0.30f;
    private static final float SHOULDER_W=0.60f, SHOULDER_D=0.60f, SHOULDER_H=0.30f;
    private static final float LINK1_L=1.20f, LINK1_W=0.22f, LINK1_H=0.22f;
    private static final float LINK2_L=1.00f, LINK2_W=0.20f, LINK2_H=0.20f;
    private static final float WRIST_W=0.25f, WRIST_H=0.25f, WRIST_D=0.25f;

    private static final float EPS = 0.0008f; // gegen Z-Fighting bei ausgeschaltetem DepthBuffer

    // ---- Basisposition
    private final Vector3 basePos;

    // ---- Teile
    private final ClickPart base, column, slider, shoulder, link1, link2, wrist;
    private final List<SceneObject> parts;

    public StanfordArm(Vector3 basePosition) {
        this.basePos = basePosition;

        float range = (COLUMN_H - SLIDER_H) * 0.5f;
        this.slideMin = -range;
        this.slideMax =  range;
        this.slideY   =  0f;

        // farbige Teile
        base = new ClickPart(
                "Base",
                BASE_W, BASE_H, BASE_D,
                new Color(255,0,0),
                () -> baseYawDeg += 10f,         // PRIMARY
                () -> baseYawDeg -= 10f          // SECONDARY
        );

        column = new ClickPart(
                "Column",
                COLUMN_W, COLUMN_H, COLUMN_D,
                new Color(0,255,0),
                () -> baseYawDeg -= 10f,         // PRIMARY
                () -> baseYawDeg += 10f          // SECONDARY
        );

        slider = new ClickPart(
                "Slider",
                SLIDER_W, SLIDER_H, SLIDER_D,
                new Color(0,0,255),
                () -> slideY = clamp(slideY + 0.1f, slideMin, slideMax),  // PRIMARY: hoch
                () -> slideY = clamp(slideY - 0.1f, slideMin, slideMax)   // SECONDARY: runter
        );

        shoulder = new ClickPart(
                "Shoulder",
                SHOULDER_W, SHOULDER_H, SHOULDER_D,
                new Color(255,255,0),
                () -> shoulderYawDeg += 10f,     // PRIMARY
                () -> shoulderYawDeg -= 10f      // SECONDARY
        );

        link1 = new ClickPart(
                "Link1",
                LINK1_W, LINK1_H, LINK1_L,
                new Color(255,0,255),
                () -> shoulderYawDeg -= 10f,     // PRIMARY
                () -> shoulderYawDeg += 10f      // SECONDARY
        );

        link2 = new ClickPart(
                "Link2",
                LINK2_W, LINK2_H, LINK2_L,
                new Color(0,255,255),
                () -> elbowPitchDeg += 5f,       // PRIMARY
                () -> elbowPitchDeg -= 5f        // SECONDARY
        );

        wrist = new ClickPart(
                "Wrist",
                WRIST_W, WRIST_H, WRIST_D,
                new Color(255,255,255),
                () -> wristYawDeg += 10f,        // PRIMARY
                () -> wristYawDeg -= 10f         // SECONDARY
        );

        ArrayList<SceneObject> list = new ArrayList<>();
        list.add(base); list.add(column); list.add(slider);
        list.add(shoulder); list.add(link1); list.add(link2); list.add(wrist);
        parts = Collections.unmodifiableList(list);

        updateKinematics();
    }
    public List<SceneObject> getParts() { return parts; }

    // ---- Kinematik
    public void updateKinematics() {
        // 1) Base
        Vector3 baseCenter = new Vector3(basePos.x, basePos.y + BASE_H * 0.5f, basePos.z);
        base.setPosition(baseCenter);
        base.setRotation(new Vector3(0f, baseYawDeg, 0f));

        // 2) Column auf Base
        Vector3 columnCenter = baseCenter.add(0f, BASE_H * 0.5f + COLUMN_H * 0.5f, 0f);
        column.setPosition(columnCenter);
        column.setRotation(new Vector3(0f, baseYawDeg, 0f));

        // 3) Slider: bewegt sich ENTLANG der Säulenmitte (kein Stapeln -> keine Lücke)
        float s = clamp(slideY, slideMin, slideMax);
        Vector3 sliderCenter = columnCenter.add(0f, s, EPS);
        slider.setPosition(sliderCenter);
        slider.setRotation(new Vector3(0f, baseYawDeg, 0f));

        // 4) Shoulder sitzt oben auf dem Slider
        float shoulderYawTotal = baseYawDeg + shoulderYawDeg;
        Vector3 shoulderCenter = sliderCenter.add(0f, SLIDER_H * 0.5f + SHOULDER_H * 0.5f, EPS);
        shoulder.setPosition(shoulderCenter);
        shoulder.setRotation(new Vector3(0f, shoulderYawTotal, 0f));

        // 5) Link1 entlang +Z der Shoulder
        Vector3 dirL1 = dirFromYawPitch(shoulderYawTotal, 0f);
        Vector3 link1Center = shoulderCenter.add(dirL1.mul(LINK1_L * 0.5f));
        link1.setPosition(link1Center);
        link1.setRotation(new Vector3(0f, shoulderYawTotal, 0f));

        Vector3 elbowPivot = shoulderCenter.add(dirL1.mul(LINK1_L));

        // 6) Link2: Yaw von Shoulder + Pitch am Ellbogen
        Vector3 dirL2 = dirFromYawPitch(shoulderYawTotal, elbowPitchDeg);
        Vector3 link2Center = elbowPivot.add(dirL2.mul(LINK2_L * 0.5f));
        link2.setPosition(link2Center);
        link2.setRotation(new Vector3(elbowPitchDeg, shoulderYawTotal, 0f));

        // 7) Wrist am Ende von Link2
        Vector3 wristCenter = elbowPivot.add(dirL2.mul(LINK2_L));
        wrist.setPosition(wristCenter);
        wrist.setRotation(new Vector3(elbowPitchDeg, shoulderYawTotal + wristYawDeg, 0f));
    }

    // ---- Helpers
    private static Vector3 dirFromYawPitch(float yawDeg, float pitchDeg) {
        float yaw = (float)Math.toRadians(yawDeg);
        float pitch = (float)Math.toRadians(pitchDeg);
        float x = (float)Math.sin(yaw);
        float y = (float)Math.sin(pitch);
        float z = (float)(Math.cos(yaw) * Math.cos(pitch));
        return new Vector3(x, y, z).normalize();
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    // ---- Klickbares Rechteck
    private class ClickPart extends SceneRectangle {
        private final String label;
        private final Runnable onPrimary;
        private final Runnable onSecondary;

        ClickPart(String label, float w, float h, float d, Color color,
                  Runnable onPrimary, Runnable onSecondary) {
            super(0,0,0, w,h,d);
            this.label = label;
            this.onPrimary = onPrimary;
            this.onSecondary = onSecondary;
            getRenderables().forEach(r -> {
                if (r instanceof engine.scene.objects.mesh.SceneTriangle t) {
                    t.setBaseColor(color);
                }
            });
        }

        @Override
        public void interact(InteractionType type) {
            if (type == InteractionType.PRIMARY) {
                if (onPrimary != null) onPrimary.run();
            } else if (type == InteractionType.SECONDARY) {
                if (onSecondary != null) onSecondary.run();
            }

            updateKinematics();
        }
        @Override public String toString() { return "StanfordArm:" + label; }
    }
}
