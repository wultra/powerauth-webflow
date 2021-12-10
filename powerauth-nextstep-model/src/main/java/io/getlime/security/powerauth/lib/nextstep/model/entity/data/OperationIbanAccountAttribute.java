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
 * IBAN account in operation data.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OperationIbanAccountAttribute extends OperationDataAttribute {

    private String iban;
    private String bic;

    /**
     * Default constructor.
     */
    public OperationIbanAccountAttribute() {
        this.type = Type.ACCOUNT_IBAN;
    }

    /**
     * Constructor with IBAN.
     * @param iban IBAN.
     */
    public OperationIbanAccountAttribute(String iban) {
        this.type = Type.ACCOUNT_IBAN;
        this.iban = iban;
    }

    /**
     * Construtor with IBAN and BIC.
     * @param iban IBAN.
     * @param bic BIC.
     */
    public OperationIbanAccountAttribute(String iban, String bic) {
        this.type = Type.ACCOUNT_IBAN;
        this.iban = iban;
        this.bic = bic;
    }

    /**
     * Get IBAN.
     * @return IBAN.
     */
    public String getIban() {
        return iban;
    }

    /**
     * Set IBAN.
     * @param iban IBAN.
     */
    public void setIban(String iban) {
        this.iban = iban;
    }

    /**
     * Get BIC.
     * @return BIC.
     */
    public String getBic() {
        return bic;
    }

    /**
     * Set BIC.
     * @param bic BIC.
     */
    public void setBic(String bic) {
        this.bic = bic;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String formattedValue() {
        if (iban == null) {
            return "";
        }
        if (bic == null) {
            return "I"+iban;
        }
        return "I"+iban+","+bic;
    }
}
