package ch.fhnw.ip5.digitalfontclassification;

import java.util.List;

public interface FontParser {
    String getFontName();
    Glyph getGlyph(char c, float fontSize);
}
