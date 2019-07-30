/*
 * Copyright 2019 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.webflow.demo.model;

/**
 * @author Jan Kobersky, jan.kobersky@wultra.com
 */
public class AvailableOperation {

    public enum Type {
        LOGIN,
        PAYMENT,
        LOGIN_SCA,
        PAYMENT_SCA,
        AUTHORIZATION
    }

    private String name;

    private boolean isDefault;
    private Type type;

    public AvailableOperation(Type type, String name) {
        this.name = name;
        this.isDefault = false;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(Boolean value) {
        isDefault = value;
    }
}