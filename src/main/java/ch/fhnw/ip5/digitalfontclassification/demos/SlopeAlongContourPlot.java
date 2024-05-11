package ch.fhnw.ip5.digitalfontclassification.demos;

import ch.fhnw.ip5.digitalfontclassification.analysis.ContourDirectionAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.analysis.SlopeAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.domain.*;
import ch.fhnw.ip5.digitalfontclassification.domain.Point;
import ch.fhnw.ip5.digitalfontclassification.plot.PlotUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SlopeAlongContourPlot {
    public static void main(String[] args) throws IOException {
        String sourcePath = args[0];
        String targetPath = args[1];
        char character = args[2].charAt(0);
        float fontSize = Float.parseFloat(args[3]);
        double flatness = Double.parseDouble(args[4]);

        PlotUtil.doForEachFontInDirectory(sourcePath, fontPath -> {
            try {
                System.out.println(fontPath);

                FontParser parser = new JavaAwtFontParser(fontPath.toString());
                Glyph glyph = parser.getGlyph(character, fontSize);
                Flattener flattener = new JavaAwtFlattener(flatness);
                Glyph flattenedGlyph = flattener.flatten(glyph);

                // it's assumed that the first contour is the main enclosing contour of the glyph
                Point origin = new Point(0,0);
                Contour contour = flattenedGlyph.getContours().getFirst().moveStartPointToSegmentClosestTo(origin);
                double[] slopes = SlopeAnalyzer.getSlopesAlongContour(contour);

                Path relativePath = Path.of(sourcePath).relativize(fontPath);
                Path plotFilePath = Path.of(targetPath, relativePath + ".jpg");
                File plotFile = plotFilePath.toFile();
                if (!Files.exists(plotFilePath.getParent())) {
                    Files.createDirectories(plotFilePath.getParent());
                }

                JFreeChart chart = getChart(slopes, parser.getFontName(), character);
                ChartUtilities.saveChartAsJPEG(plotFile, chart, 600, 800);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static JFreeChart getChart(double[] directions, String fontName, char character) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for(int i = 0; i < directions.length; i++) {
            dataset.addValue(directions[i], "directions", String.valueOf(i));
        }

        var chart = ChartFactory.createBarChart(
                fontName +": " + character,
                "Segment Nr.",
                "Slope",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        PlotUtil.styleChartColorFlow(chart);
        return chart;
    }
}
