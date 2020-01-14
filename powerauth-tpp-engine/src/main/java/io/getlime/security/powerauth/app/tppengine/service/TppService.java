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

package io.getlime.security.powerauth.app.tppengine.service;

import com.google.common.base.Joiner;
import io.getlime.security.powerauth.app.tppengine.converter.TppAppConverter;
import io.getlime.security.powerauth.app.tppengine.errorhandling.exception.TppAppNotFoundException;
import io.getlime.security.powerauth.app.tppengine.errorhandling.exception.TppNotFoundException;
import io.getlime.security.powerauth.app.tppengine.model.request.CreateTppAppRequest;
import io.getlime.security.powerauth.app.tppengine.model.response.TppAppDetailResponse;
import io.getlime.security.powerauth.app.tppengine.repository.OAuthClientDetailsRepository;
import io.getlime.security.powerauth.app.tppengine.repository.TppAppDetailRepository;
import io.getlime.security.powerauth.app.tppengine.repository.TppRepository;
import io.getlime.security.powerauth.app.tppengine.repository.model.entity.OAuthClientDetailsEntity;
import io.getlime.security.powerauth.app.tppengine.repository.model.entity.TppAppDetailEntity;
import io.getlime.security.powerauth.app.tppengine.repository.model.entity.TppEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Service from handling information about TPP and TPP apps.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Service
public class TppService {

    private static final Long OAUTH_ACCESS_TOKEN_VALIDITY = 5 * 60L;

    private final TppRepository tppRepository;
    private final TppAppDetailRepository appDetailRepository;
    private final OAuthClientDetailsRepository clientDetailsRepository;

    @Autowired
    public TppService(TppRepository tppRepository, TppAppDetailRepository appDetailRepository, OAuthClientDetailsRepository clientDetailsRepository) {
        this.tppRepository = tppRepository;
        this.appDetailRepository = appDetailRepository;
        this.clientDetailsRepository = clientDetailsRepository;
    }

    /**
     * Fetch application details by provided client ID (OAuth 2.0 identification).
     *
     * @param clientId Client ID.
     * @return Application details for app with given client ID, or null
     * if no app for given client ID exists.
     */
    public TppAppDetailResponse fetchAppDetailByClientId(String clientId) throws TppAppNotFoundException {
        final Optional<TppAppDetailEntity> tppAppEntityOptional = appDetailRepository.findByClientId(clientId);
        if (tppAppEntityOptional.isPresent()) {
            final TppAppDetailEntity tppAppDetailEntity = tppAppEntityOptional.get();
            final Optional<OAuthClientDetailsEntity> clientDetailsRepositoryById = clientDetailsRepository.findById(clientId);
            if (clientDetailsRepositoryById.isPresent()) {
                return TppAppConverter.fromTppAppEntity(tppAppDetailEntity, clientDetailsRepositoryById.get());
            } else {
                return TppAppConverter.fromTppAppEntity(tppAppDetailEntity);
            }
        } else {
            throw new TppAppNotFoundException(clientId);
        }
    }

    /**
     * Fetch application details by provided client ID (OAuth 2.0 identification) and TPP license information.
     *
     * @param clientId Client ID.
     * @param tppLicense TPP license info.
     * @return Application details for app with given client ID, or null
     * if no app for given client ID exists.
     */
    public TppAppDetailResponse fetchAppDetailByClientId(String clientId, String tppLicense) throws TppNotFoundException, TppAppNotFoundException {
        // Create a TPP entity, if it does not exist
        TppEntity tppEntity = getTppEntity(tppLicense);

        final Optional<TppAppDetailEntity> tppAppEntityOptional = appDetailRepository.findByClientId(clientId);
        if (tppAppEntityOptional.isPresent()) {
            final TppAppDetailEntity tppAppDetailEntity = tppAppEntityOptional.get();

            if (Objects.equals(tppAppDetailEntity.getPrimaryKey().getTppId(), tppEntity.getTppId())) {
                final Optional<OAuthClientDetailsEntity> clientDetailsRepositoryById = clientDetailsRepository.findById(clientId);
                if (clientDetailsRepositoryById.isPresent()) {
                    return TppAppConverter.fromTppAppEntity(tppAppDetailEntity, clientDetailsRepositoryById.get());
                } else {
                    return TppAppConverter.fromTppAppEntity(tppAppDetailEntity);
                }
            } else {
                throw new TppAppNotFoundException(clientId);
            }
        } else {
            throw new TppAppNotFoundException(clientId);
        }
    }

