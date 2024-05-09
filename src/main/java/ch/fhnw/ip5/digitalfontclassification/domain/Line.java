package ch.fhnw.ip5.digitalfontclassification.domain;

public class Line extends Segment {

    private double length = -1;

    public Line(Point from, Point to) {
        super(from, to);
    }

    /**
     * Returns a new line that is perpendicular to this line, starting at the point {@code from}
     * with the length {@code length}
     * @param from the point where the new perpendicular line should start
     * @param length the length of the new perpendicular line
     * @return the new perpendicular line
     */
    public Line getPerpendicularLine(Point from, double length) {
        // turn line into vector
        double vectorX = getTo().x() - getFrom().x();
        double vectorY = getTo().y() - getFrom().y();

        // normalize vector
        double originalLength = Math.sqrt(vectorX*vectorX + vectorY*vectorY);
        double normalizedVectorX = vectorX / originalLength;
        double normalizedVectorY = vectorY / originalLength;

        // rotate the vector 90 degrees counter-clockwise
        double rotatedVectorX = -normalizedVectorY;
        double rotatedVectorY = normalizedVectorX;

        Point to = new Point(rotatedVectorX * length + from.x(), rotatedVectorY * length + from.y());

        return new Line(from, to);
    }

    public double getLength() {
        if (length < 0) {
            length = this.getFrom().distanceTo(this.getTo());
        }

        return length;
    }

    public Vector toVector() {
        return new Vector(getTo().x() - getFrom().x(), getTo().y() - getFrom().y());
    }
}
