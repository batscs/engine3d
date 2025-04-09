package engine.scene.objects;

import engine.scene.Viewport;

public interface SceneObject {

    void draw(Viewport viewport);

    void tick();

}
