package io.getlime.security.powerauth.lib.bankadapter.model.entity;


/**
 * Class representing choice of the authorization method.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class AuthMethodChoiceEntity extends FormDataChangeEntity {

    public enum ChosenAuthMethod {
        POWERAUTH_TOKEN,
        SMS_KEY
    }

    private ChosenAuthMethod chosenAuthMethod;

    public AuthMethodChoiceEntity() {
        this.type = Type.AUTH_METHOD_CHOICE;
    }

    public ChosenAuthMethod getChosenAuthMethod() {
        return chosenAuthMethod;
    }

    public void setChosenAuthMethod(ChosenAuthMethod chosenAuthMethod) {
        this.chosenAuthMethod = chosenAuthMethod;
    }

}
