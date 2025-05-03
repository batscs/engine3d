package engine.scene.objects.mesh;

import engine.Settings;
import engine.render.Camera;
import engine.scene.objects.Renderable;
import engine.scene.objects.light.SceneLight;
import engine.scene.objects.SceneObject;
import lombok.Setter;
import math.Matrix4;
import math.Triangle;
import math.Vector3;
import engine.render.Viewport;

import java.awt.*;
import java.util.List;

public class SceneTriangle implements SceneObject, Renderable {

    private final Triangle originalTri;  // <-- store the pristine geometry
    private Triangle tri;                // <-- this is what we draw

    @Setter private boolean allowBackFacing = false;
    @Setter private Color baseColor;
    private Vector3 rotation = new Vector3(0,0,0);
    private Vector3 position = new Vector3(0,0,0);

    public SceneTriangle(Triangle tri) {
        this(tri, new Color(200, 200, 200));
    }

    public SceneTriangle(Triangle tri, Color baseColor) {
        this.baseColor    = new Color(200,200,200);
        this.originalTri  = tri.copy();    // deep‐copy the input
        this.tri          = tri.copy();    // our “working” copy
        this.position     = this.tri.center();
    }

    @Override
    public void draw(Viewport viewport) {
        if (!allowBackFacing
                && !Settings.allowBackFacing
                && tri.isBackFacing(viewport.getCamera().position)) return;

        Polygon poly = getPolygon(viewport);

        if (poly == null) {
            return;
        }

        Color finalColor = computeLitColor(viewport);
        viewport.getG2d().setColor(finalColor);

        if (Settings.drawWireframes) {
            viewport.getG2d().drawPolygon(poly);
        } else {
            viewport.getG2d().fillPolygon(poly);
        }
    }


    @Override
    public void tick() {

    }

    @Override
    public Vector3 getPosition() {
        return position;
    }

    @Override
    public List<Renderable> getRenderables() {
        return List.of(this);
    }

    @Override
    public void move(Vector3 adjustment) {
        tri = tri.add(adjustment);
    }

    @Override
    public void setPosition(Vector3 newPos) {
        this.position = newPos;
        rebuild();    // reapply rotation & translation in one go
    }

    @Override
    public Vector3 getRotation() {
        return rotation;
    }

    @Override
    public void setRotation(Vector3 rotationDeg) {
        this.rotation = rotationDeg;
        rebuild();
    }

    private Vector3 rotatePoint(Vector3 pivot, Matrix4 rotation, Vector3 point) {
        Vector3 translated = point.sub(pivot);
        Vector3 rotated = rotation.transform(translated);
        return rotated.add(pivot);
    }

    private Color computeLitColor(Viewport viewport) {
        if (!Settings.useDynamicLighting) {
            Vector3 camPos = viewport.getCamera().position;
            Vector3 viewDir = tri.center().sub(camPos).normalize();
            Vector3 normal = tri.normal().normalize();

            float intensity = Math.max(0.2f, -normal.dot(viewDir));  // keep at least 0.2 to avoid total black

            int r = (int)(baseColor.getRed()   * intensity);
            int g = (int)(baseColor.getGreen() * intensity);
            int b = (int)(baseColor.getBlue()  * intensity);

            return new Color(clamp(r), clamp(g), clamp(b));
        }

        float r = 0, g = 0, b = 0;

        for (SceneLight light : viewport.getScene().getLights()) {
            Vector3 lightPos = light.getPosition();
            float angle = tri.angle(lightPos);
            float distance = tri.center().sub(lightPos).length();
            float attenuation = 1.0f / (1 + 0.2f * distance + 0.05f * distance * distance);
            float contribution = Math.max(0, -angle) * attenuation * light.getIntensity();

            r += (baseColor.getRed() / 255f) * (light.getColor().getRed() / 255f) * contribution;
            g += (baseColor.getGreen() / 255f) * (light.getColor().getGreen() / 255f) * contribution;
            b += (baseColor.getBlue() / 255f) * (light.getColor().getBlue() / 255f) * contribution;
        }

        // Clamp
        r = Math.min(1, r);
        g = Math.min(1, g);
        b = Math.min(1, b);

        return new Color(r, g, b);
    }

    private Polygon getPolygon(Viewport viewport) {
        Camera camera = viewport.getCamera();
        boolean atLeastOneInView =
                camera.isInView(tri.v0) ||
                        camera.isInView(tri.v1) ||
                        camera.isInView(tri.v2);

        if (!atLeastOneInView) return null;

        Matrix4 perspective = viewport.getPerspective();
        Vector3 p0 = perspective.transform(tri.v0);
        Vector3 p1 = perspective.transform(tri.v1);
        Vector3 p2 = perspective.transform(tri.v2);

        // Convert NDC to screen coordinates
        int x0 = (int) ((p0.x + 1) * 0.5f * viewport.getWidth());
        int y0 = (int) ((1 - p0.y) * 0.5f * viewport.getHeight());
        int x1 = (int) ((p1.x + 1) * 0.5f * viewport.getWidth());
        int y1 = (int) ((1 - p1.y) * 0.5f * viewport.getHeight());
        int x2 = (int) ((p2.x + 1) * 0.5f * viewport.getWidth());
        int y2 = (int) ((1 - p2.y) * 0.5f * viewport.getHeight());

        return new Polygon(new int[]{x0, x1, x2}, new int[]{y0, y1, y2}, 3);
    }

    private void rebuild() {
        // TODO somehow only rebuild not twice if setPosition and setRotation called

        // 1) local center of the original
        Vector3 c = originalTri.center();

        // 2) build axis‑angle mats
        float yawRad   = (float)Math.toRadians(-rotation.y);
        float pitchRad = (float)Math.toRadians( rotation.x);

        Matrix4 T1     = Matrix4.translate(c.negate());
        Matrix4 Ryaw   = Matrix4.rotationAroundAxis(new Vector3(0,1,0), yawRad);
        Matrix4 Rpitch = Matrix4.rotationAroundAxis(new Vector3(1,0,0), pitchRad);
        Matrix4 T2     = Matrix4.translate(c);

        // rotation around center
        Matrix4 R      = T2.mul(Rpitch).mul(Ryaw).mul(T1);

        // 3) apply rotation
        Vector3 v0r = R.transform(originalTri.v0);
        Vector3 v1r = R.transform(originalTri.v1);
        Vector3 v2r = R.transform(originalTri.v2);

        Triangle t = new Triangle(v0r, v1r, v2r);

        // 4) now translate so that the new center == `position`
        Vector3 newCenter = t.center();
        Vector3 delta     = position.sub(newCenter);

        this.tri = new Triangle(
                v0r.add(delta),
                v1r.add(delta),
                v2r.add(delta)
        );
    }

    public void setBaseColor(Color color) {
        this.baseColor = color;
    }

    private int clamp(int val) {
        return Math.min(255, Math.max(0, val));
    }
}
