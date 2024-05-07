package ch.fhnw.ip5.digitalfontclassification;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;

public class FontOutlineRetriever extends JFrame {

    public static void main(String[] args) {
        try {
            FontOutlineRetriever frame = new FontOutlineRetriever("/Users/julielhote/FHNW/Fonts/skript_reg/KulukundisITCStd.otf");
            frame.setVisible(true);
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
        }
    }

    public FontOutlineRetriever(String fontpath) throws IOException, FontFormatException {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        // Load the font file
        Font font = Font.createFont(Font.TRUETYPE_FONT, new File(fontpath));
        font = font.deriveFont(72f);
        char character = 'H';
        FontRenderContext frc = new FontRenderContext(null, true, true);
        GlyphVector glyphVector = font.createGlyphVector(frc, new char[]{character});
        Shape glyphOutline = glyphVector.getGlyphOutline(0);

        // Collect points from the glyph outline
        List<Point2D> all_points = new ArrayList<>();
        List<Point2D> outline_points = new ArrayList<>();
        List<Point2D> control_points = new ArrayList<>();

        PathIterator pathIterator = glyphOutline.getPathIterator(new AffineTransform());
        double[] coords = new double[6];

        double prev_pointX = 0.0f;
        double prev_pointY = 0.0f;
        while (!pathIterator.isDone()) {
            int type = pathIterator.currentSegment(coords);
            if (type != PathIterator.SEG_CLOSE) {
                if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
                    // These are outline points (either moving to or drawing a line to).
                    outline_points.add(new Point2D.Double(coords[0], -coords[1]));
                    all_points.add(new Point2D.Double(coords[0], -coords[1]));
                    prev_pointX = coords[0];
                    prev_pointY = -coords[1];
                } else if (type == PathIterator.SEG_CUBICTO) {
                    // The final coords in a CUBICTO are the actual new outline point.
                    outline_points.add(new Point2D.Double(coords[4], -coords[5]));
                    // The first two pairs are control points.
                    control_points.add(new Point2D.Double(coords[0], -coords[1]));
                    control_points.add(new Point2D.Double(coords[2], -coords[3]));

                    CubicCurve2D.Double curve = new CubicCurve2D.Double(
                        prev_pointX, prev_pointY,
                        coords[0], -coords[1],
                        coords[2], -coords[3],
                        coords[4], -coords[5]
                    );

                    List<Point2D> lineSegments = convertBezierToLineSegments(curve, 0.1);
                    all_points.addAll(lineSegments);

                    prev_pointX = coords[4];
                    prev_pointY = -coords[5];
                }
            }
            pathIterator.next();
        }

        System.out.println(outline_points.size());
        System.out.println(all_points.size());
        System.out.println(all_points);

        ArrayList<Double> allDistances = new ArrayList<>();

        // go through all points of flattened outline
        // then create lines from points
        // then calculate nearest intersection/distance of perp. line
        for(int i = 1; i < all_points.size(); i++) {
            Point2D start = all_points.get(i-1);
            Point2D end = all_points.get(i);

            System.out.println("startPoint:" + start);
            System.out.println("endPoint:" + end);

            Line2D currLine = new Line2D.Double(start, end);
            double distanceToPerpendicularPoint = distanceNearestIntersection(currLine, all_points, glyphOutline.getBounds());
            allDistances.add(distanceToPerpendicularPoint);
        }

        System.out.println(allDistances);

        XYSeries outline_series = new XYSeries("Glyph Outline");
        for (Point2D point : outline_points) {
            outline_series.add(point.getX(), point.getY());
        }

        XYSeries control_series = new XYSeries("Control Points");
        for (Point2D point : control_points) {
            control_series.add(point.getX(), point.getY());
        }

        XYSeries all_points_series = new XYSeries("All Points");
        for (Point2D point : all_points) {
            all_points_series.add(point.getX(), point.getY());
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(all_points_series);
        //dataset.addSeries(control_series);
        JFreeChart chart = ChartFactory.createScatterPlot(
            "Outline of Character '" + character + "'",
            "X", "Y",
            dataset,
            PlotOrientation.VERTICAL,
            true, true, false);

        XYPlot plot = chart.getXYPlot();
        XYItemRenderer renderer = new XYLineAndShapeRenderer(false, true);
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesPaint(1, Color.RED);

        plot.setRenderer(renderer);

        ChartPanel chartPanel = new ChartPanel(chart);
        setContentPane(chartPanel);
    }

    // Converts a Bézier curve to line segments, returning a list of points
    public static ArrayList<Point2D> convertBezierToLineSegments(CubicCurve2D curve, double flatness) {
        ArrayList<Point2D> points = new ArrayList<>();
        PathIterator iterator = curve.getPathIterator(null, flatness);
        double[] coords = new double[6];
        Point2D.Double previousPoint = null;

        while (!iterator.isDone()) {
            int type = iterator.currentSegment(coords);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    previousPoint = new Point2D.Double(coords[0], coords[1]);

                    break;
                case PathIterator.SEG_LINETO:
                    Point2D.Double currentPoint = new Point2D.Double(coords[0], coords[1]);
                    if (previousPoint != null) {
                        //points.add(previousPoint); // Add start point of the segment
                        points.add(currentPoint);  // Add end point of the segment
                    }
                    previousPoint = currentPoint; // Update the previous point
                    break;
            }
            iterator.next();
        }

