package engine.controller;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public abstract class Controller {

    private Set<Integer> pressedKeys;
    private Set<Integer> releasedKeys;

    public Controller() {
        this.pressedKeys = new HashSet<>();
        this.releasedKeys = new HashSet<>();
    }

    public void registerKeys(Set<Integer> pressedKeys, Set<Integer> releasedKeys) {
        this.pressedKeys = pressedKeys;
        this.releasedKeys = releasedKeys;
    }

    public abstract void update(float deltaTime);

}
