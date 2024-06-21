package ch.fhnw.ip5.digitalfontclassification.dataexport;

import ch.fhnw.ip5.digitalfontclassification.analysis.SerifAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.analysis.SerifExtractor;
import ch.fhnw.ip5.digitalfontclassification.analysis.thickness.EvenlyDistributedThicknessAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.analysis.thickness.ThicknessAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.domain.Flattener;
import ch.fhnw.ip5.digitalfontclassification.domain.FontParser;
import ch.fhnw.ip5.digitalfontclassification.domain.FontParserException;
import ch.fhnw.ip5.digitalfontclassification.domain.Glyph;
import ch.fhnw.ip5.digitalfontclassification.domain.JavaAwtFlattener;
import ch.fhnw.ip5.digitalfontclassification.domain.JavaAwtFontParser;
import ch.fhnw.ip5.digitalfontclassification.domain.Line;
import ch.fhnw.ip5.digitalfontclassification.domain.Point;
import ch.fhnw.ip5.digitalfontclassification.plot.PlotUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ch.fhnw.ip5.digitalfontclassification.analysis.ContourDirectionAnalyzer.ccwAngleWithXAxis;
import static ch.fhnw.ip5.digitalfontclassification.analysis.SerifAnalyzer.analyzeSerifs;
import static ch.fhnw.ip5.digitalfontclassification.analysis.SerifAnalyzer.getVerticalLines;

public class SerifFeatureExtractor {
    public static void main(String[] args) throws IOException {
        String sourcePath = args[0];
        String targetPath = args[1];
        char character = args[2].charAt(0);
        float fontSize = Float.parseFloat(args[3]);
        double flatness = Double.parseDouble(args[4]);
        double spacing = Double.parseDouble(args[5]);

        boolean fileExists = Files.exists(Path.of(targetPath));
        try (FileWriter writer = new FileWriter(targetPath, true)) {
            if (!fileExists) {
                // Write header
                writer.append("Directory,FontName,Serif,SerifThickness,MaxHorizontalSerif,StammThickness,HairlineThickness");
                writer.append('\n');
            }

            PlotUtil.doForEachFontInDirectory(sourcePath, fontPath -> {
                try {
                    System.out.println(fontPath);

                    FontParser parser = new JavaAwtFontParser(fontPath.toString());
                    Glyph glyph = parser.getGlyph(character, fontSize);
                    Flattener flattener = new JavaAwtFlattener(flatness);
                    Glyph flattenedGlyph = flattener.flatten(glyph);

                    SerifExtractor serifExtractor = new SerifExtractor(flattenedGlyph, 0.2);
                    List<List<Line>> serifs = serifExtractor.getAllSerifs();

                    Map<String, List<Line>> allSerifLines = analyzeSerifs(flattenedGlyph, 0.2, spacing);

                    List<Line> seriflinesHorizontal = allSerifLines.get("seriflinesHorizontal");
                    List<Line> stammlinesHorizontal = allSerifLines.get("stammlinesHorizontal");
                    List<Line> serifLines = allSerifLines.get("serifLines");

                    boolean hasSerif = SerifAnalyzer.hasSerif(seriflinesHorizontal, stammlinesHorizontal, serifLines);
                    List<Line> verticalThicknesses = getVerticalLines(serifLines);
                    double serifThickness = 0.0;

                    if(hasSerif) {
                        serifThickness = verticalThicknesses.stream()
                            .mapToDouble(Line::getLength)
                            .average().orElse(Double.MIN_VALUE);
                    }

                    double maxHorizontalThicknesses = seriflinesHorizontal.stream()
                        .mapToDouble(Line::getLength)
                        .max().orElse(Double.MIN_VALUE);

                    double stammThickness = stammlinesHorizontal.stream()
                        .mapToDouble(Line::getLength)
                        .average().orElse(Double.MIN_VALUE);


                    ThicknessAnalyzer.ThicknessLineFilter<Line, Line, Line> filter = (line, intersectingLine, thicknessLine) -> {
                        double angle = thicknessLine.angleTo(intersectingLine);
                        return angle > 45;
                    };
                    ThicknessAnalyzer thicknessAnalyzer = new EvenlyDistributedThicknessAnalyzer(5, filter);

                    // get all lines of glyph that are not from the serif
                    List<Line> hairlineLines = flattenedGlyph.getContours().getFirst().getSegments().stream()
                        .filter(segment -> serifs.stream().noneMatch(serif -> serif.contains(segment)))
                        .map(s -> (Line)s)
                        .toList();

                    List<Line> hairlineThicknessLines = thicknessAnalyzer
                        .computeThicknessLines(hairlineLines, hairlineLines)
                        .stream()
                        .filter(Line::isVerticalish)
                        .toList();

                    double averageHairlineThickness = hairlineThicknessLines.stream()
                        .mapToDouble(Line::getLength)
                        .average()
                        .getAsDouble();



                    String directoryName = fontPath.getParent().getFileName().toString();
                    String fontName = Paths.get(fontPath.toString()).getFileName().toString();

                    writeSerifFeaturesToCsv(hasSerif, serifThickness, maxHorizontalThicknesses, stammThickness, averageHairlineThickness, writer, directoryName, fontName);
                } catch (IOException | FontParserException e) {
                    e.printStackTrace();
                }
            });

        }

    }

    private boolean isVerticalish(Line line) {
        Point from = line.getFrom();
        Point to = line.getTo();

        double deltaX = Math.abs(to.x() - from.x());
        double deltaY = Math.abs(to.y() - from.y());

        return deltaY >= deltaX;
    }

    private static void writeSerifFeaturesToCsv(boolean hasSerif, double serifThickness, double maxHorizontalThickness, double stammThickness, double averageHailineThickness, FileWriter writer, String directoryName, String fontName) throws IOException {
        writer.append(directoryName)
            .append(',')
            .append(fontName)
            .append(',')
            .append(hasSerif ? "1" : "0")  // Conditional append for hasSerif
            .append(',')
            .append(Double.toString(serifThickness))
            .append(',')
            .append(Double.toString(maxHorizontalThickness))
            .append(',')
            .append(Double.toString(stammThickness))
            .append(',')
            .append(Double.toString(averageHailineThickness))
            .append('\n');
    }
}