        return points;
    }

    public static double distanceNearestIntersection(Line2D currentLine, List<Point2D> allPoints, Rectangle2D boundingBox) {
        // Calculate slope (Steigung)
        double currentSlope = 0; //TODO infinity überlegung
        if(currentLine.getX2() - currentLine.getX1() != 0) currentSlope = (currentLine.getY2() - currentLine.getY1())/(currentLine.getX2() - currentLine.getX1());

        // Calculate point at the middle of the line
        Point2D centerPoint = new Point2D.Double((currentLine.getX1() + currentLine.getX2()) / 2, (currentLine.getY1() + currentLine.getY2()) / 2);

        Line2D perpendicularLine = getPerpendicularLine(currentLine, currentSlope, centerPoint, boundingBox);
        System.out.println(perpendicularLine.getX1() + ", " + perpendicularLine.getY1());
        System.out.println(perpendicularLine.getX2() + ", " +  perpendicularLine.getY2());


        ArrayList<Point2D> intersections = new ArrayList<>();

        // find all intersections of perp. line with any other segment
        for(int i = 1; i < allPoints.size(); i++) {
            Point2D start = allPoints.get(i-1);
            Point2D end = allPoints.get(i);
            Line2D line = new Line2D.Double(start.getX(), start.getY(), end.getX(), end.getY());

            if(perpendicularLine.intersectsLine(line)) {
                Point2D intersection = getIntersection(perpendicularLine, line);
                System.out.println("intersection: " + intersection);
                intersections.add(intersection);
            }
        }

        // find nearst intersection
        double distanceNearestIntersectionPoint = Double.MAX_VALUE;
        double tolerance = 1e-10; // Define a small tolerance value, adjust as necessary

        for (Point2D point : intersections) {
            double tempDistance = centerPoint.distance(point);
            if (tempDistance > tolerance && distanceNearestIntersectionPoint > tempDistance) {
                distanceNearestIntersectionPoint = tempDistance;
            }
        }

        // no intersection found
        if (distanceNearestIntersectionPoint == Double.MAX_VALUE) {
            distanceNearestIntersectionPoint = 0;
        }

        System.out.println(distanceNearestIntersectionPoint);
        System.out.println();

        return distanceNearestIntersectionPoint;
    }

    public static Line2D getPerpendicularLine(Line2D line, double lineSlope, Point2D centerPoint, Rectangle2D boundingBox) {
        double endpointPerpendicularLineX = centerPoint.getX();
        double endpointPerpendicularLineY = centerPoint.getY();

        // calculate slope of perp. line
        double perpendicularSlope = 0; // TODO infinity überlegung
        if(lineSlope != 0) perpendicularSlope = (-1) / lineSlope;

        // calculate y-axis-intersection --> y = mx + b
        double b = centerPoint.getY() - perpendicularSlope * centerPoint.getX();

        double diffX = line.getX2() - line.getX1();
        double diffY = line.getY2() - line.getY1();

        boolean horizontalMoveRight = diffY == 0 && diffX > 0;
        boolean horizontalMoveLeft = diffY == 0 && 0 > diffX;
        boolean verticalMoveUp = diffX == 0 && diffY > 0;
        boolean verticalMoveDown = diffX == 0 && 0 > diffY;

        if(horizontalMoveRight) {
            System.out.println("horizontalMoveRight");
            endpointPerpendicularLineX = centerPoint.getX();
            endpointPerpendicularLineY = boundingBox.getMinY() * -1; // MaxY
        } else if(horizontalMoveLeft) {
            System.out.println("horizontalMoveLeft");
            endpointPerpendicularLineY = boundingBox.getMaxY() * -1; // MinY
            endpointPerpendicularLineX = centerPoint.getX();
        } else if(verticalMoveUp) {
            System.out.println("verticalMoveUp");
            endpointPerpendicularLineX = boundingBox.getMinX(); // MinX
            endpointPerpendicularLineY = centerPoint.getY();
        } else if(verticalMoveDown ) {
            System.out.println("verticalMoveDown");
            endpointPerpendicularLineX = boundingBox.getMaxX(); // MaxX
            endpointPerpendicularLineY = centerPoint.getY();
        } else if(diffX > 0 && diffY > 0) {
            System.out.println("NO");
            endpointPerpendicularLineX = boundingBox.getMinX();
            endpointPerpendicularLineY = linearEquationGetY(endpointPerpendicularLineX, perpendicularSlope, b);
        } else if(0 > diffX && 0 > diffY) {
            System.out.println("SW");
            endpointPerpendicularLineX = boundingBox.getMaxX();
            endpointPerpendicularLineY = linearEquationGetY(endpointPerpendicularLineX, perpendicularSlope, b);
        } else if(diffX > 0 && 0 > diffY) {
            System.out.println("SO");
            endpointPerpendicularLineX = boundingBox.getMaxX();
            endpointPerpendicularLineY = linearEquationGetY(endpointPerpendicularLineX, perpendicularSlope, b);
        } else if(0 > diffX && diffY > 0) {
            System.out.println("NW");
            endpointPerpendicularLineX = boundingBox.getMinX();
            endpointPerpendicularLineY = linearEquationGetY(endpointPerpendicularLineX, perpendicularSlope, b);
        }

        Point2D endPoint = new Point2D.Double(endpointPerpendicularLineX, endpointPerpendicularLineY);
        return new Line2D.Double(centerPoint, endPoint);
    }

    public static Point2D getIntersection(Line2D line0, Line2D line1) {
        // Line 1
        double x1 = line0.getX1();
        double y1 = line0.getY1();
        double x2 = line0.getX2();
        double y2 = line0.getY2();

        // Line 2
        double x3 = line1.getX1();
        double y3 = line1.getY1();
        double x4 = line1.getX2();
        double y4 = line1.getY2();

        // Denominator for the equations of the lines
        double denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

        if (denom == 0) {
            return null; // Lines are parallel or coincident
        }

        // Intersection point
        double intersectX = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / denom;
        double intersectY = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / denom;

        return new Point2D.Double(intersectX, intersectY);
    }

    public static double linearEquationGetY(double x, double slope, double c) {
        return slope * x + c;
    }
}
