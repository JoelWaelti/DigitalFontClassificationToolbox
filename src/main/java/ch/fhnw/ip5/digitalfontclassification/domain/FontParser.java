package ch.fhnw.ip5.digitalfontclassification.domain;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

public abstract class FontParser {
    public abstract String getFontName() throws FontParserException;
    public abstract Glyph getGlyph(char c, float fontSize) throws FontParserException;
    public abstract Glyph getGlyph(char c) throws FontParserException;

    protected List<Contour> buildContours(PathIterator pathIterator) {
        List<Contour> contours = new ArrayList<>();
        double[] coords = new double[6];
        Contour.Builder builder = Contour.builder();

        while (!pathIterator.isDone()) {
            int type = pathIterator.currentSegment(coords);

            if (type == PathIterator.SEG_MOVETO) {
                if(builder.isStarted()) {
                    contours.add(builder.build());
                    builder = Contour.builder();
                }
                Point to = new Point(coords[0], coords[1]);
                builder.startAt(to);
            } else if (type == PathIterator.SEG_LINETO) {
                Point to = new Point(coords[0], coords[1]);
                builder.lineTo(to);
            } else if (type == PathIterator.SEG_CUBICTO) {
                // The final coords in a CUBICTO are the actual new outline point.
                // The first two pairs are control points.
                Point control1 = new Point(coords[0], coords[1]);
                Point control2 = new Point(coords[2], coords[3]);
                Point to = new Point(coords[4], coords[5]);
                builder.cubicTo(control1, control2, to);
            } else if (type == PathIterator.SEG_QUADTO) {
                throw new UnsupportedOperationException("Only OTF-Fonts with cubic bezier curves are supported.");
            }
            pathIterator.next();
        }

        contours.add(builder.build());

        return contours;
    }
}
