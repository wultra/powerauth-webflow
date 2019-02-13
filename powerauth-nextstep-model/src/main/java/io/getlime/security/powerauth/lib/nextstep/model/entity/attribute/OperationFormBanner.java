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
