package ch.fhnw.ip5.digitalfontclassification.domain;

import org.apache.fontbox.FontBoxFont;
import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFType1Font;
import org.apache.fontbox.cff.Type1CharStringParser;
import org.apache.fontbox.ttf.*;
import org.apache.fontbox.type1.Type1Font;
import org.apache.pdfbox.io.RandomAccessInputStream;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.fontbox.type1.Type1CharStringReader;

import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.io.*;
import java.util.List;

public class ApacheFontBoxFontParser extends FontParser {
    private final OpenTypeFont font;
    private CFFType1Font cffType1Font = null;
    private final HeaderTable headerTable;

    public ApacheFontBoxFontParser(String fontPath) throws FontParserException {
        try {
            File fontFile = new File(fontPath);
            OTFParser parser = new OTFParser();
            this.font = parser.parse(new RandomAccessReadBufferedFile(fontFile));

            if(this.font.isPostScript()) {
                CFFFont cffFont = font.getCFF().getFont();

                if (cffFont instanceof CFFType1Font) {
                    this.cffType1Font = (CFFType1Font) cffFont;
                } else {
                    System.out.println("The font does not contain CFFType1Font data.");
                }
            }

            this.headerTable = this.font.getHeader();
        } catch(IOException e) {
            throw new FontParserException("Error while parsing font with Apache FontBox", e);
        }
    }

    @Override
    public String getFontName() throws FontParserException {
        try {
            return font.getName();
        } catch (IOException e) {
            throw new FontParserException("Error while retrieving font name with Apache FontBox", e);
        }
    }

    @Override
    public Glyph getGlyph(char c, int unitsPerEm) throws FontParserException {
        try {
            GeneralPath glyphPath;
            BoundingBox boundingBox;

            if(cffType1Font != null) {
                int glyphID = font.getUnicodeCmapLookup().getGlyphId(c);
                String name = cffType1Font.getCharset().getNameForGID(glyphID);
                glyphPath = cffType1Font.getPath(name);
                var bbox = cffType1Font.getFontBBox();
                boundingBox = new BoundingBox(
                        bbox.getLowerLeftX(),
                        bbox.getUpperRightX(),
                        bbox.getLowerLeftY(),
                        bbox.getUpperRightY()
                );
            } else {
                GlyphTable glyphTable = font.getGlyph();
                int glyphIndex = font.getUnicodeCmapLookup().getGlyphId(c);
                GlyphData glyphData = glyphTable.getGlyph(glyphIndex);
                glyphPath = glyphData.getPath();
                boundingBox = new BoundingBox(
                        glyphData.getXMinimum(),
                        glyphData.getXMaximum(),
                        glyphData.getYMinimum(),
                        glyphData.getYMaximum()
                );
            }

            double scaleFactor = (double) unitsPerEm / getUnitsPerEm();
            List<Contour> contours = buildContours(glyphPath.getPathIterator(null), scaleFactor);

            return new Glyph(c, contours, boundingBox);
        } catch(IOException e) {
            throw new FontParserException("Error while retrieving glyph with Apache FontBox", e);
        }
    }

    @Override
    public int getUnitsPerEm() {
        return headerTable.getUnitsPerEm();
    }
}
