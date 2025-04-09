package util;

import math.Vector3;

import java.util.Random;

public class Utility {

    private static final Random random = new Random();

    public static int randomInt(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    public static float randomFloat(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    public static Vector3 randomVector3(float minX, float maxX, float minY, float maxY, float minZ, float maxZ) {
        return new Vector3(
                randomFloat(minX, maxX),
                randomFloat(minY, maxY),
                randomFloat(minZ, maxZ)
        );
    }

    public static Vector3 randomVector3(float min, float max) {
        return new Vector3(
                randomFloat(min, max),
                randomFloat(min, max),
                randomFloat(min, max)
        );
    }

}
