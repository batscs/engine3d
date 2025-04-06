package engine;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Vector3 {
    public float x, y, z;

    public float length() {
        return (float)Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3 normalize() {
        float len = length();
        return new Vector3(x / len, y / len, z / len);
    }

    public float dot(Vector3 o) {
        return x * o.x + y * o.y + z * o.z;
    }

    public Vector3 cross(Vector3 o) {
        return new Vector3(
                y * o.z - z * o.y,
                z * o.x - x * o.z,
                x * o.y - y * o.x
        );
    }

    public Vector3 add(Vector3 o) {
        return new Vector3(x + o.x, y + o.y, z + o.z);
    }

    public Vector3 sub(Vector3 o) {
        return new Vector3(x - o.x, y - o.y, z - o.z);
    }

    public Vector3 mul(float s) {
        return new Vector3(x * s, y * s, z * s);
    }
}
