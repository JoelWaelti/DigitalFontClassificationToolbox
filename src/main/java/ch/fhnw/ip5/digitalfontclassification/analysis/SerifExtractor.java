package ch.fhnw.ip5.digitalfontclassification.analysis;

import ch.fhnw.ip5.digitalfontclassification.domain.*;
import ch.fhnw.ip5.digitalfontclassification.domain.Point;
import ch.fhnw.ip5.digitalfontclassification.drawing.DrawUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class SerifExtractor {
    public static void main(String[] args) throws IOException, FontParserException {
        String fontPath = args[0];
        String targetPath = args[1];
        char character = args[2].charAt(0);
        int unitsPerEm = Integer.parseInt(args[3]);
        double flatness = Double.parseDouble(args[4]);

        FontParser parser = new ApacheFontBoxFontParser(fontPath);
        Glyph glyph = parser.getGlyph(character, unitsPerEm);
        Flattener flattener = new JavaAwtFlattener(flatness);
        Glyph flattenedGlyph = flattener.flatten(glyph);

        BufferedImage bi = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        DrawUtil.setOriginToBottomLeft(g2d, bi.getHeight(), 100);

        Supplier<Color> colorSupplier = () -> Color.RED;
        DrawUtil.drawLines(getSerifLines(flattenedGlyph), g2d, colorSupplier);

        Path plotFilePath = Path.of(targetPath, "serif.png");
        File plotFile = plotFilePath.toFile();
        if (!Files.exists(plotFilePath.getParent())) {
            Files.createDirectories(plotFilePath.getParent());
        }
        System.out.println(plotFilePath);
        ImageIO.write(bi, "PNG", plotFile);
    }

    private static final double THRESHOLD = 0.2;

    public static List<Line> getSerifLines(Glyph glyph) {
        Contour contour = glyph.getContours().getFirst();
        contour = contour.moveStartPointToSegmentClosestTo(new Point(0,0));
        Line[] lines = new Line[contour.getSegments().size()];
        int i = 0;
        for(Segment s : contour.getSegments()) {
            lines[i] = (Line) s;
            i++;
        }

        double[] directions = ContourDirectionAnalyzer.getDirectionsAlongContour(contour);
        double distanceWalkedUpwards = 0;
        int serifEndIndex = 0;

        for(int j = 0; j < directions.length; j++) {
            if(directions[j] > 45 && directions[j] < 135) {
                distanceWalkedUpwards += lines[j].getLength();

                if(distanceWalkedUpwards > glyph.getBoundingBox().getHeight() * THRESHOLD) {
                    break;
                }
            } else {
                distanceWalkedUpwards = 0;
            }

            serifEndIndex = j;
        }

        double distanceWalkedDownwards = 0;
        int serifStartIndex = directions.length;

        for(int j = directions.length - 1; j >= 0; j--) {
            if(directions[j] > 225 && directions[j] < 315) {
                distanceWalkedDownwards += lines[j].getLength();

                if(distanceWalkedDownwards > glyph.getBoundingBox().getHeight() * THRESHOLD) {
                    break;
                }
            } else {
                distanceWalkedDownwards = 0;
            }
            serifStartIndex = j;
        }

        List<Line> serifLines = new ArrayList<>();
        int serifLineCount = lines.length - serifStartIndex + serifEndIndex + 1;
        for(int j = 0; j < serifLineCount; j++) {
            serifLines.add(lines[(serifStartIndex + j) % lines.length]);
        }

        return serifLines;
    }
}
