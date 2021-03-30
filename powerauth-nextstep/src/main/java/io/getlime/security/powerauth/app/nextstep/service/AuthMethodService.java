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
package io.getlime.security.powerauth.app.nextstep.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import io.getlime.security.powerauth.app.nextstep.repository.AuthMethodRepository;
import io.getlime.security.powerauth.app.nextstep.repository.OperationHistoryRepository;
import io.getlime.security.powerauth.app.nextstep.repository.StepDefinitionRepository;
import io.getlime.security.powerauth.app.nextstep.repository.UserPrefsRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.AuthMethodEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.StepDefinitionEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserIdentityEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserPrefsEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthMethodDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.UserAuthMethodDetail;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateAuthMethodRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteAuthMethodRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetEnabledMethodListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateAuthMethodResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteAuthMethodResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetEnabledMethodListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This service handles persistence of user authentication methods.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class AuthMethodService {

    private static final Logger logger = LoggerFactory.getLogger(AuthMethodService.class);

    private final AuthMethodRepository authMethodRepository;
    private final UserPrefsRepository userPrefsRepository;
    private final UserIdentityLookupService userIdentityLookupService;
    private final StepDefinitionRepository stepDefinitionRepository;
    private final OperationHistoryRepository operationHistoryRepository;
    private final MobileTokenConfigurationService mobileTokenConfigurationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Service constructor.
     * @param authMethodRepository Authentication method repository.
     * @param userPrefsRepository User preferences repository.
     * @param userIdentityLookupService User identity lookup service.
     * @param stepDefinitionRepository Step definition repository.
     * @param operationHistoryRepository Operation history repository.
     * @param mobileTokenConfigurationService Mobile token configuration service.
     */
    @Autowired
    public AuthMethodService(AuthMethodRepository authMethodRepository, UserPrefsRepository userPrefsRepository, UserIdentityLookupService userIdentityLookupService, StepDefinitionRepository stepDefinitionRepository, OperationHistoryRepository operationHistoryRepository, @Lazy MobileTokenConfigurationService mobileTokenConfigurationService) {
        this.authMethodRepository = authMethodRepository;
        this.userPrefsRepository = userPrefsRepository;
        this.userIdentityLookupService = userIdentityLookupService;
        this.stepDefinitionRepository = stepDefinitionRepository;
        this.operationHistoryRepository = operationHistoryRepository;
        this.mobileTokenConfigurationService = mobileTokenConfigurationService;
    }

    /**
     * Create an authentication method.
     * @param request Create authentication method request.
     * @return Create authentication method response.
     * @throws AuthMethodAlreadyExistsException Thrown when authentication method already exists.
     */
    @Transactional
    public CreateAuthMethodResponse createAuthMethod(CreateAuthMethodRequest request) throws AuthMethodAlreadyExistsException {
        Optional<AuthMethodEntity> authMethodOptional = authMethodRepository.findByAuthMethod(request.getAuthMethod());
        if (authMethodOptional.isPresent()) {
            throw new AuthMethodAlreadyExistsException("Authentication method already exists: " + request.getAuthMethod());
        }
        AuthMethodEntity authMethod = new AuthMethodEntity();
        authMethod.setAuthMethod(request.getAuthMethod());
        authMethod.setOrderNumber(request.getOrderNumber());
        authMethod.setCheckUserPrefs(request.getCheckUserPrefs());
        authMethod.setUserPrefsColumn(request.getUserPrefsColumn());
        authMethod.setUserPrefsDefault(request.getUserPrefsDefault());
        authMethod.setCheckAuthFails(request.getCheckAuthFails());
        authMethod.setMaxAuthFails(request.getMaxAuthFails());
        authMethod.setHasUserInterface(request.getHasUserInterface());
        authMethod.setDisplayNameKey(request.getDisplayNameKey());
        authMethod.setHasMobileToken(request.getHasMobileToken());
        authMethodRepository.save(authMethod);
        CreateAuthMethodResponse response = new CreateAuthMethodResponse();
        response.setAuthMethod(authMethod.getAuthMethod());
        response.setOrderNumber(authMethod.getOrderNumber());
        response.setCheckUserPrefs(authMethod.getCheckUserPrefs());
        response.setUserPrefsColumn(authMethod.getUserPrefsColumn());
        response.setUserPrefsDefault(authMethod.getUserPrefsDefault());
        response.setCheckAuthFails(authMethod.getCheckAuthFails());
        response.setMaxAuthFails(authMethod.getMaxAuthFails());
        response.setHasUserInterface(authMethod.getHasUserInterface());
        response.setDisplayNameKey(authMethod.getDisplayNameKey());
        response.setHasMobileToken(authMethod.getHasMobileToken());
        return response;
    }

    /**
     * Lists all authentication methods supported by the Next Step server.
     *
     * @return List of all authentication methods.
     */
    @Transactional
    public List<AuthMethodDetail> listAuthMethods() {
        List<AuthMethodDetail> allMethods = new ArrayList<>();
        List<AuthMethodEntity> authMethodList = authMethodRepository.findAllAuthMethods();
        for (AuthMethodEntity authMethodEntity : authMethodList) {
            allMethods.add(getAuthMethodDetail(authMethodEntity));
        }
        return allMethods;
    }

    /**
     * List authentication methods enabled for given user. Methods which can be enabled/disabled are retrieved from
     * user preferences. All other methods are enabled.
     *
     * @param userId User ID
     * @return List of authentication methods enabled for given user.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @Transactional
    public List<UserAuthMethodDetail> listAuthMethodsEnabledForUser(String userId) throws InvalidConfigurationException {
        List<UserAuthMethodDetail> enabledMethods = new ArrayList<>();
        List<AuthMethodEntity> authMethodList = authMethodRepository.findAllAuthMethods();
        UserPrefsEntity userPrefs = null;
        if (userId != null) {
            // read user prefs only when user ID is not null, for some authentication methods user ID is not known
            userPrefs = userPrefsRepository.findUserPrefs(userId);
        }
        for (AuthMethodEntity authMethodEntity : authMethodList) {
            if (authMethodEntity.getCheckUserPrefs()) {
                // methods with user prefs require special handling
                if (userPrefs != null) {
                    // get status of methods with user prefs
                    if (userPrefs.getAuthMethodEnabled(authMethodEntity.getUserPrefsColumn())) {
                        // read configuration of method from user prefs
                        String config = userPrefs.getAuthMethodConfig(authMethodEntity.getUserPrefsColumn());
                        Map<String, String> configMap;
                        try {
                            MapType mapType = objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class);
                            configMap = objectMapper.readValue(config, mapType);
                        } catch (IOException e) {
                            logger.error("Error while deserializing config", e);
                            configMap = new HashMap<>();
                        }
                        // add method in case it is enabled in user prefs
                        enabledMethods.add(getUserAuthMethodDetail(userId, authMethodEntity, configMap));
                    }
                } else {
                    // user prefs are not set - resolve methods with user prefs by their default value
                    if (authMethodEntity.getUserPrefsDefault()) {
                        // add method in case it is enabled by default
                        enabledMethods.add(getUserAuthMethodDetail(userId, authMethodEntity, Collections.emptyMap()));
                    }
                }
            } else {
                // add all methods without user prefs
                enabledMethods.add(getUserAuthMethodDetail(userId, authMethodEntity, Collections.emptyMap()));
            }
        }
        return enabledMethods;
    }

    /**
     * Enable or disable an authentication method for given user.
     *
     * @param userId User ID.
     * @param authMethod Authentication method.
     * @param enabled True if enabled, false if disabled, null if unspecified.
     * @param config Authentication method configuration.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Transactional
    public void updateAuthMethodForUser(String userId, AuthMethod authMethod, Boolean enabled, Map<String, String> config) throws InvalidConfigurationException, InvalidRequestException {
        List<AuthMethodEntity> authMethodList = authMethodRepository.findAllAuthMethods();
        boolean authMethodFound = false;
        // check whether this method supports modifications at all
        for (AuthMethodEntity authMethodEntity : authMethodList) {
            if (authMethodEntity.getAuthMethod() == authMethod) {
                authMethodFound = true;
                if (!authMethodEntity.getCheckUserPrefs()) {
                    throw new InvalidRequestException("Authentication method " + authMethod + " does not support user preferences.");
                }
            }
        }
        if (!authMethodFound) {
            throw new InvalidRequestException("Authentication method " + authMethod + " is not supported.");
        }
        String configAsStr;
        try {
            configAsStr = objectMapper.writeValueAsString(config);
        } catch (IOException e) {
            logger.error("Error while serializing config", e);
            configAsStr = "{}";
        }
        UserPrefsEntity userPrefs = userPrefsRepository.findUserPrefs(userId);
        if (userPrefs == null) {
            // create new user prefs
            userPrefs = new UserPrefsEntity();
            userPrefs.setUserId(userId);
            // set defaults
            for (AuthMethodEntity authMethodEntity : authMethodList) {
                if (authMethodEntity.getCheckUserPrefs()) {
                    if (authMethodEntity.getAuthMethod() == authMethod) {
                        // set requested value for method which is being updated
                        userPrefs.setAuthMethodEnabled(authMethodEntity.getUserPrefsColumn(), enabled);
                    } else {
                        // set default value for other methods than the method which is being updated
                        userPrefs.setAuthMethodEnabled(authMethodEntity.getUserPrefsColumn(), authMethodEntity.getUserPrefsDefault());
                    }
                    // set authMethod configuration
                    userPrefs.setAuthMethodConfig(authMethodEntity.getUserPrefsColumn(), configAsStr);
                }
            }
        } else {
            // update existing user prefs
            for (AuthMethodEntity authMethodEntity : authMethodList) {
                if (authMethodEntity.getCheckUserPrefs()) {
                    if (authMethodEntity.getAuthMethod() == authMethod) {
                        // set requested value for method which is being updated
                        userPrefs.setAuthMethodEnabled(authMethodEntity.getUserPrefsColumn(), enabled);
                        // set authMethod configuration
                        userPrefs.setAuthMethodConfig(authMethodEntity.getUserPrefsColumn(), configAsStr);
                    }
                }
            }

        }
        // finally save created or updated userPrefs
        userPrefsRepository.save(userPrefs);
    }

    /**
     * Get list of enabled authentication methods for a user and operation. Check current availability of mobile token.
     * @param request Get enabled method list request.
     * @return Get enabled method list response.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @Transactional
    public GetEnabledMethodListResponse getEnabledMethodList(GetEnabledMethodListRequest request) throws InvalidConfigurationException {
        String userId = request.getUserId();
        String operationName = request.getOperationName();
        // Lookup user identity to obtain its status
        Optional<UserIdentityEntity> userIdentityOptional = userIdentityLookupService.findUserOptional(userId);
        // Get all methods enabled for user
        List<AuthMethod> enabledAuthMethods = listAuthMethodsEnabledForUser(userId).stream()
                .map(UserAuthMethodDetail::getAuthMethod)
                .collect(Collectors.toList());
        // Filter methods by step definitions for given operation to return only relevant methods for the operation.
        // Do not return INIT method, it is not used for authentication.
        List<StepDefinitionEntity> stepDefinitions = stepDefinitionRepository.findStepDefinitionsForOperation(operationName);
        List<AuthMethod> methodsPerOperation = stepDefinitions.stream()
                .map(StepDefinitionEntity::getRequestAuthMethod)
                .filter(authMethod -> authMethod != AuthMethod.INIT)
                .collect(Collectors.toList());
        // Merge enabled methods and methods used in the operation
        List<AuthMethod> filteredMethods = enabledAuthMethods.stream()
                .filter(methodsPerOperation::contains)
                .collect(Collectors.toList());
        // Check mobile token status, remove POWERAUTH_TOKEN method in case it is not currently available
        if (filteredMethods.contains(AuthMethod.POWERAUTH_TOKEN)) {
            if (!mobileTokenConfigurationService.isMobileTokenActive(userId, operationName, AuthMethod.POWERAUTH_TOKEN)) {
                filteredMethods.remove(AuthMethod.POWERAUTH_TOKEN);
            }
        }
        GetEnabledMethodListResponse response = new GetEnabledMethodListResponse();
        response.setUserId(userId);
        response.setOperationName(operationName);
        if (userIdentityOptional.isPresent()) {
            UserIdentityEntity user = userIdentityOptional.get();
            response.setUserIdentityStatus(user.getStatus());
        }
        response.getEnabledAuthMethods().addAll(filteredMethods);
        return response;
    }

    /**
     * Delete an authentication method.
     * @param request Delete authentication method request.
     * @return Delete authentication method response.
     * @throws AuthMethodNotFoundException Thrown when authentication method is not found.
     * @throws DeleteNotAllowedException Thrown when delete action is not allowed.
     */
    @Transactional
    public DeleteAuthMethodResponse deleteAuthMethod(DeleteAuthMethodRequest request) throws AuthMethodNotFoundException, DeleteNotAllowedException {
        Optional<AuthMethodEntity> authMethodOptional = authMethodRepository.findByAuthMethod(request.getAuthMethod());
        if (!authMethodOptional.isPresent()) {
            throw new AuthMethodNotFoundException("Authentication method not found: " + request.getAuthMethod());
        }
        long requestAuthMethods = stepDefinitionRepository.countByRequestAuthMethod(request.getAuthMethod());
        long responseAuthMethods = stepDefinitionRepository.countByResponseAuthMethod(request.getAuthMethod());
        long historyRequestAuthMethods = operationHistoryRepository.countByRequestAuthMethod(request.getAuthMethod());
        long historyChosenAuthMethods = operationHistoryRepository.countByChosenAuthMethod(request.getAuthMethod());
        if (requestAuthMethods > 0 || responseAuthMethods > 0 || historyRequestAuthMethods > 0 || historyChosenAuthMethods > 0) {
            throw new DeleteNotAllowedException("Authentication method cannot be deleted because it is used: " + request.getAuthMethod());
        }
        AuthMethodEntity authMethod = authMethodOptional.get();
        authMethodRepository.delete(authMethod);
        DeleteAuthMethodResponse response = new DeleteAuthMethodResponse();
        response.setAuthMethod(authMethod.getAuthMethod());
        return response;
    }


    /**
     * Converts AuthMethodEntity into AuthMethodDetail which contains less fields available for the UI.
     *
     * @param authMethodEntity entity representing the authentication method.
     * @return Authentication method detail.
     */
    private AuthMethodDetail getAuthMethodDetail(AuthMethodEntity authMethodEntity) {
        if (authMethodEntity == null) {
            return null;
        }
        AuthMethodDetail authMethodDetail = new AuthMethodDetail();
        authMethodDetail.setAuthMethod(authMethodEntity.getAuthMethod());
        authMethodDetail.setOrderNumber(authMethodDetail.getOrderNumber());
        authMethodDetail.setCheckUserPrefs(authMethodDetail.getCheckUserPrefs());
        authMethodDetail.setUserPrefsColumn(authMethodDetail.getUserPrefsColumn());
        authMethodDetail.setUserPrefsDefault(authMethodDetail.getUserPrefsDefault());
        authMethodDetail.setCheckAuthFails(authMethodDetail.getCheckAuthFails());
        authMethodDetail.setMaxAuthFails(authMethodDetail.getMaxAuthFails());
        authMethodDetail.setHasUserInterface(authMethodEntity.getHasUserInterface());
        authMethodDetail.setDisplayNameKey(authMethodEntity.getDisplayNameKey());
        authMethodDetail.setHasMobileToken(authMethodEntity.getHasMobileToken());
        return authMethodDetail;
    }

    /**
     * Converts AuthMethodEntity into UserAuthMethodDetail which contains less fields available for the UI.
     * @param userId User ID.
     * @param authMethodEntity Authentication method entity.
     * @param config Authentication method configuration.
     * @return Authentication method detail for given userId.
     */
    private UserAuthMethodDetail getUserAuthMethodDetail(String userId, AuthMethodEntity authMethodEntity, Map<String, String> config) {
        if (authMethodEntity == null) {
            return null;
        }
        UserAuthMethodDetail userAuthMethodDetail = new UserAuthMethodDetail();
        userAuthMethodDetail.setUserId(userId);
        userAuthMethodDetail.setAuthMethod(authMethodEntity.getAuthMethod());
        userAuthMethodDetail.setHasUserInterface(authMethodEntity.getHasUserInterface());
        userAuthMethodDetail.setDisplayNameKey(authMethodEntity.getDisplayNameKey());
        userAuthMethodDetail.setHasMobileToken(authMethodEntity.getHasMobileToken());
        userAuthMethodDetail.getConfig().putAll(config);
        return userAuthMethodDetail;
    }

}
