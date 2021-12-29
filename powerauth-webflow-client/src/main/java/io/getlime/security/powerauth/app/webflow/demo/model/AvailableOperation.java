/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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