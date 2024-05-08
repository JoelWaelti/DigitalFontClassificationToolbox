package ch.fhnw.ip5.digitalfontclassification.plot;

import java.util.List;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.function.Consumer;

public class PlotUtil {

    public static void doForEachFontInDirectory(String sourceDirectory, Consumer<Path> doForEachFont) throws IOException {
        for (Path p : findOTFFiles(sourceDirectory)) {
            doForEachFont.accept(p);
        }
    }

    private static List<Path> findOTFFiles(String sourcePath) throws IOException {
        List<Path> otfFiles = new ArrayList<>();
        Path start = Paths.get(sourcePath);

        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().toLowerCase().endsWith(".otf")) {
                    otfFiles.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return otfFiles;
    }
}
