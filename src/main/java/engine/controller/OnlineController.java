package engine.controller;

import engine.scene.objects.SceneObject;
import util.MathUtility;

public class OnlineController extends Controller {

    private final SceneObject object;

    public OnlineController(SceneObject object) {
        this.object = object;
    }

    @Override
    public void update(float deltaTime) {
        object.move(MathUtility.randomVector3(-0.005f, 0.005f));
    }
}
