package ch.fhnw.ip5.digitalfontclassification.demos;
import ch.fhnw.ip5.digitalfontclassification.analysis.SlopeAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.analysis.thickness.MiddleOfLineThicknessAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.domain.Contour;
import ch.fhnw.ip5.digitalfontclassification.domain.Flattener;
import ch.fhnw.ip5.digitalfontclassification.domain.FontParser;
import ch.fhnw.ip5.digitalfontclassification.domain.FontParserException;
import ch.fhnw.ip5.digitalfontclassification.domain.Glyph;
import ch.fhnw.ip5.digitalfontclassification.domain.JavaAwtFlattener;
import ch.fhnw.ip5.digitalfontclassification.domain.JavaAwtFontParser;
import ch.fhnw.ip5.digitalfontclassification.domain.Line;
import ch.fhnw.ip5.digitalfontclassification.domain.Point;
import ch.fhnw.ip5.digitalfontclassification.domain.Segment;
import ch.fhnw.ip5.digitalfontclassification.plot.PlotUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FeatureExtractor {

    // Args: <source path> <target path> <character> <font size> <flatness> <spacing>
    public static void main(String[] args) throws IOException {
        String sourcePath = args[0];
        String targetPath = args[1];
        char character = args[2].charAt(0);
        float fontSize = Float.parseFloat(args[3]);
        double flatness = Double.parseDouble(args[4]);
        int maxLineCount = findMaxLineCount(sourcePath, character, fontSize, flatness);

        System.out.println(maxLineCount);

        // Check if the file exists and write header only if it doesn't exist
        boolean fileExists = Files.exists(Path.of(targetPath));

        try (FileWriter writer = new FileWriter(targetPath, true)) {
            if (!fileExists) {
                // Write header
                writer.append("Directory,FontName");
                for (int i = 0; i < maxLineCount; i++) {
                    writer.append(",Length").append(String.valueOf(i)).append(",Thickness").append(String.valueOf(i))
                        .append(",Slope").append(String.valueOf(i));
                }
                writer.append('\n');
            }

            PlotUtil.doForEachFontInDirectory(sourcePath, fontPath -> {
                try {
                    FontParser parser = new JavaAwtFontParser(fontPath.toString());
                    Glyph glyph = parser.getGlyph(character, fontSize);
                    Flattener flattener = new JavaAwtFlattener(flatness);
                    Glyph flattenedGlyph = flattener.flatten(glyph);

                    List<Double> thicknesses = new MiddleOfLineThicknessAnalyzer().computeThicknessesAsList(flattenedGlyph);
                    // shift thicknesses to start with the segment closest to point (0,0)
                    Point origin = new Point(0,0);
                    List<Segment> lineSegments = flattenedGlyph.getContours().getFirst().getSegments();
                    Segment closestSegment = Collections.min(
                        lineSegments,
                        (s1, s2) -> (int) (s1.getFrom().distanceTo(origin) - s2.getFrom().distanceTo(origin))
                    );
                    int closestSegmentIndex = lineSegments.indexOf(closestSegment);
                    Collections.rotate(thicknesses, -closestSegmentIndex);

                    lineSegments = flattenedGlyph.getContours().getFirst().moveStartPointToSegmentClosestTo(origin).getSegments();

                    Contour contour = flattenedGlyph.getContours().getFirst().moveStartPointToSegmentClosestTo(origin);
                    double[] slopes = SlopeAnalyzer.getSlopesAlongContour(contour);

                    String directoryName = fontPath.getParent().getFileName().toString();
                    String fontName = Paths.get(fontPath.toString()).getFileName().toString();

                    writeFeaturesToCsv(lineSegments, thicknesses, slopes, writer, directoryName, fontName, maxLineCount);
                } catch (IOException | FontParserException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static void writeFeaturesToCsv(List<Segment> lines, List<Double> thicknesses, double[] slopes, FileWriter writer, String directoryName, String fontName, int maxLineCount) throws IOException {
        System.out.println(directoryName + ": " + fontName);
        // Writing the directory name and font name
        writer.append(directoryName)
            .append(',')
            .append(fontName);

        int numSegments = lines.size();

        for (int i = 0; i < maxLineCount; i++) {
            if (i < numSegments) {
                // Interleave thicknesses and slopes
                writer.append(',')
                    .append(Double.toString(((Line) lines.get(i)).getLength()))
                    .append(',')
                    .append(thicknesses.get(i).toString())
                    .append(',')
                    .append(Double.toString(slopes[i]));
            } else {
                writer.append(',')
                    .append("-1")
                    .append(',')
                    .append("-1")
                    .append(',')
                    .append("0");
            }
        }

        writer.append('\n');
    }

    private static int findMaxLineCount(String sourcePath, char character, float fontSize, double flatness)
        throws IOException {
        AtomicInteger maxSlopeCount = new AtomicInteger(0);


        PlotUtil.doForEachFontInDirectory(sourcePath, fontPath -> {
            try {
                FontParser parser = new JavaAwtFontParser(fontPath.toString());
                Glyph glyph = parser.getGlyph(character, fontSize);
                Flattener flattener = new JavaAwtFlattener(flatness);
                Glyph flattenedGlyph = flattener.flatten(glyph);

                int size = flattenedGlyph.getContours().getFirst().getSegments().size();

                if (size > maxSlopeCount.get()) {
                    maxSlopeCount.set(size);
                }
            } catch (FontParserException e) {
                e.printStackTrace();
            }
        });

        return maxSlopeCount.get() ;
    }
}
