package ch.fhnw.ip5.digitalfontclassification.analysis.thickness;

import ch.fhnw.ip5.digitalfontclassification.domain.*;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ThicknessAnalyzer {
    private ThicknessLineFilter<Line, Line, Line> filter = null;

    ThicknessAnalyzer() {}

    ThicknessAnalyzer(ThicknessLineFilter<Line, Line, Line> filter) {
        this.filter = filter;
    }

    @FunctionalInterface
    public interface ThicknessLineFilter<Segment, Line1, Line2> {
        boolean apply(Segment t, Line1 u, Line2 v);
    }

    public abstract List<Line> computeThicknessLines(List<Line> linesToGetThicknessLinesOf, List<Line> allLines);

    public List<Line> computeThicknessLines(Glyph glyph) {
        // It's assumed that this first contour is the main enclosing contour of the glyph
        List<Line> linesToGetThicknessLinesOf = glyph.getContours()
                .getFirst()
                .getSegments()
                .stream()
                .map(s -> (Line)s)
                .toList();
        List<Line> allGlyphLines = getAllLinesFromGlyph(glyph);

        return computeThicknessLines(linesToGetThicknessLinesOf, allGlyphLines);
    }

    public List<Double> computeThicknessesAsList(Glyph glyph) {
        return computeThicknessLines(glyph).stream()
                .map(Line::getLength).collect(Collectors.toList());
    }

    public double[] computeThicknesses(Glyph glyph) {
        return computeThicknessLines(glyph).stream()
                .mapToDouble(Line::getLength).toArray();
    }

    protected List<Line> getAllLinesFromGlyph(Glyph glyph) {
        List<Line> linesOfGlyph = new ArrayList<>();

        for (Contour contour : glyph.getContours()) {
            for (Segment segment : contour.getSegments()) {
                if (segment instanceof Line) {
                    linesOfGlyph.add((Line) segment);
                } else {
                    throw new ClassCastException("Glyph can only contain lines");
                }
            }
        }

        return linesOfGlyph;
    }

    protected Line thicknessLineAtPointOfLine(Line line, Point p, List<Line> otherLines) {
        // Define a length for the perpendicular line, so that it is certainly long enough to cross the entire shape
        double length = Integer.MAX_VALUE;

        Line perpendicularLine = line.getPerpendicularLine(p, length);

        // find nearest intersection of perpendicular line with any other segment of the glyph
        double distanceToNearestIntersection = Double.MAX_VALUE;
        Point nearestIntersection = null;
        Line intersectingLine = null;
        for(Line possiblyIntersectingLine : otherLines) {
            if(possiblyIntersectingLine == line) {
                continue;
            }

            Point intersection = getLineLineIntersection(perpendicularLine, possiblyIntersectingLine);
            if(intersection != null) {
                double dist = intersection.distanceTo(p);
                if(dist < distanceToNearestIntersection) {
                    distanceToNearestIntersection = dist;
                    nearestIntersection = intersection;
                    intersectingLine = possiblyIntersectingLine;
                }
            }
        }

        if(nearestIntersection == null) {
            return null;
        }

        Line thicknessLine = new Line(p, nearestIntersection);

        if(filter != null && !filter.apply(line, intersectingLine, thicknessLine)) {
            return null;
        }

        return thicknessLine;
    }

    protected Point getLineLineIntersection(Line line1, Line line2) {
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
