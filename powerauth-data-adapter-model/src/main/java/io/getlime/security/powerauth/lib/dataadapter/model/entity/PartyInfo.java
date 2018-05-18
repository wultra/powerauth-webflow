package io.getlime.security.powerauth.lib.dataadapter.model.entity;

/**
 * Class representing party information.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class PartyInfo {

    private String logoURL;
    private String name;
    private String description;
    private String websiteURL;

    /**
     * Default constructor.
     */
    public PartyInfo() {
    }

    /**
     * Constructor with party details.
     * @param logoURL URL with party logo.
     * @param name Party name.
     * @param description Party description.
     * @param websiteURL Party website URL.
     */
    public PartyInfo(String logoURL, String name, String description, String websiteURL) {
        this.logoURL = logoURL;
        this.name = name;
        this.description = description;
        this.websiteURL = websiteURL;
    }

    /**
     * Get URL with party logo.
     * @return URL with party logo.
     */
    public String getLogoURL() {
        return logoURL;
    }

    /**
     * Set URL with party logo.
     * @param logoURL URL with party logo.
     */
    public void setLogoURL(String logoURL) {
        this.logoURL = logoURL;
    }

    /**
     * Get party name.
     * @return Party name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set party name.
     * @param name Party name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get party description.
     * @return Party description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set party description.
     * @param description Party description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get party website URL.
     * @return Party website URL.
     */
    public String getWebsiteURL() {
        return websiteURL;
    }

    /**
     * Set party website URL.
     * @param websiteURL Party website URL.
     */
    public void setWebsiteURL(String websiteURL) {
        this.websiteURL = websiteURL;
    }

}
