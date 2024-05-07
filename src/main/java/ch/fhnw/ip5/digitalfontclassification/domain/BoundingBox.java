package ch.fhnw.ip5.digitalfontclassification.domain;

public class BoundingBox {
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    public BoundingBox(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }
}
