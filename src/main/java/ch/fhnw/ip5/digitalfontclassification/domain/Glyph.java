package ch.fhnw.ip5.digitalfontclassification.domain;

import ch.fhnw.ip5.digitalfontclassification.domain.Contour;

import java.util.List;

public class Glyph {
    private char character;
    private float fontSize;
    private List<Contour> contours;
    private BoundingBox boundingBox;

    public Glyph(char character, float fontSize, List<Contour> contours, BoundingBox boundingBox) {
        this.character = character;
        this.fontSize = fontSize;
        this.contours = contours;
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

    public float getFontSize() {
        return fontSize;
    }
}
