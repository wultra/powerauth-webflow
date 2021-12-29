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

package io.getlime.security.powerauth.lib.dataadapter.model.request;

import io.getlime.security.powerauth.lib.dataadapter.model.entity.FormDataChange;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;

/**
 * Request object for notifying data adapter about operation form data change.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class FormDataChangeNotificationRequest {

    private String userId;
    private String organizationId;
    private OperationContext operationContext;
    private FormDataChange formDataChange;

    /**
     * Default constructor.
     */
    public FormDataChangeNotificationRequest() {
    }

    /**
     * Constructor with user ID, operation ID and form data change.
     * @param userId User ID.
     * @param organizationId Organization ID.
     * @param formDataChange Form data change.
     * @param operationContext Operation context.
     */
    public FormDataChangeNotificationRequest(String userId, String organizationId, FormDataChange formDataChange, OperationContext operationContext) {
        this.userId = userId;
        this.organizationId = organizationId;
        this.formDataChange = formDataChange;
        this.operationContext = operationContext;
    }

    /**
     * Get user ID.
     * @return User ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set user ID.
     * @param userId User ID.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get organization ID.
     * @return Organization ID.
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * Set organization ID.
     * @param organizationId Organization ID.
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * Get operation context.
     * @return Operation context.
     */
    public OperationContext getOperationContext() {
        return operationContext;
    }

    /**
     * Set operation context.
     * @param operationContext Operation context.
     */
    public void setOperationContext(OperationContext operationContext) {
        this.operationContext = operationContext;
    }

    /**
     * Get formData change.
     * @return FormData change.
     */
    public FormDataChange getFormDataChange() {
        return formDataChange;
    }

    /**
     * Set formData change.
     * @param formDataChange Change of form data.
     */
    public void setFormDataChange(FormDataChange formDataChange) {
        this.formDataChange = formDataChange;
    }
}
