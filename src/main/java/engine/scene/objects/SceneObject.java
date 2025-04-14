package engine.scene.objects;

import engine.render.Viewport;
import math.Vector3;

import java.util.List;

public interface SceneObject {

    void tick();

    Vector3 getPosition();

    List<Renderable> getRenderables();

    default void move(Vector3 adjustment) {

    }

}
