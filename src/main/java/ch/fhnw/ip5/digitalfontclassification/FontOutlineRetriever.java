package ch.fhnw.ip5.digitalfontclassification;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
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
            FontOutlineRetriever frame = new FontOutlineRetriever("/Users/julielhote/FHNW/Fonts/skript_reg/AmadeoStd.otf");
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

                    List<Point2D> lineSegments = convertBezierToLineSegments(curve, 0.001);
                    all_points.addAll(lineSegments);

                    prev_pointX = coords[4];
                    prev_pointY = -coords[5];
                }
            }
            pathIterator.next();
        }

        System.out.println(all_points.size());
        System.out.println(control_points.size());

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
        dataset.addSeries(control_series);
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
                        points.add(previousPoint); // Add start point of the segment
                        points.add(currentPoint);  // Add end point of the segment
                    }
                    previousPoint = currentPoint; // Update the previous point
                    break;
            }
            iterator.next();
        }

        System.out.println(points.size());
        for (int i = 0; i < points.size(); i += 2) {
            Point2D start = points.get(i);
            Point2D end = points.get(i + 1);
            System.out.println("Segment from (" + start.getX() + ", " + start.getY() + ") to (" + end.getX() + ", " + end.getY() + ")");
        }

        return points;
    }
}
