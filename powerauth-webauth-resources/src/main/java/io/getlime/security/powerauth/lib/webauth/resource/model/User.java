package io.getlime.security.powerauth.lib.webauth.resource.model;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class User {

    private String id;
    private String givenName;
    private String familyName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }
}
