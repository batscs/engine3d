package engine;

public class Camera {
    public Vector3 position;
    public float yaw = 0f;
    public float pitch = 0f;

    public Camera(Vector3 position) {
        this.position = position;
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
}
