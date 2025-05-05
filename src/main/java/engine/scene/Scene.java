package engine.scene;

import engine.Settings;
import engine.render.Viewport;
import engine.render.util.DepthBuffer;
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

    private final List<Renderable> cacheRenderables = new ArrayList<>();

    private int lastCache = -1;

    private List<Renderable> getCachedRenderables() {
        if (objects != null && objects.hashCode() != lastCache) {
            lastCache = objects.hashCode();
            cacheRenderables.clear();
            for (SceneObject obj : objects) {
                cacheRenderables.addAll(obj.getRenderables());
            }
        }

        return cacheRenderables;
    }

    public List<Renderable> getAllRenderable(Viewport viewport) {
        long start, end;

        start = System.nanoTime();
        getCachedRenderables();

        end = System.nanoTime();
        System.out.printf("Step 1 - Gather renderables: %.6f seconds%n", (end - start) / 1_000_000_000.0);

        start = System.nanoTime();
        List<Renderable> result = new ArrayList<>(1000000);
        for (Renderable renderable : cacheRenderables) {
            if (renderable.isVisible(viewport)) {
                result.add(renderable);
            }
        }
        end = System.nanoTime();
        System.out.printf("Step 2 - Visibility check: %.6f seconds%n", (end - start) / 1_000_000_000.0);

        start = System.nanoTime();
        List<Renderable> sorted = SceneUtil.sortByDistance(result, viewport.getCamera().position);
        end = System.nanoTime();
        System.out.printf("Step 3 - Sort by distance: %.6f seconds%n", (end - start) / 1_000_000_000.0);

        start = System.nanoTime();
        if (Settings.useDepthBuffer) {
            sorted = DepthBuffer.cull(sorted, viewport);
        }
        end = System.nanoTime();
        System.out.printf("Step 4 - Depth buffer culling: %.6f seconds%n", (end - start) / 1_000_000_000.0);

        start = System.nanoTime();
        Settings.enginePolygons = sorted.size();
        end = System.nanoTime();
        System.out.printf("Step 5 - Store polygon count: %.6f seconds%n", (end - start) / 1_000_000_000.0);

        return sorted;
    }


}
