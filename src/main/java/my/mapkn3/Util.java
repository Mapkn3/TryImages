package my.mapkn3;

import my.mapkn3.model.Point3D;

public class Util {

    public static Point3D plus(Point3D a, Point3D b) {
        return new Point3D(a.getX() + b.getX(), a.getY() + b.getY(), a.getZ() + b.getZ());
    }

    public static Point3D minus(Point3D a, Point3D b) {
        return new Point3D(a.getX() - b.getX(), a.getY() - b.getY(), a.getZ() - b.getZ());
    }

    public static Point3D multiply(Point3D a, double value) {
        return new Point3D(a.getX() * value, a.getY() * value, a.getZ() * value);
    }

    public static double norm(Point3D a) {
        return Math.sqrt(a.getX() * a.getX() + a.getY() * a.getY() + a.getZ() * a.getZ());
    }

    public static Point3D normalize(Point3D a) {
        double norm = norm(a);
        return new Point3D(a.getX() / norm, a.getY() / norm, a.getZ() / norm);
    }

    public static double dotProduct(Point3D a, Point3D b) {
        return a.getX() * b.getX() + a.getY() * b.getY() + a.getZ() * b.getZ();
    }
    public static Point3D crossProduct(Point3D a, Point3D b) {
        return new Point3D(
                (a.getY() * b.getZ() - a.getZ() * b.getY()),
                (a.getX() * b.getZ() - a.getZ() * b.getX()),
                (a.getX() * b.getY() - a.getY() * b.getX())
        );
    }
}
