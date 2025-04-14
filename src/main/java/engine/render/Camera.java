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

    public Camera(Vector3 position, int width, int height) {
        this.position = position;
        this.width = width;
        this.height = height;
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
        Matrix4 projection = Matrix4.perspective(70, width / (float) height, 0.1f, 100f);
        Matrix4 view = getViewMatrix();
        return view.mul(projection);
    }
}
