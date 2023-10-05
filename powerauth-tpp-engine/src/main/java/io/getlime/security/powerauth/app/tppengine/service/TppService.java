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

package io.getlime.security.powerauth.app.tppengine.service;

import io.getlime.security.powerauth.app.tppengine.configuration.TppEngineConfiguration;
import io.getlime.security.powerauth.app.tppengine.converter.TppAppConverter;
import io.getlime.security.powerauth.app.tppengine.errorhandling.exception.TppAppNotFoundException;
import io.getlime.security.powerauth.app.tppengine.errorhandling.exception.TppNotFoundException;
import io.getlime.security.powerauth.app.tppengine.errorhandling.exception.UnableToCreateAppException;
import io.getlime.security.powerauth.app.tppengine.model.entity.TppInfo;
import io.getlime.security.powerauth.app.tppengine.model.request.CreateTppAppRequest;
import io.getlime.security.powerauth.app.tppengine.model.response.TppAppDetailResponse;
import io.getlime.security.powerauth.app.tppengine.repository.TppAppDetailRepository;
import io.getlime.security.powerauth.app.tppengine.repository.TppRepository;
import io.getlime.security.powerauth.app.tppengine.repository.model.entity.TppAppDetailEntity;
import io.getlime.security.powerauth.app.tppengine.repository.model.entity.TppEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Service from handling information about TPP and TPP apps.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Service
public class TppService {

    private static final Logger logger = LoggerFactory.getLogger(TppService.class);

