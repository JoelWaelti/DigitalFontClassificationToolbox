package ch.fhnw.ip5.digitalfontclassification.analysis;

import ch.fhnw.ip5.digitalfontclassification.analysis.thickness.MiddleOfLineThicknessAnalyzer;
import ch.fhnw.ip5.digitalfontclassification.domain.Glyph;
import ch.fhnw.ip5.digitalfontclassification.domain.Point;
import ch.fhnw.ip5.digitalfontclassification.domain.Segment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SymmetryAnalyzer {
    public static double[] getHaarstrichRange(Glyph glyph) {
        List<Double> thicknesses =  new MiddleOfLineThicknessAnalyzer().computeThicknessesAsList(glyph);

        Point origin = new Point(0,0);
        List<Segment> segments = glyph.getContours().getFirst().getSegments();
        Segment closestSegment = Collections.min(
            segments,
            (s1, s2) -> (int) (s1.getFrom().distanceTo(origin) - s2.getFrom().distanceTo(origin))
        );
        int closestSegmentIndex = segments.indexOf(closestSegment);
        Collections.rotate(thicknesses, -closestSegmentIndex);

        double maxThickness = Collections.max(thicknesses);
        List<Integer> maxThicknessIndices = new ArrayList<>();
        for (int i = 0; i < thicknesses.size(); i++) {
            if (thicknesses.get(i) == maxThickness) {
                maxThicknessIndices.add(i);
            }
        }

        double[] range = new double[thicknesses.size()];
        if(maxThicknessIndices.size() % 4  == 0) {
            for(int j = 1; j < maxThicknessIndices.size(); j=j+2){
                int n = maxThicknessIndices.get(j-1);
                while (maxThicknessIndices.get(j) >= n) {
                    range[n] = thicknesses.get(n);
                    n++;
                }

            }
        }

        return range;
    }
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

    // TODO: equals? aber wie mit toleranz umgehen?
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
