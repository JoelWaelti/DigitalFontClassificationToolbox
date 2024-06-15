package ch.fhnw.ip5.digitalfontclassification.analysis;

import ch.fhnw.ip5.digitalfontclassification.domain.Line;
import ch.fhnw.ip5.digitalfontclassification.domain.Point;
import ch.fhnw.ip5.digitalfontclassification.domain.Vector;

import java.util.List;
import java.util.stream.Collectors;

public class SerifAnalyzer {

    public static boolean isHorizontalish(Line line) {
        Point from = line.getFrom();
        Point to = line.getTo();

        double deltaX = Math.abs(to.x() - from.x());
        double deltaY = Math.abs(to.y() - from.y());

        return deltaX >= deltaY * 1;
    }

    private static boolean isVerticalish(Line line) {
        Point from = line.getFrom();
        Point to = line.getTo();

        double deltaX = Math.abs(to.x() - from.x());
        double deltaY = Math.abs(to.y() - from.y());

        return deltaY >= deltaX;
    }

    public static double[] getShortHorizontalEndOfLineThicknesses(List<Line> lines, double width, double height) {
        // Filter horizontal lines using isHorizontalish and lines < width / 2
        return lines.stream()
            .filter(line -> isHorizontalish(line) && line.getLength() < width / 2 && getEndOfLine(line, width, height))
            .mapToDouble(Line::getLength)
            .toArray();
    }

    public static List<Line> getShortHorizontalEndOfLines(List<Line> lines, double width, double height) {
        return lines.stream()
            .filter(line -> isHorizontalish(line) && line.getLength() < width / 2 && getEndOfLine(line, width, height))
            .collect(Collectors.toList());
    }

    public static List<Line> getShortVerticalEndOfLines(List<Line> lines, double width, double height) {
        return lines.stream()
            .filter(line -> isVerticalish(line) && line.getLength() < width / 4 && getEndOfLine(line, width, height))
            .collect(Collectors.toList());
    }

    public static boolean getEndOfLine(Line line, double width, double height) {
        Point start = line.getFrom();

        if(start.x() >= width/2 && start.y() >= height - height/4) return true;
        else if (start.x() >= width/2 && height/4 >= start.y()) return true;
        else if (width/2 >= start.x() && start.y() >= height - height/4) return true;
        else if (width/2 >= start.x() && height/4 >= start.y()) return true;
        else return false;
    }

}
