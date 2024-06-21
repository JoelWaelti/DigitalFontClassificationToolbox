package ch.fhnw.ip5.digitalfontclassification.domain;

import ch.fhnw.ip5.digitalfontclassification.domain.Contour;

import java.util.List;

public class Glyph {
    private final char character;
    private final List<Contour> contours;
    private final BoundingBox boundingBox;

    public Glyph(char character, List<Contour> contours, BoundingBox boundingBox) {
        this.character = character;
        this.contours = contours;
        this.boundingBox = boundingBox;
    }

    public List<Contour> getContours() {
        return contours;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public char getCharacter() {
        return character;
    }
}
