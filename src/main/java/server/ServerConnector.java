package server;

import engine.render.Camera;
import math.Vector3;
import server.protocol.Message;
import server.message.PositionMessage;
import server.message.UpdateMessage;
import server.message.WelcomeMessage;
import server.protocol.MessageFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
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
                    Message msg = MessageFactory.parse(line); // Use factory instead of raw checks
                    if (msg instanceof WelcomeMessage) {
                        WelcomeMessage welcomeMsg = (WelcomeMessage) msg;
                        ownPlayerId = welcomeMsg.getPlayerId();
                        System.out.println("Received welcome message with id " + ownPlayerId);
                    }
                    else if (msg instanceof UpdateMessage) {
                        UpdateMessage updateMsg = (UpdateMessage) msg;
                        remotePositions.clear();
                        updateMsg.getPlayerData().forEach((id, playerData) -> {
                            if (id != ownPlayerId) {
                                remotePositions.put(id, playerData);
                            }
                        });
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
            // Change from raw string to proper message
            PositionMessage msg = new PositionMessage(pos, rot);
            out.println(msg.serialize());
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

    public Map<Integer, PlayerData> getRemotePositions() {
        return new HashMap<>(remotePositions);
    }
}
