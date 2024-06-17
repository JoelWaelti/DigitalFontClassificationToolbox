package ch.fhnw.ip5.digitalfontclassification.demos;

import ch.fhnw.ip5.digitalfontclassification.analysis.thickness.EvenlyDistributedThicknessAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.analysis.thickness.ThicknessAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.domain.Contour;
import ch.fhnw.ip5.digitalfontclassification.domain.CubicBezierCurve;
import ch.fhnw.ip5.digitalfontclassification.domain.Flattener;
import ch.fhnw.ip5.digitalfontclassification.domain.FontParser;
import ch.fhnw.ip5.digitalfontclassification.domain.Glyph;
import ch.fhnw.ip5.digitalfontclassification.domain.JavaAwtFlattener;
import ch.fhnw.ip5.digitalfontclassification.domain.JavaAwtFontParser;
import ch.fhnw.ip5.digitalfontclassification.domain.Line;

import ch.fhnw.ip5.digitalfontclassification.domain.Segment;
import ch.fhnw.ip5.digitalfontclassification.domain.Vector;
import ch.fhnw.ip5.digitalfontclassification.plot.PlotUtil;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import static ch.fhnw.ip5.digitalfontclassification.analysis.SerifAnalyzer.getEndOfLine;
import static ch.fhnw.ip5.digitalfontclassification.analysis.SerifAnalyzer.getShortHorizontalEndOfLineThicknesses;
import static ch.fhnw.ip5.digitalfontclassification.analysis.SerifAnalyzer.getShortVerticalEndOfLines;
import static ch.fhnw.ip5.digitalfontclassification.demos.HorizontalThicknessVisualization.getVisualizationAsBufferedImage;


public class SerifSort {

    // Args: <source path> <target path1> <target path2> <character> <font size> <flatness> <spacing>
    public static void main(String[] args) throws IOException {
            String sourcePath = args[0];
            String targetPath1 = args[1];
            String targetPath2 = args[2];
            char character = args[3].charAt(0);
            float fontSize = Float.parseFloat(args[4]);
            double flatness = Double.parseDouble(args[5]);
            double spacing = Double.parseDouble(args[6]);

            PlotUtil.doForEachFontInDirectory(sourcePath, fontPath -> {
                try {
                    System.out.println(fontPath);

                    FontParser parser = new JavaAwtFontParser(fontPath.toString());
                    Glyph glyph = parser.getGlyph(character, fontSize);
                    Flattener flattener = new JavaAwtFlattener(flatness);
                    Glyph flattenedGlyph = flattener.flatten(glyph);

                    ThicknessAnalyzer.ThicknessLineFilter<Line, Line, Line> filter = (line, intersectingLine, thicknessLine) -> {
                        double angle = thicknessLine.angleTo(intersectingLine);
                        return angle > 30;
                    };

                    List<Line> lines = new EvenlyDistributedThicknessAnalyzer(spacing, filter).computeThicknessLines(flattenedGlyph);
                    double width = flattenedGlyph.getBoundingBox().getWidth();
                    double height = flattenedGlyph.getBoundingBox().getHeight();
                    BufferedImage bufferedImage = getVisualizationAsBufferedImage(flattenedGlyph, spacing);

                    if(hasSerif(glyph, lines, width, height)) {
                        Path relativePath = Path.of(sourcePath).relativize(fontPath);
                        Path plotFilePath = Path.of(targetPath1, relativePath + ".jpg");
                        File plotFile = plotFilePath.toFile();
                        if (!Files.exists(plotFilePath.getParent())) {
                            Files.createDirectories(plotFilePath.getParent());
                        }

                        ImageIO.write(bufferedImage, "PNG", plotFile);
                    } else {
                        Path relativePath = Path.of(sourcePath).relativize(fontPath);
                        Path plotFilePath = Path.of(targetPath2, relativePath + ".jpg");
                        File plotFile = plotFilePath.toFile();
                        if (!Files.exists(plotFilePath.getParent())) {
                            Files.createDirectories(plotFilePath.getParent());
                        }

                        ImageIO.write(bufferedImage, "PNG", plotFile);
                    }

                }  catch (Exception e) {
                    System.err.println("Error processing " + fontPath + ": " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }

    public static boolean hasSerif(Glyph glyph, List<Line> lines, double width, double height) {
        List<Line> verticalThicknesses = getShortVerticalEndOfLines(lines, width, height);
        double[] horizontalThicknesses = getShortHorizontalEndOfLineThicknesses(lines, width, height);
        double stdDeviation = calculateStandardDeviation(horizontalThicknesses);
        System.out.println(stdDeviation);

        if (verticalThicknesses.size() > 5) {
            int curves = hasCurveAtEndLines(glyph);
            if(16 > curves && curves > 0) {
                return !(stdDeviation < 10);
            }
            else return true;
        } else return false;
    }

    private static int hasCurveAtEndLines(Glyph glyph) {
        List<Contour> contours = new ArrayList<>(glyph.getContours());

        Contour c = contours.getFirst();
        int count = 0;

        for (Segment s : c.getSegments()) {
            // only from point --> anpassen
            boolean isEndOfLine = getEndOfLine(s, glyph.getBoundingBox().getWidth(), glyph.getBoundingBox().getHeight());
            if (isEndOfLine && s instanceof CubicBezierCurve) {
                count += 1;
            }
        }

        System.out.println("CurveAtEndLines: " + count);
        return count;
    }



    private static double calculateSlope(Line line) {
        Vector v = line.toVector();
        return v.x() == 0 ? Double.POSITIVE_INFINITY : v.y() / v.x();
    }
    private static double calculateStandardDeviation(double[] values) {
        double mean = Arrays.stream(values).average().orElse(0);
        double variance = Arrays.stream(values)
            .map(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0);
        return Math.sqrt(variance);
    }

}
