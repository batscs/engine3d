package engine.scene;

import engine.scene.objects.light.SceneLight;
import engine.scene.objects.SceneObject;
import math.Vector3;

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

    public List<SceneObject> getAllByDistance(Vector3 position) {
        List<SceneObject> result = getAll();
        result.sort((a, b) -> {
            float distA = a.getPosition().sub(position).length();
            float distB = b.getPosition().sub(position).length();
            return Float.compare(distB, distA);
        });

        return result;
    }

    public List<SceneObject> getAll() {
        return new ArrayList<>(objects);
    }
}
