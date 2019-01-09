/*
 * Copyright 2017 Wultra s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.entity;

import com.google.common.io.BaseEncoding;
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

/**
 * Signature and data for QR code in offline mode for mobile token.
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OfflineSignatureQRCode {

    private static final Logger logger = LoggerFactory.getLogger(OfflineSignatureQRCode.class);

    private final int size;
    private final String data;
    private final String nonce;

    /**
     * QR code constructor.
     * @param size QR code size.
     * @param data QR code data.
     * @param nonce Nonce.
     */
    public OfflineSignatureQRCode(int size, String data, String nonce) {
        this.size = size;
        this.data = data;
        this.nonce = nonce;
    }

    /**
     * Get QR code size.
     * @return QR code size.
     */
    public int getSize() {
        return size;
    }

    /**
     * Get QR code data.
     * @return QR code data.
     */
    public String getData() {
        return data;
    }

    /**
     * Get nonce.
     * @return Nonce.
     */
    public String getNonce() {
        return nonce;
    }

    /**
     * Encodes the QR code data into a String-based PNG image.
     * @return Generated QR code.
     */
    public String generateImage() {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(
                    new String(data.getBytes("UTF-8"), "ISO-8859-1"),
                    BarcodeFormat.QR_CODE,
                    size,
                    size);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] bytes = baos.toByteArray();
            return "data:image/png;base64," + BaseEncoding.base64().encode(bytes);
        } catch (WriterException | IOException e) {
            logger.error(
                    "Error occurred while generating QR code",
                    e
            );
        }
        return null;
    }

}
