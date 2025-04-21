package server.message;

import math.Vector3;
import server.protocol.Message;
import server.protocol.Protocol;

public class PositionMessage extends Message {
    private final Vector3 position;
    private final Vector3 rotation;

    public PositionMessage(Vector3 pos, Vector3 rot) {
        this.position = pos;
        this.rotation = rot;
    }

    public static PositionMessage deserialize(String data) {
        String[] parts = data.split(" ");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid position data: " + data);
        }

        return new PositionMessage(
                new Vector3(
                        Float.parseFloat(parts[0]),
                        Float.parseFloat(parts[1]),
                        Float.parseFloat(parts[2])
                ),
                new Vector3(
                        Float.parseFloat(parts[3]),
                        Float.parseFloat(parts[4]),
                        Float.parseFloat(parts[5])
                )
        );
    }

    @Override
    public String serialize() {
        return Protocol.POSITION_UPDATE + Protocol.DELIMITER +
                position.getX() + " " + position.getY() + " " + position.getZ() + " " +
                rotation.getX() + " " + rotation.getY() + " " + rotation.getZ();
    }

    // Add getters
    public Vector3 getPosition() { return position; }
    public Vector3 getRotation() { return rotation; }
}