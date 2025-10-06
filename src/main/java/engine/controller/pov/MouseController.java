package engine.controller.pov;

import engine.Settings;
import engine.controller.Controller;
import engine.render.Camera;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MouseController extends Controller {
    private volatile float deltaX, deltaY;
    private volatile boolean mouseCaptured = false;
    private volatile boolean running = false;

    private final Component targetComponent;
    private final Cursor invisibleCursor;
    private Thread pollingThread;
    private Robot robot;

    private int centerX, centerY;
    private int lastMouseX, lastMouseY;

    private final Camera camera;

    private final List<Consumer<MouseEvent>> clickListeners = new ArrayList<>();

    public MouseController(Component targetComponent, Camera camera) {
        this.targetComponent = targetComponent;
        this.camera = camera;

        this.invisibleCursor = targetComponent.getToolkit().createCustomCursor(
                new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB),
                new Point(0, 0), "invisible"
        );

        try {
            this.robot = new Robot();
            this.robot.setAutoDelay(0);
        } catch (AWTException e) {
            System.err.println("Robot initialization failed: " + e.getMessage());
        }

        targetComponent.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (mouseCaptured) {
                    for (Consumer<MouseEvent> listener : clickListeners) {
                        listener.accept(e);
                    }
                }
            }
        });

        calculateCenter();
    }

    public void addClickListener(Consumer<MouseEvent> listener) {
        clickListeners.add(listener);
    }

    @Override
    public void update(float deltaTime) {
        handleEscapeToggle();
        handleMouseLook(deltaTime);
    }

    private void handleEscapeToggle() {
        if (getReleasedKeys().contains(KeyEvent.VK_ESCAPE)) {
            Settings.mouseEscape = !Settings.mouseEscape;
            setMouseCaptured(!Settings.mouseEscape);
        }
    }

    private void handleMouseLook(float deltaTime) {
        if (!Settings.mouseEscape && mouseCaptured) {
            float mouseSensitivity = 0.001f;
            float dx = consumeDeltaX();
            float dy = consumeDeltaY();

            camera.yaw += dx * mouseSensitivity;
            camera.pitch -= dy * mouseSensitivity;
        }
    }

    private void calculateCenter() {
        Point location = targetComponent.getLocationOnScreen();
        centerX = location.x + targetComponent.getWidth() / 2;
        centerY = location.y + targetComponent.getHeight() / 2;
    }

    public void setMouseCaptured(boolean captured) {
        if (this.mouseCaptured == captured) return;

        this.mouseCaptured = captured;
        if (captured) {
            startPolling();
            captureMouse();
        } else {
            stopPolling();
            releaseMouse();
        }
    }

    private void startPolling() {
        if (running) return;

        running = true;
        pollingThread = new Thread(this::pollingLoop, "MousePollingThread");
        pollingThread.setDaemon(true);
        pollingThread.start();
    }

    private void stopPolling() {
        running = false;
        if (pollingThread != null) {
            try {
                pollingThread.join(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            pollingThread = null;
        }
    }

    private void pollingLoop() {
        PointerInfo initialInfo = MouseInfo.getPointerInfo();
        if (initialInfo != null) {
            Point initialPoint = initialInfo.getLocation();
            lastMouseX = initialPoint.x;
            lastMouseY = initialPoint.y;
        }

        while (running) {
            try {
                PointerInfo pointerInfo = MouseInfo.getPointerInfo();
                if (pointerInfo != null && mouseCaptured) {
                    Point currentPoint = pointerInfo.getLocation();
                    int currentX = currentPoint.x;
                    int currentY = currentPoint.y;

                    deltaX = currentX - lastMouseX;
                    deltaY = currentY - lastMouseY;

                    lastMouseX = currentX;
                    lastMouseY = currentY;

                    if (Math.abs(deltaX) > 1 || Math.abs(deltaY) > 1) {
                        resetMouseToCenter();
                    }
                }

                Thread.sleep(1000 / 120);
            } catch (Exception e) {
                if (!(e instanceof InterruptedException)) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void captureMouse() {
        calculateCenter();

        if (targetComponent.isShowing()) {
            targetComponent.setCursor(invisibleCursor);
        }

        resetMouseToCenter();

        try { Thread.sleep(20); } catch (InterruptedException ignored) {}

        PointerInfo info = MouseInfo.getPointerInfo();
        if (info != null) {
            Point point = info.getLocation();
            lastMouseX = point.x;
            lastMouseY = point.y;
        }
    }

    private void releaseMouse() {
        if (targetComponent.isShowing()) {
            targetComponent.setCursor(Cursor.getDefaultCursor());
        }
    }

    private void resetMouseToCenter() {
        if (robot != null) {
            try {
                robot.mouseMove(centerX, centerY);
                lastMouseX = centerX;
                lastMouseY = centerY;
            } catch (Exception e) {
                System.err.println("Failed to reset mouse: " + e.getMessage());
            }
        }
    }

    public float consumeDeltaX() {
        float dx = deltaX;
        deltaX = 0;
        return dx;
    }

    public float consumeDeltaY() {
        float dy = deltaY;
        deltaY = 0;
        return dy;
    }

    public boolean isMouseCaptured() {
        return mouseCaptured;
    }

    public void dispose() {
        stopPolling();
        releaseMouse();
    }
}
