package ch.fhnw.ip5.digitalfontclassification.domain;

import ch.fhnw.ip5.digitalfontclassification.domain.*;

import java.awt.geom.CubicCurve2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class JavaAwtFlattener implements Flattener {
    private final double flatness;

    public JavaAwtFlattener(double flatness) {
        this.flatness = flatness;
    }

    @Override
    public Glyph flatten(Glyph glyph) {
        List<Contour> flattenedContours = new ArrayList<>();
        for(Contour contour : glyph.getContours()) {
            flattenedContours.add(flatten(contour));
        }
        return new Glyph(glyph.getCharacter(), glyph.getFontSize(), flattenedContours, glyph.getBoundingBox());
    }

    @Override
    public Contour flatten(Contour contour) {
        Contour flattenedContour = new Contour(contour.getSegments().getFirst().getFrom());
        for (Segment s : contour.getSegments()) {
            if (s instanceof CubicBezierCurve) {
                ArrayList<Line> lines = new ArrayList<>();
                CubicCurve2D.Double curve = convertCurve((CubicBezierCurve) s);
                PathIterator iterator = curve.getPathIterator(null, flatness);
                double[] coords = new double[6];
                Point2D.Double previousPoint = null;

                while (!iterator.isDone()) {
                    int type = iterator.currentSegment(coords);
                    if (type == PathIterator.SEG_LINETO) {
                        Point to = new Point(coords[0], coords[1]);
                        flattenedContour.lineTo(to);
                    }
                    iterator.next();
                }
            } else if (s instanceof Line) {
                flattenedContour.lineTo(s.getTo());
            }
        }
        return flattenedContour;
    }


    private CubicCurve2D.Double convertCurve(CubicBezierCurve c) {
        return new CubicCurve2D.Double(
                c.getFrom().getX(), c.getFrom().getY(),
                c.getControl1().getX(), c.getControl1().getY(),
                c.getControl2().getX(), c.getControl2().getY(),
                c.getTo().getX(), c.getTo().getY()
        );
    }
}
