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
