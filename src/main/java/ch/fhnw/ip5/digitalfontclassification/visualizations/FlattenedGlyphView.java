package ch.fhnw.ip5.digitalfontclassification.visualizations;

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

public class FlattenedGlyphView {

    // Args: <source path> <target path> <character> <font size> <flatness>
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

                BufferedImage bufferedImage = getVisualizationAsBufferedImage(flattenedGlyph);

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

    private static BufferedImage getVisualizationAsBufferedImage(Glyph glyph) {
        BufferedImage bi = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0,0,bi.getWidth(),bi.getHeight());

        g2d.scale(1, -1);

        int tolerance = 100;
        g2d.translate(0, -bi.getHeight() + tolerance);

        drawContoursWithColorFlow(glyph.getContours(), g2d);
        return bi;
    }

    private static void drawContoursWithColorFlow(List<Contour> contours, Graphics2D g2d) {
        for (Contour contour : contours) {
            // Draw segments connecting outline points
            Contour rotatedContour = contour.moveStartPointToSegmentClosestTo(new Point(0,0));
            List<Segment> segments = rotatedContour.getSegments();
            int totalSegments = segments.size();

            for (int i = 0; i < totalSegments; i++) {
                Segment segment = segments.get(i);
                float hue = (float) i / (totalSegments - 1);

                Color color = Color.getHSBColor(hue, 1.0f, 1.0f);
                g2d.setColor(color);

                float lineThickness = 3.0f; // You can adjust the thickness here
                g2d.setStroke(new BasicStroke(lineThickness));

                Point from = segment.getFrom();
                Point to = segment.getTo();
                g2d.drawLine((int) from.x(), (int) from.y(), (int) to.x(), (int) to.y());
            }
        }

    }

}
