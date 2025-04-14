package server;

import engine.render.Camera;
import engine.render.Viewport;
import engine.scene.objects.SceneObject;
import engine.scene.objects.composite.SceneCube;
import lombok.Getter;
import math.Vector3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerConnector {

    private final String endpoint;
    private final Map<Integer, SceneObject> players = new ConcurrentHashMap<>();
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
                    if (line.startsWith("UPDATE ")) {
                        String data = line.substring(7);
                        String[] tokens = data.split(";");

                        // Temp map to track current update's players
                        Map<Integer, SceneObject> currentPlayers = new ConcurrentHashMap<>();

                        for (String token : tokens) {
                            if (token.trim().isEmpty()) continue;
                            String[] parts = token.trim().split("\\s+");

                            if (parts.length == 4) {
                                int id = Integer.parseInt(parts[0]);
                                float x = Float.parseFloat(parts[1]);
                                float y = Float.parseFloat(parts[2]);
                                float z = Float.parseFloat(parts[3]);

                                // Identify own ID once by matching initial position
                                if (ownPlayerId == -1) {
                                    Vector3 myPos = pov.getPosition();
                                    if (Math.abs(myPos.getX() - x) < 0.01f &&
                                            Math.abs(myPos.getY() - y) < 0.01f &&
                                            Math.abs(myPos.getZ() - z) < 0.01f) {
                                        ownPlayerId = id;
                                        continue;
                                    }
                                }

                                if (id != ownPlayerId) {
                                    currentPlayers.put(id, new SceneCube(x, y, z, 1));
                                }
                            }
                        }

                        // Replace previous players map with the updated one
                        players.clear();
                        players.putAll(currentPlayers);
                    }
                }
            } catch (Exception e) {
                System.err.println("Disconnected from server.");
            }
        });

        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void tick() {
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            Vector3 pos = pov.getPosition();
            String message = "POS " + pos.getX() + " " + pos.getY() + " " + pos.getZ();
            out.println(message);
            out.flush();
        }
    }

    public void disconnect() {
        running = false;
        try {
            if (socket != null) socket.close();
        } catch (Exception ignored) {}
        players.clear();
    }

    public List<SceneObject> getPlayers() {
        return new ArrayList<>(players.values());
    }
}
