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
package io.getlime.security.powerauth.lib.mtoken.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Object representing types of signatures that are admissible for
 * a given operation approval.
 *
 * @author Petr Dvorak, petr@wultra.com
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
        ASYMMETRIC_ECDSA("ECDSA");  // ECDSA private key signature

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
