package engine.scene.objects.composite;

import engine.render.Viewport;
import engine.scene.objects.SceneObject;
import engine.scene.objects.mesh.ScenePlane;
import engine.scene.objects.mesh.SceneTriangle;
import math.Triangle;
import math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class ScenePolygon extends Composite {

    public ScenePolygon(List<Vector3> vertices) {
        super(triangulate(vertices));
    }

    private static List<SceneTriangle> triangulate(List<Vector3> faceVertices) {
        List<SceneTriangle> triangles = new ArrayList<>();
        if (faceVertices.size() < 3) return triangles;

        Vector3 v0 = faceVertices.get(0);
        for (int i = 1; i < faceVertices.size() - 1; i++) {
            Vector3 v1 = faceVertices.get(i);
            Vector3 v2 = faceVertices.get(i + 1);
            triangles.add(new SceneTriangle(new Triangle(v0, v1, v2)));
        }

        return triangles;
    }
}
