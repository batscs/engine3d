package engine.scene.objects.light;

import math.Vector3;
import java.awt.Color;

public class SceneLightFade extends SceneLight {
    // Fading control
    private float fadeSpeed = 0.7f; // Default speed (cycles per second)
    private float huePosition = 0.0f; // 0-1 range for color cycle

    public SceneLightFade(Vector3 position, Color startColor, float intensity) {
        super(position, startColor, intensity);
        // Initialize hue position based on start color
        float[] hsb = Color.RGBtoHSB(
                startColor.getRed(),
                startColor.getGreen(),
                startColor.getBlue(),
                null
        );
        this.huePosition = hsb[0];
    }

    @Override
    public void tick() {
        // Update hue position (wrapping around at 1.0)
        huePosition += fadeSpeed / 60f; // Assuming 60 ticks per second
        if (huePosition > 1.0f) {
            huePosition -= 1.0f;
        }

        // Convert HSB to RGB (using full saturation and brightness)
        int rgb = Color.HSBtoRGB(huePosition, 1.0f, 1.0f);
        super.color = new Color(rgb);
    }

    // Getter/setter for fade speed
    public float getFadeSpeed() {
        return fadeSpeed;
    }

    public void setFadeSpeed(float fadeSpeed) {
        this.fadeSpeed = fadeSpeed;
    }
}