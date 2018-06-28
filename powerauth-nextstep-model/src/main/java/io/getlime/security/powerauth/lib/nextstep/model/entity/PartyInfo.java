package io.getlime.security.powerauth.lib.nextstep.model.entity;

/**
 * Class representing information about third party.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class PartyInfo {

    private String logoUrl;
    private String name;
    private String description;
    private String websiteUrl;

    /**
     * Default constructor.
     */
    public PartyInfo() {
    }

    /**
     * Constructor with party information.
     * @param logoUrl URL with party logo.
     * @param name Party name.
     * @param description Party description.
     * @param websiteUrl Party website URL.
     */
    public PartyInfo(String logoUrl, String name, String description, String websiteUrl) {
        this.logoUrl = logoUrl;
        this.name = name;
        this.description = description;
        this.websiteUrl = websiteUrl;
    }

    /**
     * Get URL with party logo.
     * @return URL with party logo.
     */
    public String getLogoUrl() {
        return logoUrl;
    }

    /**
     * Set URL with party logo.
     * @param logoUrl URL with party logo.
     */
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
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
    public String getWebsiteUrl() {
        return websiteUrl;
    }

    /**
     * Set party website URL.
     * @param websiteUrl Party website URL.
     */
    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }
}
