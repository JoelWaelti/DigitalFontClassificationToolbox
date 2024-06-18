package ch.fhnw.ip5.digitalfontclassification.demos;

import ch.fhnw.ip5.digitalfontclassification.analysis.SerifExtractor;
import ch.fhnw.ip5.digitalfontclassification.analysis.thickness.EvenlyDistributedThicknessAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.analysis.thickness.MiddleOfLineThicknessAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.analysis.thickness.ThicknessAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.domain.Contour;
import ch.fhnw.ip5.digitalfontclassification.domain.Flattener;
import ch.fhnw.ip5.digitalfontclassification.domain.FontParser;
import ch.fhnw.ip5.digitalfontclassification.domain.Glyph;
import ch.fhnw.ip5.digitalfontclassification.domain.JavaAwtFlattener;
import ch.fhnw.ip5.digitalfontclassification.domain.JavaAwtFontParser;
import ch.fhnw.ip5.digitalfontclassification.domain.Line;

import ch.fhnw.ip5.digitalfontclassification.domain.Point;
import ch.fhnw.ip5.digitalfontclassification.domain.Segment;
import ch.fhnw.ip5.digitalfontclassification.plot.PlotUtil;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static ch.fhnw.ip5.digitalfontclassification.analysis.ContourDirectionAnalyzer.ccwAngleWithXAxis;
import static ch.fhnw.ip5.digitalfontclassification.analysis.SerifAnalyzer.getVerticalLines;
import static ch.fhnw.ip5.digitalfontclassification.analysis.SerifAnalyzer.hasSerif;

public class SerifSort {

    // Args: <source path> <target path1> <target path2> <character> <font size> <flatness> <spacing>
    public static void main(String[] args) throws IOException {
            String sourcePath = args[0];
            String targetPath1 = args[1];
            String targetPath2 = args[2];
            char character = args[3].charAt(0);
            float fontSize = Float.parseFloat(args[4]);
            double flatness = Double.parseDouble(args[5]);
            double spacing = Double.parseDouble(args[6]);

            PlotUtil.doForEachFontInDirectory(sourcePath, fontPath -> {
                try {
                    System.out.println(fontPath);

                    FontParser parser = new JavaAwtFontParser(fontPath.toString());
                    Glyph glyph = parser.getGlyph(character, fontSize);
                    Flattener flattener = new JavaAwtFlattener(flatness);
                    Glyph flattenedGlyph = flattener.flatten(glyph);

                    SerifExtractor serifExtractor = new SerifExtractor(flattenedGlyph, 0.2);
                    List<List<Line>> serifs = serifExtractor.getAllSerifs();

                    ThicknessAnalyzer.ThicknessLineFilter<Line, Line, Line> filter = (line, intersectingLine, thicknessLine) -> {
                        double ccangleLine = ccwAngleWithXAxis(line.toVector());
                        double angle = thicknessLine.angleTo(intersectingLine);
                        return angle > 45 && (ccangleLine >= 90 && 270 >= ccangleLine);
                    };

                    List<Line> serifLines = new ArrayList<>();
                    List<Line> stammlinesHorizontal = new ArrayList<>();
                    List<Line> seriflinesHorizontal = new ArrayList<>();
                    for(List<Line> serif : serifs) {

                        List<Line> thicknessLinesOfSerif = new EvenlyDistributedThicknessAnalyzer(spacing, filter).computeThicknessLines(serif, serif);

                        serifLines.addAll(thicknessLinesOfSerif);

                        if(serif.size() >= 4) {
                            List<Line> stamm = new ArrayList<>();
                            List<Line> partOfSerif = new ArrayList<>();

                            stamm.add(serif.get(0));
                            for (int i = 1; i < serif.size() - 1; i++) {
                                partOfSerif.add(serif.get(i));
                            }
                            stamm.add(serif.get(serif.size() - 1));

                            List<Line> thicknessLinesOfStamm = new EvenlyDistributedThicknessAnalyzer(spacing, filter).computeHorizontalLines(stamm, stamm);
                            List<Line> horizontalLinesOfSerif = new MiddleOfLineThicknessAnalyzer().computeHorizontalLines(partOfSerif, serif);

                            seriflinesHorizontal.addAll(horizontalLinesOfSerif);
                            stammlinesHorizontal.addAll(thicknessLinesOfStamm);
                        }
                    }

                    List<Line> verticalThicknesses = getVerticalLines(serifLines);
                    List<Line> linesToDraw = new ArrayList<>();
                    linesToDraw.addAll(verticalThicknesses);
                    linesToDraw.addAll(seriflinesHorizontal);

                    BufferedImage bufferedImage = getVisualizationAsBufferedImage(flattenedGlyph, linesToDraw);

                    if(hasSerif(seriflinesHorizontal, stammlinesHorizontal, serifLines)) {
                        Path relativePath = Path.of(sourcePath).relativize(fontPath);
                        Path plotFilePath = Path.of(targetPath1, relativePath + ".jpg");
                        File plotFile = plotFilePath.toFile();
                        if (!Files.exists(plotFilePath.getParent())) {
                            Files.createDirectories(plotFilePath.getParent());
                        }

                        ImageIO.write(bufferedImage, "PNG", plotFile);
                    } else {
                        Path relativePath = Path.of(sourcePath).relativize(fontPath);
                        Path plotFilePath = Path.of(targetPath2, relativePath + ".jpg");
                        File plotFile = plotFilePath.toFile();
                        if (!Files.exists(plotFilePath.getParent())) {
                            Files.createDirectories(plotFilePath.getParent());
                        }

                        ImageIO.write(bufferedImage, "PNG", plotFile);
                    }

                }  catch (Exception e) {
                    System.err.println("Error processing " + fontPath + ": " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }

    static BufferedImage getVisualizationAsBufferedImage(Glyph glyph, List<Line> serifLines) {
        BufferedImage bi = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0,0,bi.getWidth(),bi.getHeight());

        g2d.scale(1, -1);

        int tolerance = 100;
        g2d.translate(0, -bi.getHeight() + tolerance);

        drawGlyph(glyph, g2d);
        drawThicknessLinesWithColorFlow(serifLines, g2d);
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

    private static void drawThicknessLinesWithColorFlow(List<Line> serifLines, Graphics2D g2d) {
        for (int i = 0; i < serifLines.size(); i++) {
            Line line = serifLines.get(i);

            float hue = (float) i / (serifLines.size() - 1);

            Color color = Color.getHSBColor(hue, 1.0f, 1.0f);
            g2d.setColor(color);

            Point from = line.getFrom();
            Point to = line.getTo();
            g2d.drawLine((int) from.x(), (int) from.y(), (int) to.x(), (int) to.y());
        }
    }

}
