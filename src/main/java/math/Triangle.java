package math;

import java.util.ArrayList;
import java.util.Arrays;
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

    public static List<Triangle> makeBox(float x, float y, float z, float w, float h, float d) {
        return makeBox(x, y, z, w, h, d, 1);
    }

    public static List<Triangle> makeBox(float x, float y, float z,
                                         float w, float h, float d,
                                         int segmentsPerEdge) {

        float hw = w / 2f, hh = h / 2f, hd = d / 2f;

        Vector3[] verts = new Vector3[] {
                new Vector3(x - hw, y - hh, z - hd), // 0: bottom-left-back
                new Vector3(x - hw, y + hh, z - hd), // 1: top-left-back
                new Vector3(x + hw, y + hh, z - hd), // 2: top-right-back
                new Vector3(x + hw, y - hh, z - hd), // 3: bottom-right-back
                new Vector3(x - hw, y - hh, z + hd), // 4: bottom-left-front
                new Vector3(x - hw, y + hh, z + hd), // 5: top-left-front
                new Vector3(x + hw, y + hh, z + hd), // 6: top-right-front
                new Vector3(x + hw, y - hh, z + hd), // 7: bottom-right-front
        };

        List<Triangle> tris = new ArrayList<>();

        int s = segmentsPerEdge;

        // Each face is defined by 4 corners in the same order as your original faces
        // (v00, v01, v11, v10) such that original tris were (v00, v01, v11) and (v00, v11, v10).

        // front face (facing negative Z) – indices {0,1,2},{0,2,3}
        addSubdividedFace(tris, verts[0], verts[1], verts[2], verts[3], s, s);

        // right face (facing positive X) – {3,2,6},{3,6,7}
        addSubdividedFace(tris, verts[3], verts[2], verts[6], verts[7], s, s);

        // back face (facing positive Z) – {7,6,5},{7,5,4}
        addSubdividedFace(tris, verts[7], verts[6], verts[5], verts[4], s, s);

        // left face (facing negative X) – {4,5,1},{4,1,0}
        addSubdividedFace(tris, verts[4], verts[5], verts[1], verts[0], s, s);

        // top face (facing positive Y) – {1,5,6},{1,6,2}
        addSubdividedFace(tris, verts[1], verts[5], verts[6], verts[2], s, s);

        // bottom face (facing negative Y) – {4,0,3},{4,3,7}
        addSubdividedFace(tris, verts[4], verts[0], verts[3], verts[7], s, s);

        return tris;
    }

    private static void addSubdividedFace(List<Triangle> tris,
                                          Vector3 v00, Vector3 v01,
                                          Vector3 v11, Vector3 v10,
                                          int segU, int segV) {
        // v00 --- v10
        //  |       |
        // v01 --- v11
        //
        // segU segments from v00->v10, segV from v00->v01

        for (int i = 0; i < segU; i++) {
            float u0 = i / (float) segU;
            float u1 = (i + 1) / (float) segU;

            for (int j = 0; j < segV; j++) {
                float v0 = j / (float) segV;
                float v1 = (j + 1) / (float) segV;

                Vector3 p00 = bilerp(v00, v10, v01, v11, u0, v0);
                Vector3 p10 = bilerp(v00, v10, v01, v11, u1, v0);
                Vector3 p11 = bilerp(v00, v10, v01, v11, u1, v1);
                Vector3 p01 = bilerp(v00, v10, v01, v11, u0, v1);

                // gleiche Winding-Order wie im Original:
                tris.add(new Triangle(p00, p01, p11));
                tris.add(new Triangle(p00, p11, p10));
            }
        }
    }

    private static Vector3 bilerp(Vector3 v00, Vector3 v10,
                                  Vector3 v01, Vector3 v11,
                                  float u, float v) {
        Vector3 a = lerp(v00, v10, u);
        Vector3 b = lerp(v01, v11, u);
        return lerp(a, b, v);
    }

    private static Vector3 lerp(Vector3 a, Vector3 b, float t) {
        float x = a.x + (b.x - a.x) * t;
        float y = a.y + (b.y - a.y) * t;
        float z = a.z + (b.z - a.z) * t;
        return new Vector3(x, y, z);
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
