package ch.fhnw.ip5.digitalfontclassification.analysis.thickness;

import ch.fhnw.ip5.digitalfontclassification.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MiddleOfLineThicknessAnalyzer extends ThicknessAnalyzer {
    @Override
    public List<Line> computeThicknessLines(Glyph glyph) {
        ArrayList<Line> thicknessLines = new ArrayList<>();

        // It's assumed that this first contour is the main enclosing contour of the glyph
        Contour contour = glyph.getContours().getFirst();

        for(Segment s : contour.getSegments()) {
            if(!(s instanceof Line line)) {
                throw new IllegalArgumentException("Contours of the glyph can only contain lines for this operation. Consider flattening the entire glyph.");
            }

            Point centerOfLine = new Point(
                    (line.getFrom().x() + line.getTo().x()) / 2,
                    (line.getFrom().y() + line.getTo().y()) / 2
            );
            Line thicknessLine = thicknessLineAtPointOfSegment(line, centerOfLine, glyph);
            if(thicknessLine != null) {
                thicknessLines.add(thicknessLine);
            }
        }

        return thicknessLines;
    }
}
