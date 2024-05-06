package ch.fhnw.ip5.digitalfontclassification;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Contour {
    private Point currentPosition;

    private List<Segment> segments;
    private List<Point> outlinePoints;
    private List<Point> controlPoints;

    public Contour(Point start) {
        this.segments = new LinkedList<>();
        this.outlinePoints = new ArrayList<>();
        this.controlPoints = new ArrayList<>();

        this.currentPosition = start;
        this.outlinePoints.add(start);
    }

    public void lineTo(Point to) {
        segments.add(new Line(currentPosition, to));
        outlinePoints.add(to);
        currentPosition = to;
    }

    public void cubicTo(Point c1, Point c2, Point to) {
        segments.add(new CubicBezierCurve(currentPosition, c1, c2, to));
        outlinePoints.add(to);
        controlPoints.add(c1);
        controlPoints.add(c2);
        currentPosition = to;
    }

    public void close() {
        Point start = segments.getFirst().getFrom();
        if(start.getX() != currentPosition.getX() || start.getY() != currentPosition.getY()) {
            segments.add(new Line(currentPosition, start));
        }
    }

    public List<Segment> getSegments() {
        return this.segments;
    }

    public List<Point> getOutlinePoints() {
        return this.outlinePoints;
    }

    public List<Point> getControlPoints() {
        return this.controlPoints;
    }
}
