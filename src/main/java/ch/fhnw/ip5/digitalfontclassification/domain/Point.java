package ch.fhnw.ip5.digitalfontclassification.domain;

public class Point {
    private final double x;
    private final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }

    public double distanceTo(Point p) {
        double deltaX = getX() - p.getX();
        double deltaY = getY() - p.getY();
        return Math.sqrt(deltaX*deltaX + deltaY*deltaY);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
