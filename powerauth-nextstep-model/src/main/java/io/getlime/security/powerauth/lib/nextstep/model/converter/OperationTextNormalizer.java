/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2018 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
