package engine.assets.template;

import engine.Engine;
import engine.controller.ObjectController;
import engine.scene.Scene;
import engine.scene.objects.SceneObject;
import engine.scene.objects.composite.SceneCube;
import engine.scene.objects.light.SceneLight;
import engine.scene.objects.light.SceneLightBulb;
import engine.scene.objects.light.SceneLightFade;
import math.Vector3;

public class TestScene {

    public static void build(Engine engine) {
        Scene scene = new Scene();
        scene.add(new SceneLight(new Vector3(-2, 3, 3), java.awt.Color.RED, 2f));
        scene.add(new SceneLightBulb(new Vector3(2, 0, 3), java.awt.Color.BLUE, 1.2f));
        scene.add(new SceneLightFade(new Vector3(-2, -2, 7), java.awt.Color.GREEN, 1f));

        SceneObject cube = new SceneCube(0, 0, 5, 2);
        scene.add(cube);

        engine.registerController(new ObjectController(cube));
        engine.setScene(scene);

        engine.setCamera(new Vector3(2, 2, 0), -0.5f, -0.2f);
    }

}
