package engine.scene.objects.mesh;

import engine.scene.objects.SceneObject;
import engine.render.Viewport;
import math.Vector3;
import java.awt.Color;

public class SceneLine implements SceneObject {
    private Vector3 start;
    private Vector3 end;
    private Color color;

    public SceneLine(Vector3 start, Vector3 end) {
        this.start = start;
        this.end = end;
        this.color = Color.BLACK;
    }

    @Override
    public void draw(Viewport viewport) {
        // Transform both points to screen space
        Vector3 screenStart = viewport.perspective.transform(start);
        Vector3 screenEnd = viewport.perspective.transform(end);

        // Convert from NDC to viewport coordinates
        int viewportWidth = viewport.width;
        int viewportHeight = viewport.height;

        int x1 = (int) ((screenStart.x + 1) * viewportWidth / 2);
        int y1 = (int) ((1 - screenStart.y) * viewportHeight / 2);
        int x2 = (int) ((screenEnd.x + 1) * viewportWidth / 2);
        int y2 = (int) ((1 - screenEnd.y) * viewportHeight / 2);

        // Set the color and draw the line
        viewport.g2d.setColor(color);
        viewport.g2d.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void tick() {
        // No animation by default
    }

    @Override
    public Vector3 getPosition() {
        // Return midpoint as the position
        return start.add(end).div(2);
    }

    @Override
    public void move(Vector3 adjustment) {
        start = start.add(adjustment);
        end = end.add(adjustment);
    }
}
