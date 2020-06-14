package my.mapkn3;

import my.mapkn3.model.Image;
import my.mapkn3.model.ObjFile;
import my.mapkn3.model.Point3D;

import java.io.IOException;
import java.util.List;

public class App {
    static int[][] scaleImage(int[][] img, int windowSide, int size) {
        int[][] scaledImg = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                scaledImg[i][j] = 0;
            }
        }
        int scale = size / windowSide;
        int height = img.length;
        int width = img[0].length;
        int xMin = (width / 2) - (windowSide / 2);
        int xMax = (width / 2) + (windowSide / 2);
        int yMin = (height / 2) - (windowSide / 2);
        int yMax = (height / 2) + (windowSide / 2);
        for (int i = yMin; i < yMax; i++) {
            for (int j = xMin; j < xMax; j++) {
                int value = img[i][j];
                for (int n = 0; n < scale; n++) {
                    for (int m = 0; m < scale; m++) {
                        scaledImg[(i - yMin) * scale + n][(j - xMin) * scale + m] = value;
                    }
                }
            }
        }
        return scaledImg;
    }

    public static void main(String[] args) throws IOException {
        int windowSide = 100;
        int size = 1000;

        Image aImg = new Image(size, size);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                aImg.point(j, i, 0);
            }
        }
        int value = 1;
        for (int i = size / 2 - windowSide / 2; i < size / 2 + windowSide / 2; i++) {
            for (int j = size / 2 - windowSide / 2; j < size / 2 + windowSide / 2; j++) {
                int k = (i * j) / (windowSide * windowSide) * 100;
                int color = 0;
                color |= k << 16;
                color |= k << 8;
                color |= k;
                aImg.point(j, i, color);
            }
        }

        int[][] a = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                a[i][j] = aImg.getValue(j, i);
            }
        }
        Image bImg = new Image(size, size);
        int[][] b = scaleImage(a, windowSide, size);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                bImg.point(j, i, b[i][j]);
            }
        }
        aImg.save("aImg.png", "png");
        bImg.save("bImg.png", "png");


        int height = 2000;
        int width = 2000;
        Image img = new Image(height, width);
        ObjFile objFile = new ObjFile("african_head.obj");
        Point3D lightDir = new Point3D(0, 0, -1);
        List<Point3D> vertexes = objFile.getVertexes();
        for (List<Integer> face : objFile.getFaces()) {
            Point3D vertex0 = vertexes.get(face.get(0));
            Point3D vertex1 = vertexes.get(face.get(1));
            Point3D vertex2 = vertexes.get(face.get(2));

            Point3D firstVec = new Point3D(
                    vertex2.getX() - vertex0.getX(),
                    vertex2.getY() - vertex0.getY(),
                    vertex2.getZ() - vertex0.getZ()
            );
            Point3D secondVec = new Point3D(
                    vertex1.getX() - vertex0.getX(),
                    vertex1.getY() - vertex0.getY(),
                    vertex1.getZ() - vertex0.getZ()
            );
            Point3D faceNormal = Util.normalize(Util.crossProduct(firstVec, secondVec));
            double intensity = Util.dotProduct(faceNormal, lightDir);
            if (intensity > 0) {
                int color = 0;
                color |= (int) (intensity * 255) << 16;
                color |= (int) (intensity * 255) << 8;
                color |= (int) (intensity * 255);
                img.triangle(vertex0, vertex1, vertex2, color);
            }
        }
        img.flipVertically();
        img.save("1.png", "png");
    }
}
