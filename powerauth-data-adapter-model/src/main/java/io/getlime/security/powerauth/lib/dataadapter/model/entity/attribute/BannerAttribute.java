/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2018 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute;

import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.BannerType;

/**
 * Class representing an operation form field attribute for banner with message.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class BannerAttribute extends Attribute {

    private String message;
    private final BannerType bannerType;

    /**
     * Default constructor.
     */
    public BannerAttribute() {
        this.type = Type.BANNER;
        this.bannerType = BannerType.BANNER_INFO;
    }

    /**
     * Constructor with banner type.
     * @param bannerType Banner type.
     */
    public BannerAttribute(BannerType bannerType) {
        this.type = Type.BANNER;
        this.bannerType = bannerType;
    }

    /**
     * Constructor with all details.
     * @param id Attribute ID.
     * @param label Label.
     * @param bannerType Banner type.
     * @param message Banner message.
     */
    public BannerAttribute(String id, String label, BannerType bannerType, String message) {
        this.type = Type.BANNER;
        this.id = id;
        this.label = label;
        this.bannerType = bannerType;
        this.message = message;
    }

    /**
     * Get banner message.
     * @return Banner message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set banner message.
     * @param message Banner message.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get banner type.
     * @return Banner type.
     */
    public BannerType getBannerType() {
        return bannerType;
    }

}