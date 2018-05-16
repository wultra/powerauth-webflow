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
package io.getlime.security.powerauth.lib.mtoken.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Object representing types of signatures that are admissible for
 * a given operation approval.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class AllowedSignatureType {

    /**
     * Signature types.
     */
    public enum Type {
        @JsonProperty("1FA")
        MULTIFACTOR_1FA("1FA"),     // 1FA signature
        @JsonProperty("2FA")
        MULTIFACTOR_2FA("2FA"),     // 2FA signature
        @JsonProperty("ECDSA")
        ASSYMETRIC_ECDSA("ECDSA");  // ECDSA private key signature

        private final String type;

        Type(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return this.type;
        }
    }

    private Type type;
    private List<String> variants;

    /**
     * Default constructor.
     */
    public AllowedSignatureType() {
        this.variants = new ArrayList<>();
    }

    /**
     * Get signature type.
     * @return Signature type.
     */
    public Type getType() {
        return type;
    }

    /**
     * Set signature type.
     * @param type Signature type.
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Extras with supported variants.
     * @return Extras with supported variants.
     */
    public List<String> getVariants() {
        return variants;
    }

    /**
     * Set extras with supported variants.
     * @param variants Extras with supported variants.
     */
    public void setVariants(List<String> variants) {
        this.variants = variants;
    }
}
