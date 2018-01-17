package io.getlime.security.powerauth.lib.nextstep.model.entity.attribute;

import java.util.Objects;

/**
 * Operation form data configuration.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 *
 */
public class OperationFormFieldConfig {

    protected String id;
    private boolean enabled;
    private String defaultValue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationFormFieldConfig fieldConfig = (OperationFormFieldConfig) o;
        return Objects.equals(id, fieldConfig.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
