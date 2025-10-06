package engine;

import engine.assets.io.OBJScene;
import engine.controller.Controller;
import engine.controller.pov.KeyboardController;
import engine.controller.pov.MouseController;
import engine.render.Camera;
import engine.render.Renderer;
import engine.scene.Scene;
import engine.scene.objects.BoundingBox;
import engine.scene.objects.SceneObject;
import lombok.Getter;
import math.Vector3;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Engine implements Runnable {
    @Getter
    private Renderer renderer;

    @Getter
    private Scene scene;
    private final Set<Controller> controllers;
    private Set<Integer> pressedKeys;
    private Set<Integer> releasedKeys;
    private float deltaTime = 0f;
    private boolean running;


    public Engine() {
        this.scene = new Scene();
        this.controllers = new HashSet<>();
        this.running = false;
        this.pressedKeys = new HashSet<>();
        this.releasedKeys = new HashSet<>();
    }

    public void importScene(String path) {
        System.out.println("Importing from " + path);
        try {
            OBJScene.build(this, path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        renderer.setScene(scene);
    }

    public void registerController(Controller controller) {
        controllers.add(controller);
        controller.registerKeys(pressedKeys, releasedKeys);
    }

    public void start(JFrame frame) {
        this.renderer = new Renderer(scene, frame.getWidth(), frame.getHeight());

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

        initWindow(frame);

        running = true;
        new Thread(this).start();

        initControllers();
    }

    private void initControllers() {
        MouseController mouseController = new MouseController(renderer, this.getCamera());
        mouseController.setMouseCaptured(true);
        mouseController.addClickListener(this::onMouseClick);

        KeyboardController keyboardController = new KeyboardController(this.getCamera());

        this.registerController(keyboardController);
        this.registerController(mouseController);
    }

    private void onMouseClick(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            handleSceneClick();
        }
    }

    private void handleSceneClick() {
        Camera camera = getCamera();
        Scene scene = getScene();

        Vector3 origin = camera.getPosition();
        Vector3 dir = camera.getForwardDirection();

        SceneObject hit = raycastScene(scene, camera, 100f);
        if (hit != null) {
            Settings.interactedName = hit.toString();
            hit.interact();
        } else {
            Settings.interactedName = "null";
        }
    }

    private SceneObject raycastScene(Scene scene, Camera camera, float maxDistance) {
        Vector3 origin = camera.getPosition();
        Vector3 dir = camera.getForwardDirection();

        SceneObject closestHit = null;
        float closestDist = Float.MAX_VALUE;

        for (SceneObject obj : scene.getAllSceneObjects()) {
            if (obj == null) continue;

            // If it has a bounding box, test that first
            var box = obj.getBoundingBox();
            if (box != null) {
                Float hitDist = rayIntersectsAABB(origin, dir, box);
                if (hitDist != null && hitDist < closestDist && hitDist <= maxDistance) {
                    closestDist = hitDist;
                    closestHit = obj;
                    continue; // we found a more precise intersection, skip fallback
                }
            }

            // Fallback: distance-to-center heuristic (old logic)
            Vector3 toObj = obj.getPosition().sub(origin);
            float projLength = toObj.dot(dir); // projection along the view direction

            if (projLength < 0 || projLength > maxDistance) continue; // behind camera or too far

            Vector3 closestPoint = origin.add(dir.mul(projLength));
            float distanceFromRay = obj.getPosition().sub(closestPoint).length();

            if (distanceFromRay < 0.5f && projLength < closestDist) {
                closestDist = projLength;
                closestHit = obj;
            }
        }

        return closestHit;
    }

    private Float rayIntersectsAABB(Vector3 origin, Vector3 dir, BoundingBox box) {
        float tmin = (box.getMin().x - origin.x) / dir.x;
        float tmax = (box.getMax().x - origin.x) / dir.x;
        if (tmin > tmax) { float tmp = tmin; tmin = tmax; tmax = tmp; }

        float tymin = (box.getMin().y - origin.y) / dir.y;
        float tymax = (box.getMax().y - origin.y) / dir.y;
        if (tymin > tymax) { float tmp = tymin; tymin = tymax; tymax = tmp; }

        if (tmin > tymax || tymin > tmax)
            return null;

        if (tymin > tmin) tmin = tymin;
        if (tymax < tmax) tmax = tymax;

        float tzmin = (box.getMin().z - origin.z) / dir.z;
        float tzmax = (box.getMax().z - origin.z) / dir.z;
        if (tzmin > tzmax) { float tmp = tzmin; tzmin = tzmax; tzmax = tmp; }

        if (tmin > tzmax || tzmin > tmax)
            return null;

        if (tzmin > tmin) tmin = tzmin;
        if (tzmax < tmax) tmax = tzmax;

        return (tmin < 0) ? tmax : tmin; // nearest positive intersection distance
    }

    public void stop() {
        running = false;
    }

    private void initWindow(JFrame frame) {
        frame.add(renderer, BorderLayout.CENTER);

        new DropTarget(renderer, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable transferable = dtde.getTransferable();
                    java.util.List<File>
                            droppedFiles = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);

                    for (File file : droppedFiles) {
                        importScene(file.getAbsolutePath());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void run() {
        renderer.createBufferStrategy(1);
        BufferStrategy bs = renderer.getBufferStrategy();

        long lastLogicUpdate = System.nanoTime();
        long lastRenderTime = System.nanoTime();
        long tickInterval = 1_000_000_000 / 32; // 32 ticks per second

        while (running) {
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
        scene.getAllSceneObjects().forEach(SceneObject::tick);
    }

    public void setCamera(Vector3 vector3, float yaw, float pitch) {
        renderer.setCamera(vector3, yaw, pitch);
    }

    public Camera getCamera() {
        return renderer.getCamera();
    }

    public void unregisterAllController() {
        controllers.clear();
    }
}
