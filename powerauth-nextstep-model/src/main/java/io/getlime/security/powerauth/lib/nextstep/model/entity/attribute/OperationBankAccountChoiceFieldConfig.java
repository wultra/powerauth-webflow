package io.getlime.security.powerauth.lib.nextstep.model.entity.attribute;

/**
 * Class representing a bank account choice form field configuration.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class OperationBankAccountChoiceFieldConfig extends OperationFormFieldConfig {

    private boolean enabled;
    private String defaultValue;

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
}
