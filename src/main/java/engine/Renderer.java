package engine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Renderer extends Canvas implements Runnable {
    private final int width, height;
    private final BufferedImage frame;
    private final JFrame window;

    private final List<Triangle> mesh;
    private final Camera camera;
    private final Set<Integer> keys = new HashSet<>();

    public Renderer(int width, int height) {
        this.width = width;
        this.height = height;
        frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        window = new JFrame("Java Software Renderer");

        mesh = Triangle.makeCube(0, 0, 5, 1);
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
        while (true) {
            updateInput();
            render();
            repaint();
            try { Thread.sleep(32); } catch (InterruptedException ignored) {}
        }
    }

    private void updateInput() {
        float speed = 0.1f;
        Vector3 forward = new Vector3(
                (float) Math.sin(camera.yaw),
                0,
                (float) Math.cos(camera.yaw)
        ).normalize();
        Vector3 right = new Vector3(forward.z, 0, -forward.x);

        if (keys.contains(KeyEvent.VK_W)) camera.position = camera.position.add(forward.mul(speed));
        if (keys.contains(KeyEvent.VK_S)) camera.position = camera.position.sub(forward.mul(speed));
        if (keys.contains(KeyEvent.VK_A)) camera.position = camera.position.sub(right.mul(speed));
        if (keys.contains(KeyEvent.VK_D)) camera.position = camera.position.add(right.mul(speed));
        if (keys.contains(KeyEvent.VK_UP)) camera.pitch += 0.02f;
        if (keys.contains(KeyEvent.VK_DOWN)) camera.pitch -= 0.02f;
        if (keys.contains(KeyEvent.VK_LEFT)) camera.yaw -= 0.02f;
        if (keys.contains(KeyEvent.VK_RIGHT)) camera.yaw += 0.02f;
    }

    private void render() {
        Graphics g = frame.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        Matrix4 projection = Matrix4.perspective(90, width / (float) height, 0.1f, 100f);
        Matrix4 view = camera.getViewMatrix();
        Matrix4 mvp = view.mul(projection);

        for (Triangle tri : mesh) {
            Vector3 p0 = mvp.transform(tri.v0);
            Vector3 p1 = mvp.transform(tri.v1);
            Vector3 p2 = mvp.transform(tri.v2);

            int x0 = (int)((p0.x + 1) * 0.5f * width);
            int y0 = (int)((1 - p0.y) * 0.5f * height);
            int x1 = (int)((p1.x + 1) * 0.5f * width);
            int y1 = (int)((1 - p1.y) * 0.5f * height);
            int x2 = (int)((p2.x + 1) * 0.5f * width);
            int y2 = (int)((1 - p2.y) * 0.5f * height);

            g.setColor(Color.BLACK);
            g.drawLine(x0, y0, x1, y1);
            g.drawLine(x1, y1, x2, y2);
            g.drawLine(x2, y2, x0, y0);
        }
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(frame, 0, 0, null);
    }
}
