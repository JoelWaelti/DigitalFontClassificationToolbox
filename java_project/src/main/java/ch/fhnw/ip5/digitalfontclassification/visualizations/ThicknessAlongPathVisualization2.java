package ch.fhnw.ip5.digitalfontclassification.visualizations;

import ch.fhnw.ip5.digitalfontclassification.analysis.thickness.EvenlyDistributedThicknessAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.domain.Point;
import ch.fhnw.ip5.digitalfontclassification.domain.*;
import ch.fhnw.ip5.digitalfontclassification.plot.PlotUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class ThicknessAlongPathVisualization2 {

    // Args: <source path> <target path> <character> <font size> <flatness> <spacing>
    public static void main(String[] args) throws IOException {
        String sourcePath = args[0];
        String targetPath = args[1];
        char character = args[2].charAt(0);
        float fontSize = Float.parseFloat(args[3]);
        double flatness = Double.parseDouble(args[4]);
        double spacing = Double.parseDouble(args[5]);

        PlotUtil.doForEachFontInDirectory(sourcePath, fontPath -> {
            try {
                System.out.println(fontPath);

                FontParser parser = new JavaAwtFontParser(fontPath.toString());
                Glyph glyph = parser.getGlyph(character, fontSize);
                Flattener flattener = new JavaAwtFlattener(flatness);
                Glyph flattenedGlyph = flattener.flatten(glyph);

                BufferedImage bufferedImage = getVisualizationAsBufferedImage(flattenedGlyph, spacing);

                Path relativePath = Path.of(sourcePath).relativize(fontPath);
                Path plotFilePath = Path.of(targetPath, relativePath + ".jpg");
                File plotFile = plotFilePath.toFile();
                if (!Files.exists(plotFilePath.getParent())) {
                    Files.createDirectories(plotFilePath.getParent());
                }

                ImageIO.write(bufferedImage, "PNG", plotFile);
            }  catch (Exception e) {
                System.err.println("Error processing " + fontPath + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    static BufferedImage getVisualizationAsBufferedImage(Glyph glyph, double spacing) {
        BufferedImage bi = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0,0,bi.getWidth(),bi.getHeight());

        g2d.scale(1, -1);

        int tolerance = 100;
        g2d.translate(0, -bi.getHeight() + tolerance);

        drawGlyph(glyph, g2d);
        drawThicknessLinesWithColorFlow(glyph, spacing, g2d);
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
                g2d.drawLine((int) from.x(), (int) from.y(), (int) to.x(), (int) to.y());
            }
        }
    }

    private static void drawThicknessLinesWithColorFlow(Glyph glyph, double spacing, Graphics2D g2d) {
        List<Line> lines = new EvenlyDistributedThicknessAnalyzer(spacing).computeThicknessLines(glyph);
        int totalLines = lines.size();

        Point origin = new Point(0,0);
        Line closestLine = Collections.min(
            lines,
            (s1, s2) -> (int) (s1.getFrom().distanceTo(origin) - s2.getFrom().distanceTo(origin))
        );
        int closestLineIndex = lines.indexOf(closestLine);
        Collections.rotate(lines, -closestLineIndex);

        for (int i = 0; i < totalLines; i++) {
            Line line = lines.get(i);

            float hue = (float) i / (totalLines - 1);

            Color color = Color.getHSBColor(hue, 1.0f, 1.0f);
            g2d.setColor(color);

            Point from = line.getFrom();
            Point to = line.getTo();
            g2d.drawLine((int) from.x(), (int) from.y(), (int) to.x(), (int) to.y());
        }
    }
}
