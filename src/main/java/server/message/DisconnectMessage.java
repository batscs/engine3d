package server.message;

import server.protocol.Message;

public class DisconnectMessage extends Message {
    @Override
    public String serialize() {
        return "DISCONNECT";
    }
}
