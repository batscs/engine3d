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
    private final ConcurrentHashMap<Integer, Vector3> players = new ConcurrentHashMap<>();
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
                            Thread.sleep(20); // 50 updates/sec
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

                    System.out.println("Player " + playerId + " connected");

                    // Handle player in its own thread
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
                    if (parts.length == 4) {
                        float x = Float.parseFloat(parts[1]);
                        float y = Float.parseFloat(parts[2]);
                        float z = Float.parseFloat(parts[3]);

                        players.put(playerId, new Vector3(x, y, z));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Player " + playerId + " disconnected");
        } finally {
            players.remove(playerId);
            clientWriters.remove(socket);
        }
    }

    private void broadcastPlayerPositions() {
        StringBuilder sb = new StringBuilder("UPDATE ");
        players.forEach((id, pos) -> {
            sb.append(id).append(" ")
                    .append(pos.getX()).append(" ")
                    .append(pos.getY()).append(" ")
                    .append(pos.getZ()).append(";");
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

    public ConcurrentHashMap<Integer, Vector3> getPlayers() {
        return players;
    }
}
