package ch.fhnw.ip5.digitalfontclassification.analysis.thickness;

import ch.fhnw.ip5.digitalfontclassification.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MiddleOfLineThicknessAnalyzer extends ThicknessAnalyzer {
    @Override
    public List<Line> computeThicknessLines(List<Line> linesToGetThicknessLinesOf, List<Line> allLines) {
        ArrayList<Line> thicknessLines = new ArrayList<>();

        for(Line line : linesToGetThicknessLinesOf) {
            Point centerOfLine = new Point(
                    (line.getFrom().x() + line.getTo().x()) / 2,
                    (line.getFrom().y() + line.getTo().y()) / 2
            );
            Line thicknessLine = thicknessLineAtPointOfLine(line, centerOfLine, allLines);
            if(thicknessLine != null) {
                thicknessLines.add(thicknessLine);
            }
        }

        return thicknessLines;
    }
}
