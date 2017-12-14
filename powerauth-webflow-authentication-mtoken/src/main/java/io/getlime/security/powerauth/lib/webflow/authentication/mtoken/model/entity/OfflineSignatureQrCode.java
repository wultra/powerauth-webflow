/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.BaseEncoding;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Signature and data for QR code in offline mode for mobile token.
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class OfflineSignatureQrCode {

    private int size;
    private String dataHash;
    private String nonce;
    private String message;
    private String signature;

    private ObjectMapper objectMapper = new ObjectMapper();

    public OfflineSignatureQrCode(int size) {
        this.size = size;
    }

    public String getDataHash() {
        return dataHash;
    }

    public void setDataHash(String dataHash) {
        this.dataHash = dataHash;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * Encodes the QR code data into a String-based PNG image.
     * @return Generated QR code.
     */
    public String generateImage() {
        try {
            String data = getDataAsJson();
            if (data==null) {
                return null;
            }
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
            Logger.getLogger(this.getClass().getName()).log(
                    Level.SEVERE,
                    "Error occurred while generating QR code",
                    e
            );
        }
        return null;
    }

    /**
     * Generates data for the QR code as Json String.
     * @return Data as Json.
     */
    private String getDataAsJson() {
        ObjectNode qrNode = JsonNodeFactory.instance.objectNode();
        qrNode.put("dt", dataHash);
        qrNode.put("rnd", nonce);
        qrNode.put("msg", message);
        qrNode.put("sig", signature);
        try {
            return objectMapper.writeValueAsString(qrNode);
        } catch (JsonProcessingException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error while serializing QR code", e);
            return null;
        }
    }
}
