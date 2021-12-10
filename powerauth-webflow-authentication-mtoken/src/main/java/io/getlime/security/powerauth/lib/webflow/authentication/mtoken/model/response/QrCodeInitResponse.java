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
package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response;

import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.entity.ActivationEntity;

import java.util.List;

/**
 * Response to the init step of offline mobile token authentication with QR code.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class QrCodeInitResponse extends AuthStepResponse {

    private String qrCode;
    private String nonce;
    private ActivationEntity chosenActivation;
    private List<ActivationEntity> activations;

    /**
     * Get QR code as string.
     * @return QR code.
     */
    public String getQrCode() {
        return qrCode;
    }

    /**
     * Set QR code as string.
      * @param qrCode QR code.
     */
    public void setQrCode(String qrCode) {
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
