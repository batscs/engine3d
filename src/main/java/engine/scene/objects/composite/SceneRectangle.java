package engine.scene.objects.composite;

import engine.scene.objects.SceneObject;
import engine.scene.objects.mesh.SceneTriangle;
import math.Triangle;

import java.awt.Color;
import java.util.List;

/**
 * A rectangular prism (box) SceneObject built from 12 triangles.
 * Similar to SceneCube but allows independent width, height, and depth.
 */
public class SceneRectangle extends Composite implements SceneObject {

    public SceneRectangle(float x, float y, float z, float width, float height, float depth) {
        this(x, y, z, width, height, depth, 2);
    }

    // new constructor – lets you specify how fine the mesh is
    public SceneRectangle(float x, float y, float z,
                          float width, float height, float depth,
                          int segmentsPerEdge) {
        super(makeRectangle(x, y, z, width, height, depth, null, segmentsPerEdge));
    }

    // old helper kept for compatibility – delegates to the new one
    private static List<SceneTriangle> makeRectangle(float x, float y, float z,
                                                     float w, float h, float d,
                                                     Color baseColor) {
        return makeRectangle(x, y, z, w, h, d, baseColor, 1); // 1 = no subdivision
    }

    // new helper with subdivision parameter
    private static List<SceneTriangle> makeRectangle(float x, float y, float z,
                                                     float w, float h, float d,
                                                     Color baseColor,
                                                     int segmentsPerEdge) {
        List<SceneTriangle> result = Triangle
                .makeBox(x, y, z, w, h, d, segmentsPerEdge)
                .stream()
                .map(SceneTriangle::new)
                .toList();

        if (baseColor != null) {
            result.forEach(tri -> tri.setBaseColor(baseColor));
        }

        return result;
    }
}
