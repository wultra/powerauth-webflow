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
package io.getlime.security.powerauth.lib.nextstep.model.entity.attribute;

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.BannerType;

/**
 * Class representing a banner in an operation.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OperationFormBanner extends OperationFormMessageAttribute {

    private final BannerType bannerType;

    /**
     * Default constructor.
     */
    public OperationFormBanner() {
        this.bannerType = BannerType.BANNER_INFO;
    }

    /**
     * Constructor with banner type.
     * @param bannerType Banner type.
     */
    public OperationFormBanner(BannerType bannerType) {
        this.bannerType = bannerType;
    }

    /**
     * Constructor with all details.
     * @param id Attribute ID.
     * @param bannerType Banner type.
     * @param message Banner message.
     */
    public OperationFormBanner(String id, BannerType bannerType, String message) {
        this.id = id;
        this.bannerType = bannerType;
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
