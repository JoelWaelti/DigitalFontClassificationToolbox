package ch.fhnw.ip5.digitalfontclassification.demos;

import ch.fhnw.ip5.digitalfontclassification.analysis.LineThicknessAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.domain.*;
import ch.fhnw.ip5.digitalfontclassification.domain.Point;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

import static ch.fhnw.ip5.digitalfontclassification.analysis.LineThicknessAnalyzer.computeThicknessAlongPathAtMiddleOfSegments;

public class ThicknessAlongPathVisualization extends JPanel {
    private Glyph glyph;
    private Graphics2D g2d;

    public ThicknessAlongPathVisualization(Glyph glyph) {
        this.glyph = glyph;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g2d = (Graphics2D) g;
        g2d.scale(1, -1);
        g2d.translate(0, -getHeight());

        drawGlyph(glyph);
        drawThicknessLines(glyph);
    }

    private void drawGlyph(Glyph glyph) {
        System.out.println("GLYPH");
        List<Contour> contours = glyph.getContours();
        for (Contour contour : contours) {
            java.util.List<Point> outlinePoints = contour.getOutlinePoints();

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

    private void drawThicknessLines(Glyph glyph) {
        List<Line> lines = LineThicknessAnalyzer.computeThicknessLinesAlongPathAtMiddleOfSegments(glyph);
        System.out.println("HELLO");
        g2d.setColor(Color.RED);
        for(Line line : lines) {
            Point from = line.getFrom();
            Point to = line.getTo();
            g2d.drawLine((int) from.getX(), (int) from.getY(), (int) to.getX(), (int) to.getY());
        }
    }

    public static void main(String[] args) throws IOException, FontFormatException {
        String fontPath = args[0];
        char character = args[1].charAt(0);
        float fontSize = Float.parseFloat(args[2]);
        double flatness = Double.parseDouble(args[3]);
        FontParser parser = new JavaAwtFontParser(fontPath);
        Glyph glyph = parser.getGlyph(character, fontSize);
        Flattener flattener = new JavaAwtFlattener(flatness);
        Glyph flattenedGlyph = flattener.flatten(glyph);

        JFrame frame = new JFrame();
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new ThicknessAlongPathVisualization(flattenedGlyph));
        frame.setVisible(true);
    }
}
