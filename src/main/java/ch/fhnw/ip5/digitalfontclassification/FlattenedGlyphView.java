package ch.fhnw.ip5.digitalfontclassification;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class FlattenedGlyphView extends JPanel {
    private Glyph glyph;

    public FlattenedGlyphView(Glyph glyph) {
        this.glyph = glyph;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.scale(1, -1);
        g2d.translate(0, -getHeight());

        List<Contour> contours = glyph.getContours();
        for (Contour contour : contours) {
            List<Point> outlinePoints = contour.getOutlinePoints();

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

    public static void main(String[] args) throws IOException, FontFormatException {
        String fontPath = args[0];
        char character = args[1].charAt(0);
        float fontSize = Float.parseFloat(args[2]);
        double flatness = Double.parseDouble(args[3]);
        FontParser parser = new JavaAwtFontParser(fontPath);
        Glyph glyph = parser.getGlyph(character, fontSize);
        Flattener flattener = new JavaAwtFlattener(flatness);
        Glyph flattenedGlyph = flattener.flatten(glyph);

        System.out.println(
                flattenedGlyph.getContours().stream()
                .mapToInt(contour -> contour.getSegments().size())
                .sum()
        );

        JFrame frame = new JFrame();
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new FlattenedGlyphView(flattenedGlyph));
        frame.setVisible(true);
    }
}
