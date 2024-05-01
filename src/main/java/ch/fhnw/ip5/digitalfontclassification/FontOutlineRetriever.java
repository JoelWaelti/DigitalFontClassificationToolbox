package ch.fhnw.ip5.digitalfontclassification;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.IOException;

public class FontOutlineRetriever {

    public static void main(String[] args) throws IOException, FontFormatException {
        // Load the font file
        Font font = Font.createFont(Font.TRUETYPE_FONT, new File("/Users/julielhote/FHNW/Fonts/grotesk_reg/AntiqueOlivePro-Light.otf"));

        // Create a GlyphVector for the character 'H'
        char character = 'H';
        FontRenderContext frc = new FontRenderContext(null, true, true);
        GlyphVector glyphVector = font.createGlyphVector(frc, new char[]{character});

        // Get the outline of the glyph as a Shape
        Shape glyphOutline = glyphVector.getGlyphOutline(0);

        // Create a PathIterator to iterate over the outline and control points
        PathIterator pathIterator = glyphOutline.getPathIterator(new AffineTransform());
        float[] coords = new float[6];
        while (!pathIterator.isDone()) {
            int type = pathIterator.currentSegment(coords);
            switch (type) {
            case PathIterator.SEG_MOVETO:
                System.out.println("Move to: " + coords[0] + ", " + coords[1]);
                break;
            case PathIterator.SEG_LINETO:
                System.out.println("Line to: " + coords[0] + ", " + coords[1]);
                break;
            case PathIterator.SEG_QUADTO:
                System.out.println("Quad to: " + coords[0] + ", " + coords[1] + ", " + coords[2] + ", " + coords[3]);
                break;
            case PathIterator.SEG_CUBICTO:
                System.out.println("Cubic to: " + coords[0] + ", " + coords[1] + ", " + coords[2] + ", " + coords[3] + ", " + coords[4] + ", " + coords[5]);
                break;
            case PathIterator.SEG_CLOSE:
                System.out.println("Close path");
                break;
            }
            pathIterator.next();
        }
    }
}