package engine;

import engine.controller.CameraController;
import engine.controller.ObjectController;
import engine.scene.Camera;
import engine.scene.Scene;
import engine.scene.objects.composite.SceneCube;
import engine.scene.objects.SceneObject;
import engine.scene.objects.light.SceneLight;
import engine.scene.objects.light.SceneLightBulb;
import engine.scene.objects.light.SceneLightFade;
import engine.scene.objects.mesh.SceneLine;
import math.Vector3;

import java.awt.*;

public class Main {
    public static void main(String[] args) {
        Scene scene = new Scene();

        scene.add(new SceneLight(new Vector3(-2, 3, 3), Color.RED, 2f));
        scene.add(new SceneLightBulb(new Vector3(2, 0, 3), Color.BLUE, 1.2f));
        scene.add(new SceneLightFade(new Vector3(-2, -2, 7), Color.GREEN, 1f));

        SceneObject cube = new SceneCube(0, 0, 5, 2);
        scene.add(cube);

        Camera camera = new Camera(new Vector3(2, 2, 0));
        camera.yaw = -0.5f;
        camera.pitch = -0.2f;

        Renderer renderer = new Renderer(camera, scene, 700, 700);

        renderer.registerController(new CameraController(camera));
        //renderer.registerController(new ObjectController(cube));

        renderer.start();
    }
}
