package engine;

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
}