    private final TppRepository tppRepository;
    private final TppAppDetailRepository appDetailRepository;
    private final TppEngineConfiguration tppEngineConfiguration;
    private final RegisteredClientRepository registeredClientRepository;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TppService(TppRepository tppRepository, TppAppDetailRepository appDetailRepository, TppEngineConfiguration tppEngineConfiguration, RegisteredClientRepository registeredClientRepository, JdbcTemplate jdbcTemplate) {
        this.tppRepository = tppRepository;
        this.appDetailRepository = appDetailRepository;
        this.tppEngineConfiguration = tppEngineConfiguration;
        this.registeredClientRepository = registeredClientRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Fetch application details by provided client ID (OAuth 2.1 identification).
     *
     * @param clientId Client ID.
     * @return Application details for app with given client ID, or null
     * if no app for given client ID exists.
     * @throws TppAppNotFoundException In case TPP app is not found.
     */
    public TppAppDetailResponse fetchAppDetailByClientId(String clientId) throws TppAppNotFoundException {
        final Optional<TppAppDetailEntity> tppAppEntityOptional = appDetailRepository.findByClientId(clientId);
        if (tppAppEntityOptional.isPresent()) {
            final TppAppDetailEntity tppAppDetailEntity = tppAppEntityOptional.get();
            final RegisteredClient registeredClient = registeredClientRepository.findById(clientId);
            if (registeredClient != null) {
                return TppAppConverter.fromTppAppEntity(tppAppDetailEntity, registeredClient);
            } else {
                return TppAppConverter.fromTppAppEntity(tppAppDetailEntity);
            }
        } else {
            throw new TppAppNotFoundException(clientId);
        }
    }

    /**
     * Fetch application details by provided client ID (OAuth 2.1 identification) and TPP license information.
     *
     * @param clientId Client ID.
     * @param tppLicense TPP license info.
     * @return Application details for app with given client ID, or null
     * if no app for given client ID exists.
     * @throws TppNotFoundException In case TPP is not found.
     * @throws TppAppNotFoundException In case TPP app is not found.
     */
    public TppAppDetailResponse fetchAppDetailByClientId(String clientId, String tppLicense) throws TppNotFoundException, TppAppNotFoundException {
        // Create a TPP entity, if it does not exist
        TppEntity tppEntity = getTppEntity(tppLicense);

        final Optional<TppAppDetailEntity> tppAppEntityOptional = appDetailRepository.findByClientId(clientId);
        if (tppAppEntityOptional.isPresent()) {
            final TppAppDetailEntity tppAppDetailEntity = tppAppEntityOptional.get();

            if (Objects.equals(tppAppDetailEntity.getPrimaryKey().getTppId(), tppEntity.getTppId())) {
                final RegisteredClient registeredClient = registeredClientRepository.findById(clientId);
                if (registeredClient != null) {
                    return TppAppConverter.fromTppAppEntity(tppAppDetailEntity, registeredClient);
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
     * @throws TppNotFoundException In case TPP is not found.
     */
    public List<TppAppDetailResponse> fetchAppListByTppLicense(String tppLicense) throws TppNotFoundException {
        final Optional<TppEntity> tppAppEntityOptional = tppRepository.findFirstByTppLicense(tppLicense);
        if (tppAppEntityOptional.isPresent()) {
            final TppEntity tppEntity = tppAppEntityOptional.get();
            final Iterable<TppAppDetailEntity> appDetailEntityIterable = appDetailRepository.findByTppId(tppEntity.getTppId());
            List<TppAppDetailResponse> response = new ArrayList<>();
            for (TppAppDetailEntity app: appDetailEntityIterable) {
                response.add(TppAppConverter.fromTppAppEntity(app)); // no need to list all OAuth 2.1 details here
            }
            return response;
        } else {
            throw new TppNotFoundException("tpp.notFound", tppLicense);
        }
    }

    /**
     * Create a new application with provided information.
     * @param request Request with information about a newly created app.
     * @return Information about a newly created app, including the OAuth 2.1 credentials (including "client secret").
     * @throws UnableToCreateAppException When attempting to create application with a name that already exists.
     */
    @Transactional
    public TppAppDetailResponse createApp(CreateTppAppRequest request) throws UnableToCreateAppException {

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

        // Check if an app with given name exists
        final Iterable<TppAppDetailEntity> appWithName = appDetailRepository.findByTppIdAndAppName(tppEntity.getTppId(), request.getAppName());
        if (appWithName.iterator().hasNext()) { // app with given name already exists
            List<String> errors = Collections.singletonList("Application with given name already exists. Chose a different name or rename/delete existing application.");
            throw new UnableToCreateAppException(errors);
        }

        // Generate app OAuth 2.1 credentials
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
        tppAppDetailEntity.setAppType(request.getAppType());
        tppAppDetailEntity.setPrimaryKey(tppAppDetailKey);

        // Sanitize redirect URIs by Base64 decoding them
        final Set<String> redirectUris = sanitizeRedirectUris(request.getRedirectUris());

        // Sort scopes and make sure a scope is unique in the collection
        final Set<String> scopes = sanitizeScopes(request.getScopes());

        // Store the new OAuth 2.1 credentials in database
        final RegisteredClient.Builder registeredClientBuilder = RegisteredClient.withId(UUID.randomUUID().toString());
        registeredClientBuilder.clientId(clientId);
        registeredClientBuilder.clientSecret(encodedClientSecret);
        registeredClientBuilder.clientName(clientId);
        registeredClientBuilder.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
        registeredClientBuilder.authorizationGrantTypes(authorizationGrantTypes -> {
            authorizationGrantTypes.add(AuthorizationGrantType.AUTHORIZATION_CODE);
            authorizationGrantTypes.add(AuthorizationGrantType.REFRESH_TOKEN);
        });
        registeredClientBuilder.redirectUris(r -> r.addAll(redirectUris));
        registeredClientBuilder.scopes(s -> s.addAll(scopes));
        registeredClientBuilder.clientSettings(ClientSettings.builder().build());
        registeredClientBuilder.tokenSettings(TokenSettings.builder()
                .refreshTokenTimeToLive(tppEngineConfiguration.getDefaultRefreshTokenValidity())
                .accessTokenTimeToLive(Duration.ofSeconds(tppEngineConfiguration.getDefaultAccessTokenValidityInSeconds()))
                .build());
        final RegisteredClient registeredClient = registeredClientBuilder.build();
        registeredClientRepository.save(registeredClient);
        tppAppDetailEntity = appDetailRepository.save(tppAppDetailEntity);

        return TppAppConverter.fromTppAppEntity(tppAppDetailEntity, registeredClient, clientSecret);

    }

    /**
     * Update application details for an app with provided client ID.
     * @param clientId Client ID of TPP app to be updated.
     * @param request Request with information about updated app.
     * @return Information about the updated app.
     * @throws TppNotFoundException In case TPP is not found.
     * @throws TppAppNotFoundException In case TPP app is not found.
     * @throws UnableToCreateAppException When attempting to create application with a name that already exists.
     */
    @Transactional
    public TppAppDetailResponse updateApp(String clientId, CreateTppAppRequest request) throws TppNotFoundException, TppAppNotFoundException, UnableToCreateAppException {
        // Get TPP entity
        TppEntity tppEntity = getTppEntity(request.getTppLicense());

        // Check if an app (some other than the one that is updated) with given name exists
        final Iterable<TppAppDetailEntity> appWithName = appDetailRepository.findByTppIdAndAppName(tppEntity.getTppId(), request.getAppName());
        if (appWithName.iterator().hasNext()) { // app with given name already exists...
            final TppAppDetailEntity appDetailEntity = appWithName.iterator().next();
            if (!appDetailEntity.getPrimaryKey().getAppClientId().equals(clientId)) { // ... and is different from the currently updated one
                List<String> errors = Collections.singletonList("Application with given name already exists. Chose a different name or rename/delete existing application.");
                throw new UnableToCreateAppException(errors);
            }
        }

        // Find application by client ID
        final Optional<TppAppDetailEntity> appDetailEntityOptional = appDetailRepository.findByClientId(clientId);
        if (appDetailEntityOptional.isPresent()) {
            TppAppDetailEntity tppAppDetailEntity = appDetailEntityOptional.get();

            // Check if the client ID belongs to the TPP provider
            if (!Objects.equals(tppAppDetailEntity.getPrimaryKey().getTppId(), tppEntity.getTppId())) {
                throw new TppAppNotFoundException(clientId);
            }

            tppAppDetailEntity.setAppName(request.getAppName());
            tppAppDetailEntity.setAppInfo(request.getAppDescription());
            tppAppDetailEntity.setAppType(request.getAppType());

            // Sanitize redirect URIs by Base64 decoding them
            final Set<String> redirectUris = sanitizeRedirectUris(request.getRedirectUris());

            // Sort scopes and make sure a scope is unique in the collection
            final Set<String> scopes = sanitizeScopes(request.getScopes());

            // Store the new OAuth 2.1 credentials in database
            final RegisteredClient registeredClient = registeredClientRepository.findById(clientId);
            if (registeredClient != null) {
                final RegisteredClient registeredClientUpdated = RegisteredClient.from(registeredClient)
                        .redirectUris(uris -> uris.addAll(redirectUris))
                        .scopes(s -> s.addAll(scopes))
                        .build();
                if (!registeredClient.getScopes().equals(scopes)) {
                    deleteAuthorizationsForClientId(clientId);
                }
                registeredClientRepository.save(registeredClientUpdated);
                tppAppDetailEntity = appDetailRepository.save(tppAppDetailEntity);
                return TppAppConverter.fromTppAppEntity(tppAppDetailEntity, registeredClientUpdated);
            } else {
                throw new TppAppNotFoundException(clientId);
            }

        } else {
            throw new TppAppNotFoundException(clientId);
        }

    }

    /**
     * Renew OAuth 2.1 secret for an app with given client ID and belonging to TPP with specific license.
     * @param clientId Client ID for which to refresh Client Secret.
     * @param tppLicense License information of the party that owns the app with given Client ID.
     * @return Information about application, including new client secret.
     * @throws TppNotFoundException In case TPP was not found.
     * @throws TppAppNotFoundException In case TPP app was not found.
     */
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

            // Generate app OAuth 2.1 credentials
            final String clientSecret = UUID.randomUUID().toString();
            final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
            final String encodedClientSecret = bcrypt.encode(clientSecret);

            // Store the new OAuth 2.1 credentials in database
            final RegisteredClient registeredClient = registeredClientRepository.findById(clientId);
            if (registeredClient != null) {
                final RegisteredClient registeredClientUpdated = RegisteredClient.from(registeredClient).clientSecret(encodedClientSecret).build();
                registeredClientRepository.save(registeredClientUpdated);
                return TppAppConverter.fromTppAppEntity(tppAppDetailEntity, registeredClientUpdated, clientSecret);
            } else {
                throw new TppAppNotFoundException(clientId);
            }

        } else {
            throw new TppAppNotFoundException(clientId);
        }

    }

    /**
     * Delete an app based on provided client ID, with crosscheck to client license.
     * @param clientId Client ID of an app to be deleted.
     * @param tppLicense License info of a TPP party that owns the app.
     * @throws TppNotFoundException In case TPP was not found.
     * @throws TppAppNotFoundException In case TPP app was not found.
     */
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

            // Store the new OAuth 2.1 credentials in database
            final RegisteredClient registeredClient = registeredClientRepository.findById(clientId);
            if (registeredClient != null) {
                appDetailRepository.delete(tppAppDetailEntity);
                // Spring Authorization Server has no delete functionality, expire the client credentials instead
                final RegisteredClient registeredClientUpdated = RegisteredClient.from(registeredClient).clientSecretExpiresAt(Instant.now()).build();
                registeredClientRepository.save(registeredClientUpdated);
                // Delete all existing tokens
                deleteAuthorizationsForClientId(clientId);
            } else {
                throw new TppAppNotFoundException(clientId);
            }
        } else {
            throw new TppAppNotFoundException(clientId);
        }

    }

    /**
     * Block a TPP.
     * @param tppLicense License info of a TPP party that owns the app.
     * @throws TppNotFoundException In case TPP was not found.
     */
    @Transactional
    public TppInfo blockTpp(String tppLicense) throws TppNotFoundException {
        return changeBlockedFlag(tppLicense, true);
    }

    /**
     * Unblock a TPP.
     * @param tppLicense License info of a TPP party that owns the app.
     * @throws TppNotFoundException In case TPP was not found.
     */
    @Transactional
    public TppInfo unblockTpp(String tppLicense) throws TppNotFoundException {
        return changeBlockedFlag(tppLicense, false);
    }

    /**
     * Change blocked flag of a TPP entity
     * @param tppLicense TPP license info.
     * @param blocked Blocked flag
     * @return TPP info, in case the TPP entity was found.
     * @throws TppNotFoundException In case that TPP entity is not found.
     */
    private TppInfo changeBlockedFlag(String tppLicense, boolean blocked) throws TppNotFoundException {
        TppEntity tppEntity = getTppEntity(tppLicense);
        if (tppEntity.isBlocked() == blocked) {
            logger.info("TPP with id: {} is already {}", tppEntity.getTppId(), tppEntity.isBlocked() ? "blocked" : "unblocked");
        } else {
            tppEntity.setBlocked(blocked);
            logger.info("Changing TPP with id: {} to: {}", tppEntity.getTppId(), tppEntity.isBlocked() ? "blocked" : "unblocked");
        }
        return TppAppConverter.fromTppEntity(tppEntity);
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

    /**
     * Create a set with unique values from an array, sorted.
     * @param source Original array.
     * @return A set with unique values from an array, sorted.
     */
    private TreeSet<String> uniqueCommaSeparated(String[] source) {
        TreeSet<String> set = new TreeSet<>();
        Collections.addAll(set, source);
        return set;
    }

    /**
     * Create a set with unique redirect URIs.
     * @param redirectUris Original redirect URIs.
     * @return A set with unique redirect URIs, or emtpy if original array is null.
     */
    private Set<String> sanitizeRedirectUris(String[] redirectUris) {
        final Set<String> redirectUriSet = new HashSet<>();
        if (redirectUris != null) {
            for (String uris : redirectUris) {
                // comma is not allowed, we cannot encode the data in DB due to Spring OAuth support
                redirectUriSet.add(uris.replace(",", "%2C"));
            }
        }
        return redirectUriSet;
    }

    /**
     * Create a set with unique sorted scopes.
     * @param scopes Original scopes.
     * @return A set with unique sorted scopes, or null if original array is null.
     */
    private Set<String> sanitizeScopes(String[] scopes) {
        return uniqueCommaSeparated(scopes);
    }

    /**
     * Delete authorization records for a client.
     * @param clientId Client identifier.
     */
    private void deleteAuthorizationsForClientId(String clientId) {
        jdbcTemplate.query("DELETE FROM oauth2_authorization WHERE registered_client_id=?",
                preparedStatement -> preparedStatement.setString(1, clientId),
                rs -> null);
    }

}
