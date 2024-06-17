package ch.fhnw.ip5.digitalfontclassification.demos;

import ch.fhnw.ip5.digitalfontclassification.analysis.SerifExtractor;
import ch.fhnw.ip5.digitalfontclassification.domain.Point;
import ch.fhnw.ip5.digitalfontclassification.domain.*;
import ch.fhnw.ip5.digitalfontclassification.drawing.DrawUtil;
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

public class SerifVisualization {

    // Args: <source path> <target path> <character> <font size> <flatness>
    public static void main(String[] args) throws IOException {
        String sourcePath = args[0];
        String targetPath = args[1];
        char character = args[2].charAt(0);
        int unitsPerEm = Integer.parseInt(args[3]);
        double flatness = Double.parseDouble(args[4]);

        PlotUtil.doForEachFontInDirectory(sourcePath, fontPath -> {
            try {
                System.out.println(fontPath);

                FontParser parser = new JavaAwtFontParser(fontPath.toString());
                Glyph glyph = parser.getGlyph(character, unitsPerEm);
                Flattener flattener = new JavaAwtFlattener(flatness);
                Glyph flattenedGlyph = flattener.flatten(glyph);

                BufferedImage bufferedImage = getVisualizationAsBufferedImage(flattenedGlyph);

                Path relativePath = Path.of(sourcePath).relativize(fontPath);
                Path plotFilePath = Path.of(targetPath, relativePath + ".png");
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

        DrawUtil.drawBackgroundColor(g2d, Color.WHITE);
        DrawUtil.setOriginToBottomLeft(g2d, bi.getHeight(), 100);
        DrawUtil.drawGlyph(glyph, g2d);

        g2d.setStroke(new BasicStroke(3));
        SerifExtractor serifExtractor = new SerifExtractor(glyph, 0.2);
        List<List<Line>> serifLines = serifExtractor.getAllSerifs();
        serifLines.forEach(list -> DrawUtil.drawLines(list, g2d, () -> Color.RED));
        // List<Line> serifLines = serifExtractor.getSerifAt(SerifExtractor.SerifLocation.TOP_RIGHT);
        // DrawUtil.drawLines(serifLines, g2d, () -> Color.RED);

        return bi;
    }
}