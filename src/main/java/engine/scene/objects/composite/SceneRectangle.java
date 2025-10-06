package engine.scene.objects.composite;

import engine.scene.objects.SceneObject;
import engine.scene.objects.mesh.SceneTriangle;
import math.Triangle;
import java.util.List;

/**
 * A rectangular prism (box) SceneObject built from 12 triangles.
 * Similar to SceneCube but allows independent width, height, and depth.
 */
public class SceneRectangle extends Composite implements SceneObject {

    public SceneRectangle(float x, float y, float z, float width, float height, float depth) {
        super(makeRectangle(x, y, z, width, height, depth));
    }

    private static List<SceneTriangle> makeRectangle(float x, float y, float z, float w, float h, float d) {
        return Triangle.makeBox(x, y, z, w, h, d)
                .stream()
                .map(SceneTriangle::new)
                .toList();
    }
}
