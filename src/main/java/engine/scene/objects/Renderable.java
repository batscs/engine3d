package engine.scene.objects;

import engine.render.Viewport;
import math.Vector3;

public interface Renderable {

    void draw(Viewport viewport);

    boolean isVisible(Viewport viewport);

    Vector3 getPosition();

}
