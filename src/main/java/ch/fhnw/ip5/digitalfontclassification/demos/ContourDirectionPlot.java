package ch.fhnw.ip5.digitalfontclassification.demos;

import ch.fhnw.ip5.digitalfontclassification.analysis.ContourDirectionAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.domain.*;
import ch.fhnw.ip5.digitalfontclassification.plot.PlotUtil;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ContourDirectionPlot {
    public static void main(String[] args) throws IOException {
        String sourcePath = args[0];
        String targetPath = args[1];
        char character = args[2].charAt(0);
        int unitsPerEm = Integer.parseInt(args[3]);
        double flatness = Double.parseDouble(args[4]);

        PlotUtil.doForEachFontInDirectory(sourcePath, fontPath -> {
            try {
                System.out.println(fontPath);

                Path relativePath = Path.of(sourcePath).relativize(fontPath);
                Path plotFilePath = Path.of(targetPath, relativePath + ".jpg");
                File plotFile = plotFilePath.toFile();
                if (!Files.exists(plotFilePath.getParent())) {
                    Files.createDirectories(plotFilePath.getParent());
                }

                JFreeChart chart = getChart(fontPath, character, unitsPerEm, flatness);
                ChartUtilities.saveChartAsJPEG(plotFile, chart, 600, 800);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static JFreeChart getChart(Path fontPath, char character, int unitsPerEm, double flatness) throws FontParserException {
        FontParser parser = new ApacheFontBoxFontParser(fontPath.toString());
        Glyph glyph = parser.getGlyph(character, unitsPerEm);
        Flattener flattener = new JavaAwtFlattener(flatness);
        Glyph flattenedGlyph = flattener.flatten(glyph);

        // it's assumed that the first contour is the main enclosing contour of the glyph
        Point origin = new Point(0,0);
        Contour contour = flattenedGlyph.getContours().getFirst().moveStartPointToSegmentClosestTo(origin);
        double[] directions = ContourDirectionAnalyzer.getDirectionsAlongContour(contour);

        return PlotUtil.getBarChart(
                parser.getFontName() + ": " + character,
                "Segment Nr.",
                "Direction in Degrees",
                directions
        );
    }
}
