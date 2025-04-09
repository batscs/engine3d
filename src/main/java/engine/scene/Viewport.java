package engine.scene;

import math.Matrix4;
import engine.scene.objects.light.SceneLight;
import math.Triangle;
import math.Vector3;

import java.awt.*;
import java.util.Collection;
import java.util.List;

public class Viewport {

    public final Graphics2D g2d;
    public final Matrix4 perspective;
    public final Camera camera;
    public final List<SceneLight> lights;

    public Viewport(Graphics2D g2d, Matrix4 perspective, Camera camera, List<SceneLight> lights) {
        this.g2d = g2d;
        this.perspective = perspective;
        this.camera = camera;
        this.lights = lights;
    }

    public Color calculateLighting(Triangle tri) {
        float r = 0, g = 0, b = 0;

        for (SceneLight light : lights) {
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

        r = Math.min(1, r);
        g = Math.min(1, g);
        b = Math.min(1, b);

        return new Color(r, g, b);
    }
}
