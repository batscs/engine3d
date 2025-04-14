package app.screen;

import engine.Engine;
import engine.assets.template.TestScene;
import engine.controller.CameraController;
import lombok.Getter;

import javax.swing.JFrame;
import java.awt.event.ActionListener;

@Getter
public class EngineScreen implements Screen {

    private final Engine engine;

    public EngineScreen() {
        this.engine = new Engine();
    }

    @Override
    public void start(JFrame frame) {
        engine.start(frame);

        engine.registerController(new CameraController(engine.getCamera()));
        TestScene.build(engine);
    }

    @Override
    public void stop(JFrame frame) {
        engine.stop();
        engine.unregisterAllController();
    }
}
