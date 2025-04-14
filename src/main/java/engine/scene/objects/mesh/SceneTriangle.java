package engine.scene.objects.mesh;

import engine.Settings;
import engine.scene.objects.light.SceneLight;
import engine.scene.objects.SceneObject;
import lombok.Setter;
import math.Matrix4;
import math.Triangle;
import math.Vector3;
import engine.render.Viewport;

import java.awt.*;

public class SceneTriangle implements SceneObject {

    private Triangle tri;

    @Setter
    private boolean allowBackFacing = false;

    private final Color baseColor;

    public SceneTriangle(Triangle tri) {
        this.baseColor = new Color(200, 200, 200);
        this.tri = tri;
    }

    @Override
    public void draw(Viewport viewport) {
        if (!allowBackFacing
                && !Settings.allowBackFacing
                && tri.isBackFacing(viewport.getCamera().position)) return;

        Polygon poly = getPolygon(viewport);

        Color finalColor = computeLitColor(viewport);
        viewport.getG2d().setColor(finalColor);

        if (Settings.drawWireframes) {
            viewport.getG2d().drawPolygon(poly);
        } else {
            viewport.getG2d().fillPolygon(poly);
        }
    }


    @Override
    public void tick() {

    }

    @Override
    public Vector3 getPosition() {
        return tri.center();
    }

    @Override
    public void move(Vector3 adjustment) {
        tri = tri.add(adjustment);
    }

    private Color computeLitColor(Viewport viewport) {
        float r = 0, g = 0, b = 0;

        for (SceneLight light : viewport.getScene().getLights()) {
            Vector3 lightPos = light.getPosition();
            float angle = tri.angle(lightPos);
            float distance = tri.center().sub(lightPos).length();
            float attenuation = 1.0f / (1 + 0.2f * distance + 0.05f * distance * distance);
            float contribution = Math.max(0, -angle) * attenuation * light.getIntensity();

            r += (baseColor.getRed() / 255f) * (light.getColor().getRed() / 255f) * contribution;
            g += (baseColor.getGreen() / 255f) * (light.getColor().getGreen() / 255f) * contribution;
            b += (baseColor.getBlue() / 255f) * (light.getColor().getBlue() / 255f) * contribution;
        }

        // Clamp
        r = Math.min(1, r);
        g = Math.min(1, g);
        b = Math.min(1, b);

        return new Color(r, g, b);
    }

    private Polygon getPolygon(Viewport viewport) {
        Matrix4 perspective = viewport.getPerspective();
        Vector3 p0 = perspective.transform(tri.v0);
        Vector3 p1 = perspective.transform(tri.v1);
        Vector3 p2 = perspective.transform(tri.v2);

        // Convert from NDC (-1 to 1) to screen coordinates
        int x0 = (int) ((p0.x + 1) * 0.5f * viewport.getWidth());  // Assuming Renderer.WIDTH exists
        int y0 = (int) ((1 - p0.y) * 0.5f * viewport.getHeight());
        int x1 = (int) ((p1.x + 1) * 0.5f * viewport.getWidth());
        int y1 = (int) ((1 - p1.y) * 0.5f * viewport.getHeight());
        int x2 = (int) ((p2.x + 1) * 0.5f * viewport.getWidth());
        int y2 = (int) ((1 - p2.y) * 0.5f * viewport.getHeight());

        Polygon poly = new Polygon();
        poly.addPoint(x0, y0);
        poly.addPoint(x1, y1);
        poly.addPoint(x2, y2);

        return poly;
    }


}
