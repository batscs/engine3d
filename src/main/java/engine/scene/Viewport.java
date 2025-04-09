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

    public Viewport(Graphics2D g2d, Matrix4 perspective, Camera camera, List<SceneLight> lights) {
        this.g2d = g2d;
        this.perspective = perspective;
        this.camera = camera;
        this.lights = lights;
    }
}
