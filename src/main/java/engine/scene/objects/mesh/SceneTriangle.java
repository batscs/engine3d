package engine.scene.objects.mesh;

import engine.Settings;
import engine.scene.objects.light.SceneLight;
import engine.scene.objects.SceneObject;
import lombok.Setter;
import math.Triangle;
import math.Vector3;
import engine.scene.Viewport;

import java.awt.*;

public class SceneTriangle implements SceneObject {

    private Triangle tri;

    @Setter
    private boolean allowBackFacing = false;

    public SceneTriangle(Triangle tri) {
        this.tri = tri;
    }

    @Override
    public void draw(Viewport viewport) {
        if (!allowBackFacing
                && !Settings.allowBackFacing
                && tri.isBackFacing(viewport.camera)) return;

        Polygon poly = tri.getPolygon(viewport.perspective);

        float r = 0, g = 0, b = 0;

        for (SceneLight light : viewport.lights) {
            Vector3 lightPos = light.getPosition();
            float angle = tri.angle(lightPos);
            float distance = tri.center().sub(lightPos).length();
            float attenuation = 1.0f / (1 + 0.2f * distance + 0.05f * distance * distance);
            float contribution = Math.max(0, -angle) * attenuation * light.getIntensity();

            // Add light's RGB contribution scaled by brightness
            r += light.getColor().getRed() / 255f * contribution;
            g += light.getColor().getGreen() / 255f * contribution;
            b += light.getColor().getBlue() / 255f * contribution;
        }

        // Clamp final color
        r = Math.min(1, r);
        g = Math.min(1, g);
        b = Math.min(1, b);
        viewport.g2d.setColor(new Color(r, g, b));

        if (Settings.drawWireframes) {
            viewport.g2d.drawPolygon(poly);
        } else {
            viewport.g2d.fillPolygon(poly);
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

}
