package engine.controller.pov;

import engine.Settings;
import engine.controller.Controller;
import engine.render.Camera;
import math.Vector3;

import java.awt.event.KeyEvent;

public class KeyboardController extends Controller {

    private final Camera camera;

    public KeyboardController(Camera camera) {
        this.camera = camera;
    }

    @Override
    public void update(float deltaTime) {
        handleKeyboardMovement(deltaTime);
        handleToggles();
        clampCameraPitch();
    }

    private void handleKeyboardMovement(float deltaTime) {
        float speed = deltaTime * 5;

        Vector3 forward = new Vector3(
                (float) Math.sin(camera.yaw),
                0,
                (float) Math.cos(camera.yaw)
        ).normalize();

        Vector3 right = new Vector3(forward.z, 0, -forward.x);
        Vector3 up = new Vector3(0, 1, 0);

        if (getPressedKeys().contains(KeyEvent.VK_W)) camera.position = camera.position.add(forward.mul(speed));
        if (getPressedKeys().contains(KeyEvent.VK_S)) camera.position = camera.position.sub(forward.mul(speed));
        if (getPressedKeys().contains(KeyEvent.VK_A)) camera.position = camera.position.sub(right.mul(speed));
        if (getPressedKeys().contains(KeyEvent.VK_D)) camera.position = camera.position.add(right.mul(speed));
        if (getPressedKeys().contains(KeyEvent.VK_SPACE)) camera.position = camera.position.add(up.mul(speed));
        if (getPressedKeys().contains(KeyEvent.VK_SHIFT)) camera.position = camera.position.sub(up.mul(speed));
    }

    private void handleToggles() {
        if (getReleasedKeys().contains(KeyEvent.VK_F)) Settings.drawWireframes = !Settings.drawWireframes;
        if (getReleasedKeys().contains(KeyEvent.VK_B)) Settings.allowBackFacing = !Settings.allowBackFacing;
        if (getReleasedKeys().contains(KeyEvent.VK_C)) Settings.useDynamicLighting = !Settings.useDynamicLighting;
        if (getReleasedKeys().contains(KeyEvent.VK_V)) Settings.useDepthBuffer = !Settings.useDepthBuffer;
        if (getReleasedKeys().contains(KeyEvent.VK_F12)) Settings.drawHud = !Settings.drawHud;
    }

    private void clampCameraPitch() {
        camera.pitch = Math.max(-1.55f, Math.min(1.55f, camera.pitch));
    }
}
