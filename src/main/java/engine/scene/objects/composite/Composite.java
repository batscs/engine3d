package engine.scene.objects.composite;

import engine.InteractionType;
import engine.scene.objects.BoundingBox;
import engine.scene.objects.Renderable;
import engine.scene.objects.SceneObject;
import math.Matrix4;
import math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class Composite implements SceneObject {

    final List<? extends SceneObject> meshes;
    private final List<Vector3> originalOffsets;
    private Vector3 position;
    private Vector3 rotation = new Vector3(0, 0, 0);

    public Composite(List<? extends SceneObject> meshes) {
        this.meshes = meshes;
        this.position = calculateAveragePosition();

        // Capture each child’s offset *once* in object‑space
        this.originalOffsets = new ArrayList<>();
        for (SceneObject mesh : meshes) {
            originalOffsets.add(mesh.getPosition().sub(position));
        }
    }

    @Override
    public void tick() {
        meshes.forEach(SceneObject::tick);
    }

    @Override
    public Vector3 getPosition() {
        return position;
    }

    @Override
    public BoundingBox getBoundingBox() {
        BoundingBox result = null;

        for (SceneObject child : meshes) {
            BoundingBox childBox = child.getBoundingBox();
            if (childBox == null) continue; // skip if child has no box

            if (result == null) {
                result = new BoundingBox(childBox.getMin(), childBox.getMax());
            } else {
                // expand result to include child's box
                result.setMin(new Vector3(
                        Math.min(result.getMin().x, childBox.getMin().x),
                        Math.min(result.getMin().y, childBox.getMin().y),
                        Math.min(result.getMin().z, childBox.getMin().z)
                ));
                result.setMax(new Vector3(
                        Math.max(result.getMax().x, childBox.getMax().x),
                        Math.max(result.getMax().y, childBox.getMax().y),
                        Math.max(result.getMax().z, childBox.getMax().z)
                ));
            }
        }

        return result; // could be null if no children have bounding boxes
    }


    @Override
    public List<Renderable> getRenderables() {
        return meshes.stream().flatMap(mesh -> mesh.getRenderables().stream()).toList();
    }

    @Override
    public void move(Vector3 adjustment) {
        setPosition(position.add(adjustment));
    }

    @Override
    public void setPosition(Vector3 pos) {
        this.position = pos;
        updateChildren();
    }

    @Override
    public Vector3 getRotation() {
        return rotation;
    }

    @Override
    public void setRotation(Vector3 newRotation) {
        this.rotation = newRotation;
        updateChildren();
    }

    @Override
    public void interact(InteractionType type) {

    }

    private void updateChildren() {
        // Build absolute rotation matrix from Euler (pitch=x, yaw=y)
        float yawRad   = (float)Math.toRadians(-rotation.y);
        float pitchRad = (float)Math.toRadians( rotation.x);
        Matrix4 Ryaw   = Matrix4.rotationAroundAxis(new Vector3(0,1,0), yawRad);
        Matrix4 Rpitch = Matrix4.rotationAroundAxis(new Vector3(1,0,0), pitchRad);
        Matrix4 R      = Rpitch.mul(Ryaw);

        // Re‑apply to every child
        for (int i = 0; i < meshes.size(); i++) {
            SceneObject child    = meshes.get(i);
            Vector3   origOffset = originalOffsets.get(i);

            // rotate the stored offset
            Vector3 rotatedOffset = R.transform(origOffset);

            // position = composite center + rotated offset
            child.setPosition(position.add(rotatedOffset));

            // let the triangle/cube apply its own rotation logic
            child.setRotation(rotation);
        }
    }


    private Vector3 calculateAveragePosition() {
        // Alte Logik (Durchschnitt von Dreieckscentern) führt zu Versatz.
        // Nutze stattdessen die BoundingBox über alle Kinder.
        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY, minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY, maxZ = Float.NEGATIVE_INFINITY;

        for (SceneObject mesh : meshes) {
            BoundingBox bb = mesh.getBoundingBox();
            if (bb == null) continue;
            minX = Math.min(minX, bb.getMin().x);
            minY = Math.min(minY, bb.getMin().y);
            minZ = Math.min(minZ, bb.getMin().z);
            maxX = Math.max(maxX, bb.getMax().x);
            maxY = Math.max(maxY, bb.getMax().y);
            maxZ = Math.max(maxZ, bb.getMax().z);
        }

        if (Float.isInfinite(minX)) {
            // Fallback: falls irgendwas schiefgeht, nimm 0,0,0
            return new Vector3(0,0,0);
        }

        return new Vector3(
                (minX + maxX) * 0.5f,
                (minY + maxY) * 0.5f,
                (minZ + maxZ) * 0.5f
        );
    }



}
