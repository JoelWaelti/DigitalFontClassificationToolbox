package ch.fhnw.ip5.digitalfontclassification.domain;

public record Point(double x, double y) {

    public double distanceTo(Point p) {
        double deltaX = x() - p.x();
        double deltaY = y() - p.y();
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Point point = (Point) obj;
        return Double.compare(point.x, x) == 0 && Double.compare(point.y, y) == 0;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
