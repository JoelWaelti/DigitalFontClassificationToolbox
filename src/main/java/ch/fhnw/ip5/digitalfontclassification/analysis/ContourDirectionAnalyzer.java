package ch.fhnw.ip5.digitalfontclassification.analysis;

import ch.fhnw.ip5.digitalfontclassification.domain.Contour;
import ch.fhnw.ip5.digitalfontclassification.domain.Line;
import ch.fhnw.ip5.digitalfontclassification.domain.Segment;
import ch.fhnw.ip5.digitalfontclassification.domain.Vector;

import java.util.List;

public class ContourDirectionAnalyzer {
    public static double[] getDirectionsAlongContour(Contour contour) {
        List<Segment> segments = contour.getSegments();
        double[] directions = new double[segments.size()];

        for(int i = 0; i < segments.size(); i++) {
            if(!(segments.get(i) instanceof Line line)) {
                throw new IllegalArgumentException("Contours of the glyph can only contain lines for this operation. Consider flattening the entire glyph.");
            }
            directions[i] = ccwAngleWithXAxis(line.toVector());
        }

        return directions;
    }

    // calculates the counter-clockwise angle of the vector to the x-axis
    private static double ccwAngleWithXAxis(Vector v) {
        double angleRad = Math.atan2(v.y(), v.x());
        // Adjust angle to be in the range [0, 2π)
        if (angleRad < 0) {
            angleRad += 2 * Math.PI;
        }
        return Math.toDegrees(angleRad);
    }
}
