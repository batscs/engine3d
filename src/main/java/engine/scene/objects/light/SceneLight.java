package engine.scene.objects.light;

import engine.scene.objects.Renderable;
import engine.scene.objects.SceneObject;
import lombok.Getter;
import math.Vector3;
import engine.render.Viewport;

import java.awt.*;
import java.util.List;

public class SceneLight implements SceneObject, Renderable {

    @Getter
    Color color;
    @Getter
    float intensity;
    @Getter
    private Vector3 position;
    private Vector3 rotation;

    public SceneLight(Vector3 position, Color color, float intensity) {
        this.position = position;
        this.color = color;
        this.intensity = intensity;
        this.rotation = new Vector3(0,0,0);
    }

    @Override
    public void draw(Viewport viewport) {
        Vector3 lightProj = viewport.getPerspective().transform(position);
        int x = (int) ((lightProj.x + 1) * 0.5f * viewport.getWidth());
        int y = (int) ((1 - (lightProj.y + 1) * 0.5f) * viewport.getHeight());

        Vector3 lightView = viewport.getCamera().getViewMatrix().transform(position);
        float distance = lightView.length();
        int size = Math.max(2, Math.min((int) (100f / distance), 20));

        // Use light's color for visualization
        Color translucent = new Color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 0.5f);
        viewport.getG2d().setColor(translucent);
        viewport.getG2d().fillOval(x - size / 2, y - size / 2, size, size);
        viewport.getG2d().setColor(Color.BLACK);
        viewport.getG2d().drawOval(x - size / 2, y - size / 2, size, size);
    }

    @Override
    public void tick() {
        //Vector3 adjustment = Utility.randomVector3(-0.05f, 0.05f);
        //position = position.add(adjustment);
    }

    @Override
    public List<Renderable> getRenderables() {
        return List.of(this);
    }

    @Override
    public void move(Vector3 adjustment) {
        position = position.add(adjustment);
    }

    @Override
    public void setPosition(Vector3 pos) {
        position = pos;
    }

    @Override
    public Vector3 getRotation() {
        return rotation;
    }

    @Override
    public void setRotation(Vector3 rotation) {

    }

    @Override
    public void rotateAround(Vector3 pivot, Vector3 deltaRotation) {

    }
}
