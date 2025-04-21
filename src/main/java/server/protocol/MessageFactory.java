package server.protocol;

import server.message.PositionMessage;
import server.message.UpdateMessage;
import server.message.WelcomeMessage;

public class MessageFactory {
    public static Message parse(String rawMessage) {
        String[] parts = rawMessage.split("\\" + Protocol.DELIMITER, 2);
        String type = parts[0];
        String data = parts.length > 1 ? parts[1] : "";

        switch (type) {
            case Protocol.WELCOME:
                return WelcomeMessage.deserialize(data);
            case Protocol.POSITION_UPDATE:  // Now matches "POS"
                return PositionMessage.deserialize(data);
            case Protocol.PLAYER_UPDATE:     // "UPDATE"
                return UpdateMessage.deserialize(data);
            default:
                throw new IllegalArgumentException("Unknown message type: " + type);
        }
    }
}