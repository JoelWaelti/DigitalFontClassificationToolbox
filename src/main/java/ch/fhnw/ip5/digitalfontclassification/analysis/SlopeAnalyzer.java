package ch.fhnw.ip5.digitalfontclassification.analysis;

import ch.fhnw.ip5.digitalfontclassification.domain.Contour;
import ch.fhnw.ip5.digitalfontclassification.domain.Line;
import ch.fhnw.ip5.digitalfontclassification.domain.Segment;
import ch.fhnw.ip5.digitalfontclassification.domain.Vector;

import java.util.List;

public class SlopeAnalyzer {
    public static double[] getSlopesAlongContour(Contour contour) {
        List<Segment> segments = contour.getSegments();
        double[] directions = new double[segments.size()];

        for(int i = 0; i < segments.size(); i++) {
            if(!(segments.get(i) instanceof Line line)) {
                throw new IllegalArgumentException("Contours of the glyph can only contain lines for this operation. Consider flattening the entire glyph.");
            }
            Vector v = line.toVector();
            directions[i] = v.x() == 0 ? 0 : v.y() / v.x();
        }

        return directions;
    }
}
