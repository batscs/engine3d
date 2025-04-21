package server.message;

import server.protocol.Message;
import server.protocol.Protocol;

public class WelcomeMessage extends Message {
    private final int playerId;

    public WelcomeMessage(int playerId) {
        this.playerId = playerId;
    }

    public static WelcomeMessage deserialize(String data) {
        return new WelcomeMessage(Integer.parseInt(data));
    }

    @Override
    public String serialize() {
        return Protocol.WELCOME + Protocol.DELIMITER + playerId;
    }

    public int getPlayerId() { return playerId; }
}