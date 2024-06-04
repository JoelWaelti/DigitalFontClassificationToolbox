package ch.fhnw.ip5.digitalfontclassification.analysis.thickness;

import ch.fhnw.ip5.digitalfontclassification.domain.*;

import java.util.ArrayList;
import java.util.List;

public class EvenlyDistributedThicknessAnalyzer extends ThicknessAnalyzer {
    private double spacing;

    public EvenlyDistributedThicknessAnalyzer(double spacing) {
        this.spacing = spacing;
    }

    public double getSpacing() {
        return this.spacing;
    }

    public void setSpacing(double spacing) {
        this.spacing = spacing;
    }

    @Override
    public List<Line> computeThicknessLines(Glyph glyph) {
        ArrayList<Line> thicknessLines = new ArrayList<>();

        // It's assumed that this first contour is the main enclosing contour of the glyph
        Contour contour = glyph.getContours().getFirst();

        double distanceLeft = 0;
        for(Segment s : contour.getSegments()) {
            if(!(s instanceof Line line)) {
                throw new IllegalArgumentException("Contours of the glyph can only contain lines for this operation. Consider flattening the entire glyph.");
            }

            double lineLength = line.getLength();
            while (distanceLeft <= lineLength) {
                Point p = line.interpolate(distanceLeft / lineLength);
                distanceLeft += spacing;

                Line thicknessLine = thicknessLineAtPointOfSegment(line, p, glyph);
                if(thicknessLine != null) {
                    thicknessLines.add(thicknessLine);
                }
            }
            distanceLeft -= lineLength;
        }

        return thicknessLines;
    }
}
