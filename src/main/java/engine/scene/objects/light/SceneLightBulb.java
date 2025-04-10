package engine.scene.objects.light;

import math.Vector3;
import util.MathUtility;
import java.awt.Color;

public class SceneLightBulb extends SceneLight {
    // Flickering control parameters
    private float baseIntensity;

    int iterations = 0;

    int clock = 5;

    public SceneLightBulb(Vector3 position, Color color, float intensity) {
        super(position, color, intensity);
        this.baseIntensity = intensity;
    }

    @Override
    public void tick() {
        iterations++;
        if (iterations % clock != 0) {
            return;
        }
        iterations = 0;


        if (Math.random() < 0.2) {
            // Flicker down to 30-70% intensity randomly
            super.intensity = baseIntensity * MathUtility.randomFloat(0.5f, 0.7f);
        } else {
            // Otherwise return to normal (with small fluctuations)
            super.intensity = baseIntensity * MathUtility.randomFloat(0.8f, 1f);
        }
    }
}
