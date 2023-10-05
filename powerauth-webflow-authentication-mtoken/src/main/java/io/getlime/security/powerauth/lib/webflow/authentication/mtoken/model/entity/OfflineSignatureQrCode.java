/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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

package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.entity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Signature and data for QR code in offline mode for mobile token.
 *
 * @param size  QR code size.
 * @param data  QR code data.
 * @param nonce Nonce.
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public record OfflineSignatureQrCode(int size, String data, String nonce) {

    private static final Logger logger = LoggerFactory.getLogger(OfflineSignatureQrCode.class);

    /**
     * Encodes the QR code data into a String-based PNG image.
     *
     * @return Generated QR code.
     */
    public String generateImage() {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(
                    new String(data.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1),
                    BarcodeFormat.QR_CODE,
                    size,
                    size);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (WriterException | IOException e) {
            logger.error(
                    "Error occurred while generating QR code",
                    e
            );
        }
        return null;
    }

}
