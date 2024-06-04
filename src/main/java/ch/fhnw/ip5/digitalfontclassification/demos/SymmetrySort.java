package ch.fhnw.ip5.digitalfontclassification.demos;

import ch.fhnw.ip5.digitalfontclassification.domain.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static ch.fhnw.ip5.digitalfontclassification.analysis.SymmetryAnalyzer.determineSymmetry;

public class SymmetrySort {

    // Args: <source font> <source visualization> <target> <character> <fontSize> <flatness>
    public static void main(String[] args) throws FontParserException, IOException {
        String originFontPath = args[0];
        File fontPath = new File(originFontPath);

        String sourceVisPath = args[1];
        String target = args[2];

        char character = args[3].charAt(0);
        int unitsPerEm = Integer.parseInt(args[3]);
        double flatness = Double.parseDouble(args[5]);

        System.out.println(fontPath.getPath());
        for (final File fontClass : fontPath.listFiles()) {
            System.out.println(fontClass.getPath());
            for (final File file : fontClass.listFiles()) {
                FontParser parser = new ApacheFontBoxFontParser(file.getPath());
                Glyph glyph = parser.getGlyph(character, unitsPerEm);
                Flattener flattener = new JavaAwtFlattener(flatness);
                Glyph flattenedGlyph = flattener.flatten(glyph);

                boolean symmetry = determineSymmetry(flattenedGlyph);

                if (symmetry) {
                    System.out.println(file.getPath());
                    Path sourcePlot = Path.of(sourceVisPath + "/" + fontClass.getName() + "/" + file.getName() + ".jpg");
                    Path targetPlot = Path.of(target + "/" + fontClass.getName() + "/" + file.getName() + ".jpg");

                    if (!Files.exists(targetPlot.getParent())) {
                        Files.createDirectories(targetPlot.getParent());
                        System.out.println("Directory created: " + targetPlot.getParent());
                    }

                    Files.copy(sourcePlot, targetPlot, StandardCopyOption.REPLACE_EXISTING);
                }
            }

        }
    }

}
