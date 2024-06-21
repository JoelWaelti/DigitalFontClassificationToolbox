package ch.fhnw.ip5.digitalfontclassification.domain;

public record Vector(double x, double y) {
    public double dot(Vector vec) {
        return this.x * vec.x + this.y * vec.y;
    }
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }
}
