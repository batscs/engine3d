package engine.scene.objects.robots;

import engine.scene.objects.SceneObject;
import engine.scene.objects.composite.SceneCylinder;
import engine.scene.objects.composite.SceneRectangle;
import engine.scene.objects.composite.SceneSphere;
import math.Matrix4;
import math.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Re-usable attachable arm. Built in a neutral pose (all-zero angles) so Joints capture offsets safely.
 *
 * API:
 *   getBaseJoint() - the top-level Joint for this arm (attach this to the scene or to another Joint)
 *   getJoint(name) - "base","shoulder","elbow","wrist"  (returns Joint)
 *   attachChildArm(AttachableArm child) - convenience to attach child's base to this arm's wrist pivot
 *   setJointAngles(j1..j6) - pose the arm (absolute degrees)
 *
 * The arm is hierarchical:
 *   baseJoint -> shoulderJoint -> elbowJoint -> wristJoint -> end-effector
 *
 * All geometry uses only your existing SceneObject primitives and Joint.
 */
public class AttachableArm {

    // geometry (tweak for look)
    private final float baseHeight = 0.18f;
    private final float columnHeight = 1.05f;
    private final float upperArmLength = 1.0f;
    private final float forearmLength = 0.8f;
    private final float wristOffset = 0.12f;
    private final float elbowRadius = 0.13f;

    // joints
    private final Joint baseJoint;
    private final Joint shoulderJoint;
    private final Joint elbowJoint;
    private final Joint wristJoint;

    // visible primitives (kept for color/inspection)
    private final SceneCylinder basePlatform;
    private final SceneCylinder column;
    private final SceneCylinder shoulderCap;
    private final SceneRectangle upperArmRect;
    private final SceneCylinder elbowCylinder;
    private final SceneRectangle forearmRect;
    private final SceneCylinder wristCylinder;
    private final SceneSphere eeSphere;

    // mapping for convenience
    private final Map<String, Joint> jointMap = new HashMap<>();

    // joint angles (stored, degrees)
    private float j1 = 40f, j2 = -30f, j3 = 50f, j4 = 20f, j5 = -15f, j6 = 10f;

    /**
     * Build arm whose ground base rests on basePos (Vector3).
     * The arm is created in NEUTRAL pose (no rotations) so Joint captures offsets correctly.
     */
    public AttachableArm(Vector3 basePos) {
        // neutral pivot positions
        Vector3 baseCenter = basePos.add(new Vector3(0f, baseHeight / 2f, 0f));
        Vector3 columnCenter = basePos.add(new Vector3(0f, baseHeight + columnHeight / 2f, 0f));
        Vector3 shoulderPivot = basePos.add(new Vector3(0f, baseHeight + columnHeight, 0f));

        Vector3 upperNeutral = shoulderPivot.add(new Vector3(upperArmLength / 2f, 0f, 0f));
        Vector3 elbowPivot = shoulderPivot.add(new Vector3(upperArmLength, 0f, 0f));

        Vector3 forearmNeutral = elbowPivot.add(new Vector3(forearmLength / 2f, 0f, 0f));
        Vector3 wristPivot = elbowPivot.add(new Vector3(forearmLength, 0f, 0f));
        Vector3 eeNeutral = wristPivot.add(new Vector3(wristOffset + 0.08f, 0f, 0f));

        // create visible primitives at neutral positions
        basePlatform = new SceneCylinder(baseCenter.x, baseCenter.y, baseCenter.z, 0.45f, baseHeight, 32);
        column = new SceneCylinder(columnCenter.x, columnCenter.y, columnCenter.z, 0.16f, columnHeight, 24);
        shoulderCap = new SceneCylinder(shoulderPivot.x, shoulderPivot.y, shoulderPivot.z, 0.20f, 0.22f, 20);

        upperArmRect = new SceneRectangle(upperNeutral.x, upperNeutral.y, upperNeutral.z, upperArmLength, 0.14f, 0.14f);

        // elbow cylinder: rotate locally so curved side faces +Y (and pipe comes out along +X in neutral)
        elbowCylinder = new SceneCylinder(elbowPivot.x, elbowPivot.y, elbowPivot.z, elbowRadius, 0.18f, 20);
        elbowCylinder.setRotation(new Vector3(0f, 0f, -90f)); // visual alignment only

        forearmRect = new SceneRectangle(forearmNeutral.x, forearmNeutral.y, forearmNeutral.z, forearmLength, 0.12f, 0.12f);

        Vector3 wristCylinderNeutral = wristPivot.add(new Vector3(wristOffset / 2f, 0f, 0f));
        wristCylinder = new SceneCylinder(wristCylinderNeutral.x, wristCylinderNeutral.y, wristCylinderNeutral.z, 0.08f, 0.22f, 18);

        eeSphere = new SceneSphere(eeNeutral.x, eeNeutral.y, eeNeutral.z, 0.11f);

        // Build joint hierarchy bottom-up in neutral pose (so Joint captures offsets relative to each pivot)
        wristJoint = new Joint(wristPivot, List.of(wristCylinder, eeSphere));
        elbowJoint = new Joint(elbowPivot, List.of(elbowCylinder, forearmRect, wristJoint));
        shoulderJoint = new Joint(shoulderPivot, List.of(upperArmRect, elbowJoint));
        baseJoint = new Joint(baseCenter, List.of(basePlatform, column, shoulderCap, shoulderJoint));

        // register joints
        jointMap.put("base", baseJoint);
        jointMap.put("shoulder", shoulderJoint);
        jointMap.put("elbow", elbowJoint);
        jointMap.put("wrist", wristJoint);

        // apply an interesting default pose (absolute angles)
        setJointAngles(j1, j2, j3, j4, j5, j6);
    }

