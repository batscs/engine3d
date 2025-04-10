package engine.scene.objects.light;

import engine.render.Renderer;
import engine.scene.objects.SceneObject;
import lombok.Getter;
import math.Vector3;
import engine.render.Viewport;

import java.awt.*;

public class SceneLight implements SceneObject {

    @Getter
    Color color;
    @Getter
    float intensity;
    @Getter
    private Vector3 position;

    public SceneLight(Vector3 position, Color color, float intensity) {
        this.position = position;
        this.color = color;
        this.intensity = intensity;
    }

    @Override
    public void draw(Viewport viewport) {
        Vector3 lightProj = viewport.perspective.transform(position);
        int x = (int) ((lightProj.x + 1) * 0.5f * viewport.width);
        int y = (int) ((1 - (lightProj.y + 1) * 0.5f) * viewport.height);

        Vector3 lightView = viewport.camera.getViewMatrix().transform(position);
        float distance = lightView.length();
        int size = Math.max(2, Math.min((int) (100f / distance), 20));

        // Use light's color for visualization
        Color translucent = new Color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 0.5f);
        viewport.g2d.setColor(translucent);
        viewport.g2d.fillOval(x - size / 2, y - size / 2, size, size);
        viewport.g2d.setColor(Color.BLACK);
        viewport.g2d.drawOval(x - size / 2, y - size / 2, size, size);
    }

    @Override
    public void tick() {
        //Vector3 adjustment = Utility.randomVector3(-0.05f, 0.05f);
        //position = position.add(adjustment);
    }
}
