package engine.scene.objects.composite;

import engine.scene.Scene;
import engine.scene.objects.mesh.SceneTriangle;
import math.Triangle;

public class ScenePlayer extends Composite {
    public ScenePlayer() {
        super(Triangle.makeArrow(0, 0, 0, 0.2f)
                .stream()
                .map(SceneTriangle::new)
                .toList());
    }
}
