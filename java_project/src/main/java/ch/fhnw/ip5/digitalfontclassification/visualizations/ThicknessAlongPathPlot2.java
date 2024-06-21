package ch.fhnw.ip5.digitalfontclassification.visualizations;

import ch.fhnw.ip5.digitalfontclassification.analysis.thickness.EvenlyDistributedThicknessAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.domain.*;
import ch.fhnw.ip5.digitalfontclassification.plot.PlotUtil;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class ThicknessAlongPathPlot2 {
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

    public static double[] getThicknesses(Glyph glyph, double spacing) {
        List<Line> lines = new EvenlyDistributedThicknessAnalyzer(spacing).computeThicknessLines(glyph);

        // shift thicknesses to start with the line closest to point (0,0)
        Point origin = new Point(0,0);
        Line closestLine = Collections.min(
                lines,
                (s1, s2) -> (int) (s1.getFrom().distanceTo(origin) - s2.getFrom().distanceTo(origin))
        );
        int closestLineIndex = lines.indexOf(closestLine);
        Collections.rotate(lines, -closestLineIndex);

        return lines.stream().mapToDouble(Line::getLength).toArray();
    }

    public static JFreeChart getChart(Path fontPath, char character, float fontSize, double flatness, double spacing) throws FontParserException {
        FontParser parser = new JavaAwtFontParser(fontPath.toString());
        Glyph glyph = parser.getGlyph(character, fontSize);
        Flattener flattener = new JavaAwtFlattener(flatness);
        Glyph flattenedGlyph = flattener.flatten(glyph);
        double[] thicknesses = getThicknesses(flattenedGlyph, spacing);

        return PlotUtil.getBarChart(
                parser.getFontName() + ": " + character,
                "Segment Nr.",
                "Thickness",
                thicknesses
        );
    }
}
