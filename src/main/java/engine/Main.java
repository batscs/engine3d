package engine;

import engine.scene.Scene;
import engine.scene.objects.light.SceneLight;
import engine.scene.objects.light.SceneLightBulb;
import engine.scene.objects.light.SceneLightFade;
import engine.scene.objects.mesh.ScenePlane;
import engine.scene.objects.mesh.SceneTriangle;
import math.Triangle;
import math.Vector3;

import java.awt.*;

public class Main {
    public static void main(String[] args) {
        Scene scene = new Scene();

        scene.add(new SceneLight(new Vector3(-2, 3, 3), Color.RED, 2f));
        scene.add(new SceneLightBulb(new Vector3(2, 0, 3), Color.BLUE, 1.2f));
        scene.add(new SceneLightFade(new Vector3(-2, -2, 7), Color.GREEN, 1f));

        scene.addAll(Triangle.makeCube(0, 0, 5, 2).stream()
                .map(SceneTriangle::new).toList());

        Renderer renderer = new Renderer(scene, 700, 700);
        renderer.start();
    }
}
