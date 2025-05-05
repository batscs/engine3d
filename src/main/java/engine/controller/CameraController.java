package engine.controller;

import engine.Settings;
import engine.render.Camera;
import math.Vector3;

import java.awt.event.KeyEvent;

public class CameraController extends Controller {

    private final Camera camera;

    public CameraController(Camera camera) {
        this.camera = camera;
    }

    @Override
    public void update(float deltaTime) {
        float speed = deltaTime * 5;
        float camSpeed = deltaTime * 5;

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

        if (getPressedKeys().contains(KeyEvent.VK_UP)) camera.pitch += 0.184f * camSpeed;
        if (getPressedKeys().contains(KeyEvent.VK_DOWN)) camera.pitch -= 0.184f * camSpeed;
        if (getPressedKeys().contains(KeyEvent.VK_LEFT)) camera.yaw -= 0.184f * camSpeed;
        if (getPressedKeys().contains(KeyEvent.VK_RIGHT)) camera.yaw += 0.184f * camSpeed;

        if (getReleasedKeys().contains(KeyEvent.VK_F)) Settings.drawWireframes = !Settings.drawWireframes;
        if (getReleasedKeys().contains(KeyEvent.VK_B)) Settings.allowBackFacing = !Settings.allowBackFacing;
        if (getReleasedKeys().contains(KeyEvent.VK_C)) Settings.useDynamicLighting = !Settings.useDynamicLighting;
        if (getReleasedKeys().contains(KeyEvent.VK_V)) Settings.useDepthBuffer = !Settings.useDepthBuffer;
        if (getReleasedKeys().contains(KeyEvent.VK_F12)) Settings.drawHud = !Settings.drawHud;

        camera.pitch = Math.max(-1.55f, Math.min(1.55f, camera.pitch));
    }
}
