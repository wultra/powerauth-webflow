package io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute;

import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.BannerType;

/**
 * Class representing a banner in an operation.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class FormBanner extends MessageAttribute {

    private final BannerType bannerType;

    /**
     * Default constructor.
     */
    public FormBanner() {
        this.bannerType = BannerType.BANNER_INFO;
    }

    /**
     * Constructor with banner type.
     * @param bannerType Banner type.
     */
    public FormBanner(BannerType bannerType) {
        this.bannerType = bannerType;
    }

    /**
     * Constructor with all details.
     * @param id Attribute ID.
     * @param bannerType Banner type.
     * @param message Banner message.
     */
    public FormBanner(String id, BannerType bannerType, String message) {
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
