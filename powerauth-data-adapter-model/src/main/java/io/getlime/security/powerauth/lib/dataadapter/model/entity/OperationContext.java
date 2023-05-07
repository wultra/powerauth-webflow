/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2021 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.dataadapter.model.entity;

import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;

/**
 * Class representing context of an operation.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OperationContext {

    private String id;
    private String name;
    private String data;
    private String externalTransactionId;
    private FormData formData;
    private ApplicationContext applicationContext;
    private PAAuthenticationContext authenticationContext;

    /**
     * Default constructor.
     */
    public OperationContext() {
    }

    /**
     * Constructor with operation details.
     * @param id Operation ID.
     * @param name Operation name.
     * @param data Operation data.
     * @param externalTransactionId External transaction ID (for example, ID in some other system).
     * @param formData Operation form data.
     * @param applicationContext Application context.
     */
    public OperationContext(String id, String name, String data, String externalTransactionId, FormData formData, ApplicationContext applicationContext) {
        this.id = id;
        this.name = name;
        this.data = data;
        this.formData = formData;
        this.applicationContext = applicationContext;
    }

    /**
     * Get operation ID.
     * @return Operation ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Set operation ID.
     * @param id Operation ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get operation name.
     * @return Operation name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set operation name.
     * @param name Operation name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get operation data.
     * @return Operation data.
     */
    public String getData() {
        return data;
    }

    /**
     * Set operation data.
     * @param data Operation data.
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Get external transaction ID.
     * @return External transaction ID.
     */
    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    /**
     * Set external transaction ID.
     * @param externalTransactionId External transaction ID.
     */
    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    /**
     * Get operation form data.
     * @return Operation form data.
     */
    public FormData getFormData() {
        return formData;
    }

    /**
     * Set operation form data.
     * @param formData Operation form data.
     */
    public void setFormData(FormData formData) {
        this.formData = formData;
    }

    /**
     * Get application context for OAuth 2.1 consent screen.
     * @return Application context for OAuth 2.1 consent screen.
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Set application context for OAuth 2.1 consent screen.
     * @param applicationContext Application context for OAuth 2.1 consent screen.
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Get PowerAuth authentication context.
     * @return PowerAuth authentication context.
     */
    public PAAuthenticationContext getAuthenticationContext() {
        return authenticationContext;
    }

    /**
     * Set PowerAuth authentication context.
     * @param authenticationContext PowerAuth authentication context.
     */
    public void setAuthenticationContext(PAAuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }
}
