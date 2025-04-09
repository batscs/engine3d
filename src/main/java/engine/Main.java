package engine;

import engine.scene.Scene;
import engine.scene.objects.SceneLight;
import engine.scene.objects.ScenePlane;
import engine.scene.objects.SceneTriangle;
import math.Triangle;
import math.Vector3;

import java.awt.*;

public class Main {
    public static void main(String[] args) {
        Scene scene = new Scene();

        scene.addAll(Triangle.makeCube(0, 0, 5, 2).stream()
                .map(SceneTriangle::new).toList());

        scene.add(new SceneLight(new Vector3(0, 3, 3), Color.RED, 2f));
        scene.add(new SceneLight(new Vector3(2, 0, 3), Color.BLUE, 1f));

        Renderer renderer = new Renderer(scene, 800, 600);
        renderer.start();
    }
}
