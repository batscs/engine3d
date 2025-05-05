// engine/render/util/DepthBuffer.java
package engine.render.util;

import engine.render.Viewport;
import engine.scene.objects.Renderable;
import engine.scene.objects.mesh.SceneTriangle;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

public class DepthBuffer {
    /**
     * Remove any SceneTriangle whose screen‐space polygon
     * is completely covered by nearer ones.
     * Non‐triangles are always passed through.
     */
    public static List<Renderable> cull(List<Renderable> sortedByDistance, Viewport viewport) {
        List<Renderable> keep = new ArrayList<>();
        Area occlusion = new Area();

        // Process from furthest to nearest
        for (int i = sortedByDistance.size() - 1; i >= 0; i--) {
            Renderable r = sortedByDistance.get(i);

            if (r instanceof SceneTriangle) {
                SceneTriangle tri = (SceneTriangle) r;
                Polygon poly = tri.getPolygon(viewport);
                if (poly == null) {
                    // off-screen entirely → skip
                    continue;
                }
                Area triArea = new Area(poly);
                Area leftover = new Area(triArea);
                leftover.subtract(occlusion);

                if (!leftover.isEmpty()) {
                    // visible (partially or fully)
                    keep.add(0, tri); // maintain original order
                }

                // Whether kept or not, this triangle now occludes others
                occlusion.add(triArea);
            } else {
                // Always keep non-triangle renderables
                keep.add(0, r); // maintain original order
            }
        }

        return keep;
    }

}
