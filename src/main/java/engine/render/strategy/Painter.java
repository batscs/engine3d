package engine.render.strategy;

import engine.Settings;
import engine.render.Viewport;
import engine.scene.Scene;
import engine.scene.objects.Renderable;
import util.SceneUtil;

import java.awt.image.BufferedImage;
import java.util.List;

public class Painter implements RenderStrategy {

    @Override
    public void render(Scene scene, Viewport viewport, BufferedImage frame) {
        List<Renderable> renderables = scene.getAllRenderable(viewport);
        renderables = SceneUtil.sortByDistance(renderables, viewport.getCamera().position);
        Settings.enginePolygons = renderables.size();
        for (Renderable r : renderables) {
            r.draw(viewport);
        }
    }
}
