package engine.scene.objects.robots;

import engine.InteractionType;
import engine.scene.objects.SceneObject;
import engine.scene.objects.composite.SceneRectangle;
import math.Vector3;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SentryTurret {

    // ---- Gelenkzustände
    public float baseYawDeg   = 0f;   // Drehung der Basis um Y
    public float headPitchDeg = 0f;   // Neigung des Kopfes (nach oben/unten)
    public float radarYawDeg  = 0f;   // zusätzliche Drehung des Radars
    public float barrelRecoil = 0f;   // Rückstoß (0 = vorne, >0 = nach hinten gezogen)

    // Limits
    private static final float HEAD_PITCH_MIN = -45f;
    private static final float HEAD_PITCH_MAX =  45f;
    private static final float RECOIL_MAX     =  0.30f;

    // ---- Maße (beliebig anpassen)
    private static final float BASE_W   = 1.4f, BASE_D   = 1.4f, BASE_H   = 0.40f;
    private static final float COLUMN_W = 0.60f, COLUMN_D = 0.60f, COLUMN_H = 0.80f;
    private static final float HEAD_W   = 0.90f, HEAD_D   = 0.90f, HEAD_H   = 0.45f;
    private static final float GUN_L    = 1.40f, GUN_W    = 0.18f, GUN_H    = 0.18f;
    private static final float GUN_OFFSET_SIDE = 0.30f;   // seitlicher Abstand links/rechts
    private static final float RADAR_W  = 0.60f, RADAR_D  = 0.60f, RADAR_H  = 0.20f;

    private static final float EPS = 0.0008f;

    // ---- Basisposition (Weltkoordinate)
    private final Vector3 basePos;

    // ---- Teile
    private final ClickPart base, column, head, gunLeft, gunRight, radar;
    private final List<SceneObject> parts;

    public SentryTurret(Vector3 basePosition) {
        this.basePos = basePosition;

        // farbige Teile mit Primary / Secondary Aktionen
        base = new ClickPart(
                "Base",
                BASE_W, BASE_H, BASE_D,
                new Color(200, 60, 60),
                () -> baseYawDeg += 10f,   // PRIMARY
                () -> baseYawDeg -= 10f    // SECONDARY
        );

        column = new ClickPart(
                "Column",
                COLUMN_W, COLUMN_H, COLUMN_D,
                new Color(60, 200, 60),
                () -> baseYawDeg -= 10f,   // PRIMARY
                () -> baseYawDeg += 10f    // SECONDARY
        );

        head = new ClickPart(
                "Head",
                HEAD_W, HEAD_H, HEAD_D,
                new Color(60, 60, 200),
                () -> headPitchDeg = clamp(headPitchDeg + 5f, HEAD_PITCH_MIN, HEAD_PITCH_MAX),
                () -> headPitchDeg = clamp(headPitchDeg - 5f, HEAD_PITCH_MIN, HEAD_PITCH_MAX)
        );

        gunLeft = new ClickPart(
                "GunLeft",
                GUN_W, GUN_H, GUN_L,
                new Color(220, 220, 220),
                () -> barrelRecoil = clamp(barrelRecoil + 0.10f, 0f, RECOIL_MAX),   // fire/recoil
                () -> barrelRecoil = clamp(barrelRecoil - 0.10f, 0f, RECOIL_MAX)    // reset
        );

        gunRight = new ClickPart(
                "GunRight",
                GUN_W, GUN_H, GUN_L,
                new Color(220, 220, 220),
                () -> barrelRecoil = clamp(barrelRecoil + 0.10f, 0f, RECOIL_MAX),
                () -> barrelRecoil = clamp(barrelRecoil - 0.10f, 0f, RECOIL_MAX)
        );

        radar = new ClickPart(
                "Radar",
                RADAR_W, RADAR_H, RADAR_D,
                new Color(255, 255, 0),
                () -> radarYawDeg += 15f,
                () -> radarYawDeg -= 15f
        );

        ArrayList<SceneObject> list = new ArrayList<>();
        list.add(base);
        list.add(column);
        list.add(head);
        list.add(gunLeft);
        list.add(gunRight);
        list.add(radar);
        parts = Collections.unmodifiableList(list);

        updateKinematics();
    }

    public List<SceneObject> getParts() {
        return parts;
    }

    // ---- Kinematik / Hierarchie
    public void updateKinematics() {
        // 1) Base (steht auf Boden bei basePos)
        Vector3 baseCenter = new Vector3(
                basePos.x,
                basePos.y + BASE_H * 0.5f,
                basePos.z
        );
        base.setPosition(baseCenter);
        base.setRotation(new Vector3(0f, baseYawDeg, 0f));

        // 2) Column auf Base
        Vector3 columnCenter = baseCenter.add(
                0f,
                BASE_H * 0.5f + COLUMN_H * 0.5f,
                0f
        );
        column.setPosition(columnCenter);
        column.setRotation(new Vector3(0f, baseYawDeg, 0f));

        // 3) Head auf Column
        Vector3 headCenter = columnCenter.add(
                0f,
                COLUMN_H * 0.5f + HEAD_H * 0.5f,
                EPS
        );
        head.setPosition(headCenter);
        head.setRotation(new Vector3(headPitchDeg, baseYawDeg, 0f));

        // 4) Richtungsvektoren für Kopf vorne und Seite
        Vector3 dirForward = dirFromYawPitch(baseYawDeg, headPitchDeg);  // wohin schaut der Kopf?
        Vector3 dirSide = new Vector3(dirForward.z, 0f, -dirForward.x).normalize(); // seitlich (links/rechts)

        float headRadius = HEAD_D * 0.5f;
        float gunHalfL   = GUN_L * 0.5f;

        // Recoil wirkt entgegengesetzt zur Schussrichtung
        float effectiveGunOffset = headRadius + gunHalfL - barrelRecoil;

        // 5) Linkes Rohr
        Vector3 gunLeftCenter = headCenter
                .add(dirForward.mul(effectiveGunOffset))     // nach vorne
                .add(dirSide.mul(+GUN_OFFSET_SIDE));         // seitlich links
        gunLeft.setPosition(gunLeftCenter);
        gunLeft.setRotation(new Vector3(headPitchDeg, baseYawDeg, 0f));

        // 6) Rechtes Rohr
        Vector3 gunRightCenter = headCenter
                .add(dirForward.mul(effectiveGunOffset))
                .add(dirSide.mul(-GUN_OFFSET_SIDE));         // seitlich rechts
        gunRight.setPosition(gunRightCenter);
        gunRight.setRotation(new Vector3(headPitchDeg, baseYawDeg, 0f));

        // 7) Radar oben auf Head
        Vector3 radarCenter = headCenter.add(
                0f,
                HEAD_H * 0.5f + RADAR_H * 0.5f,
                0f
        );
        radar.setPosition(radarCenter);
        radar.setRotation(new Vector3(0f, baseYawDeg + radarYawDeg, 0f));
    }

    // ---- Helpers

    private static Vector3 dirFromYawPitch(float yawDeg, float pitchDeg) {
        float yaw   = (float) Math.toRadians(yawDeg);
        float pitch = (float) Math.toRadians(pitchDeg);
        float x = (float) Math.sin(yaw);
        float y = (float) Math.sin(pitch);
        float z = (float) (Math.cos(yaw) * Math.cos(pitch));
        return new Vector3(x, y, z).normalize();
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    // ---- Klickbares Rechteck mit PRIMARY/SECONDARY Aktionen
    private class ClickPart extends SceneRectangle {
        private final String label;
        private final Runnable onPrimary;
        private final Runnable onSecondary;

        ClickPart(String label,
                  float w, float h, float d,
                  Color color,
                  Runnable onPrimary,
                  Runnable onSecondary) {
            super(0f, 0f, 0f, w, h, d);
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

        @Override
        public String toString() {
            return "SentryTurret:" + label;
        }
    }
}
