package ch.fhnw.ip5.digitalfontclassification.analysis;

import ch.fhnw.ip5.digitalfontclassification.analysis.thickness.EvenlyDistributedThicknessAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.analysis.thickness.MiddleOfLineThicknessAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.analysis.thickness.ThicknessAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.domain.Glyph;
import ch.fhnw.ip5.digitalfontclassification.domain.Line;
import ch.fhnw.ip5.digitalfontclassification.domain.Point;

import ch.fhnw.ip5.digitalfontclassification.domain.Segment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.fhnw.ip5.digitalfontclassification.analysis.ContourDirectionAnalyzer.ccwAngleWithXAxis;

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

    public static boolean hasSerif(Glyph glyph, int spacing, double serifHeightThreshold) {
        SerifExtractor serifExtractor = new SerifExtractor(glyph, serifHeightThreshold);
        List<List<Line>> serifs = serifExtractor.getAllSerifs();

        List<List<Line>> serifsBottom = new ArrayList<>();
        List<Line>  bottomLeft = serifExtractor.getSerifAt(SerifExtractor.SerifLocation.BOTTOM_LEFT);
        List<Line>  bottomRight = serifExtractor.getSerifAt(SerifExtractor.SerifLocation.BOTTOM_RIGHT);
        serifsBottom.add(bottomLeft);
        serifsBottom.add(bottomRight);

        List<List<Line>> serifsTop = new ArrayList<>();
        List<Line>  topRight = serifExtractor.getSerifAt(SerifExtractor.SerifLocation.TOP_RIGHT);
        List<Line>  topLeft = serifExtractor.getSerifAt(SerifExtractor.SerifLocation.TOP_LEFT);
        serifsTop.add(topLeft);
        serifsTop.add(topRight);

        ThicknessAnalyzer.ThicknessLineFilter<Line, Line, Line> filterBottom = (line, intersectingLine, thicknessLine) -> {
            double ccangleLine = ccwAngleWithXAxis(line.toVector());
            double angle = thicknessLine.angleTo(intersectingLine);
            return angle > 45 && (ccangleLine >= 90 && 270 >= ccangleLine);
        };

        ThicknessAnalyzer.ThicknessLineFilter<Line, Line, Line> filterTop = (line, intersectingLine, thicknessLine) -> {
            double ccangleLine = ccwAngleWithXAxis(line.toVector());
            double angle = thicknessLine.angleTo(intersectingLine);
            return angle > 45 && !(ccangleLine >= 90 && 270 >= ccangleLine);
        };


        List<Line> serifLines = new ArrayList<>();
        List<Line> stammlinesHorizontal = new ArrayList<>();
        List<Line> seriflinesHorizontal = new ArrayList<>();

        for(List<Line> serif : serifs) {
            List<Line> thicknessLinesOfSerif = new ArrayList<>();

            if(serifsBottom.contains(serif)) {
                thicknessLinesOfSerif = new EvenlyDistributedThicknessAnalyzer(spacing, filterBottom).computeThicknessLines(serif, serif);
            }

            if(serifsTop.contains(serif)) {
                thicknessLinesOfSerif = new EvenlyDistributedThicknessAnalyzer(spacing, filterTop).computeThicknessLines(serif, serif);
            }

            serifLines.addAll(thicknessLinesOfSerif);

            if(serif.size() > 2) {
                List<Line> stamm = new ArrayList<>();
                List<Line> partOfSerif = new ArrayList<>();

                stamm.add(serif.get(0));
                for (int i = 1; i < serif.size() - 1; i++) {
                    partOfSerif.add(serif.get(i));
                }
                stamm.add(serif.get(serif.size() - 1));

                List<Line> thicknessLinesOfStamm = new EvenlyDistributedThicknessAnalyzer(spacing).computeHorizontalLines(stamm, stamm);
                List<Line> horizontalLinesOfSerif = new MiddleOfLineThicknessAnalyzer().computeHorizontalLines(partOfSerif, serif);

                seriflinesHorizontal.addAll(horizontalLinesOfSerif);
                stammlinesHorizontal.addAll(thicknessLinesOfStamm);
            }
        }

        return hasSerif(seriflinesHorizontal, stammlinesHorizontal, serifLines);
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
