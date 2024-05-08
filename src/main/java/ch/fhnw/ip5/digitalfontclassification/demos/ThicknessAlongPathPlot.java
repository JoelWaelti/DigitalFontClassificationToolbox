package ch.fhnw.ip5.digitalfontclassification.demos;

import ch.fhnw.ip5.digitalfontclassification.domain.*;
import ch.fhnw.ip5.digitalfontclassification.plot.PlotUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static ch.fhnw.ip5.digitalfontclassification.analysis.LineThicknessAnalyzer.computeThicknessAlongPathAtMiddleOfSegments;

public class ThicknessAlongPathPlot {

    // Args: <source> <target> <character> <fontSize> <flatness>
    public static void main(String[] args) throws IOException {
        String originPath = args[0];
        String targetPath = args[1];
        char character = args[2].charAt(0);
        float fontSize = Float.parseFloat(args[3]);
        double flatness = Double.parseDouble(args[4]);

        PlotUtil.doForEachFontInDirectory(originPath, fontPath -> {
            try {
                System.out.println(fontPath);

                FontParser parser = new JavaAwtFontParser(fontPath.toString());
                Glyph glyph = parser.getGlyph(character, fontSize);
                Flattener flattener = new JavaAwtFlattener(flatness);
                Glyph flattenedGlyph = flattener.flatten(glyph);
                List<Double> thicknesses = getShiftedThicknesses(flattenedGlyph);

                JFreeChart chart = getChart(thicknesses, parser.getFontName(), character);
                Path relativePath = Path.of(originPath).relativize(fontPath);
                Path plotFilePath = Path.of(targetPath, relativePath + ".jpg");
                File plotFile = plotFilePath.toFile();

                if (!Files.exists(plotFilePath.getParent())) {
                    Files.createDirectories(plotFilePath.getParent());
                }

                ChartUtilities.saveChartAsJPEG(plotFile, chart, 600, 800);
            } catch (Exception ignored) {}
        });
    }

    public static List<Double> getShiftedThicknesses(Glyph glyph) {
        List<Double> thicknesses = computeThicknessAlongPathAtMiddleOfSegments(glyph);

        // shift thicknesses
        Point origin = new Point(0,0);
        List<Segment> segments = glyph.getContours().getFirst().getSegments();
        int minSegmentIndex = 0;
        double minDistance = Double.MAX_VALUE;
        for(int i = 0; i < segments.size(); i++) {
            double d = segments.get(i).getFrom().distanceTo(origin);
            if(d < minDistance) {
                minSegmentIndex = i;
                minDistance = d;
            }
        }

        List<Double> shiftedThicknesses = new ArrayList<>();
        for(int i = 0; i < thicknesses.size(); i++) {
            int shiftedIndex = (i + minSegmentIndex) % thicknesses.size();
            shiftedThicknesses.add(thicknesses.get(shiftedIndex));
        }

        return shiftedThicknesses;
    }

    public static JFreeChart getChart(List<Double> thicknesses, String fontName, char character) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for(int i = 0; i < thicknesses.size(); i++) {
            dataset.addValue(thicknesses.get(i), "thicknesses", String.valueOf(i));
        }

        return ChartFactory.createBarChart(
            fontName +": " + character,
            "Segment Nr.",
            "Thickness",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
    }
}
