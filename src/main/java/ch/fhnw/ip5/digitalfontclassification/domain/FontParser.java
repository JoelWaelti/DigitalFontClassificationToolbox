package ch.fhnw.ip5.digitalfontclassification.domain;

import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

public abstract class FontParser {
    public abstract String getFontName() throws FontParserException;

    /**
     * Parses the glyph for the character {@code c}.
     * The coordinates will be scaled to match the specified {@code unitsPerEm}.
     * This allows to have a uniform coordinate system over different fonts with different units per em.
     * To learn more about "units per em" see <a href="https://help.fontlab.com/fontlab-vi/Font-Sizes-and-the-Coordinate-System/">here</a>.
     */
    public abstract Glyph getGlyph(char c, int unitsPerEm) throws FontParserException;

    /**
     * Parses the glyph for the character {@code c}.
     * The coordinates will NOT be scaled and will use the units per em defined by the font.
     * Use {@link #getGlyph(char c, int unitsPerEm)} to get a scaled glyph.
     * To learn more about "units per em" see <a href="https://help.fontlab.com/fontlab-vi/Font-Sizes-and-the-Coordinate-System/">here</a>.
     */
    public Glyph getGlyph(char c) throws FontParserException {
        return getGlyph(c, getUnitsPerEm());
    }

    /**
     * Returns the units per EM of the font.
     * To learn more see <a href="https://help.fontlab.com/fontlab-vi/Font-Sizes-and-the-Coordinate-System/">here</a>.
     */
    public abstract int getUnitsPerEm() throws FontParserException;

    /**
     * Creates a list of contours by parsing the path of the {@code pathIterator}.
     * The contours are scaled by using the {@code scaleFactor}.
     * It simply multiplies all coordinates with the {@code scaleFactor}
     * @param pathIterator path iterator with a valid OTF path with cubic curves
     * @param scaleFactor factor by which the contours are scaled by
     * @throws UnsupportedOperationException the provided path contains a quadratic Bézier curve
     */
    protected List<Contour> buildContours(PathIterator pathIterator, double scaleFactor) {
        List<Contour> contours = new ArrayList<>();
        double[] coords = new double[6];
        Contour.Builder builder = Contour.builder();

        while (!pathIterator.isDone()) {
            int type = pathIterator.currentSegment(coords);
            scaleCoords(coords, scaleFactor);

            if (type == PathIterator.SEG_MOVETO) {
                if(builder.isStarted()) {
                    contours.add(builder.build());
                    builder = Contour.builder();
                }
                Point to = new Point(coords[0], -coords[1]);
                builder.startAt(to);
            } else if (type == PathIterator.SEG_LINETO) {
                Point to = new Point(coords[0], -coords[1]);
                builder.lineTo(to);
            } else if (type == PathIterator.SEG_CUBICTO) {
                // The final coords in a CUBICTO are the actual new outline point.
                // The first two pairs are control points.
                Point control1 = new Point(coords[0], -coords[1]);
                Point control2 = new Point(coords[2], -coords[3]);
                Point to = new Point(coords[4], -coords[5]);
                builder.cubicTo(control1, control2, to);
            } else if (type == PathIterator.SEG_QUADTO) {
                throw new UnsupportedOperationException("Only OTF-Fonts with cubic bezier curves are supported.");
            }
            pathIterator.next();
        }

        if(!builder.getSegments().isEmpty()) {
            contours.add(builder.build());
        }

        return contours;
    }

    /**
     * Scales the values in the array {@code coords} with the {@code scaleFactor}
     * @param coords array of coordinates to be scaled
     * @param scaleFactor factor by which the values in the {@code coords} array should be scaled
     */
    private void scaleCoords(double[] coords, double scaleFactor) {
        if(scaleFactor != 0) {
            for(int i = 0; i < coords.length; i++) {
                coords[i] = coords[i] * scaleFactor;
            }
        }
    }
}
