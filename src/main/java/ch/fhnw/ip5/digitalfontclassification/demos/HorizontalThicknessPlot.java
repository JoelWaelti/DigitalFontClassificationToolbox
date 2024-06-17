package ch.fhnw.ip5.digitalfontclassification.demos;

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
import ch.fhnw.ip5.digitalfontclassification.domain.Vector;
import ch.fhnw.ip5.digitalfontclassification.plot.PlotUtil;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static ch.fhnw.ip5.digitalfontclassification.analysis.SerifAnalyzer.getShortHorizontalEndOfLineThicknesses;

public class HorizontalThicknessPlot {
    // Args: <source> <target> <character> <fontSize> <flatness> <spacing>
    public static void main(String[] args) throws IOException {
        String originPath = args[0];
        String targetPath = args[1];
        char character = args[2].charAt(0);
        float fontSize = Float.parseFloat(args[3]);
        double flatness = Double.parseDouble(args[4]);
        double spacing = Double.parseDouble(args[5]);

        PlotUtil.doForEachFontInDirectory(originPath, fontPath -> {
            try {
                System.out.println(fontPath);

                Path relativePath = Path.of(originPath).relativize(fontPath);
                Path plotFilePath = Path.of(targetPath, relativePath + ".jpg");
                File plotFile = plotFilePath.toFile();
                if (!Files.exists(plotFilePath.getParent())) {
                    Files.createDirectories(plotFilePath.getParent());
                }

                JFreeChart chart = getChart(fontPath, character, fontSize, flatness, spacing);
                ChartUtilities.saveChartAsJPEG(plotFile, chart, 600, 800);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static JFreeChart getChart(Path fontPath, char character, float fontSize, double flatness, double spacing) throws
        FontParserException {
        FontParser parser = new JavaAwtFontParser(fontPath.toString());
        Glyph glyph = parser.getGlyph(character, fontSize);
        Flattener flattener = new JavaAwtFlattener(flatness);
        Glyph flattenedGlyph = flattener.flatten(glyph);

        ThicknessAnalyzer.ThicknessLineFilter<Line, Line, Line> filter = (line, intersectingLine, thicknessLine) -> {
            double angle = thicknessLine.angleTo(intersectingLine);
            return angle > 30;
        };


        List<Line> lines = new EvenlyDistributedThicknessAnalyzer(spacing, filter).computeThicknessLines(flattenedGlyph);

        // shift thicknesses to start with the line closest to point (0,0)
        Point origin = new Point(0,0);
        Line closestLine = Collections.min(
            lines,
            (s1, s2) -> (int) (s1.getFrom().distanceTo(origin) - s2.getFrom().distanceTo(origin))
        );
        int closestLineIndex = lines.indexOf(closestLine);
        Collections.rotate(lines, -closestLineIndex);

        double width = flattenedGlyph.getBoundingBox().getWidth();
        double height = glyph.getBoundingBox().getHeight();

        double[] thicknesses = getShortHorizontalEndOfLineThicknesses(lines, width, height);

        return PlotUtil.getBarChart(
            parser.getFontName() + ": " + character,
            "Segment Nr.",
            "Thickness",
            thicknesses
        );
    }
}
