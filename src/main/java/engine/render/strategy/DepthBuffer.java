// engine/render/util/DepthBuffer.java
package engine.render.strategy;

import engine.Settings;
import engine.render.Viewport;
import engine.scene.Scene;
import engine.scene.objects.Renderable;
import engine.scene.objects.light.SceneLight;
import engine.scene.objects.mesh.SceneTriangle;
import math.Matrix4;
import math.Vector3;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.*;

public class DepthBuffer implements RenderStrategy {

    @Override
    public void render(Scene scene, Viewport vp, BufferedImage frame) {
        final int W = frame.getWidth();
        final int H = frame.getHeight();
        List<Renderable> renderables = scene.getAllRenderable(vp);
        Settings.enginePolygons = renderables.size();

        float[] depth = new float[W * H];
        Arrays.fill(depth, Float.POSITIVE_INFINITY);

        // Hintergrund löschen
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                frame.setRGB(x, y, 0xFFFFFFFF); // weiß
            }
        }

        int visibleTris = 0;
        var camera = vp.getCamera();   // <--- NEU

        // --- 1) Triangles ---
        for (Renderable r : renderables) {
            if (!(r instanceof SceneTriangle tri)) {
                continue; // Lights etc. später
            }

            // >>> NEU: gleicher Sichtbarkeits-Check wie in SceneTriangle.getPolygon()
            var t = tri.getTri();
            boolean anyInView =
                    camera.isInView(t.v0) ||
                            camera.isInView(t.v1) ||
                            camera.isInView(t.v2);

            if (!anyInView) {
                continue; // komplett hinter Kamera / außerhalb FOV
            }
            // <<< Ende NEU

            ScreenTri s = new ScreenTri(tri, vp);
            if (s.culled || Math.abs(s.area) < 1e-6f) {
                continue;
            }

            Color c = tri.computeLitColor(vp);
            int rgb = c.getRGB();

            boolean any = false;

            if (Settings.drawWireframes) {
                // --- Wireframe-Modus: nur Kanten zeichnen, mit Depth-Test ---
                any = drawWireframeTriangle(s, rgb, depth, frame);
            } else {
                // --- Füllmodus: dein bisheriges Barycentric-Raster ---
                float minXf = Math.min(s.x0, Math.min(s.x1, s.x2));
                float maxXf = Math.max(s.x0, Math.max(s.x1, s.x2));
                float minYf = Math.min(s.y0, Math.min(s.y1, s.y2));
                float maxYf = Math.max(s.y0, Math.max(s.y1, s.y2));

                int minX = Math.max(0, (int) Math.floor(minXf));
                int maxX = Math.min(W - 1, (int) Math.ceil(maxXf));
                int minY = Math.max(0, (int) Math.floor(minYf));
                int maxY = Math.min(H - 1, (int) Math.ceil(maxYf));

                if (minX > maxX || minY > maxY) {
                    continue;
                }

                for (int y = minY; y <= maxY; y++) {
                    float cy = y + 0.5f;
                    for (int x = minX; x <= maxX; x++) {
                        float cx = x + 0.5f;

                        float w0 = s.edge0A * cx + s.edge0B * cy + s.edge0C;
                        float w1 = s.edge1A * cx + s.edge1B * cy + s.edge1C;
                        float w2 = s.edge2A * cx + s.edge2B * cy + s.edge2C;

                        if ((s.area > 0 && (w0 < 0 || w1 < 0 || w2 < 0)) ||
                                (s.area < 0 && (w0 > 0 || w1 > 0 || w2 > 0))) {
                            continue;
                        }

                        float z = (w0 * s.z0 + w1 * s.z1 + w2 * s.z2) * s.invArea;
                        int idx = x + y * W;

                        if (z < depth[idx]) {
                            depth[idx] = z;
                            frame.setRGB(x, y, rgb);
                            any = true;
                        }
                    }
                }
            }

            if (any) {
                visibleTris++;
            }
        }

        // --- 2) Lights wie von uns vorher gebaut ---
        for (Renderable r : renderables) {
            if (r instanceof SceneLight light) {
                rasterizeLight(light, vp, frame, depth);
            }
        }

        engine.Settings.enginePolygons = visibleTris;
    }


    private static void rasterizeLight(SceneLight light, Viewport vp,
                                       BufferedImage frame, float[] depth) {
        final int W = frame.getWidth();
        final int H = frame.getHeight();

        // --- 1) Projektion wie in SceneLight.draw() ---
        Matrix4 P = vp.getPerspective();
        Vector3 pos = light.getPosition();

        Vector3 lightProj = P.transform(pos); // NDC (x,y,z in [-1,1] nach deiner Mathe)

        // In view-space checken, ob Licht überhaupt vor der Kamera ist
        Vector3 lightView = vp.getCamera().getViewMatrix().transform(pos);
        if (lightView.z < -0.1f) {
            return;
        }

        // Komplett außerhalb des NDC? -> ignorieren
        if (lightProj.x < -1f || lightProj.x > 1f ||
                lightProj.y < -1f || lightProj.y > 1f ||
                lightProj.z < -1f || lightProj.z > 1f) {
            return;
        }

        // NDC -> Screen (Formel wie in SceneLight.draw, aber wir clampen auf Frame)
        int sx = (int) ((lightProj.x + 1f) * 0.5f * vp.getWidth());
        int sy = (int) ((1f - (lightProj.y + 1f) * 0.5f) * vp.getHeight());
        float z = lightProj.z;

        // --- 2) Größe des "Licht-Kreises" wie bisher ---
        float distance = lightView.length();
        int size = Math.max(2, Math.min((int) (100f / distance), 20));
        int radius = size / 2;

        Color col = light.getColor();
        float alpha = 0.5f; // wie dein translucent 0.5

        for (int dy = -radius; dy <= radius; dy++) {
            int y = sy + dy;
            if (y < 0 || y >= H) continue;

            for (int dx = -radius; dx <= radius; dx++) {
                int x = sx + dx;
                if (x < 0 || x >= W) continue;

                // kreisförmige Maske
                if (dx * dx + dy * dy > radius * radius) continue;

                int idx = x + y * W;

                // Z-Test: Licht kann von Geometrie verdeckt sein
                if (z >= depth[idx]) {
                    continue;
                }

                // --- 3) Alpha-Blending über existierenden Pixel ---
                int bgRgb = frame.getRGB(x, y);
                Color bg = new Color(bgRgb);

                int rr = (int) (bg.getRed()   * (1 - alpha) + col.getRed()   * alpha);
                int gg = (int) (bg.getGreen() * (1 - alpha) + col.getGreen() * alpha);
                int bb = (int) (bg.getBlue()  * (1 - alpha) + col.getBlue()  * alpha);

                rr = Math.min(255, Math.max(0, rr));
                gg = Math.min(255, Math.max(0, gg));
                bb = Math.min(255, Math.max(0, bb));

                frame.setRGB(x, y, new Color(rr, gg, bb).getRGB());
                depth[idx] = z; // Licht ist jetzt das vorderste Fragment
            }
        }
    }

    private static boolean drawWireframeTriangle(ScreenTri s, int rgb,
                                                 float[] depth, BufferedImage frame) {
        boolean any = false;

        any |= drawDepthLine(s.x0, s.y0, s.z0,
                s.x1, s.y1, s.z1,
                rgb, depth, frame);

        any |= drawDepthLine(s.x1, s.y1, s.z1,
                s.x2, s.y2, s.z2,
                rgb, depth, frame);

        any |= drawDepthLine(s.x2, s.y2, s.z2,
                s.x0, s.y0, s.z0,
                rgb, depth, frame);

        return any;
    }

    /**
     * Einfache Linien-Rasterization mit linearer Z-Interpolation und Depth-Test.
     */
    private static boolean drawDepthLine(float x0f, float y0f, float z0,
                                         float x1f, float y1f, float z1,
                                         int rgb, float[] depth, BufferedImage frame) {
        int W = frame.getWidth();
        int H = frame.getHeight();

        int x0 = Math.round(x0f);
        int y0 = Math.round(y0f);
        int x1 = Math.round(x1f);
        int y1 = Math.round(y1f);

        int dx = x1 - x0;
        int dy = y1 - y0;

        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        if (steps == 0) {
            // einzelner Pixel
            if (x0 >= 0 && x0 < W && y0 >= 0 && y0 < H) {
                int idx = x0 + y0 * W;
                if (z0 < depth[idx]) {
                    depth[idx] = z0;
                    frame.setRGB(x0, y0, rgb);
                    return true;
                }
            }
            return false;
        }

        boolean any = false;

        for (int i = 0; i <= steps; i++) {
            float t = i / (float) steps;

            float xf = x0 + dx * t;
            float yf = y0 + dy * t;
            float z = z0 * (1f - t) + z1 * t;

            int x = Math.round(xf);
            int y = Math.round(yf);

            if (x < 0 || x >= W || y < 0 || y >= H) continue;

            int idx = x + y * W;
            if (z < depth[idx]) {
                depth[idx] = z;
                frame.setRGB(x, y, rgb);
                any = true;
            }
        }

        return any;
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