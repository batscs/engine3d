package engine.scene;

import engine.scene.objects.light.SceneLight;
import engine.scene.objects.SceneObject;

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

    public List<SceneObject> getAll() {
        return new ArrayList<>(objects);
    }
}
