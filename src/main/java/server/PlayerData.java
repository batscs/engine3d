package server;

import lombok.Getter;
import lombok.Setter;
import math.Vector3;

@Getter
@Setter
public class PlayerData {

    private Vector3 position;
    private Vector3 rotation;

    public PlayerData() {
        this.position = new Vector3(0, 0, 0);
        this.rotation = new Vector3(0, 0, 0);
    }

}
