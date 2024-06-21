package ch.fhnw.ip5.digitalfontclassification.domain;

import ch.fhnw.ip5.digitalfontclassification.domain.Point;

public abstract class Segment {
    private Point from;
    private Point to;

    public Segment(Point from, Point to) {
        if(from == null || to == null) {
            throw new IllegalArgumentException();
        }

        this.from = from;
        this.to = to;
    }

    public Point getFrom() {
        return from;
    }

    public Point getTo() {
        return to;
    }
}
