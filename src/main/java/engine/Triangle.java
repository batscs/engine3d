package engine;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Triangle {
    public Vector3 v0, v1, v2;

    public Triangle(Vector3 v0, Vector3 v1, Vector3 v2) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
    }

    public static List<Triangle> makeCube(float cx, float cy, float cz, float size) {
        float s = size / 2;
        Vector3[] verts = new Vector3[] {
                new Vector3(cx - s, cy - s, cz - s),
                new Vector3(cx - s, cy + s, cz - s),
                new Vector3(cx + s, cy + s, cz - s),
                new Vector3(cx + s, cy - s, cz - s),
                new Vector3(cx - s, cy - s, cz + s),
                new Vector3(cx - s, cy + s, cz + s),
                new Vector3(cx + s, cy + s, cz + s),
                new Vector3(cx + s, cy - s, cz + s),
        };
        int[][] faces = {
                {0,1,2},{0,2,3}, {3,2,6},{3,6,7},
                {7,6,5},{7,5,4}, {4,5,1},{4,1,0},
                {1,5,6},{1,6,2}, {4,0,3},{4,3,7}
        };
        List<Triangle> tris = new ArrayList<>();
        for (int[] f : faces) {
            tris.add(new Triangle(verts[f[0]], verts[f[1]], verts[f[2]]));
        }
        return tris;
    }

    public boolean isBackFacing(Camera camera) {
        return angle(camera.position) >= 0;
    }

    public float angle(Vector3 direction) {
        Vector3 normal = calculateNormal();
        Vector3 camDirection = v0.sub(direction).normalize();

        return normal.dot(camDirection);
    }

    public Vector3 center() {
        return v0.add(v1).add(v2).div(3);
    }

    public Vector3 calculateNormal() {
        Vector3 edge1 = v1.sub(v0);
        Vector3 edge2 = v2.sub(v0);
        return edge1.cross(edge2).normalize();
    }

    public Polygon getPolygon(Matrix4 viewport) {
        Vector3 p0 = viewport.transform(v0);
        Vector3 p1 = viewport.transform(v1);
        Vector3 p2 = viewport.transform(v2);

        // Convert from NDC (-1 to 1) to screen coordinates
        int x0 = (int) ((p0.x + 1) * 0.5f * Renderer.width);  // Assuming Renderer.WIDTH exists
        int y0 = (int) ((1 - p0.y) * 0.5f * Renderer.height);
        int x1 = (int) ((p1.x + 1) * 0.5f * Renderer.width);
        int y1 = (int) ((1 - p1.y) * 0.5f * Renderer.height);
        int x2 = (int) ((p2.x + 1) * 0.5f * Renderer.width);
        int y2 = (int) ((1 - p2.y) * 0.5f * Renderer.height);

        Polygon poly = new Polygon();
        poly.addPoint(x0, y0);
        poly.addPoint(x1, y1);
        poly.addPoint(x2, y2);

        return poly;
    }

}
