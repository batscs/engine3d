package util;

import engine.Main;

public class PathUtility {

    public static String getResourcePath(String relativePath) {
        return Main.class.getResource(relativePath).getPath();
    }

}
