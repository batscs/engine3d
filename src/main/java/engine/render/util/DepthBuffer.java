// engine/render/util/DepthBuffer.java
package engine.render.util;

import engine.render.Viewport;
import engine.scene.objects.Renderable;
import engine.scene.objects.mesh.SceneTriangle;
import math.Matrix4;
import math.Vector3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.*;

public class DepthBuffer {

    public static List<Renderable> cull(List<Renderable> sorted, Viewport vp) {
        final int W = vp.getWidth(), H = vp.getHeight();
        float[] depth = new float[W * H];
        Arrays.fill(depth, Float.POSITIVE_INFINITY);

        List<Renderable> keep = new ArrayList<>();
        for (int i = sorted.size() - 1; i >= 0; i--) {
            Renderable r = sorted.get(i);
            if (!(r instanceof SceneTriangle)) {
                keep.add(0, r);
                continue;
            }

            ScreenTri s = new ScreenTri((SceneTriangle) r, vp);
            if (s.culled) {
                continue; // Frustum culled
            }

            if (abs(s.area) < 1e-6f) {
                keep.add(0, (SceneTriangle) r);
                continue;
            }

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
                    float cx = x + 0.5f, cy = y + 0.5f;
                    // Barycentric using precomputed edges
                    float w0 = s.edge0A * cx + s.edge0B * cy + s.edge0C;
                    float w1 = s.edge1A * cx + s.edge1B * cy + s.edge1C;
                    float w2 = s.edge2A * cx + s.edge2B * cy + s.edge2C;

                    if ((s.area > 0 && (w0 < 0 || w1 < 0 || w2 < 0)) ||
                            (s.area < 0 && (w0 > 0 || w1 > 0 || w2 > 0))) {
                        continue;
                    }

                    float z = (w0 * s.z0 + w1 * s.z1 + w2 * s.z2) * s.invArea;
                    int idx = x + y * W;
                    if (z <= depth[idx]) {
                        anyVisible = true;
                        depth[idx] = z;
                    }
                }
            }

            if (anyVisible) {
                keep.add(0, (SceneTriangle) r);
            }
        }
        return keep;
    }

    private static class ScreenTri {
        final float x0, y0, z0, x1, y1, z1, x2, y2, z2;
        final float area, invArea;
        final float edge0A, edge0B, edge0C, edge1A, edge1B, edge1C, edge2A, edge2B, edge2C;
        final boolean culled;

        ScreenTri(SceneTriangle tri, Viewport vp) {
            Matrix4 P = vp.getPerspective();
            Vector3 p0 = P.transform(tri.getTri().v0);
            Vector3 p1 = P.transform(tri.getTri().v1);
            Vector3 p2 = P.transform(tri.getTri().v2);

            // Frustum culling in clip space
            boolean outsideLeft = p0.x < -p0.z && p1.x < -p1.z && p2.x < -p2.z;
            boolean outsideRight = p0.x > p0.z && p1.x > p1.z && p2.x > p2.z;
            boolean outsideBottom = p0.y < -p0.z && p1.y < -p1.z && p2.y < -p2.z;
            boolean outsideTop = p0.y > p0.z && p1.y > p1.z && p2.y > p2.z;
            boolean outsideNear = p0.z < -1 && p1.z < -1 && p2.z < -1;
            boolean outsideFar = p0.z > 1 && p1.z > 1 && p2.z > 1;
            culled = outsideLeft || outsideRight || outsideBottom || outsideTop || outsideNear || outsideFar;

            if (culled) {
                x0 = x1 = x2 = y0 = y1 = y2 = z0 = z1 = z2 = 0;
                area = invArea = 0;
                edge0A = edge0B = edge0C = edge1A = edge1B = edge1C = edge2A = edge2B = edge2C = 0;
                return;
            }

            x0 = (p0.x + 1f) * 0.5f * vp.getWidth();
            y0 = (1f - p0.y) * 0.5f * vp.getHeight();
            z0 = p0.z;

            x1 = (p1.x + 1f) * 0.5f * vp.getWidth();
            y1 = (1f - p1.y) * 0.5f * vp.getHeight();
            z1 = p1.z;

            x2 = (p2.x + 1f) * 0.5f * vp.getWidth();
            y2 = (1f - p2.y) * 0.5f * vp.getHeight();
            z2 = p2.z;

            // Precompute edge equations: ax + by + c = 0
            edge0A = -(y2 - y1);
            edge0B = x2 - x1;
            edge0C = (y2 - y1) * x1 - (x2 - x1) * y1;

            edge1A = -(y0 - y2);
            edge1B = x0 - x2;
            edge1C = (y0 - y2) * x2 - (x0 - x2) * y2;

            edge2A = -(y1 - y0);
            edge2B = x1 - x0;
            edge2C = (y1 - y0) * x0 - (x1 - x0) * y0;

            area = (x1 - x0) * (y2 - y0) - (y1 - y0) * (x2 - x0);
            invArea = 1.0f / area;
        }
    }
}