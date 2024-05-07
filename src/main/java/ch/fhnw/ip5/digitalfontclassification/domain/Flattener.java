package ch.fhnw.ip5.digitalfontclassification.domain;

public interface Flattener {
    Glyph flatten(Glyph glyph);
    Contour flatten(Contour contour);
}
