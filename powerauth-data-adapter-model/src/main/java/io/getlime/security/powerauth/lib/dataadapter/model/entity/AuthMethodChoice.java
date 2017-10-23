package io.getlime.security.powerauth.lib.dataadapter.model.entity;


/**
 * Class representing choice of the authorization method.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class AuthMethodChoice extends FormDataChange {

    public enum ChosenAuthMethod {
        POWERAUTH_TOKEN,
        SMS_KEY
    }

    private ChosenAuthMethod chosenAuthMethod;

    public AuthMethodChoice() {
        this.type = Type.AUTH_METHOD_CHOICE;
    }

    public ChosenAuthMethod getChosenAuthMethod() {
        return chosenAuthMethod;
    }

    public void setChosenAuthMethod(ChosenAuthMethod chosenAuthMethod) {
        this.chosenAuthMethod = chosenAuthMethod;
    }

}