    /** convenience: return the top-level joint to add to the scene */
    public SceneObject getBaseJoint() {
        return baseJoint;
    }

    /** get named joint: "base","shoulder","elbow","wrist" */
    public Joint getJoint(String name) {
        return jointMap.get(name);
    }

    /** convenience: attach a child AttachableArm to THIS arm's wrist pivot (local point (0,0,0) ) */
    public void attachChildArm(AttachableArm child) {
        // attach base of child at this wrist pivot local point (0,0,0)
        this.wristJoint.attachChildAtLocal(child.getBaseJoint(), new Vector3(0f, 0f, 0f));
    }

    /** attach child arm at a specific local point on a named joint (e.g. shoulder, elbow, wrist) */
    public void attachChildArmAt(AttachableArm child, String jointName, Vector3 localPointOnThatJoint) {
        Joint j = jointMap.get(jointName);
        if (j == null) throw new IllegalArgumentException("unknown joint: " + jointName);
        j.attachChildAtLocal(child.getBaseJoint(), localPointOnThatJoint);
    }

    /** return the list to add to scene (single top-level joint) */
    public List<SceneObject> getParts() {
        List<SceneObject> out = new ArrayList<>();
        out.add(baseJoint);
        return out;
    }

    /**
     * Set absolute joint angles (degrees).
     * j1 = base yaw (applied on baseJoint as rotation.y)
     * j2 = shoulder pitch (applied on shoulderJoint as rotation.x)
     * j3 = elbow pitch (applied on elbowJoint as rotation.x)
     * j4 = wrist roll  (applied z)
     * j5 = wrist pitch (applied x)
     * j6 = wrist yaw   (applied y)
     */
    public void setJointAngles(float j1Deg, float j2Deg, float j3Deg, float j4Deg, float j5Deg, float j6Deg) {
        this.j1 = j1Deg;
        this.j2 = j2Deg;
        this.j3 = j3Deg;
        this.j4 = j4Deg;
        this.j5 = j5Deg;
        this.j6 = j6Deg;

        baseJoint.setRotation(new Vector3(0f, j1, 0f));
        shoulderJoint.setRotation(new Vector3(j2, 0f, 0f));
        elbowJoint.setRotation(new Vector3(j3, 0f, 0f));
        // wrist: x = pitch (j5), y = yaw (j6), z = roll (j4)
        wristJoint.setRotation(new Vector3(j5, j6, j4));
    }
}
