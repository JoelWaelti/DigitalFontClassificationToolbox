package ch.fhnw.ip5.digitalfontclassification.domain;

import ch.fhnw.ip5.digitalfontclassification.domain.Contour;

import java.util.List;

public class Glyph {
    private char character;
    private float fontSize;
    private List<Contour> contours;

    public Glyph(char character, float fontSize, List<Contour> contours) {
        this.character = character;
        this.fontSize = fontSize;
        this.contours = contours;
    }

    public List<Contour> getContours() {
        return contours;
    }

    public char getCharacter() {
        return character;
    }

    public float getFontSize() {
        return fontSize;
    }
}
