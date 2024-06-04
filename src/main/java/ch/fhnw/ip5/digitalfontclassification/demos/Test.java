package ch.fhnw.ip5.digitalfontclassification.demos;

import ch.fhnw.ip5.digitalfontclassification.domain.*;
import ch.fhnw.ip5.digitalfontclassification.domain.Point;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Test {
    public static void main(String[] args) throws FontParserException, IOException {
        Path fontPath = Path.of(args[0]);
        String targetPath = args[1];
        char character = args[2].charAt(0);
        int unitsPerEm = Integer.parseInt(args[3]);
        double flatness = Double.parseDouble(args[4]);

        FontParser parser = new ApacheFontBoxFontParser(fontPath.toString());
        Glyph glyph = parser.getGlyph(character, unitsPerEm);
        Flattener flattener = new JavaAwtFlattener(flatness);
        Glyph flattenedGlyph = flattener.flatten(glyph);

        BufferedImage bi = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();

        JFreeChart directionPlot = ContourDirectionPlot.getChart(fontPath, character, unitsPerEm, flatness);
        ChartPanel directionPlotPanel = new ChartPanel(directionPlot);
        Graphics2D plotGraphics = (Graphics2D) g2d.create(bi.getWidth() / 2, 0, bi.getWidth() / 2, bi.getHeight());

        BoundingBox bb = flattenedGlyph.getBoundingBox();
        Graphics2D visualizationGraphics = (Graphics2D) g2d.create(
                bi.getWidth() / 2,
                0,
                (int) Math.ceil(bb.getWidth()),
                (int) Math.ceil(bb.getHeight())
        );
        drawGlyph(visualizationGraphics, flattenedGlyph);
        visualizationGraphics.scale(0, -1);
        visualizationGraphics.translate(0, flattenedGlyph.getBoundingBox().getMaxY());

        Path targetFilePath = Path.of(targetPath);
        File plotFile = targetFilePath.toFile();
        if (!Files.exists(targetFilePath.getParent())) {
            Files.createDirectories(targetFilePath.getParent());
        }

        ImageIO.write(bi, "png", plotFile);
    }

    private static void drawGlyph(Graphics2D g2d, Glyph glyph) {
        List<Contour> contours = glyph.getContours();
        for (Contour contour : contours) {
            // Draw segments connecting outline points
            g2d.setColor(Color.BLACK);
            List<Segment> segments = contour.getSegments();
            for (Segment segment : segments) {
                Point from = segment.getFrom();
                Point to = segment.getTo();
                g2d.drawLine((int) from.x(), (int) from.y(), (int) to.x(), (int) to.y());
            }
        }
    }
}
