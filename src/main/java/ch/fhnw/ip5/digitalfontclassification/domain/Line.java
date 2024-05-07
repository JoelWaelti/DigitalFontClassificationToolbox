package ch.fhnw.ip5.digitalfontclassification.domain;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class Line extends Segment {

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
        double vectorX = getTo().getX() - getFrom().getX();
        double vectorY = getTo().getY() - getFrom().getY();

        // normalize vector
        double originalLength = Math.sqrt(vectorX*vectorX + vectorY*vectorY);
        double normalizedVectorX = vectorX / originalLength;
        double normalizedVectorY = vectorY / originalLength;

        // rotate the vector 90 degrees counter-clockwise
        double rotatedVectorX = -normalizedVectorY;
        double rotatedVectorY = normalizedVectorX;

        Point to = new Point(rotatedVectorX * length + from.getX(), rotatedVectorY * length + from.getY());

        return new Line(from, to);
    }
}
