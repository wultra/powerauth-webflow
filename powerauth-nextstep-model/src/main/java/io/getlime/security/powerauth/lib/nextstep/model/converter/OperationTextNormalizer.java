package io.getlime.security.powerauth.lib.nextstep.model.converter;

/**
 * Normalizer for text used in QR codes.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OperationTextNormalizer {

    /**
     * Normalize generic text in QR code (e.g. title and message).
     * @param text Text to normalize.
     * @return Normalized text.
     */
    public String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        // Escape \ character.
        text = text.replace("\\", "\\\\");
        // Convert newlines.
        text = text.replaceAll("\r?\n", "\\\\n");
        // Remove special characters with ASCII code < 32.
        text = text.replaceAll("^[\\\\u0000-\\\\u001F]*$", "");
        // Compress spaces.
        text = text.replaceAll("\\s+", " ");
        return text;
    }

    /**
     * Normalize operation data text in QR code (e.g. note, reference and text).
     * @param text Text to normalize.
     * @return Normalized text.
     */
    public String normalizeOperationData(String text) {
        if (text == null) {
            return "";
        }
        // Perform regular text normalization.
        text = normalizeText(text);
        // Escape * character, it has special meaning in operation data.
        text = text.replace("*", "\\*");
        return text;
    }

}
