package ch.fhnw.ip5.digitalfontclassification.domain;

public class FontParserException extends Exception {
    public FontParserException(String message) {
        super(message);
    }
    public FontParserException(String message, Throwable cause) {
        super(message, cause);
    }
    public FontParserException(Throwable cause) {
        super(cause);
    }
}
