package math;

public class Vector4 {
    public final float x, y, z, w;

    public Vector4(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vector3 perspectiveDivide() {
        if (w == 0) return new Vector3(x, y, z); // Avoid division by zero
        return new Vector3(x / w, y / w, z / w);
    }
}
