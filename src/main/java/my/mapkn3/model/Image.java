package my.mapkn3.model;

import my.mapkn3.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Image {
    private int height;
    private int width;
    private int[] data;

    private double[] zbuffer;

    public Image(int height, int width) {
        this.height = height;
        this.width = width;
        this.data = new int[height * width];
        this.zbuffer = new double[height * width];
        Arrays.fill(zbuffer, Double.MIN_VALUE);
    }

    public Image(String filename) throws IOException {
        BufferedImage image;
        try (FileInputStream fis = new FileInputStream(filename)) {
            image = ImageIO.read(fis);
        }
        height = image.getHeight();
        width = image.getWidth();
        data = new int[height * width];
        this.zbuffer = new double[height * width];
        Arrays.fill(zbuffer, Double.MIN_VALUE);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int pixel = image.getRGB(j, i);
                point(j, i, pixel);
            }
        }
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void save(String filename, String format) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int value = getValue(j, i) & 0xffffff;
                image.setRGB(j, i, value);
            }
        }
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            ImageIO.write(image, format, fos);
        }

        BufferedImage zbufferImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int value = (zbuffer[i * width + j] > 0) ? (int) (zbuffer[i * width + j] * 255) : 0;
                int color = 0;
                color |= value << 16;
                color |= value << 8;
                color |= value;
                zbufferImage.setRGB(j, i, color);
            }
        }
        try (FileOutputStream fos = new FileOutputStream("zbuffer-" + filename)) {
            ImageIO.write(zbufferImage, format, fos);
        }
    }

    public int getValue(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return data[y * width + x];
        } else {
            return 0;
        }
    }

    public void point(int x, int y, int color) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            data[y * width + x] = color;
        }
    }

    public void flipVertically() {
        for (int i = 0; i < height / 2; i++) {
            for (int j = 0; j < width; j++) {
                int topValue = getValue(j, i);
                int bottomValue = getValue(j, height - 1 - i);
                point(j, i, bottomValue);
                point(j, height - 1 - i, topValue);

                double topZBuffer = zbuffer[i * width + j];
                double bottomZBuffer = zbuffer[(height - 1 - i) * width + j];
                zbuffer[i * width + j] = bottomZBuffer;
                zbuffer[(height - 1 - i) * width + j] = topZBuffer;
            }
        }
    }

    public void line(Point3D a, Point3D b, int color) {
        int aX = (int) ((a.getX() + 1) * width / 2);
        int aY = (int) ((a.getY() + 1) * height / 2);
        int bX = (int) ((b.getX() + 1) * width / 2);
        int bY = (int) ((b.getY() + 1) * height / 2);
        line(aX, aY, bX, bY, color);
    }

    public void line(Point2D a, Point2D b, int color) {
        line(a.getX(), a.getY(), b.getX(), b.getY(), color);
    }

    public void line(int x0, int y0, int x1, int y1, int color) {
        if (x0 >= 0 && x0 < width && y0 >= 0 && y0 < height && x1 >= 0 && x1 < width && y1 >= 0 && y1 < height) {
            int aX = x0;
            int aY = y0;
            int bX = x1;
            int bY = y1;
            int v;
            boolean steep = false;
            if (Math.abs(aX - bX) < Math.abs(aY - bY)) {
                v = aX;
                aX = aY;
                aY = v;
                v = bX;
                bX = bY;
                bY = v;
                steep = true;
            }
            if (aX > bX) {
                v = aX;
                aX = bX;
                bX = v;
                v = aY;
                aY = bY;
                bY = v;
            }
            int dx = bX - aX;
            int dy = bY - aY;
            int dError = Math.abs(dy) * 2;
            int error = 0;
            int y = aY;
            for (int x = aX; x <= bX; x++) {
                if (steep) {
                    point(y, x, color);
                } else {
                    point(x, y, color);
                }
                error += dError;
                if (error > dx) {
                    y += (bY > aY ? 1 : -1);
                    error -= dx * 2;
                }
            }
        }
    }

    private Point3D convertPoint3D(Point3D a) {
        return new Point3D((int) ((a.getX() + 1) * width / 2), (int) ((a.getY() + 1) * height / 2), a.getZ());
    }

    public void triangle(Point3D a, Point3D b, Point3D c, int color) {
        if (a.getY() == b.getY() && a.getY() == c.getY()) return;
        List<Point3D> points = Stream.of(a, b, c)
                .sorted(Comparator.comparingDouble(Point3D::getY))
                .collect(Collectors.toList());
        Point3D bottom = convertPoint3D(points.get(0));
        Point3D middle = convertPoint3D(points.get(1));
        Point3D top = convertPoint3D(points.get(2));

        double total_height = top.getY() - bottom.getY();
        for (double i = 0; i < total_height; i++) {
            boolean second_half = i > middle.getY() - bottom.getY() || middle.getY() == bottom.getY();
            double segment_height = second_half ? top.getY() - middle.getY() : middle.getY() - bottom.getY();
            double alpha =  i / total_height;
            double beta =  (i - (second_half ? middle.getY() - bottom.getY() : 0)) / segment_height;
            Point3D aPoint = Util.plus(bottom, Util.multiply(Util.minus(top, bottom), alpha));
            Point3D bPoint = second_half ? Util.plus(middle, Util.multiply(Util.minus(top, middle), beta)) : Util.plus(bottom, Util.multiply(Util.minus(middle, bottom), beta));
            if (aPoint.getX() > bPoint.getX()) {
                Point3D bucket = aPoint;
                aPoint = new Point3D(bPoint.getX(), bPoint.getY(), bPoint.getZ());
                bPoint = new Point3D(bucket.getX(), bucket.getY(), bucket.getZ());
            }
            for (double j = aPoint.getX(); j <= bPoint.getX(); j++) {
                double phi = bPoint.getX() == aPoint.getX() ? 1 : (j - aPoint.getX()) / (bPoint.getX() - aPoint.getX());
                Point3D pPoint = Util.plus(aPoint, Util.multiply(Util.minus(bPoint, aPoint), phi));
                int idx = (int) (pPoint.getX() + pPoint.getY() * width);
                if (zbuffer[idx] < pPoint.getZ()) {
                    zbuffer[idx] = pPoint.getZ();
                    point((int) pPoint.getX(), (int) pPoint.getY(), color);
                }
            }
        }
    }
}
