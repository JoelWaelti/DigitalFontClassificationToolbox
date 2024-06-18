package ch.fhnw.ip5.digitalfontclassification.analysis;

import ch.fhnw.ip5.digitalfontclassification.domain.*;
import ch.fhnw.ip5.digitalfontclassification.domain.Point;
import ch.fhnw.ip5.digitalfontclassification.drawing.DrawUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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

    public List<List<Line>> getAllSerifs() {
        return new ArrayList<>() {{
            add(getSerifAt(SerifLocation.BOTTOM_LEFT));
            add(getSerifAt(SerifLocation.BOTTOM_RIGHT));
            add(getSerifAt(SerifLocation.TOP_LEFT));
            add(getSerifAt(SerifLocation.TOP_RIGHT));
        }};
    }

    public List<Line> getSerifAt(SerifLocation location) {
        BoundingBox bb = glyph.getBoundingBox();

        Point startPoint = null;
        StopDirection forwardsStopDirection = StopDirection.WHEN_GOING_UP;
        StopDirection backwardsStopDirection = StopDirection.WHEN_GOING_DOWN;
        if(location == SerifLocation.BOTTOM_LEFT) {
            startPoint = new Point(0,0);
        } else if(location == SerifLocation.BOTTOM_RIGHT) {
            startPoint = new Point(bb.getMaxX(), 0);
        } else if (location == SerifLocation.TOP_LEFT) {
            startPoint = new Point(0, bb.getMaxY());
            forwardsStopDirection = StopDirection.WHEN_GOING_DOWN;
            backwardsStopDirection = StopDirection.WHEN_GOING_UP;
        } else if(location == SerifLocation.TOP_RIGHT) {
            startPoint = new Point(bb.getMaxX(), bb.getMaxY());
            forwardsStopDirection = StopDirection.WHEN_GOING_DOWN;
            backwardsStopDirection = StopDirection.WHEN_GOING_UP;
        }

        int startIndex = findIndexOfLineClosestToPoint(startPoint);
        int serifEndIndex = findIndexOfEndOfSerif(startIndex, forwardsStopDirection, true);
        int serifStartIndex = findIndexOfEndOfSerif(startIndex, backwardsStopDirection, false);

        return getLinesOfSerifByStartAndEndIndex(serifStartIndex, serifEndIndex);
    }

    private List<Line> getLinesOfSerifByStartAndEndIndex(int startIndex, int endIndex) {
        List<Line> serifLines = new ArrayList<>();
        int index = startIndex;
        while(index != endIndex) {
            serifLines.add(lines[index]);
            index = (index + 1) % lines.length;
        }
        return serifLines;
    }

    private int findIndexOfEndOfSerif(int startIndex, StopDirection stopDirection, boolean forward) {
        double distanceWalked = 0;
        int index = forward ? 0 : directions.length - 1;
        int step = forward ? 1 : -1;
        int endIndex = forward ? directions.length : -1;

        int correctedIndex = -1;
        while (index != endIndex) {
            correctedIndex = (startIndex + index) % directions.length;
            if (directions[correctedIndex] > stopDirection.minAngle && directions[correctedIndex] < stopDirection.maxAngle) {
                distanceWalked += lines[correctedIndex].getLength();
                if (distanceWalked > glyph.getBoundingBox().getHeight() * serifHeightThreshold) {
                    break;
                }
            } else {
                distanceWalked = 0;
            }
            index += step;
        }

        return correctedIndex;
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

    private enum StopDirection {
        WHEN_GOING_UP(45, 135),
        WHEN_GOING_DOWN(225, 315);

        private final int minAngle;
        private final int maxAngle;

        StopDirection(int minAngle, int maxAngle) {
            this.minAngle = minAngle;
            this.maxAngle = maxAngle;
        }

        public int getMinAngle() {
            return minAngle;
        }

        public int getMaxAngle() {
            return maxAngle;
        }
    }
}