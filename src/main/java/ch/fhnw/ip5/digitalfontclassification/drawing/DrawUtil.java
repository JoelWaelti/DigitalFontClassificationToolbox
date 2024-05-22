package ch.fhnw.ip5.digitalfontclassification.drawing;

import ch.fhnw.ip5.digitalfontclassification.domain.Line;

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
}
