package server.message;

import math.Vector3;
import server.protocol.Message;
import server.protocol.Protocol;
import server.PlayerData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class UpdateMessage extends Message {
    private final Map<Integer, PlayerData> playerData;

    public UpdateMessage(Map<Integer, PlayerData> playerData) {
        this.playerData = new HashMap<>(playerData);
    }

    public static UpdateMessage deserialize(String data) {
        Map<Integer, PlayerData> players = new HashMap<>();
        String[] entries = data.split(";");

        for (String entry : entries) {
            if (entry.isEmpty()) continue;
            String[] parts = entry.split(" ");
            if (parts.length != 7) continue;

            int id = Integer.parseInt(parts[0]);
            Vector3 pos = new Vector3(
                    Float.parseFloat(parts[1]),
                    Float.parseFloat(parts[2]),
                    Float.parseFloat(parts[3])
            );
            Vector3 rot = new Vector3(
                    Float.parseFloat(parts[4]),
                    Float.parseFloat(parts[5]),
                    Float.parseFloat(parts[6])
            );

            PlayerData pd = new PlayerData();
            pd.setPosition(pos);
            pd.setRotation(rot);
            players.put(id, pd);
        }

        return new UpdateMessage(players);
    }

    @Override
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        playerData.forEach((id, data) -> {
            sb.append(id).append(" ")
                    .append(round(data.getPosition().getX())).append(" ")
                    .append(round(data.getPosition().getY())).append(" ")
                    .append(round(data.getPosition().getZ())).append(" ")
                    .append(round(data.getRotation().getX())).append(" ")
                    .append(round(data.getRotation().getY())).append(" ")
                    .append(round(data.getRotation().getZ())).append(";");
        });
        return Protocol.PLAYER_UPDATE + Protocol.DELIMITER + sb.toString();
    }

    private float round(float num) {
        BigDecimal bd = new BigDecimal(Float.toString(num));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    public Map<Integer, PlayerData> getPlayerData() {
        return new HashMap<>(playerData);
    }
}