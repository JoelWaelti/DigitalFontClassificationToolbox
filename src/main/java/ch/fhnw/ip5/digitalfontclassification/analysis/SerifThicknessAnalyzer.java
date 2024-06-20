package ch.fhnw.ip5.digitalfontclassification.analysis;

import ch.fhnw.ip5.digitalfontclassification.analysis.thickness.EvenlyDistributedThicknessAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.analysis.thickness.ThicknessAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.domain.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class SerifThicknessAnalyzer {

    private static final int THICKNESS_LINE_SPACING = 5;
    private static final double LINE_PARALLELISM_THRESHOLD = 45;
    private static final double SERIF_HEIGHT_THRESHOLD = 0.2;
    private static final double SERIF_HAIRLINE_DIFFERENCE_THRESHOLD = 0.5;

    public List<Line> hairlineThicknessLines = null;
    public List<Line> serifThicknessLines = null;

    // implementation assumes: - hairline thickness >= serif thickness
    //                         - first contour is main enclosing contour
    public boolean serifThicknessIsSmallerThanHairLineThickness(Glyph glyph) {
        SerifExtractor serifExtractor = new SerifExtractor(glyph, SERIF_HEIGHT_THRESHOLD);
        List<List<Line>> serifs = serifExtractor.getAllSerifs();

        ThicknessAnalyzer.ThicknessLineFilter<Line, Line, Line> filter = (line, intersectingLine, thicknessLine) -> {
            double angle = thicknessLine.angleTo(intersectingLine);
            return angle > LINE_PARALLELISM_THRESHOLD;
        };
        ThicknessAnalyzer thicknessAnalyzer = new EvenlyDistributedThicknessAnalyzer(THICKNESS_LINE_SPACING, filter);

        double summedSerifHeight = 0;
        double numSerifThicknessLines = 0;
        serifThicknessLines = new ArrayList<>();
        for(List<Line> s : serifs) {
            List<Line> verticalThicknessLines = thicknessAnalyzer
                    .computeThicknessLines(s, s)
                    .stream()
                    .filter(this::isVerticalish)
                    .toList();
            serifThicknessLines.addAll(verticalThicknessLines);
            summedSerifHeight += verticalThicknessLines.stream().mapToDouble(Line::getLength).sum();
            numSerifThicknessLines += verticalThicknessLines.size();
        }
        double averageSerifThickness = summedSerifHeight / numSerifThicknessLines;

        // get all lines of glyph that are not from the serif
        List<Line> hairlineLines = glyph.getContours().getFirst().getSegments().stream()
                .filter(segment -> serifs.stream().noneMatch(serif -> serif.contains(segment)))
                .map(s -> (Line)s)
                .toList();

        hairlineThicknessLines = thicknessAnalyzer
                .computeThicknessLines(hairlineLines, hairlineLines)
                .stream()
                .filter(this::isVerticalish)
                .toList();
        double averageHairlineThickness = hairlineThicknessLines.stream()
                .mapToDouble(Line::getLength)
                .average()
                .getAsDouble();

        return averageSerifThickness + SERIF_HAIRLINE_DIFFERENCE_THRESHOLD * averageHairlineThickness < averageHairlineThickness;
    }

    public List<Line> getFilteredLines(Glyph glyph) {
        ThicknessAnalyzer analyzer = getThicknessAnalyzer();
        List<Line> lines = analyzer.computeThicknessLines(glyph);

        // filter verticalish lines and filter out lines from stem
        return lines.stream()
                .filter(this::isVerticalish)
                .filter(l -> !isFromStem(l, glyph.getBoundingBox()))
                .toList();
    }

    private ThicknessAnalyzer getThicknessAnalyzer() {
        // remove thickness line, if it is relatively parallel to the segment it is intersecting
        ThicknessAnalyzer.ThicknessLineFilter<Line, Line, Line> filter = (line, intersectingLine, thicknessLine) -> {
            double angle = thicknessLine.angleTo(intersectingLine);
            return angle > LINE_PARALLELISM_THRESHOLD;
        };

        return new EvenlyDistributedThicknessAnalyzer(THICKNESS_LINE_SPACING, filter);
    }

    private boolean isVerticalish(Line line) {
        Point from = line.getFrom();
        Point to = line.getTo();

        double deltaX = Math.abs(to.x() - from.x());
        double deltaY = Math.abs(to.y() - from.y());

        return deltaY >= deltaX;
    }

    private boolean isFromStem(Line line, BoundingBox bb) {
        return line.getLength() > bb.getHeight() / 4;
    }

    // calculates the counter-clockwise angle of the vector to the x-axis
    private double ccwAngleWithXAxis(Vector v) {
        double angleRad = Math.atan2(v.y(), v.x());
        // Adjust angle to be in the range [0, 2π)
        if (angleRad < 0) {
            angleRad += 2 * Math.PI;
        }
        return Math.toDegrees(angleRad);
    }

    private double findMedian(double[] numbers) {
        Arrays.sort(numbers); // Sort the array

        int length = numbers.length;
        if (length % 2 == 0) {
            // If the length is even, return the average of the two middle elements
            return (numbers[length / 2 - 1] + numbers[length / 2]) / 2.0;
        } else {
            // If the length is odd, return the middle element
            return numbers[length / 2];
        }
    }
}
