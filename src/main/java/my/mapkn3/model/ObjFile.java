package my.mapkn3.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ObjFile {
    private final List<Point3D> vertexes = new ArrayList<>();
    private final List<Point3D> vertexTextures = new ArrayList<>();
    private final List<List<Integer>> faces = new ArrayList<>();

    public ObjFile(String filename) throws IOException {
        vertexes.add(new Point3D(0, 0, 0));
        try (FileInputStream fis = new FileInputStream(filename); Scanner scanner = new Scanner(fis)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("v ")) {
                    String[] vertex = line.split(" ");
                    double x = Double.parseDouble(vertex[1]);
                    double y = Double.parseDouble(vertex[2]);
                    double z = Double.parseDouble(vertex[3]);
                    vertexes.add(new Point3D(x, y, z));
                } else if (line.startsWith("vt ")) {
                    String[] vertex = line.split(" ");
                    double x = Double.parseDouble(vertex[2]);
                    double y = Double.parseDouble(vertex[3]);
                    double z = Double.parseDouble(vertex[4]);
                    vertexTextures.add(new Point3D(x, y, z));
                } else if (line.startsWith("f ")) {
                    String[] vertexIndexes = line.split(" ");
                    String[] first = vertexIndexes[1].split("/");
                    String[] second = vertexIndexes[2].split("/");
                    String[] third = vertexIndexes[3].split("/");
                    int firstVertex = Integer.parseInt(first[0]);
                    int secondVertex = Integer.parseInt(second[0]);
                    int thirdVertex = Integer.parseInt(third[0]);
                    faces.add(new ArrayList<>() {{
                        add(firstVertex);
                        add(secondVertex);
                        add(thirdVertex);
                    }});
                }
            }
        }
    }

    public List<Point3D> getVertexes() {
        return vertexes;
    }

    public List<Point3D> getVertexTextures() {
        return vertexTextures;
    }

    public List<List<Integer>> getFaces() {
        return faces;
    }
}