    /**
     * Fetch applications for given TPP provider based on license information.
     *
     * @param tppLicense TPP license information.
     * @return Application list for given third party.
     */
    public List<TppAppDetailResponse> fetchAppListByTppLicense(String tppLicense) throws TppNotFoundException {
        final Optional<TppEntity> tppAppEntityOptional = tppRepository.findFirstByTppLicense(tppLicense);
        if (tppAppEntityOptional.isPresent()) {
            final TppEntity tppEntity = tppAppEntityOptional.get();
            final Iterable<TppAppDetailEntity> appDetailEntityIterable = appDetailRepository.findByTppId(tppEntity.getTppId());
            List<TppAppDetailResponse> response = new ArrayList<>();
            for (TppAppDetailEntity app: appDetailEntityIterable) {
                response.add(TppAppConverter.fromTppAppEntity(app)); // no need to list all OAuth 2.0 details here
            }
            return response;
        } else {
            throw new TppNotFoundException("tpp.notFound", tppLicense);
        }
    }

    @Transactional
    public TppAppDetailResponse createApp(CreateTppAppRequest request) {

        // Create a TPP entity, if it does not exist
        final Optional<TppEntity> tppEntityOptional = tppRepository.findFirstByTppLicense(request.getTppLicense());
        TppEntity tppEntity;
        if (tppEntityOptional.isPresent()) { // This TPP already exists
            tppEntity = tppEntityOptional.get();
        } else { // TPP does not exist yet, must be created
            tppEntity = new TppEntity();
            tppEntity.setTppLicense(request.getTppLicense());
            tppEntity.setTppName(request.getTppName());
            tppEntity.setTppAddress(request.getTppAddress());
            tppEntity.setTppWebsite(request.getTppWebsite());
            tppEntity = tppRepository.save(tppEntity);
        }

        // Generate app OAuth 2.0 credentials
        final String clientId = UUID.randomUUID().toString();
        final String clientSecret = UUID.randomUUID().toString();
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        final String encodedClientSecret = bcrypt.encode(clientSecret);

        // Create a new TPP app record in database
        TppAppDetailEntity tppAppDetailEntity = new TppAppDetailEntity();
        tppAppDetailEntity.setAppName(request.getAppName());
        tppAppDetailEntity.setAppInfo(request.getAppDescription());
        tppAppDetailEntity.setTpp(tppEntity);
        TppAppDetailEntity.TppAppDetailKey tppAppDetailKey = new TppAppDetailEntity.TppAppDetailKey();
        tppAppDetailKey.setTppId(tppEntity.getTppId());
        tppAppDetailKey.setAppClientId(clientId);
        tppAppDetailEntity.setTpp(tppEntity);
        tppAppDetailEntity.setPrimaryKey(tppAppDetailKey);

        // Sanitize redirect URIs by Base64 decoding them
        final String[] redirectUris = request.getRedirectUris();
        for (int i = 0; i < redirectUris.length; i++) {
            redirectUris[i] = UriUtils.encode(redirectUris[i], StandardCharsets.UTF_8);
        }

        // Store the new OAuth 2.0 credentials in database
        OAuthClientDetailsEntity oAuthClientDetailsEntity = new OAuthClientDetailsEntity();
        oAuthClientDetailsEntity.setClientId(clientId);
        oAuthClientDetailsEntity.setClientSecret(encodedClientSecret);
        oAuthClientDetailsEntity.setAuthorizedGrantTypes("authorization_code");
        oAuthClientDetailsEntity.setWebServerRedirectUri(Joiner.on(",").join(request.getRedirectUris()));
        oAuthClientDetailsEntity.setScope(Joiner.on(",").join(request.getScopes()));
        oAuthClientDetailsEntity.setAccessTokenValidity(OAUTH_ACCESS_TOKEN_VALIDITY);
        oAuthClientDetailsEntity.setAdditionalInformation("{}");
        oAuthClientDetailsEntity.setAutoapprove("true");
        clientDetailsRepository.save(oAuthClientDetailsEntity);
        tppAppDetailEntity = appDetailRepository.save(tppAppDetailEntity);

        return TppAppConverter.fromTppAppEntity(tppAppDetailEntity, oAuthClientDetailsEntity, clientSecret);

    }

    @Transactional
    public TppAppDetailResponse updateApp(String clientId, CreateTppAppRequest request) throws TppNotFoundException, TppAppNotFoundException {
        // Create a TPP entity, if it does not exist
        TppEntity tppEntity = getTppEntity(request.getTppLicense());

        final Optional<TppAppDetailEntity> appDetailEntityOptional = appDetailRepository.findByClientId(clientId);
        if (appDetailEntityOptional.isPresent()) {
            TppAppDetailEntity tppAppDetailEntity = appDetailEntityOptional.get();
            // Check if the client ID belongs to the TPP provider
            if (!Objects.equals(tppAppDetailEntity.getPrimaryKey().getTppId(), tppEntity.getTppId())) {
                throw new TppAppNotFoundException(clientId);
            }

            tppAppDetailEntity.setAppName(request.getAppName());
            tppAppDetailEntity.setAppInfo(request.getAppDescription());

            // Store the new OAuth 2.0 credentials in database
            Optional<OAuthClientDetailsEntity> oAuthClientDetailsEntityOptional = clientDetailsRepository.findById(clientId);
            if (oAuthClientDetailsEntityOptional.isPresent()) {
                final OAuthClientDetailsEntity oAuthClientDetailsEntity = oAuthClientDetailsEntityOptional.get();
                oAuthClientDetailsEntity.setWebServerRedirectUri(Joiner.on(",").join(request.getRedirectUris()));
                oAuthClientDetailsEntity.setScope(Joiner.on(",").join(request.getScopes()));
                clientDetailsRepository.save(oAuthClientDetailsEntity);
                tppAppDetailEntity = appDetailRepository.save(tppAppDetailEntity);
                return TppAppConverter.fromTppAppEntity(tppAppDetailEntity, oAuthClientDetailsEntity);
            } else {
                throw new TppAppNotFoundException(clientId);
            }

        } else {
            throw new TppAppNotFoundException(clientId);
        }

    }

