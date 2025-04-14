package server;

import engine.render.Camera;
import engine.render.Viewport;
import engine.scene.objects.SceneObject;
import math.Vector3;

public class ServerObject implements SceneObject {

    private final ServerConnector serverConnector;

    public ServerObject(Camera pov, String endpoint) {
        serverConnector = new ServerConnector(pov, endpoint);
    }

    @Override
    public void draw(Viewport viewport) {
        serverConnector.draw(viewport);
    }

    @Override
    public void tick() {
        serverConnector.tick();
    }

    // For our own position, you might use more advanced logic.
    // Here, we simply return a fixed position (or one that could be updated later).
    @Override
    public Vector3 getPosition() {
        // For testing you may wish to return a position that changes over time.
        return new Vector3(0, 0, 0);
    }
}
