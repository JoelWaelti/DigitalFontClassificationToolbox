package ch.fhnw.ip5.digitalfontclassification.analysis;

import ch.fhnw.ip5.digitalfontclassification.domain.*;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LineThicknessAnalyzer {
    public static List<Double> computeThicknessAlongPathAtMiddleOfSegments(Glyph glyph) {
        return computeThicknessLinesAlongPathAtMiddleOfSegments(glyph).stream()
                .map(Line::getLength).collect(Collectors.toList());
    }

    public static List<Line> computeThicknessLinesAlongPathAtMiddleOfSegments(Glyph glyph) {
        ArrayList<Line> thicknessLines = new ArrayList<>();

        // It's assumed that this first contour is the main enclosing contour of the glyph
        Contour contour = glyph.getContours().getFirst();

        for(Segment s : contour.getSegments()) {
            if(!(s instanceof Line line)) {
                throw new IllegalArgumentException("Contours of the glyph can only contain lines for this operation. Consider flattening the entire glyph.");
            }

            Line thicknessLine = thicknessLineAtMiddleOfSegment(line, glyph);
            if(thicknessLine != null) {
                thicknessLines.add(thicknessLine);
            }
        }

        return thicknessLines;
    }

    private static Line thicknessLineAtMiddleOfSegment(Line line, Glyph glyph) {
        // Calculate point at the middle of the line
        double centerX = (line.getFrom().x() + line.getTo().x()) / 2;
        double centerY = (line.getFrom().y() + line.getTo().y()) / 2;
        Point center = new Point(centerX, centerY);

        // Define a length for the perpendicular line, so that it is certainly long enough to cross the entire glyph
        double length = glyph.getBoundingBox().getHeight() + glyph.getBoundingBox().getWidth();

        Line perpendicularLine = line.getPerpendicularLine(center, length);

        // find nearest intersection of perpendicular line with any other segment of the glyph
        double distanceToNearestIntersection = Double.MAX_VALUE;
        Point nearestIntersection = null;
        for(Contour contour : glyph.getContours()) {
            for(Segment s : contour.getSegments()) {
                if(s == line) {
                    continue;
                }

                if(!(s instanceof Line)) {
                    throw new IllegalArgumentException("Contours of the glyph can only contain lines for this operation. Consider flattening the entire glyph.");
                }

                Point intersection = getLineLineIntersection(perpendicularLine, (Line) s);
                if(intersection != null) {
                    double dist = intersection.distanceTo(center);
                    if(dist < distanceToNearestIntersection) {
                        distanceToNearestIntersection = dist;
                        nearestIntersection = intersection;
                    }
                }
            }
        }

        if(nearestIntersection == null) {
            return null;
        }

        return new Line(center, nearestIntersection);
    }

    private static Point getLineLineIntersection(Line line1, Line line2) {
        // use awt lines to calculate intersection
        Line2D awtLine1 = new Line2D.Double(line1.getFrom().x(), line1.getFrom().y(), line1.getTo().x(), line1.getTo().y());
        Line2D awtLine2 = new Line2D.Double(line2.getFrom().x(), line2.getFrom().y(), line2.getTo().x(), line2.getTo().y());

        if(awtLine1.intersectsLine(awtLine2)) {
            double x1 = line1.getFrom().x();
            double y1 = line1.getFrom().y();
            double x2 = line1.getTo().x();
            double y2 = line1.getTo().y();

            double x3 = line2.getFrom().x();
            double y3 = line2.getFrom().y();
            double x4 = line2.getTo().x();
            double y4 = line2.getTo().y();

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
