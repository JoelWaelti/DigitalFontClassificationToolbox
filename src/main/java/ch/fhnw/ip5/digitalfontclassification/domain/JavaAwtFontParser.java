package ch.fhnw.ip5.digitalfontclassification.domain;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class JavaAwtFontParser extends FontParser {

    private final Font defaultFont;
    private final FontRenderContext frc;

    public JavaAwtFontParser(String fontPath) throws FontParserException {
        try {
            defaultFont = Font.createFont(java.awt.Font.TRUETYPE_FONT, new File(fontPath));
            frc = new FontRenderContext(null, true, true);
        } catch(FontFormatException | IOException e) {
            throw new FontParserException("Error while parsing font with Java AWT", e);
        }
    }

    @Override
    public String getFontName() {
        return this.defaultFont.getFontName();
    }

    @Override
    public Glyph getGlyph(char character, int fontSize) {
        Font font = defaultFont.deriveFont(fontSize);
        GlyphVector glyphVector = font.createGlyphVector(frc, new char[]{character});
        Shape glyphOutline = glyphVector.getGlyphOutline(0);

        List<Contour> contours;
        PathIterator pathIterator = glyphOutline.getPathIterator(new AffineTransform());
        try {
            contours = buildContours(pathIterator, 1);
        } catch(IllegalStateException e) {
            throw new IllegalArgumentException("The provided font does not have a valid format in the path description of this character.");
        }

        Rectangle bounds = glyphOutline.getBounds();
        BoundingBox boundingBox = new BoundingBox(
                bounds.getMinX(),
                bounds.getMaxX(),
                bounds.getMinY(),
                bounds.getMaxY()
        );

        return new Glyph(character, contours, boundingBox);
    }

    @Override
    public Glyph getGlyph(char c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getUnitsPerEm() throws FontParserException {
        return 0;
    }
}
