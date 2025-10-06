package app.screen;

import engine.Engine;
import engine.assets.template.TestScene;
import engine.controller.pov.KeyboardController;
import engine.controller.pov.MouseController;
import engine.render.Renderer;
import lombok.Getter;

import javax.swing.JFrame;

@Getter
public class EngineScreen implements Screen {

    private final Engine engine;

    public EngineScreen() {
        this.engine = new Engine();
    }

    @Override
    public void start(JFrame frame) {
        engine.start(frame);

        // TODO eventuell das gaze in engine.start() ??
        TestScene.build(engine);
    }

    @Override
    public void stop(JFrame frame) {
        engine.stop();
        engine.unregisterAllController();
    }
}
