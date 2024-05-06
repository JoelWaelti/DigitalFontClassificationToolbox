package ch.fhnw.ip5.digitalfontclassification;

public class Point {
    private double x;
    private double y;

    public Point() {
        this.x = 0.0f;
        this.y = 0.0f;
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setX(double x) {
        this.x = x;
    }
    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
