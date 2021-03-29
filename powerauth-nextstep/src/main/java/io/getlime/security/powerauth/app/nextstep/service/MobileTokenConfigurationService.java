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
package io.getlime.security.powerauth.app.nextstep.service;

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.UserAuthMethodDetail;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OperationConfigNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationConfigDetailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for managing mobile token configuration.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class MobileTokenConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(MobileTokenConfigurationService.class);

    private final OperationConfigurationService operationConfigurationService;
    private final AuthMethodService authMethodService;

    /**
     * Service constructor.
     * @param operationConfigurationService Operation configuration service.
     * @param authMethodService Authentication method service.
     */
    @Autowired
    public MobileTokenConfigurationService(OperationConfigurationService operationConfigurationService, AuthMethodService authMethodService) {
        this.operationConfigurationService = operationConfigurationService;
        this.authMethodService = authMethodService;
    }

    /**
     * Decide whether mobile token is enabled for given user ID, operation name and authentication method.
     * @param userId User ID.
     * @param operationName Operation name.
     * @param authMethod Authentication method.
     * @return Whether mobile token is enabled.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    public boolean isMobileTokenEnabled(String userId, String operationName, AuthMethod authMethod) throws InvalidConfigurationException {
        // Check input parameters
        if (userId == null) {
            logger.debug("Mobile token is disabled because user is unknown for this authentication step");
            return false;
        }
        if (operationName == null) {
            logger.warn("Invalid call of isMobileTokenEnabled, operation name is null");
            return false;
        }
        if (authMethod == null) {
            logger.warn("Invalid call of isMobileTokenEnabled, authentication method is null");
            return false;
        }

        // Check whether mobile token is enabled for operation by operation name
        try {
            GetOperationConfigDetailResponse config = operationConfigurationService.getOperationConfig(operationName);
            if (!config.isMobileTokenEnabled()) {
                logger.debug("Mobile token is disabled for operation name: {}", operationName);
                // Mobile token is not enabled for this operation, skip it
                return false;
            }
        } catch (OperationConfigNotFoundException e) {
            // Operation is not configured, skip it
            logger.error(e.getMessage(), e);
            return false;
        }

        // Consider only authentication methods which are enabled for user
        List<UserAuthMethodDetail> authMethods = authMethodService.listAuthMethodsEnabledForUser(userId);
        boolean activationConfiguredForMobileToken = false;
        for (UserAuthMethodDetail userAuthMethod : authMethods) {
            // Check whether activation ID is configured for mobile token, this configuration is set using
            // POWERAUTH_TOKEN authentication method.
            if (userAuthMethod.getAuthMethod() == AuthMethod.POWERAUTH_TOKEN) {
                Map<String, String> config = userAuthMethod.getConfig();
                if (config != null) {
                    String activationId = config.get("activationId");
                    if (activationId != null && !activationId.isEmpty()) {
                        activationConfiguredForMobileToken = true;
                    }
                }
            }
        }
        if (!activationConfiguredForMobileToken) {
            // Activation ID is not configured for mobile token, so mobile token cannot be used
            logger.debug("Mobile token is disabled because activation is not configured in user preferences for user: {}", userId);
            return false;
        }

        // TODO - implement PowerAuth server status check for activation

        for (UserAuthMethodDetail userAuthMethod : authMethods) {
            // In case the chosen auth method is enabled for user and it supports mobile token,
            // this operation should be added into pending operation list.
            if (userAuthMethod.getAuthMethod() == authMethod && userAuthMethod.getHasMobileToken()) {
                logger.debug("Mobile token is enabled for user ID: {}, operation name: {}, authentication method: {}", userId, operationName, authMethod);
                return true;
            }
        }

        // Mobile token is disabled for this authentication method
        logger.debug("Mobile token is disabled because authentication method {} does not support mobile token", authMethod);
        return false;
    }

    /**
     * Enable mobile token authentication method.
     *
     * @param operation Operation entity.
     * @return Whether mobile token was enabled.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    public boolean enableMobileToken(OperationEntity operation) throws InvalidConfigurationException {
        if (operation == null || operation.getUserId() == null || operation.getOperationName() == null) {
            return false;
        }
        String userId = operation.getUserId();
        String operationName = operation.getOperationName();
        if (!isMobileTokenEnabled(userId, operationName, AuthMethod.POWERAUTH_TOKEN)){
            return false;
        }

        // TODO - create an operation in PowerAuth server

        return true;
    }
}
