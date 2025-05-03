package engine.assets.io;

import engine.Engine;
import engine.controller.ObjectController;
import engine.scene.Scene;
import engine.scene.objects.SceneObject;
import engine.scene.objects.composite.Composite;
import engine.scene.objects.composite.ScenePolygon;
import engine.scene.objects.light.SceneLight;
import engine.scene.objects.light.SceneLightFade;
import math.Vector3;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class OBJScene {

    private static class Material {
        public Color diffuseColor = new Color(200, 200, 200);
        public String name = "";
    }

    private static Map<String, Material> loadMaterials(String mtlPath) throws IOException {
        Map<String, Material> materials = new HashMap<>();
        Material current = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(mtlPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("newmtl ")) {
                    current = new Material();
                    current.name = line.substring(7);
                    materials.put(current.name, current);
                } else if (line.startsWith("Kd ")) {
                    if (current != null) {
                        String[] parts = line.split("\\s+");
                        current.diffuseColor = new Color(
                                (int) (Float.parseFloat(parts[1]) * 255),
                                (int) (Float.parseFloat(parts[2]) * 255),
                                (int) (Float.parseFloat(parts[3]) * 255)
                        );
                    }
                }
            }
        }
        return materials;
    }

    public static void build(Engine engine, String path) throws IOException {
        Scene scene = new Scene();
        List<Vector3> vertices = new ArrayList<>();
        List<SceneObject> faces = new ArrayList<>();
        Map<String, Material> materials = new HashMap<>();
        Material currentMaterial = new Material();
        String baseDir = new File(path).getParent();

        boolean controllable = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                // Parsing vertices (v)
                if (line.startsWith("v ")) {
                    String[] tokens = line.split("\\s+");
                    float x = Float.parseFloat(tokens[1]);
                    float y = Float.parseFloat(tokens[2]);
                    float z = Float.parseFloat(tokens[3]);
                    vertices.add(new Vector3(x, y, z));
                }
                else if (line.startsWith("mtllib ")) {
                    String mtlFile = line.substring(7);
                    materials = loadMaterials(baseDir + File.separator + mtlFile);
                }
                else if (line.startsWith("usemtl ")) {
                    String mtlName = line.substring(7);
                    currentMaterial = materials.getOrDefault(mtlName, new Material());
                }
                // Parsing faces (f)
                else if (line.startsWith("f ")) {
                    int commentIndex = line.indexOf('#');
                    if (commentIndex != -1) {
                        line = line.substring(0, commentIndex).trim();
                    }

                    String[] tokens = line.split("\\s+");
                    List<Vector3> faceVerts = new ArrayList<>();
                    for (int i = 1; i < tokens.length; i++) {
                        if (tokens[i].isEmpty()) continue;

                        String[] parts = tokens[i].split("/"); // Handles v/vt/vn or just v
                        int vertexIndex = Integer.parseInt(parts[0]) - 1;
                        faceVerts.add(vertices.get(vertexIndex));
                    }

                    ScenePolygon scenePolygon = new ScenePolygon(faceVerts);
                    scenePolygon.setBaseColor(currentMaterial.diffuseColor);
                    faces.add(scenePolygon);
                }
                // Parsing lights (l)
                else if (line.startsWith("l ")) {
                    String[] tokens = line.split("\\s+");
                    if (tokens.length == 9) {
                        // Expecting: l x y z intensity r g b
                        float x = Float.parseFloat(tokens[1]);
                        float y = Float.parseFloat(tokens[2]);
                        float z = Float.parseFloat(tokens[3]);
                        float intensity = Float.parseFloat(tokens[4]);
                        float r = Float.parseFloat(tokens[5]);
                        float g = Float.parseFloat(tokens[6]);
                        float b = Float.parseFloat(tokens[7]);

                        Vector3 position = new Vector3(x, y, z);
                        Vector3 color = new Vector3(r, g, b);
                        if (tokens[8].equals("fade")) {
                            scene.add(new SceneLightFade(position, new Color(color.x, color.y, color.z), intensity));
                        } else {
                            scene.add(new SceneLight(position, new Color(color.x, color.y, color.z), intensity));
                        }
                    }
                } else if (line.startsWith("c ")) {
                    String[] tokens = line.split("\\s+");
                    if (tokens.length == 6) {
                        engine.setCamera(
                                new Vector3(
                                    Float.parseFloat(tokens[1]),
                                    Float.parseFloat(tokens[2]),
                                    Float.parseFloat(tokens[3])
                                    ), Float.parseFloat(tokens[4]), Float.parseFloat(tokens[5]));

                    }
                } else if (line.startsWith("_ctrl")) {
                    controllable = true;
                }

            }
        }

        SceneObject model = new Composite(faces);
        scene.add(model);

        if (controllable) {
            engine.registerController(new ObjectController(model));
        }

        // Add the polygons and lights to the scene
        engine.setScene(scene);
    }
}
