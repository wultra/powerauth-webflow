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
    private FormData formData;
    private ApplicationContext applicationContext;

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
     * @param formData Operation form data.
     */
    public OperationContext(String id, String name, String data, FormData formData, ApplicationContext applicationContext) {
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
     * Get application context for OAuth 2.0 consent screen.
     * @return Application context for OAuth 2.0 consent screen.
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Set application context for OAuth 2.0 consent screen.
     * @param applicationContext Application context for OAuth 2.0 consent screen.
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
