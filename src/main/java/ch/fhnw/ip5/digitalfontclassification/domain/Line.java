package ch.fhnw.ip5.digitalfontclassification.domain;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class Line extends Segment {

    public Line(Point from, Point to) {
        super(from, to);
    }

    /**
     * Finds the point of the intersection with the given line
     *
     * @param line the line with which to find the intersection
     * @return the {@code Point} where the two lines intersect
     *          or {@code null} if the lines either don't intersect or are coincident
     */
    public Point getIntersection(Line line) {
        // This line
        double x1 = getFrom().getX();
        double y1 = getFrom().getY();
        double x2 = getTo().getX();
        double y2 = getTo().getY();

        // Line to intersect with
        double x3 = line.getFrom().getX();
        double y3 = line.getFrom().getY();
        double x4 = line.getTo().getX();
        double y4 = line.getTo().getY();

        // Denominator for the equations of the lines
        double denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

        if (denom == 0) {
            return null; // Lines are parallel or coincident
        }

        // Intersection point
        double intersectX = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / denom;
        double intersectY = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / denom;

        return new Point(intersectX, intersectY);
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
