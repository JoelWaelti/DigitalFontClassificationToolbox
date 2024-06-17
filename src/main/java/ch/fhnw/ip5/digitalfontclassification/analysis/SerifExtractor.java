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
    public static void main(String[] args) throws IOException, FontParserException {
        String fontPath = args[0];
        String targetPath = args[1];
        char character = args[2].charAt(0);
        int unitsPerEm = Integer.parseInt(args[3]);
        double flatness = Double.parseDouble(args[4]);

        FontParser parser = new JavaAwtFontParser(fontPath);
        Glyph glyph = parser.getGlyph(character, unitsPerEm);
        Flattener flattener = new JavaAwtFlattener(flatness);
        Glyph flattenedGlyph = flattener.flatten(glyph);

        BufferedImage bi = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        DrawUtil.setOriginToBottomLeft(g2d, bi.getHeight(), 100);

        Supplier<Color> colorSupplier = () -> Color.RED;
        DrawUtil.drawLines(getSerifLines(flattenedGlyph), g2d, colorSupplier);

        Path plotFilePath = Path.of(targetPath, "serif.png");
        File plotFile = plotFilePath.toFile();
        if (!Files.exists(plotFilePath.getParent())) {
            Files.createDirectories(plotFilePath.getParent());
        }
        System.out.println(plotFilePath);
        ImageIO.write(bi, "PNG", plotFile);
    }

    private static final double THRESHOLD = 0.2;

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
        /*int index = forward ? startIndex : startIndex - 1;
        int step = forward ? 1 : -1;
        int endIndex = forward ? startIndex + directions.length : startIndex;*/
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

    public static List<Line> getSerifLines(Glyph glyph) {
        Contour contour = glyph.getContours().getFirst();
        contour = contour.moveStartPointToSegmentClosestTo(new Point(0,0));
        Line[] lines = new Line[contour.getSegments().size()];
        int i = 0;
        for(Segment s : contour.getSegments()) {
            lines[i] = (Line) s;
            i++;
        }

        double[] directions = ContourDirectionAnalyzer.getDirectionsAlongContour(contour);
        double distanceWalkedUpwards = 0;
        int serifEndIndex = 0;

        for(int j = 0; j < directions.length; j++) {
            if(directions[j] > 45 && directions[j] < 135) {
                distanceWalkedUpwards += lines[j].getLength();

                if(distanceWalkedUpwards > glyph.getBoundingBox().getHeight() * THRESHOLD) {
                    break;
                }
            } else {
                distanceWalkedUpwards = 0;
            }

            serifEndIndex = j;
        }

        double distanceWalkedDownwards = 0;
        int serifStartIndex = directions.length;

        for(int j = directions.length - 1; j >= 0; j--) {
            if(directions[j] > 225 && directions[j] < 315) {
                distanceWalkedDownwards += lines[j].getLength();

                if(distanceWalkedDownwards > glyph.getBoundingBox().getHeight() * THRESHOLD) {
                    break;
                }
            } else {
                distanceWalkedDownwards = 0;
            }
            serifStartIndex = j;
        }

        List<Line> serifLines = new ArrayList<>();
        int serifLineCount = lines.length - serifStartIndex + serifEndIndex + 1;
        for(int j = 0; j < serifLineCount; j++) {
            serifLines.add(lines[(serifStartIndex + j) % lines.length]);
        }

        return serifLines;
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