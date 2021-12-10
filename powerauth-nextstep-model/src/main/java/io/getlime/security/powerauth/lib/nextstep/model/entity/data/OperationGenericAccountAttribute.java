/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2018 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.nextstep.model.entity.data;

/**
 * Generic (non-IBAN) account in operation data.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OperationGenericAccountAttribute extends OperationDataAttribute {

    private String account;

    /**
     * Default constructor.
     */
    public OperationGenericAccountAttribute() {
        this.type = Type.ACCOUNT_GENERIC;
    }

    /**
     * Constructor with account.
     * @param account Account.
     */
    public OperationGenericAccountAttribute(String account) {
        this.type = Type.ACCOUNT_GENERIC;
        this.account = account;
    }

    /**
     * Get account.
     * @return Account.
     */
    public String getAccount() {
        return account;
    }

    /**
     * Set account.
     * @param account Account.
     */
    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String formattedValue() {
        if (account == null) {
            return "";
        }
        return "Q"+ account;
    }
}
