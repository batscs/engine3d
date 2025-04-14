package server;

import engine.render.Camera;
import engine.render.Viewport;
import engine.scene.objects.Renderable;
import engine.scene.objects.SceneObject;
import math.Vector3;

import java.util.ArrayList;
import java.util.List;

public class ServerObject implements SceneObject {

    private final ServerConnector serverConnector;

    public ServerObject(Camera pov, String endpoint) {
        serverConnector = new ServerConnector(pov, endpoint);
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

    @Override
    public List<Renderable> getRenderables() {
        List<Renderable> result = new ArrayList<>();
        for (SceneObject player : serverConnector.getPlayers()) {
            result.addAll(player.getRenderables());
        }
        return result;
    }

}
