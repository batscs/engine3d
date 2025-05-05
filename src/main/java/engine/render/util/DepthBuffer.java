// engine/render/util/DepthBuffer.java
package engine.render.util;

import engine.render.Viewport;
import engine.scene.objects.Renderable;
import engine.scene.objects.mesh.SceneTriangle;
import math.Matrix4;
import math.Triangle;
import math.Vector3;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.*;

public class DepthBuffer {
    /**
     * Remove any SceneTriangle whose screen‐space polygon
     * is completely covered by nearer ones.
     * Non‐triangles are always passed through.
     */
    public static List<Renderable> cull(List<Renderable> sorted, Viewport vp) {
        int W = vp.getWidth(), H = vp.getHeight();
        // 1) initialize depth buffer
        float[][] depth = new float[W][H];
        for (int x = 0; x < W; x++)
            Arrays.fill(depth[x], Float.POSITIVE_INFINITY);

        List<Renderable> keep = new ArrayList<>();
        // 2) far→near
        for (int i = sorted.size() - 1; i >= 0; i--) {
            Renderable r = sorted.get(i);
            if (!(r instanceof SceneTriangle)) {
                keep.add(0, r);
                continue;
            }
            SceneTriangle t = (SceneTriangle) r;
            ScreenTri s = new ScreenTri(t, vp);

            // 3) bounding‐box raster
            float minXf = min(s.x0, min(s.x1, s.x2));
            float maxXf = max(s.x0, max(s.x1, s.x2));
            float minYf = min(s.y0, min(s.y1, s.y2));
            float maxYf = max(s.y0, max(s.y1, s.y2));

            int minX = max(0, (int) floor(minXf));
            int maxX = min(W - 1, (int) ceil(maxXf));
            int minY = max(0, (int) floor(minYf));
            int maxY = min(H - 1, (int) ceil(maxYf));

            boolean anyVisible = false;
            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    if (!insideTriangle(x,y,s)) continue;
                    float z = interpolateDepth(x, y, s);
                    if (z < depth[x][y]) {
                        anyVisible = true;
                        depth[x][y] = z;
                    }
                }
            }

            if (anyVisible) keep.add(0, t);
        }
        return keep;
    }

    // Helper to hold one triangle in screen‐space (x,y in pixels, z in NDC-depth)
    private static class ScreenTri {
        final float x0,y0,z0,
                x1,y1,z1,
                x2,y2,z2;
        final float area;    // signed area*2

        ScreenTri(SceneTriangle t, Viewport vp) {
            Matrix4 P = vp.getPerspective();
            // project each vertex to NDC
            Vector3 p0 = P.transform(t.getTri().v0);
            Vector3 p1 = P.transform(t.getTri().v1);
            Vector3 p2 = P.transform(t.getTri().v2);

            // to screen‐space
            x0 = (p0.x + 1f) * 0.5f * vp.getWidth();
            y0 = (1f - p0.y) * 0.5f * vp.getHeight();
            z0 = p0.z;

            x1 = (p1.x + 1f) * 0.5f * vp.getWidth();
            y1 = (1f - p1.y) * 0.5f * vp.getHeight();
            z1 = p1.z;

            x2 = (p2.x + 1f) * 0.5f * vp.getWidth();
            y2 = (1f - p2.y) * 0.5f * vp.getHeight();
            z2 = p2.z;

            // precompute the “double‐area” (signed) for barycentrics
            area = edge(x0,y0, x1,y1, x2,y2);
        }
    }

    // 2× area of triangle ABC (signed).
    private static float edge(float ax, float ay, float bx, float by, float cx, float cy) {
        return (bx - ax)*(cy - ay) - (by - ay)*(cx - ax);
    }

    /** True if (px,py) is inside the 2D tri in ScreenTri s. */
    private static boolean insideTriangle(int px, int py, ScreenTri s) {
        float w0 = edge(s.x1, s.y1, s.x2, s.y2, px, py);
        float w1 = edge(s.x2, s.y2, s.x0, s.y0, px, py);
        float w2 = edge(s.x0, s.y0, s.x1, s.y1, px, py);

        // same sign as s.area means point is on the same side of all edges
        if (s.area > 0)
            return w0 >= 0 && w1 >= 0 && w2 >= 0;
        else
            return w0 <= 0 && w1 <= 0 && w2 <= 0;
    }

    /** Linearly interpolate Z at (px,py) via barycentric weights. */
    private static float interpolateDepth(int px, int py, ScreenTri s) {
        float w0 = edge(s.x1, s.y1, s.x2, s.y2, px, py);
        float w1 = edge(s.x2, s.y2, s.x0, s.y0, px, py);
        float w2 = edge(s.x0, s.y0, s.x1, s.y1, px, py);
        float sum = w0 + w1 + w2;
        // guard against degenerate (sum==0), but that shouldn’t happen if insideTriangle passed
        return (w0*s.z0 + w1*s.z1 + w2*s.z2) / sum;
    }

}
