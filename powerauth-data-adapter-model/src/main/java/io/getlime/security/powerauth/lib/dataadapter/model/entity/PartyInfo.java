/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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

/**
 * Class representing party information.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
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
     * Constructor with party details.
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
