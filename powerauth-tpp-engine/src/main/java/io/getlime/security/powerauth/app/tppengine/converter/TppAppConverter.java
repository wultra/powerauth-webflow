/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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

package io.getlime.security.powerauth.app.tppengine.converter;

import io.getlime.security.powerauth.app.tppengine.model.entity.TppInfo;
import io.getlime.security.powerauth.app.tppengine.model.response.TppAppDetailResponse;
import io.getlime.security.powerauth.app.tppengine.repository.model.entity.OAuthClientDetailsEntity;
import io.getlime.security.powerauth.app.tppengine.repository.model.entity.TppAppDetailEntity;
import io.getlime.security.powerauth.app.tppengine.repository.model.entity.TppEntity;

/**
 * Converter class for TPP and TPP app entity conversion.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class TppAppConverter {

    /**
     * Convert TppInfo response entity from database TppEntity entity in database.
     *
     * @param tppEntity DB entity to be converted.
     * @return Response entity with TPP info.
     */
    public static TppInfo fromTppEntity(TppEntity tppEntity) {
        if (tppEntity == null) {
            return null;
        }
        TppInfo tpp = new TppInfo();
        tpp.setName(tppEntity.getTppName());
        tpp.setLicense(tppEntity.getTppLicense());
        tpp.setInfo(tppEntity.getTppInfo());
        tpp.setAddress(tppEntity.getTppAddress());
        tpp.setWebsite(tppEntity.getTppWebsite());
        tpp.setEmail(tppEntity.getTppEmail());
        tpp.setPhone(tppEntity.getTppPhone());
        tpp.setBlocked(tppEntity.isBlocked());
        return tpp;
    }

    /**
     * Convert TppAppDetailResponse response entity from TppAppDetail entity in database.
     * @param tppAppDetailEntity DB entity of TPP app to be converted.
     * @param oAuthClientDetailsEntity DB entity of OAuth client details to be converted.
     * @param clientSecret OAuth 2.0 client secret, available when new app is created or on renewal only (otherwise, we store a bcrypted version).
     * @return Response entity with TPP app details.
     */
    public static TppAppDetailResponse fromTppAppEntity(TppAppDetailEntity tppAppDetailEntity, OAuthClientDetailsEntity oAuthClientDetailsEntity, String clientSecret) {
        if (tppAppDetailEntity == null) {
            return null;
        }
        TppAppDetailResponse result = new TppAppDetailResponse();
        // Convert app info
        result.setClientId(tppAppDetailEntity.getPrimaryKey().getAppClientId());
        result.setName(tppAppDetailEntity.getAppName());
        result.setDescription(tppAppDetailEntity.getAppInfo());
        result.setAppType(tppAppDetailEntity.getAppType());

        // Convert TPP info
        result.setTpp(fromTppEntity(tppAppDetailEntity.getTpp()));

        // Convert OAuth 2.0 info
        if (oAuthClientDetailsEntity != null) {
            // Decode sanitized redirect URIs
            final String[] redirectUris = oAuthClientDetailsEntity.getWebServerRedirectUri().split(",");
            final String[] scopes = oAuthClientDetailsEntity.getScope().split(",");
            result.setRedirectUris(redirectUris);
            result.setScopes(scopes);
            result.setClientSecret(clientSecret);
        }
        return result;
    }

    /**
     * Convert TppAppDetailResponse response entity from TppAppDetail entity in database.
     * @param tppAppDetailEntity DB entity of TPP app to be converted.
     * @param oAuthClientDetailsEntity DB entity of OAuth client details to be converted.
     * @return Response entity with TPP app details.
     */
    public static TppAppDetailResponse fromTppAppEntity(TppAppDetailEntity tppAppDetailEntity, OAuthClientDetailsEntity oAuthClientDetailsEntity) {
        return fromTppAppEntity(tppAppDetailEntity, oAuthClientDetailsEntity, null);
    }

    /**
     * Convert TppAppDetailResponse response entity from TppAppDetail entity in database.
     * @param tppAppDetailEntity DB entity of TPP app to be converted.
     * @return Response entity with TPP app details.
     */
    public static TppAppDetailResponse fromTppAppEntity(TppAppDetailEntity tppAppDetailEntity) {
        return fromTppAppEntity(tppAppDetailEntity, null);
    }

}
