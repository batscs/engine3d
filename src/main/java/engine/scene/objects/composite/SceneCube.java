package engine.scene.objects.composite;

import engine.scene.objects.SceneObject;
import engine.scene.objects.mesh.SceneTriangle;
import math.Triangle;

public class SceneCube extends Composite implements SceneObject {

    public SceneCube(float x, float y, float z, float size) {
        super(Triangle.makeCube(x, y, z, size).stream().map(SceneTriangle::new).toList());
    }

}
