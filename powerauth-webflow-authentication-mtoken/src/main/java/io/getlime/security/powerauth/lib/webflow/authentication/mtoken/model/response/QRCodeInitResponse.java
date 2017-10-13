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
package io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.response;

import io.getlime.security.powerauth.lib.webflow.authentication.base.AuthStepResponse;
import io.getlime.security.powerauth.lib.webflow.authentication.mtoken.model.entity.ActivationEntity;

import java.util.List;

/**
 * Response to the init step of offline mobile token authentication with QR code.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class QRCodeInitResponse extends AuthStepResponse {

    private String qrCode;
    private String nonce;
    private String dataHash;
    private ActivationEntity chosenActivation;
    private List<ActivationEntity> activations;

    public String getQRCode() {
        return qrCode;
    }

    public void setQRCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getDataHash() {
        return dataHash;
    }

    public void setDataHash(String dataHash) {
        this.dataHash = dataHash;
    }

    public ActivationEntity getChosenActivation() {
        return chosenActivation;
    }

    public void setChosenActivation(ActivationEntity chosenActivation) {
        this.chosenActivation = chosenActivation;
    }

    public List<ActivationEntity> getActivations() {
        return activations;
    }

    public void setActivations(List<ActivationEntity> activations) {
        this.activations = activations;
    }
}
