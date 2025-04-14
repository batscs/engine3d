package app.screen;

import engine.Engine;
import server.Server;
import server.ServerObject;

import javax.swing.*;

public class MultiplayerScreen extends EngineScreen {

    private final boolean isHost;
    private final String endpoint; // null if host
    private final Engine engine = getEngine();
    private ServerObject serverObject;
    private Server server;

    // Constructor for hosting
    public MultiplayerScreen() {
        this.isHost = true;
        this.endpoint = "localhost:1111";
    }

    // Constructor for joining with provided endpoint
    public MultiplayerScreen(String endpoint) {
        this.isHost = false;
        this.endpoint = endpoint;
    }

    @Override
    public void start(JFrame frame) {
        super.start(frame);

        if (isHost) {
            server = new Server();
            server.startServer();
        }

        serverObject = new ServerObject(engine.getCamera(), endpoint);
        engine.getScene().add(serverObject);
    }

    @Override
    public void stop(JFrame frame) {
        super.stop(frame);
        if (server != null) {
            server.stopServer();
        }
        if (server != null) {
            server.stopServer();
        }
    }
}
