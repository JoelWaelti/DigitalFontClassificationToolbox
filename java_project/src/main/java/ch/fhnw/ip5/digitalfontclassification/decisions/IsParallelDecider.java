package ch.fhnw.ip5.digitalfontclassification.decisions;

import ch.fhnw.ip5.digitalfontclassification.domain.Flattener;
import ch.fhnw.ip5.digitalfontclassification.domain.FontParser;
import ch.fhnw.ip5.digitalfontclassification.domain.Glyph;
import ch.fhnw.ip5.digitalfontclassification.domain.JavaAwtFlattener;
import ch.fhnw.ip5.digitalfontclassification.domain.JavaAwtFontParser;
import ch.fhnw.ip5.digitalfontclassification.domain.Line;
import ch.fhnw.ip5.digitalfontclassification.plot.PlotUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ch.fhnw.ip5.digitalfontclassification.analysis.SerifAnalyzer.analyzeSerifs;
import static ch.fhnw.ip5.digitalfontclassification.analysis.SerifAnalyzer.hasSerif;
import static ch.fhnw.ip5.digitalfontclassification.analysis.SerifAnalyzer.isParallel;
import static ch.fhnw.ip5.digitalfontclassification.decisions.SerifSort.getVisualizationAsBufferedImage;

public class IsParallelDecider {
    // Args: <source path> <target path1> <target path2> <character> <font size> <flatness> <spacing>
    public static void main(String[] args) throws IOException {
        String sourcePath = args[0];
        String targetPath1 = args[1];
        String targetPath2 = args[2];
        char character = args[3].charAt(0);
        float fontSize = Float.parseFloat(args[4]);
        double flatness = Double.parseDouble(args[5]);
        double spacing = Double.parseDouble(args[6]);

        PlotUtil.doForEachFontInDirectory(sourcePath, fontPath -> {
            try {
                System.out.println(fontPath);

                FontParser parser = new JavaAwtFontParser(fontPath.toString());
                Glyph glyph = parser.getGlyph(character, fontSize);
                Flattener flattener = new JavaAwtFlattener(flatness);
                Glyph flattenedGlyph = flattener.flatten(glyph);

                Map<String, List<Line>> allSerifLines = analyzeSerifs(flattenedGlyph, 0.2, spacing);

                List<Line> seriflinesHorizontal = allSerifLines.get("seriflinesHorizontal");
                List<Line> stammlinesHorizontal = allSerifLines.get("stammlinesHorizontal");
                List<Line> serifLines = allSerifLines.get("serifLines");

                List<Line> linesToDraw = new ArrayList<>();
                linesToDraw.addAll(seriflinesHorizontal);
                linesToDraw.addAll(stammlinesHorizontal);

                BufferedImage bufferedImage = getVisualizationAsBufferedImage(flattenedGlyph, linesToDraw);


                boolean hasSerif = hasSerif(seriflinesHorizontal, stammlinesHorizontal, serifLines);

                if(!hasSerif && isParallel(seriflinesHorizontal, stammlinesHorizontal)) {
                    Path relativePath = Path.of(sourcePath).relativize(fontPath);
                    Path plotFilePath = Path.of(targetPath1, relativePath + ".jpg");
                    File plotFile = plotFilePath.toFile();
                    if (!Files.exists(plotFilePath.getParent())) {
                        Files.createDirectories(plotFilePath.getParent());
                    }

                    ImageIO.write(bufferedImage, "PNG", plotFile);
                } else {
                    Path relativePath = Path.of(sourcePath).relativize(fontPath);
                    Path plotFilePath = Path.of(targetPath2, relativePath + ".jpg");
                    File plotFile = plotFilePath.toFile();
                    if (!Files.exists(plotFilePath.getParent())) {
                        Files.createDirectories(plotFilePath.getParent());
                    }

                    ImageIO.write(bufferedImage, "PNG", plotFile);
                }

            }  catch (Exception e) {
                System.err.println("Error processing " + fontPath + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
