package engine.controller;

import engine.scene.objects.SceneObject;
import math.Vector3;

import java.awt.event.KeyEvent;

public class ObjectController extends Controller {

    private final SceneObject object;

    public ObjectController(SceneObject object) {
        this.object = object;
    }

    @Override
    public void update(float deltaTime) {
        float speed = deltaTime * 5;

        System.out.println("hi");

        // Basic movement vectors
        Vector3 forward = new Vector3(0, 0, -1); // -Z is "forward"
        Vector3 right = new Vector3(1, 0, 0);    // +X is "right"
        Vector3 up = new Vector3(0, 1, 0);       // +Y is "up"

        if (getPressedKeys().contains(KeyEvent.VK_I)) object.move(forward.mul(speed));      // forward
        if (getPressedKeys().contains(KeyEvent.VK_K)) object.move(forward.mul(-speed));     // backward
        if (getPressedKeys().contains(KeyEvent.VK_J)) object.move(right.mul(-speed));       // left
        if (getPressedKeys().contains(KeyEvent.VK_L)) object.move(right.mul(speed));        // right
        if (getPressedKeys().contains(KeyEvent.VK_U)) object.move(up.mul(speed));           // up
        if (getPressedKeys().contains(KeyEvent.VK_O)) object.move(up.mul(-speed));          // down
    }

}
