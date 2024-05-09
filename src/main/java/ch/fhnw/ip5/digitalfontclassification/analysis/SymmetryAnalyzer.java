package ch.fhnw.ip5.digitalfontclassification.analysis;

import ch.fhnw.ip5.digitalfontclassification.domain.Glyph;
import ch.fhnw.ip5.digitalfontclassification.domain.Point;
import java.util.List;

public class SymmetryAnalyzer {
    public static boolean determineSymmetry(Glyph glyph) {
        List<Point> outline_points = glyph.getContours().getFirst().getOutlinePoints();

        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;

        for (Point point : outline_points) {
            minX = Math.min(minX, point.x());
            maxX = Math.max(maxX, point.x());
        }

        double midGlyph = (maxX + minX) / 2;

        for (Point point : outline_points) {
            if (point.x() >= midGlyph) {
                continue;
            }


            double reflectedX = 2 * midGlyph - point.x();
            Point reflectedPoint =  new Point(reflectedX, point.y());

            if(!contains(outline_points, reflectedPoint)) {
                return false;
            }

        }

        return true;
    }

    // TODO: equals? aaber wie mit toleranz umgehen?
    private static boolean contains(List<Point> allPoints, Point point) {
        double tol = 1;
        for (Point p : allPoints) {
            if( point.x() >= p.x() - tol &&
                p.x() + tol >= point.x() &&
                point.y() >= p.y() - tol &&
                p.y() + tol >= point.y()
            ) return true;
        }
        return false;
    }
}
