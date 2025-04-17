package server;

import engine.render.Camera;
import engine.scene.objects.Renderable;
import engine.scene.objects.SceneObject;
import engine.scene.objects.composite.SceneCube;
import engine.scene.objects.composite.ScenePlayer;
import math.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerObject implements SceneObject {

    private final ServerConnector serverConnector;
    // Keep a cache of remote player scene objects.
    private final Map<Integer, SceneObject> remotePlayers = new HashMap<>();

    public ServerObject(Camera pov, String endpoint) {
        serverConnector = new ServerConnector(pov, endpoint);
    }

    @Override
    public void tick() {
        serverConnector.tick();
    }

    // This returns our own position.
    @Override
    public Vector3 getPosition() {
        // For testing, you may wish to return a dynamically updated position.
        return new Vector3(0, 0, 0);
    }

    @Override
    public List<Renderable> getRenderables() {
        // Get the latest raw position data from the server connector.
        Map<Integer, PlayerData> latestData = serverConnector.getRemotePositions();

        // Update or create remote player scene objects.
        for (Map.Entry<Integer, PlayerData> entry : latestData.entrySet()) {
            int playerId = entry.getKey();
            Vector3 newPos = entry.getValue().getPosition();
            Vector3 newRot = entry.getValue().getRotation();

            if (remotePlayers.containsKey(playerId)) {
                // Calculate adjustment based on the difference between the new and current positions.
                SceneObject so = remotePlayers.get(playerId);
                so.setRotation(newRot);
                so.setPosition(newPos);
            } else {
                // Create a new scene object for the remote player.
                SceneObject so = new ScenePlayer();
                so.setPosition(newPos);
                so.setRotation(newRot);
                remotePlayers.put(playerId, so);
            }
        }

        // Optionally, you might remove players that are no longer in the latest update.
        remotePlayers.keySet().retainAll(latestData.keySet());

        // Aggregate renderables from all remote player scene objects.
        List<Renderable> result = new ArrayList<>();
        for (SceneObject so : remotePlayers.values()) {
            result.addAll(so.getRenderables());
        }
        return result;
    }

    @Override
    public void move(Vector3 adjustment) {

    }

    @Override
    public void setPosition(Vector3 pos) {

    }

    @Override
    public Vector3 getRotation() {
        return new Vector3(0, 0, 0);
    }

    @Override
    public void setRotation(Vector3 rotation) {

    }

    @Override
    public void rotateAround(Vector3 pivot, Vector3 deltaRotation) {

    }
}
