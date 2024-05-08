package ch.fhnw.ip5.digitalfontclassification.domain;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JavaAwtFontParser implements FontParser {

    private final Font defaultFont;
    private final FontRenderContext frc;

    public JavaAwtFontParser(String fontPath) throws IOException, FontFormatException {
        defaultFont = Font.createFont(java.awt.Font.TRUETYPE_FONT, new File(fontPath));
        frc = new FontRenderContext(null, true, true);
    }

    @Override
    public String getFontName() {
        return this.defaultFont.getFontName();
    }

    @Override
    public Glyph getGlyph(char character, float fontSize) {
        Font font = defaultFont.deriveFont(fontSize);
        GlyphVector glyphVector = font.createGlyphVector(frc, new char[]{character});
        Shape glyphOutline = glyphVector.getGlyphOutline(0);

        List<Contour> contours = new ArrayList<>();

        PathIterator pathIterator = glyphOutline.getPathIterator(new AffineTransform());
        double[] coords = new double[6];

        while (!pathIterator.isDone()) {
            int type = pathIterator.currentSegment(coords);

            if (type == PathIterator.SEG_MOVETO) {
                Point to = new Point(coords[0], -coords[1]);
                contours.add(new Contour(to));
            } else if (type == PathIterator.SEG_LINETO) {
                Point to = new Point(coords[0], -coords[1]);
                contours.getLast().lineTo(to);
            } else if (type == PathIterator.SEG_CUBICTO) {
                // The final coords in a CUBICTO are the actual new outline point.
                // The first two pairs are control points.
                Point control1 = new Point(coords[0], -coords[1]);
                Point control2 = new Point(coords[2], -coords[3]);
                Point to = new Point(coords[4], -coords[5]);
                contours.getLast().cubicTo(control1, control2, to);
            } else if (type == PathIterator.SEG_CLOSE) {
                // close path --> add line from last to first point
                contours.getLast().close();
            } else if (type == PathIterator.SEG_QUADTO) {
                throw new UnsupportedOperationException("Only OTF-Fonts with cubic bezier curves are supported.");
            }

            pathIterator.next();
        }

        Rectangle bounds = glyphOutline.getBounds();
        BoundingBox boundingBox = new BoundingBox(
                bounds.getMinX(),
                bounds.getMaxX(),
                bounds.getMinY(),
                bounds.getMaxY()
        );

        return new Glyph(character, fontSize, contours, boundingBox);
    }
}
