package engine.assets.template;

import engine.Engine;
import engine.controller.misc.ObjectController;
import engine.scene.Scene;
import engine.scene.objects.SceneObject;
import engine.scene.objects.composite.SceneCube;
import engine.scene.objects.light.SceneLight;
import engine.scene.objects.light.SceneLightBulb;
import engine.scene.objects.light.SceneLightFade;
import engine.scene.objects.robots.SentryTurret;
import engine.scene.objects.robots.StanfordArm;
import math.Vector3;

import java.awt.Color;

public class TestScene {

    public static void build(Engine engine) {
        Scene scene = new Scene();
        scene.add(new SceneLight(new Vector3(-2, 3, 3), java.awt.Color.RED, 2f));
        scene.add(new SceneLightBulb(new Vector3(2, 0, 3), java.awt.Color.BLUE, 1.2f));
        scene.add(new SceneLightFade(new Vector3(-2, -2, 7), java.awt.Color.GREEN, 1f));

        scene.add(new SceneLight(new Vector3(-3f, 1, -4f), Color.GREEN, 2f));
        StanfordArm arm = new StanfordArm(new Vector3(-2, 0, -3));
        // scene.addAll(arm.getParts());

        SentryTurret turret = new SentryTurret(new Vector3(-2f, 0f, -3f));
        scene.addAll(turret.getParts());


        SceneObject cube = new SceneCube(0, 0, 5, 2);
        scene.add(cube);

        engine.registerController(new ObjectController(cube));
        engine.setScene(scene);

        engine.setCamera(new Vector3(2, 2, 0), 0f, 0f);
    }

}
