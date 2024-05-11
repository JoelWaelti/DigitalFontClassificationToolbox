package ch.fhnw.ip5.digitalfontclassification.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Contour {
    private final List<Segment> segments;
    private final List<Point> outlinePoints;
    private final List<Point> controlPoints;

    public static Builder builder() {
        return new Builder();
    }

    private Contour(List<Segment> segments, List<Point> outlinePoints, List<Point> controlPoints) {
        this.segments = List.copyOf(segments);
        this.outlinePoints = List.copyOf(outlinePoints);
        this.controlPoints = List.copyOf(controlPoints);
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

    public Contour moveStartPointToSegmentClosestTo(Point p) {
        List<Segment> movedSegments = new LinkedList<>(segments);
        Segment closestSegment = Collections.min(
                movedSegments,
                (s1, s2) -> (int) (s1.getFrom().distanceTo(p) - s2.getFrom().distanceTo(p))
        );
        int closestSegmentIndex = segments.indexOf(closestSegment);
        Collections.rotate(movedSegments, -closestSegmentIndex);
        return new Contour(movedSegments, outlinePoints, controlPoints);
    }

    public static class Builder {
        private Point currentPosition;
        private final List<Segment> segments;
        private final List<Point> outlinePoints;
        private final List<Point> controlPoints;

        private Builder() {
            this.segments = new LinkedList<>();
            this.outlinePoints = new ArrayList<>();
            this.controlPoints = new ArrayList<>();
        }

        public Builder startAt(Point p) {
            if(currentPosition != null) {
                throw new IllegalStateException("Contour has already been started.");
            }

            this.currentPosition = p;
            this.outlinePoints.add(p);
            return this;
        }

        public Builder lineTo(Point to) {
            checkIfValidToOperation(to);

            segments.add(new Line(currentPosition, to));
            outlinePoints.add(to);
            currentPosition = to;
            return this;
        }

        public Builder cubicTo(Point c1, Point c2, Point to) {
            checkIfValidToOperation(to);

            segments.add(new CubicBezierCurve(currentPosition, c1, c2, to));
            outlinePoints.add(to);
            controlPoints.add(c1);
            controlPoints.add(c2);
            currentPosition = to;
            return this;
        }

        public Contour build() {
            if(segments.isEmpty()) {
                throw new IllegalStateException("Cannot create an emtpy contour. Add a segment first.");
            }

            // close contour with line to beginning
            Point start = segments.getFirst().getFrom();
            if(!start.equals(currentPosition)) {
                segments.add(new Line(currentPosition, start));
            }
            return new Contour(segments, outlinePoints, controlPoints);
        }

        public List<Segment> getSegments() {
            return Collections.unmodifiableList(segments);
        }

        public boolean isStarted() {
            return currentPosition != null;
        }

        private void checkIfValidToOperation(Point to) {
            if(currentPosition == null) {
                throw new IllegalStateException("Contour has not been started. Call startAt(Point p) first.");
            }

            if(currentPosition.equals(to)) {
                throw new IllegalStateException("Cannot add a segment where start point is equals to end point.");
            }
        }
    }
}
