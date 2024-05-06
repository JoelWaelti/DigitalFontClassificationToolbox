package ch.fhnw.ip5.digitalfontclassification;

import java.util.List;

public interface Flattener {
    Glyph flatten(Glyph glyph);
    Contour flatten(Contour contour);
}
