package ch.fhnw.ip5.digitalfontclassification.demos;

import ch.fhnw.ip5.digitalfontclassification.analysis.SerifAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.analysis.SerifThicknessAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.domain.*;
import ch.fhnw.ip5.digitalfontclassification.drawing.DrawUtil;
import ch.fhnw.ip5.digitalfontclassification.plot.PlotUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class SerifThicknessDecider {
    private static char character;
    private static float fontSize;
    private static double flatness;
    private static String targetPath;

    public static void main(String[] args) throws FontParserException, IOException {
        character = args[0].charAt(0);
        fontSize = Float.parseFloat(args[1]);
        flatness = Double.parseDouble(args[2]);
        int spacing = Integer.parseInt(args[3]);
        double serifHeightThreshold = Double.parseDouble(args[4]);
        targetPath = args[5];
        String sourcePath = args[6];

        PlotUtil.doForEachFontInDirectory(sourcePath, fontPath -> {
            try {
                FontParser parser = new JavaAwtFontParser(fontPath.toString());
                Glyph glyph = parser.getGlyph(character, fontSize);
                Flattener flattener = new JavaAwtFlattener(flatness);
                Glyph flattenedGlyph = flattener.flatten(glyph);

                if(SerifAnalyzer.hasSerif(flattenedGlyph, spacing, serifHeightThreshold)) {
                    SerifThicknessAnalyzer analyzer = new SerifThicknessAnalyzer(flattenedGlyph);
                    System.out.println(
                            fontPath.getParent().getFileName()
                            + "\t" +
                            fontPath.getFileName()
                            + "\t" +
                            analyzer.serifThicknessIsSmallerThanHairLineThickness()
                            + "\t" +
                            (analyzer.getSerifThicknessToStemThicknessRatio() > 0.5)
                    );

                    saveImageWithGlyphAndLines(flattenedGlyph, analyzer.getSerifThicknessLines(), analyzer.getHairlineThicknessLines(), Path.of(targetPath, fontPath.getFileName().toString() + ".png"));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void saveImageWithGlyphAndLines(Glyph glyph, List<Line> serifLines, List<Line> hairlineLines, Path filePath) throws IOException {
        BufferedImage bi = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        DrawUtil.drawBackgroundColor(g2d, Color.WHITE);
        DrawUtil.setOriginToBottomLeft(g2d, bi.getHeight(), 100);
        DrawUtil.drawGlyph(glyph, g2d);
        DrawUtil.drawLines(hairlineLines, g2d, () -> Color.RED);
        DrawUtil.drawLines(serifLines, g2d, () -> Color.GREEN);
        ImageIO.write(bi, "PNG", filePath.toFile());
    }
}
