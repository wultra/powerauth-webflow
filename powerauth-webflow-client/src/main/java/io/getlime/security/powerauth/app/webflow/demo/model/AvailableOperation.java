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