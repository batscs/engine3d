package engine;

import assets.scene.io.OBJScene;
import assets.scene.template.TestScene;
import engine.controller.CameraController;
import util.PathUtility;

public class Main {
    public static void main(String[] args) {
        Engine engine = new Engine();

        engine.registerController(new CameraController(engine.getCamera()));

        TestScene.build(engine);

        engine.start();
    }
}
