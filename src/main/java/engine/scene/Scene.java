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
        List<Renderable> result = new ArrayList<>();
        List<Renderable> temp = new ArrayList<>();

        for (SceneObject obj : objects) {
            temp.addAll(obj.getRenderables());
        }

        for (Renderable renderable : temp) {
            if (renderable.isVisible(viewport)) {
                result.add(renderable);
            }
        }

        Settings.enginePolygons = result.size();

        return SceneUtil.sortByDistance(result, viewport.getCamera().position);
    }

}
