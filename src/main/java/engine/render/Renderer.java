package engine.render;

import engine.Settings;
import engine.scene.Scene;
import engine.scene.objects.SceneObject;
import lombok.Getter;
import lombok.Setter;
import math.Matrix4;
import java.awt.*;
import java.util.List;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import math.Vector3;

import javax.swing.*;

public class Renderer extends Canvas {

    @Getter
    private Camera camera;

    @Setter
    private Scene scene;

    private static int width, height;
    // Provide the current frame for drawing.
    @Getter
    private BufferedImage frame;

    @Getter
    private JFrame window;

    public Renderer(Scene scene, int width, int height) {
        Renderer.width = width;
        Renderer.height = height;
        this.scene = scene;
        this.camera = new Camera(new Vector3(0, 0, 0), width, height);
        frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    // Static getters for window dimensions.
    public static int getWidthStatic() { return width; }
    public static int getHeightStatic() { return height; }

    public void setCamera(Vector3 vector3, float yaw, float pitch) {
        camera.position = vector3;
        camera.yaw = yaw;
        camera.pitch = pitch;
    }

    public void render() {
        // If size has changed, recreate frame.
        if (frame.getWidth() != width || frame.getHeight() != height) {
            frame.flush();
            frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }

        Graphics g = frame.getGraphics();
        Graphics2D g2d = (Graphics2D) g;

        // Create a viewport to handle drawing the scene.
        Viewport viewport = new Viewport(g2d, camera, scene);

        // Clear the background.
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // Draw all scene objects (sorted by distance if needed).
        for (SceneObject obj : scene.getAllByDistance(camera.position)) {
            obj.draw(viewport);
        }

        // Draw Heads-Up Display.
        drawHud(g2d);
    }

    private void drawHud(Graphics2D g2d) {
        // For simplicity, we assume a constant FPS display here.
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.drawString("FPS: " + (int) (1 / Settings.deltaTime), 10, 20);
        g2d.drawString("Wireframes (F): " + Settings.drawWireframes, 10, 35);
        g2d.drawString("Backfacing (B): " + Settings.allowBackFacing, 10, 50);

        String cam = String.format("(%f %f %f) y:%f p:%f", camera.position.x, camera.position.y, camera.position.z, camera.yaw, camera.pitch);
        g2d.drawString("Camera: " + cam, 10, 65);

        // Draw crosshair.
        int crosshairSize = 10;
        int centerX = width / 2;
        int centerY = height / 2;

        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawLine(centerX - crosshairSize, centerY, centerX + crosshairSize, centerY);
        g2d.drawLine(centerX, centerY - crosshairSize, centerX, centerY + crosshairSize);
    }
}
