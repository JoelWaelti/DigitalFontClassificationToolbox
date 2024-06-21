package ch.fhnw.ip5.digitalfontclassification.analysis;

import ch.fhnw.ip5.digitalfontclassification.domain.*;
import ch.fhnw.ip5.digitalfontclassification.domain.Point;
import java.util.ArrayList;
import java.util.List;

public class SerifExtractor {
    private final Glyph glyph;
    private final double serifHeightThreshold;
    private double[] directions;
    private Line[] lines;

    public SerifExtractor(Glyph glyph, double serifHeightThreshold) {
        this.glyph = glyph;
        this.serifHeightThreshold = serifHeightThreshold;
        init();
    }

    private void init() {
        Contour contour = glyph.getContours().getFirst();

        this.lines = new Line[contour.getSegments().size()];
        int i = 0;
        for(Segment s : contour.getSegments()) {
            lines[i] = (Line) s;
            i++;
        }

        this.directions = ContourDirectionAnalyzer.getDirectionsAlongContour(contour);
    }

    public List<Line> getSerifAt(SerifLocation location) {
        BoundingBox bb = glyph.getBoundingBox();

        Point startPoint = null;
        boolean fromBottom = true;
        if(location == SerifLocation.BOTTOM_LEFT) {
            startPoint = new Point(0,0);
        } else if(location == SerifLocation.BOTTOM_RIGHT) {
            startPoint = new Point(bb.getMaxX(), 0);
        } else if (location == SerifLocation.TOP_LEFT) {
            startPoint = new Point(0, bb.getMaxY());
            fromBottom = false;
        } else if(location == SerifLocation.TOP_RIGHT) {
            startPoint = new Point(bb.getMaxX(), bb.getMaxY());
            fromBottom = false;
        }

        int startAtIndex = findIndexOfLineClosestToPoint(startPoint);
        int endIndex = walkPathUntilThreshold(startAtIndex, true, fromBottom);
        int startIndex = walkPathUntilThreshold(startAtIndex, false, fromBottom);

        return getLinesOfSerifByStartAndEndIndex(startIndex, endIndex);
    }

    private int walkPathUntilThreshold(int startIndex, boolean forward, boolean fromBottom) {
        BoundingBox bb = glyph.getBoundingBox();
        double threshold;
        if(fromBottom) {
            threshold = bb.getHeight() * serifHeightThreshold;
        } else {
            threshold = bb.getHeight() - (serifHeightThreshold * bb.getHeight());
        }

        int index = forward ? 0 : directions.length - 1;
        int step = forward ? 1 : -1;
        int endIndex = forward ? directions.length : -1;

        int correctedIndex = -1;
        while (index != endIndex) {
            correctedIndex = (startIndex + index) % directions.length;
            double y = forward ? lines[correctedIndex].getTo().y() : lines[correctedIndex].getFrom().y();
            if((fromBottom && y > threshold) || (!fromBottom && y < threshold)) {
                break;
            }
            index += step;
        }

        return correctedIndex;
    }

    public List<List<Line>> getAllSerifs() {
        return new ArrayList<>() {{
            add(getSerifAt(SerifLocation.BOTTOM_LEFT));
            add(getSerifAt(SerifLocation.BOTTOM_RIGHT));
            add(getSerifAt(SerifLocation.TOP_LEFT));
            add(getSerifAt(SerifLocation.TOP_RIGHT));
        }};
    }

    private List<Line> getLinesOfSerifByStartAndEndIndex(int startIndex, int endIndex) {
        List<Line> serifLines = new ArrayList<>();
        int index = startIndex;
        // make endIndex inclusive
        endIndex = (endIndex + 1) % lines.length;
        while(index != endIndex) {
            serifLines.add(lines[index]);
            index = (index + 1) % lines.length;
        }
        return serifLines;
    }

    private int findIndexOfLineClosestToPoint(Point point) {
        int closestLineIndex = 0;
        double clostestDistance = lines[0].getFrom().distanceTo(point);
        for(int i = 1; i < lines.length; i++) {
            double currentDistance = lines[i].getFrom().distanceTo(point);
            if(clostestDistance > currentDistance) {
                clostestDistance = currentDistance;
                closestLineIndex = i;
            }
        }
        return closestLineIndex;
    }

    public enum SerifLocation {
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        TOP_LEFT,
        TOP_RIGHT
    }
}