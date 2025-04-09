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

    private final Triangle tri;

    @Setter
    private boolean allowBackFacing = false;

    public SceneTriangle(Triangle tri) {
        this.tri = tri;
    }

    @Override
    public void draw(Viewport viewport) {
        if (!allowBackFacing && !Settings.allowBackFacing && tri.isBackFacing(viewport.camera)) {
            return;
        }

        Color color = viewport.calculateLighting(tri);
        viewport.g2d.setColor(color);

        Polygon poly = tri.getPolygon(viewport.perspective);
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

}
