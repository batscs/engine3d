package math;

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

    public static List<Triangle> makeArrow(float x, float y, float z, float bodySize) {
        List<Triangle> triangles = new ArrayList<>();
        float hs = bodySize * 0.3f;  // Narrower base for the tip
        float tipLength = bodySize;  // Shorter tip length

        // Pyramid tip vertices
        Vector3 tipFront = new Vector3(x, y, z + tipLength);
        Vector3 baseFR = new Vector3(x + hs, y + hs, z);
        Vector3 baseFL = new Vector3(x - hs, y + hs, z);
        Vector3 baseBR = new Vector3(x + hs, y - hs, z);
        Vector3 baseBL = new Vector3(x - hs, y - hs, z);

        // Front-facing triangles (arrow tip)
        triangles.add(new Triangle(baseFR, baseFL, tipFront));
        triangles.add(new Triangle(baseFL, baseBL, tipFront));
        triangles.add(new Triangle(baseBL, baseBR, tipFront));
        triangles.add(new Triangle(baseBR, baseFR, tipFront));

        // Back-facing triangles (base quad - ensures visibility from behind)
        triangles.add(new Triangle(baseFL, baseFR, baseBL));  // First half of quad
        triangles.add(new Triangle(baseFR, baseBR, baseBL));  // Second half of quad

        return triangles;
    }

    public boolean isBackFacing(Vector3 pov) {
        return angle(pov) >= 0;
    }

    public Triangle add(Vector3 adjustment) {
        return new Triangle(
            v0.add(adjustment),
            v1.add(adjustment),
            v2.add(adjustment)
        );
    }

    public float angle(Vector3 direction) {
        Vector3 normal = normal();
        Vector3 camDirection = v0.sub(direction).normalize();

        return normal.dot(camDirection);
    }

    public Vector3 center() {
        return v0.add(v1).add(v2).div(3);
    }

    public Vector3 normal() {
        Vector3 edge1 = v1.sub(v0);
        Vector3 edge2 = v2.sub(v0);
        return edge1.cross(edge2).normalize();
    }

    public Triangle copy() {
        return new Triangle(v0.copy(), v1.copy(), v2.copy());
    }
}
