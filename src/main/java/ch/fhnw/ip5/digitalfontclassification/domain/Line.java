package ch.fhnw.ip5.digitalfontclassification.domain;

import static ch.fhnw.ip5.digitalfontclassification.analysis.ContourDirectionAnalyzer.ccwAngleWithXAxis;

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

    public Line getHorizontalLine(Point from, double length) {
        double ccangle = ccwAngleWithXAxis(this.toVector());
        Point to = null;

        if(ccangle > 180) {
            to = new Point(from.x() + length, from.y());
        } else  to = new Point(from.x() - length, from.y());

        return new Line(from, to);
    }


    public Point interpolate(double t) {
        double x = this.getFrom().x() + t * (this.getTo().x() - this.getFrom().x());
        double y = this.getFrom().y() + t * (this.getTo().y() - this.getFrom().y());
        return new Point(x, y);
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

    public double angleTo(Line line) {
        Vector vecThicknessLine = this.toVector();
        Vector vecIntersectingLine = line.toVector();

        double dotProduct = vecThicknessLine.dot(vecIntersectingLine);
        double magThicknessLine = vecThicknessLine.magnitude();
        double magIntersectingLine = vecIntersectingLine.magnitude();

        double cosTheta = dotProduct / (magThicknessLine * magIntersectingLine);

        // Ensure the value is within the valid range for acos
        cosTheta = Math.max(-1.0, Math.min(1.0, cosTheta));

        double angleRadians = Math.acos(cosTheta);
        double angleDegrees = Math.abs(Math.toDegrees(angleRadians));

        return Math.min(angleDegrees, 180 - angleDegrees);
    }
    public boolean isHorizontal() {
        return getFrom().y() == getTo().y();
    }
}
