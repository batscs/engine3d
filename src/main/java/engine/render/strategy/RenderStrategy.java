package engine.render.strategy;

import engine.render.Viewport;
import engine.scene.Scene;

import java.awt.image.BufferedImage;

public interface RenderStrategy {
    void render(Scene scene, Viewport viewport, BufferedImage frame);
}
