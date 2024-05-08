package ch.fhnw.ip5.digitalfontclassification.demos;

import ch.fhnw.ip5.digitalfontclassification.analysis.LineThicknessAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.domain.*;
import ch.fhnw.ip5.digitalfontclassification.domain.Point;
import ch.fhnw.ip5.digitalfontclassification.plot.PlotUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ThicknessAlongPathVisualization {

    // Args: <source path> <target path> <character> <font size> <flatness>
    public static void main(String[] args) throws IOException {
        String sourcePath = args[0];
        String targetPath = args[1];
        char character = args[2].charAt(0);
        float fontSize = Float.parseFloat(args[3]);
        double flatness = Double.parseDouble(args[4]);

        PlotUtil.doForEachFontInDirectory(sourcePath, fontPath -> {
            try {
                FontParser parser = new JavaAwtFontParser(fontPath.toString());
                Glyph glyph = parser.getGlyph(character, fontSize);
                Flattener flattener = new JavaAwtFlattener(flatness);
                Glyph flattenedGlyph = flattener.flatten(glyph);

                BufferedImage bufferedImage = getVisualizationAsBufferedImage(flattenedGlyph);

                Files.createDirectories(Path.of(targetPath));
                String filePath = Path.of(targetPath, parser.getFontName() + ".png").toString();
                ImageIO.write(bufferedImage, "PNG", new File(filePath));
            } catch(Exception ignored) {}
        });
    }

    private static BufferedImage getVisualizationAsBufferedImage(Glyph glyph) {
        BufferedImage bi = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0,0,bi.getWidth(),bi.getHeight());

        g2d.scale(1, -1);

        int tolerance = 100;
        g2d.translate(0, -bi.getHeight() + tolerance);

        drawGlyph(glyph, g2d);
        drawThicknessLines(glyph, g2d);
        return bi;
    }

    private static void drawGlyph(Glyph glyph, Graphics2D g2d) {
        List<Contour> contours = glyph.getContours();
        for (Contour contour : contours) {
            // Draw segments connecting outline points
            g2d.setColor(Color.BLACK);
            List<Segment> segments = contour.getSegments();
            for (Segment segment : segments) {
                Point from = segment.getFrom();
                Point to = segment.getTo();
                g2d.drawLine((int) from.getX(), (int) from.getY(), (int) to.getX(), (int) to.getY());
            }
        }
    }

    private static void drawThicknessLines(Glyph glyph, Graphics2D g2d) {
        List<Line> lines = LineThicknessAnalyzer.computeThicknessLinesAlongPathAtMiddleOfSegments(glyph);
        g2d.setColor(Color.RED);
        for(Line line : lines) {
            Point from = line.getFrom();
            Point to = line.getTo();
            g2d.drawLine((int) from.getX(), (int) from.getY(), (int) to.getX(), (int) to.getY());
        }
    }
}
