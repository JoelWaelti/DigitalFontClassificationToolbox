package ch.fhnw.ip5.digitalfontclassification.domain;

public interface FontParser {
    String getFontName();
    Glyph getGlyph(char c, float fontSize);
}
