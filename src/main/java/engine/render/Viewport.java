package engine.render;

import engine.scene.Scene;
import lombok.Getter;
import math.Matrix4;
import math.Triangle;
import math.Vector3;

import java.awt.*;

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

    public boolean isBackFacing(Triangle tri) {
        return tri.angle(camera.position) >= 0;
    }
}
