package ch.fhnw.ip5.digitalfontclassification.analysis.thickness;

import ch.fhnw.ip5.digitalfontclassification.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class EvenlyDistributedThicknessAnalyzer extends ThicknessAnalyzer {
    private double spacing;

    public EvenlyDistributedThicknessAnalyzer(double spacing, ThicknessLineFilter<Line, Line, Line> filter) {
        super(filter);
        this.spacing = spacing;
    }

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
    public List<Line> computeThicknessLines(List<Line> linesToGetThicknessLinesOf, List<Line> allLines) {
        ArrayList<Line> thicknessLines = new ArrayList<>();
        double distanceLeft = 0;
        for(Line line : linesToGetThicknessLinesOf) {
            double lineLength = line.getLength();
            while (distanceLeft <= lineLength) {
                Point p = line.interpolate(distanceLeft / lineLength);
                distanceLeft += spacing;

                Line thicknessLine = thicknessLineAtPointOfLine(line, p, allLines);
                if(thicknessLine != null) {
                    thicknessLines.add(thicknessLine);
                }
            }
            distanceLeft -= lineLength;
        }

        return thicknessLines;
    }
}
