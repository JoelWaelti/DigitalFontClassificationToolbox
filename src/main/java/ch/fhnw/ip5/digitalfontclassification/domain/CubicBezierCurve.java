package ch.fhnw.ip5.digitalfontclassification.domain;

import ch.fhnw.ip5.digitalfontclassification.domain.Point;
import ch.fhnw.ip5.digitalfontclassification.domain.Segment;

public class CubicBezierCurve extends Segment {
    private Point control1;
    private Point control2;

    public CubicBezierCurve(Point from, Point control1, Point control2, Point to) {
        super(from, to);
        this.control1 = control1;
        this.control2 = control2;
    }

    public Point getControl1() {
        return control1;
    }

    public Point getControl2() {
        return control2;
    }
}
