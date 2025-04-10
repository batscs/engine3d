package engine.render;

import engine.scene.Scene;
import engine.scene.objects.SceneObject;
import lombok.Getter;
import math.Matrix4;
import engine.scene.objects.light.SceneLight;

import java.awt.*;
import java.util.List;

@Getter
public class Viewport {

    private final Graphics2D g2d;
    private final Camera camera;

    private final Scene scene;

    public Viewport(Graphics2D g2d, Camera camera, Scene scene) {
        this.g2d = g2d;
        this.camera = camera;
        this.scene = scene;
    }

    public Matrix4 getPerspective() {
        return camera.getPerspectiveMatrix();
    }

    public int getWidth() {
        return camera.width;
    }

    public int getHeight() {
        return camera.height;
    }
}
