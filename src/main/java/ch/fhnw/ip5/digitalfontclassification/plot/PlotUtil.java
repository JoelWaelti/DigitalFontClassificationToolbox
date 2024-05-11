package ch.fhnw.ip5.digitalfontclassification.plot;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;

import java.awt.*;
import java.util.List;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.function.Consumer;

public class PlotUtil {

    public static void doForEachFontInDirectory(String sourceDirectory, Consumer<Path> doForEachFont) throws IOException {
        for (Path p : findOTFFiles(sourceDirectory)) {
            doForEachFont.accept(p);
        }
    }

    public static void styleChart(JFreeChart chart) {
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        plot.setRangeGridlinePaint(Color.GRAY);
    }

    public static void styleChartColorFlow(JFreeChart chart) {
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        BarRenderer renderer = new ColorFlowBarRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.setRenderer(renderer);
    }

    private static List<Path> findOTFFiles(String sourcePath) throws IOException {
        List<Path> otfFiles = new ArrayList<>();
        Path start = Paths.get(sourcePath);

        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().toLowerCase().endsWith(".otf")) {
                    otfFiles.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return otfFiles;
    }


    static class ColorFlowBarRenderer extends BarRenderer {
        @Override
        public Paint getItemPaint(int row, int column) {
            // Generate a unique color for each bar
            float hue = (float) column / (getPlot().getDataset().getColumnCount() - 1);
            return Color.getHSBColor(hue, 1.0f, 1.0f);  // Full saturation and brightness
        }
    }
}
