package engine;

import engine.scene.Camera;
import engine.scene.Scene;
import math.Matrix4;
import math.Triangle;
import math.Vector3;
import engine.scene.Viewport;
import engine.scene.objects.SceneLight;
import engine.scene.objects.SceneObject;
import engine.scene.objects.SceneTriangle;

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
    private final BufferedImage frame;
    private final JFrame window;

    private final Camera camera;
    private final Set<Integer> keys = new HashSet<>();
    private Scene scene;

    public Renderer(int width, int height) {
        Renderer.width = width;
        Renderer.height = height;
        frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        window = new JFrame("Java Engine 3D");

        scene = new Scene();

        scene.addAll(Triangle.makeCube(0, 0, 5, 2).stream()
                .map(SceneTriangle::new).toList());

        scene.add(new SceneLight(new Vector3(0, -3, 3), Color.RED, 2f));
        scene.add(new SceneLight(new Vector3(2, 0, 3), Color.BLUE, 1f));

        camera = new Camera(new Vector3(0, 0, 0));

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) { keys.add(e.getKeyCode()); }
            public void keyReleased(KeyEvent e) { keys.remove(e.getKeyCode()); }
        });

        setFocusable(true);
    }

    public void start() {
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(width, height);
        window.add(this);
        window.setVisible(true);
        new Thread(this).start();
    }

    @Override
    public void run() {
        createBufferStrategy(1);
        BufferStrategy bs = getBufferStrategy();

        while (true) {
            updateInput();
            tick();
            render(); // still render into `frame`

            // Now draw to screen
            Graphics g = bs.getDrawGraphics();
            g.drawImage(frame, 0, 0, null);
            g.dispose();
            bs.show();

            try { Thread.sleep(16); } catch (InterruptedException ignored) {}
        }
    }

    private void updateInput() {
        float speed = 0.05f;

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
        if (keys.contains(KeyEvent.VK_SPACE)) camera.position = camera.position.sub(up.mul(speed));
        if (keys.contains(KeyEvent.VK_SHIFT)) camera.position = camera.position.add(up.mul(speed));

        if (keys.contains(KeyEvent.VK_UP)) camera.pitch -= 0.015f;
        if (keys.contains(KeyEvent.VK_DOWN)) camera.pitch += 0.015f;
        if (keys.contains(KeyEvent.VK_LEFT)) camera.yaw -= 0.025f;
        if (keys.contains(KeyEvent.VK_RIGHT)) camera.yaw += 0.025f;
    }

    private void render() {
        Graphics g = frame.getGraphics();
        Graphics2D g2d = (Graphics2D) g;

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        Matrix4 projection = Matrix4.perspective(70, width / (float) height, 0.1f, 100f);
        Matrix4 view = camera.getViewMatrix();
        Matrix4 mvp = view.mul(projection);

        Viewport viewport = new Viewport(g2d, mvp, camera, scene.getLights());

        for (SceneObject obj : scene.getAll()) {
            obj.draw(viewport);
        }
    }

    private void tick() {
        for (SceneObject obj : scene.getAll()) {
            obj.tick();
        }
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(frame, 0, 0, null);
    }
}
