package ch.fhnw.ip5.digitalfontclassification.analysis;

import ch.fhnw.ip5.digitalfontclassification.domain.*;

import java.awt.geom.Line2D;
import java.util.ArrayList;

public class LineThicknessAnalyzer {
    public static ArrayList<Double> computeThicknessAlongPathAtMiddleOfSegments(Glyph glyph) {
        ArrayList<Double> thicknesses = new ArrayList<>();

        // It's assumed that this first contour is the main enclosing contour of the glyph
        Contour contour = glyph.getContours().getFirst();

        for(Segment s : contour.getSegments()) {
            if(!(s instanceof Line line)) {
                throw new IllegalArgumentException("Contours of the glyph can only contain lines for this operation. Consider flattening the entire glyph.");
            }

            double thickness = thicknessAtMiddleOfSegment(line, glyph);
            thicknesses.add(thickness);
        }

        return thicknesses;
    }

    private static double thicknessAtMiddleOfSegment(Line line, Glyph glyph) {
        // Calculate point at the middle of the line
        double centerX = (line.getFrom().getX() + line.getTo().getX()) / 2;
        double centerY = (line.getFrom().getY() + line.getTo().getY()) / 2;
        Point center = new Point(centerX, centerY);

        // Define a length for the perpendicular line, so that it is certainly long enough to cross the entire glyph
        double length = glyph.getBoundingBox().getHeight() + glyph.getBoundingBox().getWidth();

        Line perpendicularLine = line.getPerpendicularLine(center, length);

        // find nearest intersection of perpendicular line with any other segment of the glyph
        double distanceToNearestIntersection = Double.MAX_VALUE;
        for(Contour contour : glyph.getContours()) {
            for(Segment s : contour.getSegments()) {
                if(s == line) {
                    continue;
                }

                if(!(s instanceof Line l)) {
                    throw new IllegalArgumentException("Contours of the glyph can only contain lines for this operation. Consider flattening the entire glyph.");
                }

                Point intersection = getLineLineIntersection(line, l);
                if(intersection != null) {
                    double dist = intersection.distanceTo(center);
                    if(dist < distanceToNearestIntersection) {
                        distanceToNearestIntersection = dist;
                    }
                }
            }
        }

        if(distanceToNearestIntersection == Double.MAX_VALUE) {
            distanceToNearestIntersection = -1;
        }

        return distanceToNearestIntersection;
    }

    private static Point getLineLineIntersection(Line line1, Line line2) {
        // use awt lines to calculate intersection
        Line2D awtLine1 = new Line2D.Double(line1.getFrom().getX(), line1.getFrom().getY(), line1.getTo().getX(), line1.getTo().getY());
        Line2D awtLine2 = new Line2D.Double(line2.getFrom().getX(), line2.getFrom().getY(), line2.getTo().getX(), line2.getTo().getY());

        if(awtLine1.intersectsLine(awtLine2)) {
            double x1 = line1.getFrom().getX();
            double y1 = line1.getFrom().getY();
            double x2 = line1.getTo().getX();
            double y2 = line1.getTo().getY();

            double x3 = line2.getFrom().getX();
            double y3 = line2.getFrom().getY();
            double x4 = line2.getTo().getX();
            double y4 = line2.getTo().getY();

            // Denominator for the equations of the lines
            double denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

            // Intersection point
            double intersectX = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / denom;
            double intersectY = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / denom;

            return new Point(intersectX, intersectY);
        }

        return null;
    }
}
