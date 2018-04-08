package io.getlime.security.powerauth.lib.nextstep.model.entity.attribute;

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.BannerType;

/**
 * Class representing a banner in an operation.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
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
     * Get banner type.
     * @return Banner type.
     */
    public BannerType getBannerType() {
        return bannerType;
    }

}
