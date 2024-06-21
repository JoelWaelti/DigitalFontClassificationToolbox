package ch.fhnw.ip5.digitalfontclassification.drawing;

import ch.fhnw.ip5.digitalfontclassification.domain.Contour;
import ch.fhnw.ip5.digitalfontclassification.domain.Glyph;
import ch.fhnw.ip5.digitalfontclassification.domain.Line;
import ch.fhnw.ip5.digitalfontclassification.domain.Point;
import ch.fhnw.ip5.digitalfontclassification.domain.Segment;

import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.awt.*;
import java.util.function.Supplier;

public class DrawUtil {
    public static void drawLines(List<Line> lines, Graphics2D g2d, Supplier<Color> colorSupplier) {
        for(Line line : lines) {
            g2d.setColor(colorSupplier.get());
            g2d.draw(convertLine(line));
        }
    }

    public static void setOriginToBottomLeft(Graphics2D g2d, int imageHeight, int spaceBelowZero) {
        g2d.scale(1, -1);
        g2d.translate(0, -imageHeight + spaceBelowZero);
    }

    private static Line2D.Double convertLine(Line line) {
        return new Line2D.Double(
                line.getFrom().x(),
                line.getFrom().y(),
                line.getTo().x(),
                line.getTo().y()
        );
    }

    public static void drawGlyph(Glyph glyph, Graphics2D g2d) {
        List<Contour> contours = glyph.getContours();
        for (Contour contour : contours) {
            // Draw segments connecting outline points
            g2d.setColor(Color.BLACK);
            List<Segment> segments = contour.getSegments();
            for (Segment segment : segments) {
                ch.fhnw.ip5.digitalfontclassification.domain.Point from = segment.getFrom();
                Point to = segment.getTo();
                g2d.drawLine((int) from.x(), (int) from.y(), (int) to.x(), (int) to.y());
            }
        }
    }

    public static void drawBackgroundColor(Graphics2D g2d, Color color) {
        Color previousColor = g2d.getColor();
        g2d.setColor(color);
        g2d.fillRect(-(Integer.MAX_VALUE / 2), -(Integer.MAX_VALUE / 2), Integer.MAX_VALUE, Integer.MAX_VALUE);
        g2d.setColor(previousColor);
    }
}
