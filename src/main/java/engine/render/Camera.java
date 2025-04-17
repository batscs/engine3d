package engine.render;

import lombok.Getter;
import lombok.Setter;
import math.Matrix4;
import math.Vector3;

public class Camera {

    @Getter
    public Vector3 position;
    public float yaw = 0f;
    public float pitch = 0f;

    @Setter
    public int width, height;

    private float fov;

    public Camera(Vector3 position, int width, int height) {
        this.position = position;
        this.width = width;
        this.height = height;
        this.fov = 70;
    }

    public Matrix4 getViewMatrix() {
        Vector3 forward = new Vector3(
                (float) (Math.cos(pitch) * Math.sin(yaw)),
                (float) Math.sin(pitch),
                (float) (Math.cos(pitch) * Math.cos(yaw))
        );

        Vector3 up = new Vector3(0, 1, 0);
        Vector3 target = position.add(forward);

        return Matrix4.lookAt(position, target, up);
    }

    public Matrix4 getPerspectiveMatrix() {
        Matrix4 projection = Matrix4.perspective(fov, width / (float) height, 0.1f, 100f);
        Matrix4 view = getViewMatrix();
        // view.mul(projection) is fully correct here, also matrix4.perspective() is perfectly working
        return view.mul(projection);
    }

    // Camera.java
    public Vector3 getForwardDirection() {
        return new Vector3(
                (float) (Math.cos(pitch) * Math.sin(yaw)),
                (float) Math.sin(pitch),
                (float) (Math.cos(pitch) * Math.cos(yaw))
        ).normalize();
    }

    // Checks if a point is in front of the camera and within FOV
    // In Camera.java
    public boolean isInView(Vector3 point) {
        Vector3 toPoint = point.sub(position).normalize();
        Vector3 forward = getForwardDirection();

        // Check if point is in front of the camera (with a small buffer)
        float dot = toPoint.dot(forward);
        if (dot < 0.1f) { // Slightly behind the camera? Allow a small buffer (0.1f ≈ 84 degrees)
            return false;
        }

        // Check if within FOV + buffer
        float angleRad = (float) Math.acos(dot);
        float baseFov = 70f; // Your original FOV
        float bufferDegrees = 15f; // Expand FOV by 15 degrees for "spielraum"
        float effectiveFovRad = (float) Math.toRadians((baseFov + bufferDegrees) / 2f);

        return angleRad <= effectiveFovRad;
    }

    public Vector3 getRotation() {
        return new Vector3(
                (float) Math.toDegrees(pitch),
                (float) Math.toDegrees(yaw),
                (float) Math.toDegrees(0)
        );
    }
}
