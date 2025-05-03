package engine.scene.objects.composite;

import engine.render.Viewport;
import engine.scene.objects.Renderable;
import engine.scene.objects.SceneObject;
import engine.scene.objects.mesh.SceneTriangle;
import math.Matrix4;
import math.Vector3;
import util.SceneUtil;

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
        Vector3 sum = new Vector3(0, 0, 0);
        for (SceneObject mesh : meshes) {
            sum = sum.add(mesh.getPosition());
        }
        return sum.div(meshes.size());
    }

}
