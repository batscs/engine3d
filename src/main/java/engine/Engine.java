package engine;

import assets.scene.io.OBJScene;
import engine.controller.Controller;
import engine.render.Camera;
import engine.render.Renderer;
import engine.scene.Scene;
import engine.scene.objects.SceneObject;
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
import java.awt.image.BufferStrategy;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Engine implements Runnable {
    private final Renderer renderer;
    private Scene scene;
    private final Set<Controller> controllers;
    private final Set<Integer> pressedKeys = new HashSet<>();
    private final Set<Integer> releasedKeys = new HashSet<>();
    private float deltaTime = 0f;
    private final JFrame window;

    public Engine() {
        this.scene = new Scene();
        this.renderer = new Renderer(scene, 700, 700);
        this.controllers = new HashSet<>();
        window = new JFrame("Java Engine 3D");

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

    private void importScene(String path) {
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

    public void start() {
        initWindow();
        new Thread(this).start();
    }

    private void initWindow() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem loadItem = new JMenuItem("Load File");
        loadItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                importScene(file.getAbsolutePath());
            }
        });
        fileMenu.add(loadItem);
        menuBar.add(fileMenu);
        window.setJMenuBar(menuBar);

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(Renderer.getWidthStatic(), Renderer.getHeightStatic());
        window.setLayout(new BorderLayout());
        window.add(renderer, BorderLayout.CENTER);
        window.setVisible(true);
        window.pack();
        window.setLocationRelativeTo(null);

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
