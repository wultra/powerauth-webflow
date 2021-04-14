/*
 * Copyright 2017 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.dataadapter.model.converter;

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.BannerType;

/**
 * Converter for banner types.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class BannerTypeConverter {

    /**
     * Converter from Next step BannerType.
     * @param input Next step BannerType.
     * @return Data adapter BannerType.
     */
    public io.getlime.security.powerauth.lib.dataadapter.model.enumeration.BannerType fromOperationBannerType(BannerType input) {
        switch (input) {
            case BANNER_INFO:
                return io.getlime.security.powerauth.lib.dataadapter.model.enumeration.BannerType.BANNER_INFO;
            case BANNER_WARNING:
                return io.getlime.security.powerauth.lib.dataadapter.model.enumeration.BannerType.BANNER_WARNING;
            case BANNER_ERROR:
                return io.getlime.security.powerauth.lib.dataadapter.model.enumeration.BannerType.BANNER_ERROR;
            default:
                throw new IllegalStateException("Unsupported banner type: "+input);
        }
    }

    /**
     * Converter from Data adapter BannerType.
     * @param input Data adapter BannerType.
     * @return Next step BannerType.
     */
    public BannerType fromBannerType(io.getlime.security.powerauth.lib.dataadapter.model.enumeration.BannerType input) {
        switch (input) {
            case BANNER_INFO:
                return BannerType.BANNER_INFO;
            case BANNER_WARNING:
                return BannerType.BANNER_WARNING;
            case BANNER_ERROR:
                return BannerType.BANNER_ERROR;
            default:
                throw new IllegalStateException("Unsupported banner type: "+input);
        }
    }
}
