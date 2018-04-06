/*
 * Copyright 2018 Lime - HighTech Solutions s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.getlime.security.powerauth.lib.nextstep.model.entity.attribute;

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.BannerType;

/**
 * Class representing an operation form field attribute for banner with message.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class OperationBannerFieldAttribute extends OperationFormFieldAttribute {

    private String message;
    private final BannerType bannerType;

    /**
     * Default constructor.
     */
    public OperationBannerFieldAttribute() {
        this.type = Type.BANNER;
        this.bannerType = BannerType.BANNER_INFO;
    }

    /**
     * Constructor with banner type.
     * @param bannerType Banner type.
     */
    public OperationBannerFieldAttribute(BannerType bannerType) {
        this.type = Type.BANNER;
        this.bannerType = bannerType;
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