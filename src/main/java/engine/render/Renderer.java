package engine.render;

import engine.Settings;
import engine.scene.Scene;
import engine.scene.objects.Renderable;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import math.Vector3;

import javax.swing.*;

public class Renderer extends Canvas {

    @Getter
    private Camera camera;

    @Setter
    private Scene scene;

    @Getter
    private int width, height;
    // Provide the current frame for drawing.
    @Getter
    private BufferedImage frame;

    private long lastFpsUpdateTime = 0;
    private int displayedFps = 0;

    public Renderer(Scene scene, int width, int height) {
        this.width = width;
        this.height = height;
        this.scene = scene;
        this.camera = new Camera(new Vector3(0, 0, 0), width, height);
        frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Renderer.this.width = Renderer.super.getWidth();
                Renderer.this.height = Renderer.super.getHeight();
                frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                camera.setWidth(width);
                camera.setHeight(height);
            }
        });
    }

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
        //for (Renderable obj : scene.getAllRenderablesByDistance(camera.position)) {
        List<Renderable> renderables = scene.getAllRenderable(viewport);
        for (Renderable obj : renderables) {
            obj.draw(viewport);
        }

        // Draw Heads-Up Display.
        if (Settings.drawHud) {
            drawHud(g2d);
        }
    }

    private void drawHud(Graphics2D g2d) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFpsUpdateTime >= 500) {
            displayedFps = (int) (1 / Settings.deltaTime);
            lastFpsUpdateTime = currentTime;
        }

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.drawString("FPS: " + displayedFps, 10, 20);
        g2d.drawString("Polygons: " + Settings.enginePolygons, 10, 35);
        g2d.drawString("Wireframes (F): " + Settings.drawWireframes, 10, 50);
        g2d.drawString("Backfacing (B): " + Settings.allowBackFacing, 10, 65);
        g2d.drawString("DynamicLighting (C): " + Settings.useDynamicLighting, 10, 80);
        g2d.drawString("DepthBuffering (V): " + Settings.useDepthBuffer, 10, 95);

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
