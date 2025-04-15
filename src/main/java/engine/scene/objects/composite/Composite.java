package engine.scene.objects.composite;

import engine.render.Viewport;
import engine.scene.objects.Renderable;
import engine.scene.objects.SceneObject;
import math.Vector3;
import util.SceneUtil;

import java.util.ArrayList;
import java.util.List;

public class Composite implements SceneObject {

    private List<? extends SceneObject> meshes;

    public Composite(List<? extends SceneObject> meshes) {
        if (meshes == null) {
            throw new IllegalArgumentException("SceneObject Composite Constructor doesn't accept null");
        }

        this.meshes = meshes;
    }

    @Override
    public void tick() {
        meshes.forEach(mesh -> mesh.tick());

    }

    @Override
    public Vector3 getPosition() {
        Vector3 sum = new Vector3(0, 0, 0);
        for (SceneObject mesh : meshes) {
            sum = sum.add(mesh.getPosition());
        }

        return sum.div(meshes.size());
    }

    @Override
    public List<Renderable> getRenderables() {
        return meshes.stream().flatMap(mesh -> mesh.getRenderables().stream()).toList();
    }

    @Override
    public void move(Vector3 adjustment) {
        meshes.forEach(mesh -> mesh.move(adjustment));
    }

    @Override
    public void setPosition(Vector3 pos) {
        // Calculate the current center of the composite.
        Vector3 currentCenter = getPosition();

        // For each child object, calculate its offset relative to the composite center,
        // and then set its position to the new composite position plus that offset.
        for (SceneObject mesh : meshes) {
            Vector3 offset = mesh.getPosition().sub(currentCenter);
            mesh.setPosition(pos.add(offset));
        }
    }

}
