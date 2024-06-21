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

    private final Glyph glyph;

    private List<Line> hairlineThicknessLines;
    private List<Line> serifThicknessLines;
    private List<Line> stemThicknessLines;
    private List<Line> hairlineLines;
    private List<List<Line>> serifs;
    private double averageHairlineThickness;
    private double averageSerifThickness;
    private double averageStemThickness;

    public SerifThicknessAnalyzer(Glyph glyph) {
        this.glyph = glyph;
        init();
    }

    private void init() {
        SerifExtractor serifExtractor = new SerifExtractor(glyph, SERIF_HEIGHT_THRESHOLD);
        serifs = serifExtractor.getAllSerifs();

        ThicknessAnalyzer.ThicknessLineFilter<Line, Line, Line> filter = (line, intersectingLine, thicknessLine) -> {
            double angle = thicknessLine.angleTo(intersectingLine);
            return angle > LINE_PARALLELISM_THRESHOLD;
        };
        ThicknessAnalyzer thicknessAnalyzer = new EvenlyDistributedThicknessAnalyzer(THICKNESS_LINE_SPACING, filter);

        double summedSerifHeight = 0;
        double numSerifThicknessLines = 0;
        serifThicknessLines = new ArrayList<>();
        stemThicknessLines = new ArrayList<>();
        for(List<Line> s : serifs) {
            List<Line> verticalThicknessLines = thicknessAnalyzer
                    .computeThicknessLines(s, s)
                    .stream()
                    .filter(Line::isVerticalish)
                    .toList();
            serifThicknessLines.addAll(verticalThicknessLines);

            List<Line> currentStemLines = new ArrayList<>();
            currentStemLines.add(s.getFirst());
            currentStemLines.add(s.getLast());
            stemThicknessLines.addAll(thicknessAnalyzer.computeThicknessLines(currentStemLines, currentStemLines));

            summedSerifHeight += verticalThicknessLines.stream().mapToDouble(Line::getLength).sum();
            numSerifThicknessLines += verticalThicknessLines.size();
        }
        averageSerifThickness = summedSerifHeight / numSerifThicknessLines;

        // get all lines of glyph that are not from the serif
        hairlineLines = glyph.getContours().getFirst().getSegments().stream()
                .filter(segment -> serifs.stream().noneMatch(serif -> serif.contains(segment)))
                .map(s -> (Line)s)
                .toList();

        hairlineThicknessLines = thicknessAnalyzer
                .computeThicknessLines(hairlineLines, hairlineLines)
                .stream()
                .filter(Line::isVerticalish)
                .toList();
        averageHairlineThickness = hairlineThicknessLines.stream()
                .mapToDouble(Line::getLength)
                .average()
                .getAsDouble();

        averageStemThickness = stemThicknessLines.stream()
                .mapToDouble(Line::getLength)
                .average()
                .getAsDouble();
    }

    public boolean serifThicknessIsSmallerThanHairLineThickness() {
        return averageSerifThickness + SERIF_HAIRLINE_DIFFERENCE_THRESHOLD * averageHairlineThickness < averageHairlineThickness;
    }

    public double getSerifThicknessToStemThicknessRatio() {
        return averageSerifThickness / averageStemThickness;
    }

    public List<Line> getFilteredLines(Glyph glyph) {
        ThicknessAnalyzer analyzer = getThicknessAnalyzer();
        List<Line> lines = analyzer.computeThicknessLines(glyph);

        // filter verticalish lines and filter out lines from stem
        return lines.stream()
                .filter(Line::isVerticalish)
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

    private boolean isFromStem(Line line, BoundingBox bb) {
        return line.getLength() > bb.getHeight() / 4;
    }

    public List<Line> getHairlineThicknessLines() {
        return hairlineThicknessLines;
    }

    public List<Line> getSerifThicknessLines() {
        return serifThicknessLines;
    }
}
