package engine.scene.objects.composite;

import engine.scene.Viewport;
import engine.scene.objects.SceneObject;
import engine.scene.objects.mesh.SceneTriangle;
import math.Vector3;

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
    public void draw(Viewport viewport) {
        meshes.forEach(mesh -> mesh.draw(viewport));
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
    public void move(Vector3 adjustment) {
        meshes.forEach(mesh -> mesh.move(adjustment));
    }
}
