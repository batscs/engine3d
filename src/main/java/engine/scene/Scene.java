package engine.scene;

import engine.Settings;
import engine.render.Viewport;
import engine.scene.objects.Renderable;
import engine.scene.objects.light.SceneLight;
import engine.scene.objects.SceneObject;
import math.Vector3;
import util.SceneUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Scene {

    private List<SceneObject> objects;

    public Scene() {
        objects = new ArrayList<>();
    }

    public void add(SceneObject o) {
        objects.add(o);
    }

    public void addAll(Collection<? extends SceneObject> os) {
        objects.addAll(os);
    }

    public List<SceneLight> getLights() {
        List<SceneLight> lights = new ArrayList<>();

        for (SceneObject obj : objects) {
            if (obj instanceof SceneLight) {
                lights.add((SceneLight) obj);
            }
        }

        return lights;
    }

    @Deprecated
    public List<Renderable> getAllRenderablesByDistance(Vector3 position) {
        return SceneUtil.sortByDistance(getAllRenderables(), position);
    }

    public List<SceneObject> getAllSceneObjects() {
        return new ArrayList<>(objects);
    }

    @Deprecated
    public List<Renderable> getAllRenderables() {
        ArrayList<Renderable> result = new ArrayList<>();
        objects.forEach(objects -> result.addAll(objects.getRenderables()));
        return result;
    }

    public List<Renderable> getAllRenderable(Viewport viewport) {
        long startTotal = System.nanoTime();

        long startCollect = System.nanoTime();
        List<Renderable> result = new ArrayList<>();
        List<Renderable> temp = new ArrayList<>();
        for (SceneObject obj : objects) {
            temp.addAll(obj.getRenderables());
        }
        long endCollect = System.nanoTime();
        System.out.printf("Collecting renderables: %.9f seconds%n", (endCollect - startCollect) / 1_000_000_000.0);

        long startVisibility = System.nanoTime();
        for (Renderable renderable : temp) {
            if (renderable.isVisible(viewport)) {
                result.add(renderable);
            }
        }
        long endVisibility = System.nanoTime();
        System.out.printf("Visibility check: %.9f seconds%n", (endVisibility - startVisibility) / 1_000_000_000.0);

        long startSort = System.nanoTime();
        List<Renderable> sorted = SceneUtil.sortByDistance(result, viewport.getCamera().position);
        long endSort = System.nanoTime();
        System.out.printf("Sorting by distance: %.9f seconds%n", (endSort - startSort) / 1_000_000_000.0);

        long startCull = System.nanoTime();
        if (Settings.useDepthBuffer) {
            sorted = engine.render.util.DepthBuffer.cull(sorted, viewport);
        }
        long endCull = System.nanoTime();
        System.out.printf("Depth buffer culling: %.9f seconds%n", (endCull - startCull) / 1_000_000_000.0);

        Settings.enginePolygons = sorted.size();

        long endTotal = System.nanoTime();
        System.out.printf("Total getAllRenderable time: %.9f seconds%n", (endTotal - startTotal) / 1_000_000_000.0);

        return sorted;
    }

}
