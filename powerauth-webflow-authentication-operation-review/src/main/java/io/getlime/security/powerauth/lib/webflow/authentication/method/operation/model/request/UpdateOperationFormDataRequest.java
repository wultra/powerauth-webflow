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

package io.getlime.security.powerauth.lib.webflow.authentication.method.operation.model.request;

import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;

/**
 * Request to update operation form data.
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UpdateOperationFormDataRequest {

    private OperationFormData formData;

    /**
     * Default constructor.
     */
    public UpdateOperationFormDataRequest() {
    }

    /**
     * Constructor with form data.
     * @param formData Form data.
     */
    public UpdateOperationFormDataRequest(OperationFormData formData) {
        this.formData = formData;
    }

    /**
     * Get the form data.
     * @return Form data.
     */
    public OperationFormData getFormData() {
        return formData;
    }

    /**
     * Set the form data.
     * @param formData form data.
     */
    public void setFormData(OperationFormData formData) {
        this.formData = formData;
    }
}
