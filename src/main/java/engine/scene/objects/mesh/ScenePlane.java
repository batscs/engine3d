package engine.scene.objects.mesh;

import engine.render.Viewport;
import engine.scene.objects.SceneObject;
import math.Triangle;
import math.Vector3;

public class ScenePlane implements SceneObject {

    private SceneTriangle triangle1, triangle2;

    public ScenePlane(int x, int y, int z, int width, int height, int length) {
        Vector3 v1 = new Vector3(x, y, z);
        Vector3 v2 = new Vector3(x + width, y, z);
        Vector3 v3 = new Vector3(x + width, y + height, z + length);
        Vector3 v4 = new Vector3(x, y + height, z + length);

        // Create two triangles that form the plane
        this.triangle1 = new SceneTriangle(new Triangle(v1, v2, v3));
        this.triangle2 = new SceneTriangle(new Triangle(v1, v3, v4));

        triangle1.setAllowBackFacing(true);
        triangle2.setAllowBackFacing(true);
    }

    @Override
    public void draw(Viewport viewport) {
        triangle1.draw(viewport);
        triangle2.draw(viewport);
    }

    @Override
    public void tick() {
        triangle1.tick();
        triangle2.tick();
    }

    @Override
    public Vector3 getPosition() {
        return triangle1.getPosition().cross(triangle2.getPosition());
    }
}
