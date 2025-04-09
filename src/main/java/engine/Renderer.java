package engine;

import engine.scene.Camera;
import engine.scene.Scene;
import math.Matrix4;
import math.Vector3;
import engine.scene.Viewport;
import engine.scene.objects.SceneObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

public class Renderer extends Canvas implements Runnable {
    public static int width, height;
    private BufferedImage frame;
    private final JFrame window;
    private final Camera camera;
    private final Set<Integer> keys = new HashSet<>();
    private final Set<Integer> releasedKeys = new HashSet<>();
    private final Scene scene;

    private float deltaTime = 0f;

    public Renderer(Scene scene, int width, int height) {
        Renderer.width = width;
        Renderer.height = height;
        this.scene = scene;
        frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        window = new JFrame("Java Engine 3D");

        camera = new Camera(new Vector3(2, 2, 0));
        camera.yaw = -0.5f;
        camera.pitch = -0.2f;

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) { keys.add(e.getKeyCode()); }
            public void keyReleased(KeyEvent e) { keys.remove(e.getKeyCode()); releasedKeys.add(e.getKeyCode()); }
        });

        setFocusable(true);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int newWidth = getWidth();
                int newHeight = getHeight();
                if (newWidth > 0 && newHeight > 0) {
                    Renderer.width = newWidth;
                    Renderer.height = newHeight;
                    frame.flush(); // Clean up old image
                    frame.getGraphics().dispose();
                }
            }
        });
    }

    public void start() {
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(width, height);
        window.setLayout(new BorderLayout());
        window.add(this, BorderLayout.CENTER);
        window.setVisible(true);
        window.pack();
        window.setLocationRelativeTo(null);
        new Thread(this).start();
    }

    @Override
    public void run() {
        createBufferStrategy(1);
        BufferStrategy bs = getBufferStrategy();

        long lastLogicUpdate = System.nanoTime();
        long lastRedraw = System.nanoTime();
        long tickrate = 1_000_000_000 / 32;

        while (true) {
            long currentTime = System.nanoTime();
            deltaTime = (currentTime - lastRedraw) / 1_000_000_000.0f; // Convert to seconds
            lastRedraw = currentTime;

            float difference = currentTime - lastLogicUpdate;
            if (difference > tickrate) {
                tick();
                lastLogicUpdate = currentTime;
            }

            updateInput();
            render();

            // Now draw to screen
            Graphics g = bs.getDrawGraphics();
            g.drawImage(frame, 0, 0, null);
            g.dispose();
            bs.show();
        }
    }

    private void updateInput() {
        float speed = deltaTime * 5;

        Vector3 forward = new Vector3(
                (float) Math.sin(camera.yaw),
                0,
                (float) Math.cos(camera.yaw)
        ).normalize();

        Vector3 right = new Vector3(forward.z, 0, -forward.x);
        Vector3 up = new Vector3(0, 1, 0);

        if (keys.contains(KeyEvent.VK_W)) camera.position = camera.position.add(forward.mul(speed));
        if (keys.contains(KeyEvent.VK_S)) camera.position = camera.position.sub(forward.mul(speed));
        if (keys.contains(KeyEvent.VK_A)) camera.position = camera.position.sub(right.mul(speed));
        if (keys.contains(KeyEvent.VK_D)) camera.position = camera.position.add(right.mul(speed));
        if (keys.contains(KeyEvent.VK_SPACE)) camera.position = camera.position.add(up.mul(speed));
        if (keys.contains(KeyEvent.VK_SHIFT)) camera.position = camera.position.sub(up.mul(speed));

        if (keys.contains(KeyEvent.VK_UP)) camera.pitch += 0.19f * speed;
        if (keys.contains(KeyEvent.VK_DOWN)) camera.pitch -= 0.19f * speed;
        if (keys.contains(KeyEvent.VK_LEFT)) camera.yaw -= 0.19f * speed;
        if (keys.contains(KeyEvent.VK_RIGHT)) camera.yaw += 0.19f * speed;

        if (releasedKeys.contains(KeyEvent.VK_F)) Settings.drawWireframes = !Settings.drawWireframes;
        if (releasedKeys.contains(KeyEvent.VK_B)) Settings.allowBackFacing = !Settings.allowBackFacing;

        releasedKeys.clear();
    }

    private void render() {
        if (frame.getWidth() != width || frame.getHeight() != height) {
            frame.flush();
            frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }

        Matrix4 projection = Matrix4.perspective(70, width / (float) height, 0.1f, 100f);
        Matrix4 view = camera.getViewMatrix();
        Matrix4 mvp = view.mul(projection);

        Graphics g = frame.getGraphics();
        Graphics2D g2d = (Graphics2D) g;

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        Viewport viewport = new Viewport(g2d, mvp, camera, scene.getLights());

        for (SceneObject obj : scene.getAll()) {
            obj.draw(viewport);
        }

        drawHud(g2d);
    }

    private void drawHud(Graphics2D g2d) {
        // Calculate and draw FPS
        int fps = (int)(1f / deltaTime);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.drawString("FPS: " + fps, 10, 20);
        g2d.drawString("Wireframes (F): " + Settings.drawWireframes, 10, 35);
        g2d.drawString("Backfacing (B): " + Settings.allowBackFacing, 10, 50);

        int crosshairSize = 10;
        int centerX = width / 2;
        int centerY = height / 2;

        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawLine(centerX - crosshairSize, centerY, centerX + crosshairSize, centerY);
        g2d.drawLine(centerX, centerY - crosshairSize, centerX, centerY + crosshairSize);
    }

    private void tick() {
        scene.getAll().forEach(SceneObject::tick);
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(frame, 0, 0, null);
    }
}
