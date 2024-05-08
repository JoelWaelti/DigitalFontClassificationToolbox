package ch.fhnw.ip5.digitalfontclassification.demos;

import ch.fhnw.ip5.digitalfontclassification.domain.Flattener;
import ch.fhnw.ip5.digitalfontclassification.domain.FontParser;
import ch.fhnw.ip5.digitalfontclassification.domain.Glyph;
import ch.fhnw.ip5.digitalfontclassification.domain.JavaAwtFlattener;
import ch.fhnw.ip5.digitalfontclassification.domain.JavaAwtFontParser;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ch.fhnw.ip5.digitalfontclassification.analysis.LineThicknessAnalyzer.computeThicknessAlongPathAtMiddleOfSegments;

public class ThicknessAlongPathPlot {
    private static char character;
    private static float fontSize;
    private static double flatness;

    // Args: <origin> <target> <character> <fontSize> <flatness>
    public static void main(String[] args) throws IOException, FontFormatException {
        String originPath = args[0];
        File fontDirectory = new File(originPath);

        String targetPath = args[1];
        File targetDirectory = new File(targetPath);

        character = args[2].charAt(0);
        fontSize = Float.parseFloat(args[3]);
        flatness = Double.parseDouble(args[4]);

        createDirectoriesForGraph(fontDirectory, targetDirectory);
        plotThickness(fontDirectory, targetDirectory);
    }

    public static void createDirectoriesForGraph(final File fontFolder, final File targetFolder)
        throws IOException, FontFormatException {
        File targetDirectory = null;
        for (final File fontClass : fontFolder.listFiles()) {
            if (fontClass.isDirectory()) {
                targetDirectory = new File(targetFolder.getPath() + "/" + fontClass.getName());
                if (!targetDirectory.exists()){
                    targetDirectory.mkdirs();
                } else {
                    System.out.println("Could not create dir + " + fontClass.getName());
                }

                plotThickness(fontClass, targetDirectory);
            }
        }
    }

    public static void plotThickness(final File fontFolder, File targetFolder) throws IOException, FontFormatException {
        for (final File fontClass : fontFolder.listFiles()) {
            if (fontClass.isDirectory()) {
                for (final File fileEntry : fontClass.listFiles()) {
                    int dotIndex = fileEntry.getName().lastIndexOf(".");
                    String fontName = fileEntry.getName().substring(0, dotIndex);

                    Glyph flattenedGlyph = getFlattendGlyph(fileEntry.getPath(), character, fontSize, flatness);
                    List<Double> thicknesses = computeThicknessAlongPathAtMiddleOfSegments(flattenedGlyph);

                    File graphFile = new File(targetFolder.getPath() + "/" + fontName + ".jpeg" );
                    barPlotThickness(graphFile, fontName, thicknesses);
                }
            }
        }
    }

    public static Glyph getFlattendGlyph(String fontPath, char character, float fontSize, double flatness) throws IOException, FontFormatException {
        FontParser parser = new JavaAwtFontParser(fontPath);
        Glyph glyph = parser.getGlyph(character, fontSize);
        Flattener flattener = new JavaAwtFlattener(flatness);

        return flattener.flatten(glyph);
    }

    public static void barPlotThickness(File barChart, String fontName, List<Double> var) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for(int i = 0; i < var.size(); i++) {
            dataset.addValue(var.get(i), "thicknesses", String.valueOf(i));
        }

        JFreeChart barChartObject = ChartFactory.createBarChart(
            fontName,
            "Segment Nr.",
            "Thickness",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );

        int width = 640;    /* Width of the image */
        int height = 480;   /* Height of the image */

        try {
            ChartUtilities.saveChartAsJPEG(barChart, barChartObject , width , height);
        } catch (IOException e) {
            System.out.println("Error occurred while saving the chart for " + fontName);
            e.printStackTrace();
        }
    }
}
