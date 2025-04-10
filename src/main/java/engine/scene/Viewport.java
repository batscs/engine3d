package engine.scene;

import math.Matrix4;
import engine.scene.objects.light.SceneLight;

import java.awt.*;
import java.util.List;

public class Viewport {

    public final Graphics2D g2d;
    public final Matrix4 perspective;
    public final Camera camera;
    public final List<SceneLight> lights;

    public final int width;

    public final int height;

    public Viewport(Graphics2D g2d, Matrix4 perspective, Camera camera, List<SceneLight> lights, int width, int height) {
        this.g2d = g2d;
        this.perspective = perspective;
        this.camera = camera;
        this.lights = lights;
        this.width = width;
        this.height = height;
    }
}
