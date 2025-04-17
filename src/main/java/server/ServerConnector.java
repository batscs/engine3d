package server;

import engine.render.Camera;
import engine.scene.objects.SceneObject;
import engine.scene.objects.composite.SceneCube;
import math.Vector3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerConnector {

    private final String endpoint;
    // Store raw positions received from the server.
    private final Map<Integer, PlayerData> remotePositions = new ConcurrentHashMap<>();
    private final Camera pov;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private Thread listenerThread;
    private boolean running = false;
    private int ownPlayerId = -1;

    public ServerConnector(Camera pov, String endpoint) {
        this.endpoint = endpoint;
        this.pov = pov;
        connect();
    }

    public void connect() {
        try {
            String[] parts = endpoint.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            running = true;
            startListener();
            System.out.println("Connected to server at " + endpoint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startListener() {
        listenerThread = new Thread(() -> {
            try {
                String line;
                while (running && (line = in.readLine()) != null) {
                    if (line.startsWith("WELCOME ")) {
                        // Set our own player id based on the welcome message.
                        ownPlayerId = Integer.parseInt(line.substring(8).trim());
                        System.out.println("Received welcome message with id " + ownPlayerId);
                    } else if (line.startsWith("UPDATE ")) {
                        String data = line.substring(7);
                        String[] tokens = data.split(";");

                        // Temporary map for the current update's raw positions.
                        Map<Integer, PlayerData> currentPositions = new ConcurrentHashMap<>();

                        for (String token : tokens) {
                            if (token.trim().isEmpty())
                                continue;
                            String[] parts = token.trim().split("\\s+");
                            if (parts.length == 7) {
                                int id = Integer.parseInt(parts[0]);
                                // Ignore our own update.
                                if (id == ownPlayerId) continue;
                                float posX = Float.parseFloat(parts[1]);
                                float posY = Float.parseFloat(parts[2]);
                                float posZ = Float.parseFloat(parts[3]);
                                float rotX = Float.parseFloat(parts[4]);
                                float rotY = Float.parseFloat(parts[5]);
                                float rotZ = Float.parseFloat(parts[6]);
                                PlayerData playerData = currentPositions.get(id);
                                if (playerData == null) {
                                    playerData = new PlayerData();
                                    currentPositions.put(id, playerData);
                                }
                                playerData.setPosition(new Vector3(posX, posY, posZ));
                                playerData.setRotation(new Vector3(rotX, rotY, rotZ));
                            }
                        }
                        // Replace the old raw data with the new update.
                        remotePositions.clear();
                        remotePositions.putAll(currentPositions);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Disconnected from server.");
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void tick() {
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            Vector3 pos = pov.getPosition();
            Vector3 rot = pov.getRotation();
            String message = "POS " + pos.getX() + " " + pos.getY() + " " + pos.getZ() +
                    " " + rot.getX() + " " + rot.getY() + " " + rot.getZ();
            out.println(message);
            out.flush();
        }
    }

    public void disconnect() {
        running = false;
        try {
            if (socket != null) socket.close();
        } catch (Exception ignored) { }
        remotePositions.clear();
    }

    // Returns the latest raw position data from other players.
    public Map<Integer, PlayerData> getRemotePositions() {
        return remotePositions;
    }
}
