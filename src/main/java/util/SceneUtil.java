package util;

import engine.scene.objects.Renderable;
import engine.scene.objects.SceneObject;
import math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class SceneUtil {

    public static List<Renderable> sortByDistance(List<? extends Renderable> os, Vector3 position) {
        List<Renderable> result = new ArrayList<>(os);

        result.sort((a, b) -> {
            float distA = a.getPosition().sub(position).length();
            float distB = b.getPosition().sub(position).length();
            return Float.compare(distB, distA);
        });

        return result;
    }

}