    @Transactional
    public void deleteApp(String clientId, String tppLicense) throws TppNotFoundException, TppAppNotFoundException {
        // Create a TPP entity, if it does not exist
        TppEntity tppEntity = getTppEntity(tppLicense);

        final Optional<TppAppDetailEntity> appDetailEntityOptional = appDetailRepository.findByClientId(clientId);
        if (appDetailEntityOptional.isPresent()) {
            TppAppDetailEntity tppAppDetailEntity = appDetailEntityOptional.get();
            // Check if the client ID belongs to the TPP provider
            if (!Objects.equals(tppAppDetailEntity.getPrimaryKey().getTppId(), tppEntity.getTppId())) {
                throw new TppAppNotFoundException(clientId);
            }

            // Store the new OAuth 2.0 credentials in database
            Optional<OAuthClientDetailsEntity> oAuthClientDetailsEntityOptional = clientDetailsRepository.findById(clientId);
            if (oAuthClientDetailsEntityOptional.isPresent()) {
                final OAuthClientDetailsEntity oAuthClientDetailsEntity = oAuthClientDetailsEntityOptional.get();
                appDetailRepository.delete(tppAppDetailEntity);
                clientDetailsRepository.delete(oAuthClientDetailsEntity);
            } else {
                throw new TppAppNotFoundException(clientId);
            }
        } else {
            throw new TppAppNotFoundException(clientId);
        }

    }

    /**
     * Find TPP entity based on TPP license.
     * @param tppLicense TPP license info.
     * @return TPP entity, in case the TPP entity is found.
     * @throws TppNotFoundException In case that TPP entity is not found.
     */
    private TppEntity getTppEntity(String tppLicense) throws TppNotFoundException {
        final Optional<TppEntity> tppEntityOptional = tppRepository.findFirstByTppLicense(tppLicense);
        TppEntity tppEntity;
        if (tppEntityOptional.isPresent()) { // This TPP already exists
            tppEntity = tppEntityOptional.get();
        } else { // TPP does not exist - this is an incorrect state
            throw new TppNotFoundException("tpp.notFound", tppLicense);
        }
        return tppEntity;
    }

    public TppAppDetailResponse renewAppSecret(String clientId, String tppLicense) throws TppNotFoundException, TppAppNotFoundException {
        // Create a TPP entity, if it does not exist
        TppEntity tppEntity = getTppEntity(tppLicense);

        final Optional<TppAppDetailEntity> appDetailEntityOptional = appDetailRepository.findByClientId(clientId);
        if (appDetailEntityOptional.isPresent()) {
            TppAppDetailEntity tppAppDetailEntity = appDetailEntityOptional.get();
            // Check if the client ID belongs to the TPP provider
            if (!Objects.equals(tppAppDetailEntity.getPrimaryKey().getTppId(), tppEntity.getTppId())) {
                throw new TppAppNotFoundException(clientId);
            }

            // Generate app OAuth 2.0 credentials
            final String clientSecret = UUID.randomUUID().toString();
            BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
            final String encodedClientSecret = bcrypt.encode(clientSecret);

            // Store the new OAuth 2.0 credentials in database
            Optional<OAuthClientDetailsEntity> oAuthClientDetailsEntityOptional = clientDetailsRepository.findById(clientId);
            if (oAuthClientDetailsEntityOptional.isPresent()) {
                final OAuthClientDetailsEntity oAuthClientDetailsEntity = oAuthClientDetailsEntityOptional.get();
                oAuthClientDetailsEntity.setClientSecret(encodedClientSecret);
                clientDetailsRepository.save(oAuthClientDetailsEntity);
                return TppAppConverter.fromTppAppEntity(tppAppDetailEntity, oAuthClientDetailsEntity, clientSecret);
            } else {
                throw new TppAppNotFoundException(clientId);
            }

        } else {
            throw new TppAppNotFoundException(clientId);
        }

    }
}
