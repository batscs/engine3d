package engine.scene.objects;

import engine.render.Viewport;
import math.Vector3;

public interface SceneObject {

    void draw(Viewport viewport);

    void tick();

    Vector3 getPosition();

    default void move(Vector3 adjustment) {

    }

}
