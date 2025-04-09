package engine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Renderer extends Canvas implements Runnable {
    public static int width, height;
    private final BufferedImage frame;
    private final JFrame window;

    private final List<Triangle> mesh;
    private final List<Vector3> lighting;
    private final Camera camera;
    private final Set<Integer> keys = new HashSet<>();

    public Renderer(int width, int height) {
        Renderer.width = width;
        Renderer.height = height;
        frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        window = new JFrame("Java Software Renderer");

        mesh = Triangle.makeCube(0, 0, 5, 1);
        lighting = new ArrayList<>();
        lighting.add(new Vector3(0, 0, 1));      // Above/behind camera
        lighting.add(new Vector3(2, 2, 4));      // Overhead light

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
        if (keys.contains(KeyEvent.VK_LEFT)) camera.yaw -= 0.015f;
        if (keys.contains(KeyEvent.VK_RIGHT)) camera.yaw += 0.015f;
    }

    private void render() {
        Graphics g = frame.getGraphics();
        Graphics2D g2d = (Graphics2D) g;

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        Matrix4 projection = Matrix4.perspective(70, width / (float) height, 0.1f, 100f);
        Matrix4 view = camera.getViewMatrix();
        Matrix4 mvp = view.mul(projection);

        for (Triangle tri : mesh) {
            if (tri.isBackFacing(camera)) {
                continue;
            }
            Polygon poly = tri.getPolygon(mvp);

            float brightness = 0;

            for (Vector3 light : lighting) {
                float angle = tri.angle(light);  // negative dot product = facing light
                float distance = tri.center().sub(light).length();

                float attenuation = 1.0f / (1 + 0.2f * distance + 0.05f * distance * distance);
                float lightContribution = Math.max(0, -angle) * attenuation;
                brightness += lightContribution;
            }

            brightness = Math.min(1, brightness);  // Clamp to [0,1]

            g2d.setColor(new Color(brightness, 0.5f * brightness, 0.5f * brightness));
            g2d.fillPolygon(poly);
        }

        for (Vector3 light : lighting) {
            // Use mvp to project the light position into normalized device coordinates (NDC)
            Vector3 lightProj = mvp.transform(light);

            // Convert from NDC to screen space
            int x = (int) ((lightProj.x + 1) * 0.5f * width);
            int y = (int) ((1 - (lightProj.y + 1) * 0.5f) * height);  // Y flipped for screen coordinates

            // Use view transformation to get the distance in view space for scaling the light
            Vector3 lightView = view.transform(light);
            float distance = lightView.length();
            int size = (int) (100f / distance);
            size = Math.max(2, Math.min(size, 20));
            System.out.println(size);

            // Draw translucent yellow light
            g2d.setColor(new Color(1f, 1f, 0f, 0.5f));
            g2d.fillOval(x - size / 2, y - size / 2, size, size);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(x - size / 2, y - size / 2, size, size);
        }

    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(frame, 0, 0, null);
    }
}
