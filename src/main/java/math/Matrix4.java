package math;

public class Matrix4 {
    float[][] m = new float[4][4];

    public static Matrix4 perspective(float fovDeg, float aspect, float near, float far) {
        float fovRad = 1f / (float) Math.tan(Math.toRadians(fovDeg) / 2);
        Matrix4 mat = new Matrix4();

        mat.m[0][0] = aspect * fovRad;
        mat.m[1][1] = fovRad;
        mat.m[2][2] = far / (far - near);
        mat.m[2][3] = 1.0f;
        mat.m[3][2] = (-far * near) / (far - near);

        return mat;
    }

    public static Matrix4 lookAt(Vector3 eye, Vector3 target, Vector3 up) {
        Vector3 z = target.sub(eye).normalize();
        Vector3 x = up.cross(z).normalize();
        Vector3 y = z.cross(x).normalize();

        Matrix4 mat = new Matrix4();
        mat.m[0][0] = x.x; mat.m[1][0] = x.y; mat.m[2][0] = x.z;
        mat.m[0][1] = y.x; mat.m[1][1] = y.y; mat.m[2][1] = y.z;
        mat.m[0][2] = z.x; mat.m[1][2] = z.y; mat.m[2][2] = z.z;
        mat.m[3][0] = -x.dot(eye);
        mat.m[3][1] = -y.dot(eye);
        mat.m[3][2] = -z.dot(eye);
        mat.m[3][3] = 1f;
        return mat;
    }

    public Matrix4 mul(Matrix4 b) {
        Matrix4 r = new Matrix4();
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                r.m[row][col] =
                        m[row][0] * b.m[0][col] +
                                m[row][1] * b.m[1][col] +
                                m[row][2] * b.m[2][col] +
                                m[row][3] * b.m[3][col];
            }
        }
        return r;
    }

    public Vector3 transform(Vector3 v) {
        float x = v.x * m[0][0] + v.y * m[1][0] + v.z * m[2][0] + m[3][0];
        float y = v.x * m[0][1] + v.y * m[1][1] + v.z * m[2][1] + m[3][1];
        float z = v.x * m[0][2] + v.y * m[1][2] + v.z * m[2][2] + m[3][2];
        float w = v.x * m[0][3] + v.y * m[1][3] + v.z * m[2][3] + m[3][3];
        if (w != 0.0f) {
            x /= w;
            y /= w;
            z /= w;
        }
        return new Vector3(x, y, z);
    }

    public Vector4 transform(Vector4 vec) {
        float x = m[0][0] * vec.x + m[0][1] * vec.y + m[0][2] * vec.z + m[0][3] * vec.w;
        float y = m[1][0] * vec.x + m[1][1] * vec.y + m[1][2] * vec.z + m[1][3] * vec.w;
        float z = m[2][0] * vec.x + m[2][1] * vec.y + m[2][2] * vec.z + m[2][3] * vec.w;
        float w = m[3][0] * vec.x + m[3][1] * vec.y + m[3][2] * vec.z + m[3][3] * vec.w;
        return new Vector4(x, y, z, w);
    }
}
