package ch.fhnw.ip5.digitalfontclassification.demos;

import ch.fhnw.ip5.digitalfontclassification.domain.Flattener;
import ch.fhnw.ip5.digitalfontclassification.domain.FontParser;
import ch.fhnw.ip5.digitalfontclassification.domain.Glyph;
import ch.fhnw.ip5.digitalfontclassification.domain.JavaAwtFlattener;
import ch.fhnw.ip5.digitalfontclassification.domain.JavaAwtFontParser;
import ch.fhnw.ip5.digitalfontclassification.plot.PlotUtil;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.imageio.ImageIO;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class AllPlots extends JPanel {

    private final int width = 800;
    private final int height = 600;
    List<JFreeChart> charts = new ArrayList<>();
    List<ChartPanel> panels = new ArrayList<>();
    ChartLayoutInstructions layoutInstructions;
    public AllPlots(List<JFreeChart> charts, ChartLayoutInstructions layoutInstructions) {
        super();
        this.layoutInstructions = layoutInstructions;
        this.charts.addAll(charts);
        createUIComponents();
    }

    // Method to create a dummy dataset
    private static DefaultCategoryDataset createDataset(int seed) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 1; i <= 5; i++) {
            dataset.addValue(i * seed, "Series" + seed, "Category" + i);
        }
        return dataset;
    }

    // Method to create a chart
    private static JFreeChart createChart(DefaultCategoryDataset dataset, String title) {
        return ChartFactory.createBarChart(
            title,                // Chart title
            "Category",           // Domain axis label
            "Value",              // Range axis label
            dataset,              // Data
            PlotOrientation.VERTICAL,
            true,                 // Include legend
            true,                 // Tooltips
            false                 // URLs
        );
    }

    protected void createUIComponents() {
        int size = Math.min(layoutInstructions.getColumns() * layoutInstructions.getRows(), charts.size());
        this.setLayout(new GridLayout(layoutInstructions.getRows(), layoutInstructions.getColumns()));

        int panelWidth = width / layoutInstructions.getColumns();  // Assuming total width as 800
        int panelHeight = height / layoutInstructions.getRows();    // Assuming total height as 600

        for (int i = 0; i < size; i++) {
            ChartPanel chartPanel = new ChartPanel(charts.get(i));
            chartPanel.setPreferredSize(new Dimension(panelWidth, panelHeight));
            chartPanel.setSize(panelWidth, panelHeight);  // Explicitly set size
            chartPanel.setMaximumDrawHeight(20000);
            chartPanel.setMinimumDrawHeight(panelHeight);
            chartPanel.setMaximumDrawWidth(20000);
            chartPanel.setMinimumDrawWidth(panelWidth);
            chartPanel.setPopupMenu(null);
            panels.add(chartPanel);
            this.add(chartPanel);
        }
    }


    public void saveAsImage(File plotFile) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        double cellWidth = width / (double) layoutInstructions.getColumns();
        double cellHeight = height / (double) layoutInstructions.getRows();

        for (int i = 0; i < panels.size(); i++) {
            ChartPanel panel = panels.get(i);
            int row = i / layoutInstructions.getColumns();
            int col = i % layoutInstructions.getColumns();

            if (panel.getWidth() <= 0 || panel.getHeight() <= 0) {
                panel.setSize((int) cellWidth, (int) cellHeight);
            }

            Graphics2D chartGraphics = (Graphics2D) g2d.create((int) (col * cellWidth), (int) (row * cellHeight), (int) cellWidth, (int) cellHeight);
            panel.validate();
            panel.paintComponent(chartGraphics);
            chartGraphics.dispose();
        }

        g2d.dispose();

        try {
            ImageIO.write(image, "jpg", plotFile);
            System.out.println("Image saved to " + plotFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws PrinterException, IOException {
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

                Path relativePath = Path.of(originPath).relativize(fontPath);
                Path plotFilePath = Path.of(targetPath, relativePath + ".jpg");
                File plotFile = plotFilePath.toFile();
                if (!Files.exists(plotFilePath.getParent())) {
                    Files.createDirectories(plotFilePath.getParent());
                }

                // Generate charts
                List<JFreeChart> charts = new ArrayList<>();
                charts.add(ContourDirectionPlot.getChart(fontPath, character, fontSize, flatness));
                charts.add(SlopeAlongContourPlot.getChart(fontPath, character, fontSize, flatness));
                charts.add(ThicknessAlongPathPlot.getChart(fontPath, character, fontSize, flatness));

                ChartLayoutInstructions layoutInstructions = new ChartLayoutInstructions(2, 2);
                AllPlots customChartPanel = new AllPlots(charts, layoutInstructions);
                customChartPanel.createUIComponents();

                customChartPanel.saveAsImage(plotFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


    }

}
