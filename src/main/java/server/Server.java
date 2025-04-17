package server;

import math.Vector3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    public static final int PORT = 1111;
    private final ConcurrentHashMap<Integer, PlayerData> players = new ConcurrentHashMap<>();
    private final Map<Socket, PrintWriter> clientWriters = new ConcurrentHashMap<>();
    private boolean running = false;
    private int playerIdCounter = 0;

    public void startServer() {
        if (running) return;

        running = true;

        Thread serverThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Server started on port " + PORT);

                // Start broadcast thread
                Thread broadcastThread = new Thread(() -> {
                    while (running) {
                        try {
                            broadcastPlayerPositions();
                            Thread.sleep(20); // 50 updates per second
                        } catch (InterruptedException ignored) {}
                    }
                });
                broadcastThread.setDaemon(true);
                broadcastThread.start();

                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    int playerId = ++playerIdCounter;

                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
                    clientWriters.put(clientSocket, writer);

                    // Send the welcome message with the player's id.
                    writer.println("WELCOME " + playerId);
                    System.out.println("Player " + playerId + " connected");
                    players.put(playerId, new PlayerData());
                    // Handle player in its own thread.
                    Thread clientThread = new Thread(() -> handleClient(clientSocket, playerId));
                    clientThread.setDaemon(true);
                    clientThread.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        serverThread.setDaemon(true);
        serverThread.start();
    }

    private void handleClient(Socket socket, int playerId) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("POS ")) {
                    String[] parts = line.split(" ");
                    if (parts.length == 7) {
                        float posX = Float.parseFloat(parts[1]);
                        float posY = Float.parseFloat(parts[2]);
                        float posZ = Float.parseFloat(parts[3]);

                        float rotX = Float.parseFloat(parts[4]);
                        float rotY = Float.parseFloat(parts[5]);
                        float rotZ = Float.parseFloat(parts[6]);

                        PlayerData data = players.get(playerId);
                        data.setPosition(new Vector3(posX, posY, posZ));
                        data.setRotation(new Vector3(rotX, rotY, rotZ));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Player " + playerId + " disconnected");
            e.printStackTrace();
        } finally {
            players.remove(playerId);
            clientWriters.remove(socket);
        }
    }

    private void broadcastPlayerPositions() {
        StringBuilder sb = new StringBuilder("UPDATE ");
        players.forEach((id, data) -> {
            sb.append(id).append(" ")
                    .append(data.getPosition().getX()).append(" ")
                    .append(data.getPosition().getY()).append(" ")
                    .append(data.getPosition().getZ()).append(" ")
                    .append(data.getRotation().getX()).append(" ")
                    .append(data.getRotation().getY()).append(" ")
                    .append(data.getRotation().getZ()).append(";");
        });

        String message = sb.toString();
        for (PrintWriter writer : clientWriters.values()) {
            writer.println(message);
        }
    }

    public void stopServer() {
        running = false;
        System.out.println("Server stopped.");
    }

}
