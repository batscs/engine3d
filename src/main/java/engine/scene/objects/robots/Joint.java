package engine.scene.objects.robots;

import engine.Engine;
import engine.InteractionType;
import engine.scene.objects.BoundingBox;
import engine.scene.objects.Renderable;
import engine.scene.objects.SceneObject;
import math.Matrix4;
import math.Vector3;

import java.util.ArrayList;
import java.util.List;

/**
 * Hierarchical joint which rotates around a pivot and keeps children attached.
 * - children captured at construction or when attached (their world position & local rotation)
 * - setRotation(...) rotates children around pivot (only descendants move)
 * - attachChildAtLocal(...) & attachChildAtWorld(...) allow runtime attaching
 *
 * Rotation order / conventions follow the engine used in earlier parts of your code:
 *  yaw = -rotation.y (negated)
 *  pitch = rotation.x
 *  roll = rotation.z
 *
 * Uses only SceneObject API (setPosition / setRotation).
 */
public class Joint implements SceneObject {

    private Vector3 pivot;                   // world pivot position
    private Vector3 rotation = new Vector3(0f, 0f, 0f);

    private final List<SceneObject> children = new ArrayList<>();
    private final List<Vector3> originalOffsets = new ArrayList<>();
    private final List<Vector3> originalLocalRotations = new ArrayList<>();

    public Joint(Vector3 pivot, List<? extends SceneObject> initialChildren) {
        this.pivot = pivot;
        if (initialChildren != null) {
            for (SceneObject c : initialChildren) {
                addChildCapture(c);
            }
        }
        updateChildren();
    }

    // capture child's current world pos & local rotation relative to this pivot (used for later rotation)
    private void addChildCapture(SceneObject child) {
        children.add(child);
        originalOffsets.add(child.getPosition().sub(pivot));
        originalLocalRotations.add(child.getRotation());
    }

    // Build rotation matrix from this.rotation using engine convention
    private Matrix4 buildRotationMatrix() {
        float yawRad   = (float) Math.toRadians(-rotation.y);
        float pitchRad = (float) Math.toRadians(rotation.x);
        float rollRad  = (float) Math.toRadians(rotation.z);

        Matrix4 Ryaw   = Matrix4.rotationAroundAxis(new Vector3(0, 1, 0), yawRad);
        Matrix4 Rpitch = Matrix4.rotationAroundAxis(new Vector3(1, 0, 0), pitchRad);
        Matrix4 Rroll  = Matrix4.rotationAroundAxis(new Vector3(0, 0, 1), rollRad);

        return Rroll.mul(Rpitch).mul(Ryaw);
    }

    // reposition children according to pivot + rotation and reapply child's stored local rotation
    private void updateChildren() {
        Matrix4 R = buildRotationMatrix();

        for (int i = 0; i < children.size(); i++) {
            SceneObject child = children.get(i);
            Vector3 origOffset = originalOffsets.get(i);
            Vector3 origLocalRot = originalLocalRotations.get(i);

            Vector3 rotatedOffset = R.transform(origOffset);
            Vector3 worldPos = pivot.add(rotatedOffset);

            // set child's world position (if child is a Joint, its pivot moves too)
            child.setPosition(worldPos);

            // world rotation = joint rotation + child's captured local rotation
            Vector3 worldRot = new Vector3(
                    origLocalRot.x + rotation.x,
                    origLocalRot.y + rotation.y,
                    origLocalRot.z + rotation.z
            );
            child.setRotation(worldRot);
        }
    }

    /** Attach an already-existing SceneObject as a child using a local point in this joint's local frame.
     * localPoint is in this joint's local coordinates (neutral). It will be transformed by current joint rotation.
     * After attaching, the child's pivot/position will be set to the computed world position, and the joint will
     * capture the child's offsets so future rotations keep it glued. */
    public void attachChildAtLocal(SceneObject child, Vector3 localPoint) {
        Matrix4 R = buildRotationMatrix();
        Vector3 world = pivot.add(R.transform(localPoint));
        child.setPosition(world);             // child pivot updated (if child is a Joint)
        addChildCapture(child);
        updateChildren();
    }

    /** Attach child using a world-space point (child's pivot will be set to worldPoint). */
    public void attachChildAtWorld(SceneObject child, Vector3 worldPoint) {
        child.setPosition(worldPoint);
        addChildCapture(child);
        updateChildren();
    }

    /** Detach child (if present). */
    public boolean detachChild(SceneObject child) {
        int idx = children.indexOf(child);
        if (idx == -1) return false;
        children.remove(idx);
        originalOffsets.remove(idx);
        originalLocalRotations.remove(idx);
        return true;
    }

    @Override
    public void tick() {
        for (SceneObject c : children) c.tick();
    }

    @Override
    public Vector3 getPosition() {
        return pivot;
    }

    @Override
    public BoundingBox getBoundingBox() {
        BoundingBox result = null;
        for (SceneObject c : children) {
            BoundingBox cb = c.getBoundingBox();
            if (cb == null) continue;
            if (result == null) result = new BoundingBox(cb.getMin(), cb.getMax());
            else {
                result.setMin(new Vector3(
                        Math.min(result.getMin().x, cb.getMin().x),
                        Math.min(result.getMin().y, cb.getMin().y),
                        Math.min(result.getMin().z, cb.getMin().z)
                ));
                result.setMax(new Vector3(
                        Math.max(result.getMax().x, cb.getMax().x),
                        Math.max(result.getMax().y, cb.getMax().y),
                        Math.max(result.getMax().z, cb.getMax().z)
                ));
            }
        }
        return result;
    }

    @Override
    public List<Renderable> getRenderables() {
        List<Renderable> out = new ArrayList<>();
        for (SceneObject c : children) out.addAll(c.getRenderables());
        return out;
    }

    @Override
    public void move(Vector3 adjustment) {
        setPosition(pivot.add(adjustment));
    }

    @Override
    public void setPosition(Vector3 pos) {
        this.pivot = pos;
        updateChildren();
    }

    @Override
    public Vector3 getRotation() {
        return rotation;
    }

    @Override
    public void setRotation(Vector3 rotation) {
        this.rotation = rotation;
        updateChildren();
    }

    @Override
    public void interact(InteractionType type) { /* no-op */ }
}
