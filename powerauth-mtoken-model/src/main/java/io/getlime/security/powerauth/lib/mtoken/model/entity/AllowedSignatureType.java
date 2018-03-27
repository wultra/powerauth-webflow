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

import java.util.ArrayList;
import java.util.List;

/**
 * Object representing types of signatures that are admissible for
 * a given operation approval.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class AllowedSignatureType {

    public enum Type {
        MULTIFACTOR_1FA("1FA"),     // 1FA signature
        MULTIFACTOR_2FA("2FA"),     // 2FA signature
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

    public AllowedSignatureType() {
        this.variants = new ArrayList<>();
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public List<String> getVariants() {
        return variants;
    }

    public void setVariants(List<String> variants) {
        this.variants = variants;
    }
}
