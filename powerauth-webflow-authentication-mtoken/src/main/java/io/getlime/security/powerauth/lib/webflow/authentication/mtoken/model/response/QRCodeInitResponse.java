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
package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response;

import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.entity.ActivationEntity;

import java.util.List;

/**
 * Response to the init step of offline mobile token authentication with QR code.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class QRCodeInitResponse extends AuthStepResponse {

    private String qrCode;
    private String nonce;
    private ActivationEntity chosenActivation;
    private List<ActivationEntity> activations;

    /**
     * Get QR code as string.
     * @return QR code.
     */
    public String getQRCode() {
        return qrCode;
    }

    /**
     * Set QR code as string.
      * @param qrCode QR code.
     */
    public void setQRCode(String qrCode) {
        this.qrCode = qrCode;
    }

    /**
     * Get nonce.
     * @return Nonce.
     */
    public String getNonce() {
        return nonce;
    }

    /**
     * Set nonce.
     * @param nonce Nonce.
     */
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    /**
     * Get chosen activation.
     * @return Chosen activation.
     */
    public ActivationEntity getChosenActivation() {
        return chosenActivation;
    }

    /**
     * Set chosen activation.
     * @param chosenActivation Chosen activation.
     */
    public void setChosenActivation(ActivationEntity chosenActivation) {
        this.chosenActivation = chosenActivation;
    }

    /**
     * Get list of activations.
     * @return List of activations.
     */
    public List<ActivationEntity> getActivations() {
        return activations;
    }

    /**
     * Set list of activations.
     * @param activations List of activations.
     */
    public void setActivations(List<ActivationEntity> activations) {
        this.activations = activations;
    }
}
