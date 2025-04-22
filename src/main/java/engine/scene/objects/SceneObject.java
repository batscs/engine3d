package engine.scene.objects;

import engine.render.Viewport;
import math.Vector3;

import java.util.List;

public interface SceneObject {

    void tick();

    Vector3 getPosition();

    List<Renderable> getRenderables();

    void move(Vector3 adjustment);

    void setPosition(Vector3 pos);

    Vector3 getRotation();
    void setRotation(Vector3 rotation);

}
