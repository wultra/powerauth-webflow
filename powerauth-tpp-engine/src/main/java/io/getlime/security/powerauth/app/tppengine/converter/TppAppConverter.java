/*
 * Copyright 2019 Wultra s.r.o.
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
