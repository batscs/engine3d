package engine.scene.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import math.Vector3;

@Getter
@Setter
@AllArgsConstructor
public class BoundingBox {
    private Vector3 min;
    private Vector3 max;

    public boolean contains(Vector3 point) {
        return point.x >= min.x && point.x <= max.x &&
                point.y >= min.y && point.y <= max.y &&
                point.z >= min.z && point.z <= max.z;
    }

    public Vector3 center() {
        return new Vector3(
                (min.x + max.x) * 0.5f,
                (min.y + max.y) * 0.5f,
                (min.z + max.z) * 0.5f
        );
    }

    public Vector3 size() {
        return max.sub(min);
    }
}
