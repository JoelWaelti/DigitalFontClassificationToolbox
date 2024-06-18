package ch.fhnw.ip5.digitalfontclassification.analysis;

import ch.fhnw.ip5.digitalfontclassification.domain.Glyph;
import ch.fhnw.ip5.digitalfontclassification.domain.Line;
import ch.fhnw.ip5.digitalfontclassification.domain.Point;

import ch.fhnw.ip5.digitalfontclassification.domain.Segment;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SerifAnalyzer {

    private static boolean isVerticalish(Line line) {
        Point from = line.getFrom();
        Point to = line.getTo();

        double deltaX = Math.abs(to.x() - from.x());
        double deltaY = Math.abs(to.y() - from.y());

        return deltaY > deltaX;
    }
    public static List<Line> getVerticalLines(List<Line> lines) {
        return lines.stream()
            .filter(SerifAnalyzer::isVerticalish)
            .collect(Collectors.toList());
    }

    public static boolean hasSerif(List<Line> horizontalLines, List<Line> stammLinesHorizontal, List<Line> serifLines) {
        List<Line> verticalThicknesses = getVerticalLines(serifLines);

        double maxHorizontalThicknesses = horizontalLines.stream()
            .mapToDouble(Line::getLength)
            .max().orElse(Double.MIN_VALUE);

        double stammThickness = stammLinesHorizontal.stream()
            .mapToDouble(Line::getLength)
            .average().orElse(Double.MIN_VALUE);

        double range = maxHorizontalThicknesses - stammThickness;

        return range > 1.0 && !verticalThicknesses.isEmpty();
    }
}
