package engine.scene.objects.robots;

import engine.scene.objects.SceneObject;
import engine.scene.objects.composite.SceneRectangle;
import engine.scene.objects.BoundingBox;
import engine.scene.objects.Renderable;
import math.Vector3;
import math.Matrix4;

import java.util.ArrayList;
import java.util.List;

/**
 * Hierarchical Stanford arm that rotates each segment about the connecting joint.
 * Also resets to base pose whenever a click would push a joint past its range.
 */
public class StanfordArm implements SceneObject {

    private final ArmSegment base, upper, fore;
    private float baseYaw = 0f, shoulderPitch = 0f, elbowPitch = 0f;
    private float time = 0f;
    private final Vector3 basePos;

    // joint motion limits (tweak to your liking)
    private static final float BASE_MIN = -135f, BASE_MAX = 360f;
    private static final float SHOULDER_MIN = -20f, SHOULDER_MAX = 80f;
    private static final float ELBOW_MIN = -10f, ELBOW_MAX = 135f;

    public StanfordArm(Vector3 basePos) {
        this.basePos = basePos;

        // width, height, depth (height = length along local +Y)
        base  = new ArmSegment(this, Joint.BASE,     0.6f, 0.4f, 0.6f);
        upper = new ArmSegment(this, Joint.SHOULDER, 0.25f, 0.6f, 0.25f);
        fore  = new ArmSegment(this, Joint.ELBOW,    0.2f, 0.7f, 0.2f);

        updateHierarchy();
    }

    @Override
    public void tick() {
        time += 0.02f;

        baseYaw       = (float) Math.sin(time * 0.5f) * 90f;
        shoulderPitch = (float) Math.sin(time * 0.8f) * 70f;
        elbowPitch    = (float) Math.sin(time * 1.1f) * 100f;

        updateHierarchy();
    }

    private void updateHierarchy() {
        // Base center and rotation (base rotates only about yaw)
        base.setPosition(basePos);
        base.setRotation(new Vector3(0f, baseYaw, 0f));

        // Build rotation matrices consistent with SceneTriangle/Composite:
        // Rpitch.mul(Ryaw) where Ryaw uses a negative yaw angle inside transform code.
        float yawRad = (float) Math.toRadians(baseYaw);
        float shoulderRad = (float) Math.toRadians(shoulderPitch);
        float elbowRad = (float) Math.toRadians(elbowPitch);

        Matrix4 Ryaw_base   = Matrix4.rotationAroundAxis(new Vector3(0,1,0), -yawRad);
        Matrix4 Rpitch_base = Matrix4.rotationAroundAxis(new Vector3(1,0,0), 0f); // base has no pitch
        Matrix4 R_base = Rpitch_base.mul(Ryaw_base);

        // Top of base (joint between base and upper): base center + R_base * (0, base.height/2, 0)
        Vector3 baseTop = basePos.add(R_base.transform(new Vector3(0f, base.getHeight() * 0.5f, 0f)));

        // Upper rotation (apply base yaw then shoulder pitch)
        Matrix4 Ryaw_upper   = Matrix4.rotationAroundAxis(new Vector3(0,1,0), -yawRad);
        Matrix4 Rpitch_upper = Matrix4.rotationAroundAxis(new Vector3(1,0,0), shoulderRad);
        Matrix4 R_upper = Rpitch_upper.mul(Ryaw_upper);

        // Upper center = baseTop + R_upper * (0, upper.h/2, 0)
        Vector3 upperCenter = baseTop.add(R_upper.transform(new Vector3(0f, upper.getHeight() * 0.5f, 0f)));
        upper.setPosition(upperCenter);
        // rotation stored as (pitch, yaw, roll) following your convention
        upper.setRotation(new Vector3(shoulderPitch, baseYaw, 0f));

        // Elbow joint (top of upper): upperCenter + R_upper * (0, upper.h/2, 0)
        Vector3 elbowJoint = upperCenter.add(R_upper.transform(new Vector3(0f, upper.getHeight() * 0.5f, 0f)));

        // Fore rotation (base yaw + shoulder pitch + elbow pitch)
        Matrix4 Ryaw_fore   = Matrix4.rotationAroundAxis(new Vector3(0,1,0), -yawRad);
        Matrix4 Rpitch_fore = Matrix4.rotationAroundAxis(new Vector3(1,0,0), shoulderRad + elbowRad);
        Matrix4 R_fore = Rpitch_fore.mul(Ryaw_fore);

        // Fore center = elbowJoint + R_fore * (0, fore.h/2, 0)
        Vector3 foreCenter = elbowJoint.add(R_fore.transform(new Vector3(0f, fore.getHeight() * 0.5f, 0f)));
        fore.setPosition(foreCenter);
        fore.setRotation(new Vector3(shoulderPitch + elbowPitch, baseYaw, 0f));
    }

    /**
     * On click: either move joint by +10 deg or if that would exceed range, reset to base pose.
     */
    protected void onSegmentClicked(Joint j) {
        final float delta = 10f;

        switch (j) {
            case BASE -> {
                if (wouldExceed(baseYaw, delta, BASE_MIN, BASE_MAX)) {
                    resetToBasePose();
                } else {
                    baseYaw = clamp(baseYaw + delta, BASE_MIN, BASE_MAX);
                }
            }
            case SHOULDER -> {
                if (wouldExceed(shoulderPitch, delta, SHOULDER_MIN, SHOULDER_MAX)) {
                    resetToBasePose();
                } else {
                    shoulderPitch = clamp(shoulderPitch + delta, SHOULDER_MIN, SHOULDER_MAX);
                }
            }
            case ELBOW -> {
                if (wouldExceed(elbowPitch, delta, ELBOW_MIN, ELBOW_MAX)) {
                    resetToBasePose();
                } else {
                    elbowPitch = clamp(elbowPitch + delta, ELBOW_MIN, ELBOW_MAX);
                }
            }
        }
        updateHierarchy();
    }

    private boolean wouldExceed(float current, float delta, float min, float max) {
        float next = current + delta;
        return next > max || next < min;
    }

    private float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    private void resetToBasePose() {
        baseYaw = 0f;
        shoulderPitch = 0f;
        elbowPitch = 0f;
        updateHierarchy();
    }

    @Override public void interact() {} // parent not directly clickable
    @Override public void move(Vector3 v) {}
    @Override public void setPosition(Vector3 p) {}
    @Override public Vector3 getPosition() { return basePos; }
    @Override public Vector3 getRotation() { return new Vector3(0,0,0); }
    @Override public void setRotation(Vector3 r) {}
    @Override public BoundingBox getBoundingBox() { return null; }

    @Override
    public List<Renderable> getRenderables() {
        List<Renderable> all = new ArrayList<>();
        all.addAll(base.getRenderables());
        all.addAll(upper.getRenderables());
        all.addAll(fore.getRenderables());
        return all;
    }

    /** Adds all 3 cubes to scene */
    public List<SceneObject> getParts() {
        return List.of(base, upper, fore);
    }

    enum Joint { BASE, SHOULDER, ELBOW }

    static class ArmSegment extends SceneRectangle {
        private final StanfordArm parent;
        private final Joint joint;
        private final float height;

        public ArmSegment(StanfordArm parent, Joint joint, float w, float h, float d) {
            super(0f, 0f, 0f, w, h, d);
            this.parent = parent;
            this.joint = joint;
            this.height = h;
        }

        public float getHeight() { return height; }

        @Override
        public void interact() {
            parent.onSegmentClicked(joint);
        }
    }
}
