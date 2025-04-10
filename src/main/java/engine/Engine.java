package engine;

import engine.controller.Controller;
import engine.render.Camera;
import engine.render.Renderer;
import engine.scene.Scene;
import engine.scene.objects.SceneObject;
import math.Vector3;

import javax.swing.JFrame;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.util.HashSet;
import java.util.Set;

public class Engine implements Runnable {
    private final Renderer renderer;
    private Scene scene;
    private final Set<Controller> controllers;
    private final Set<Integer> pressedKeys = new HashSet<>();
    private final Set<Integer> releasedKeys = new HashSet<>();
    private float deltaTime = 0f;

    public Engine() {
        this.scene = new Scene();
        this.renderer = new Renderer(scene, 700, 700);
        this.controllers = new HashSet<>();

        // Set up the input listener.
        renderer.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                pressedKeys.add(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                pressedKeys.remove(e.getKeyCode());
                releasedKeys.add(e.getKeyCode());
            }
        });
        renderer.setFocusable(true);
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        renderer.setScene(scene);
    }

    public void registerController(Controller controller) {
        controllers.add(controller);
        controller.registerKeys(pressedKeys, releasedKeys);
    }

    public void start() {
        // Start the game loop in a separate thread.
        new Thread(this).start();
    }

    @Override
    public void run() {
        renderer.createBufferStrategy(1);
        BufferStrategy bs = renderer.getBufferStrategy();

        long lastLogicUpdate = System.nanoTime();
        long lastRenderTime = System.nanoTime();
        long tickInterval = 1_000_000_000 / 32; // 32 ticks per second

        while (true) {
            long currentTime = System.nanoTime();
            deltaTime = (currentTime - lastRenderTime) / 1_000_000_000.0f;
            Settings.deltaTime = deltaTime;
            lastRenderTime = currentTime;

            // Update game logic at a fixed tick rate.
            if (currentTime - lastLogicUpdate >= tickInterval) {
                updateGame();
                lastLogicUpdate = currentTime;
            }

            // Render the current frame.
            updateInput();
            renderer.render();

            // Present the frame.
            Graphics g = bs.getDrawGraphics();
            g.drawImage(renderer.getFrame(), 0, 0, null);
            g.dispose();
            bs.show();
        }
    }

    private void updateInput() {
        for (Controller controller : controllers) {
            controller.update(deltaTime);
        }

        releasedKeys.clear();
    }

    private void updateGame() {
        // Update/tick all objects in the scene.
        scene.getAll().forEach(SceneObject::tick);
    }

    public void setCamera(Vector3 vector3, float yaw, float pitch) {
        renderer.setCamera(vector3, yaw, pitch);
    }

    public Camera getCamera() {
        return renderer.getCamera();
    }
}
