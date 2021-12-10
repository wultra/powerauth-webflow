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

package io.getlime.security.powerauth.lib.webflow.authentication.method.operation.model.response;

import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;

/**
 * Response with operation detail.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class OperationReviewDetailResponse {

    private String data;
    private OperationFormData formData;
    private AuthMethod chosenAuthMethod;

    /**
     * Get data.
     * @return Data.
     */
    public String getData() {
        return data;
    }

    /**
     * Set data.
     * @param data Data.
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Get form data.
     * @return Form data.
     */
    public OperationFormData getFormData() {
        return formData;
    }

    /**
     * Set form data.
     * @param formData Form data.
     */
    public void setFormData(OperationFormData formData) {
        this.formData = formData;
    }

    /**
     * Get chosen authentication method.
     * @return Chosen authentication method.
     */
    public AuthMethod getChosenAuthMethod() {
        return chosenAuthMethod;
    }

    /**
     * Set chosen authentication method.
     * @param chosenAuthMethod Chosen authentication method.
     */
    public void setChosenAuthMethod(AuthMethod chosenAuthMethod) {
        this.chosenAuthMethod = chosenAuthMethod;
    }
}
