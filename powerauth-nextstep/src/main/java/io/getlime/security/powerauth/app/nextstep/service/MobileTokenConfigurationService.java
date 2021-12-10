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
package io.getlime.security.powerauth.app.nextstep.service;

import com.wultra.core.audit.base.Audit;
import com.wultra.security.powerauth.client.PowerAuthClient;
import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import com.wultra.security.powerauth.client.v3.ActivationStatus;
import com.wultra.security.powerauth.client.v3.GetActivationStatusResponse;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import io.getlime.security.powerauth.app.nextstep.service.catalogue.ServiceCatalogue;
import io.getlime.security.powerauth.lib.nextstep.model.entity.EnableMobileTokenResult;
import io.getlime.security.powerauth.lib.nextstep.model.entity.UserAuthMethodDetail;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.OperationConfigNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationConfigDetailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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

    private final ServiceCatalogue serviceCatalogue;
    private final PowerAuthClient powerAuthClient;
    private final Audit audit;

    /**
     * Service constructor.
     * @param serviceCatalogue Service catalogue.
     * @param powerAuthClient PowerAuth service client.
     * @param audit Audit interface.
     */
    @Autowired
    public MobileTokenConfigurationService(@Lazy ServiceCatalogue serviceCatalogue, PowerAuthClient powerAuthClient, Audit audit) {
        this.serviceCatalogue = serviceCatalogue;
        this.powerAuthClient = powerAuthClient;
        this.audit = audit;
    }

    /**
     * Decide whether mobile token is enabled for given user ID, operation name and authentication method.
     * @param userId User ID.
     * @param operationName Operation name.
     * @param authMethod Authentication method.
     * @return Whether mobile token is enabled.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    public boolean isMobileTokenActive(String userId, String operationName, AuthMethod authMethod) throws InvalidConfigurationException {
        final OperationConfigurationService operationConfigurationService = serviceCatalogue.getOperationConfigurationService();
        final AuthMethodService authMethodService = serviceCatalogue.getAuthMethodService();
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
            final GetOperationConfigDetailResponse config = operationConfigurationService.getOperationConfig(operationName);
            if (!config.isMobileTokenEnabled()) {
                logger.debug("Mobile token is disabled for operation name: {}", operationName);
                // Mobile token is not enabled for this operation, skip it
                return false;
            }
        } catch (OperationConfigNotFoundException e) {
            // Operation is not configured, skip it
            logger.error(e.getMessage(), e);
            audit.error(e.getMessage(), e);
            return false;
        }

        final String activationId = getActivationId(userId);
        boolean activationConfiguredForMobileToken = activationId != null && !activationId.isEmpty();

        if (!activationConfiguredForMobileToken) {
            // Activation ID is not configured for mobile token, so mobile token cannot be used
            logger.debug("Mobile token is disabled because activation is not configured in user preferences for user: {}", userId);
            return false;
        }

        boolean authMethodSupportsMobileToken = false;
        List<UserAuthMethodDetail> authMethods = authMethodService.listAuthMethodsEnabledForUser(userId);
        for (UserAuthMethodDetail userAuthMethod : authMethods) {
            // In case the chosen auth method is enabled for user and it supports mobile token,
            // this operation should be added into pending operation list.
            if (userAuthMethod.getAuthMethod() == authMethod && userAuthMethod.getHasMobileToken()) {
                logger.debug("Mobile token is enabled for user ID: {}, operation name: {}, authentication method: {}", userId, operationName, authMethod);
                authMethodSupportsMobileToken = true;
            }
        }

        if (!authMethodSupportsMobileToken) {
            logger.debug("Mobile token is disabled because authentication method {} does not support mobile token", authMethod);
            return false;
        }

        // Check status of activation in PowerAuth server
        try {
            final GetActivationStatusResponse statusResponse = powerAuthClient.getActivationStatus(activationId);
            final ActivationStatus activationStatus = statusResponse.getActivationStatus();
            if (activationStatus == ActivationStatus.ACTIVE) {
                logger.debug("Mobile token is active for user ID: {}, operation name: {}, authentication method: {}", userId, operationName, authMethod);
                return true;
            }
            logger.debug("Mobile token is disabled because activation status is: {} for user ID: {}", activationStatus, userId);
            return false;
        } catch (PowerAuthClientException ex) {
            logger.warn("Activation status call failed, error: " + ex.getMessage(), ex);
        }
        return false;
    }

    /**
     * Enable mobile token authentication method.
     *
     * @param operation Operation entity.
     * @return Enable mobile token result.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    public EnableMobileTokenResult enableMobileToken(OperationEntity operation) throws InvalidConfigurationException {
        final PowerAuthOperationService powerAuthOperationService = serviceCatalogue.getPowerAuthOperationService();
        if (operation == null || operation.getUserId() == null || operation.getOperationName() == null) {
            return new EnableMobileTokenResult(false, null);
        }
        final String userId = operation.getUserId();
        final String operationName = operation.getOperationName();
        if (!isMobileTokenActive(userId, operationName, AuthMethod.POWERAUTH_TOKEN)){
            return new EnableMobileTokenResult(false, null);
        }

        final String activationId = getActivationId(userId);
        if (activationId == null || activationId.isEmpty()) {
            return new EnableMobileTokenResult(false, null);
        }

        // Create operation in PowerAuth server
        final String paOperationId = powerAuthOperationService.createOperation(operation, activationId);
        return new EnableMobileTokenResult(true, paOperationId);
    }

    /**
     * Get activation ID for user ID.
     * @param userId User ID.
     * @return Activation ID or null in case activation ID is not configured.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    private String getActivationId(String userId) throws InvalidConfigurationException {
        final AuthMethodService authMethodService = serviceCatalogue.getAuthMethodService();
        final List<UserAuthMethodDetail> authMethods = authMethodService.listAuthMethodsEnabledForUser(userId);
        for (UserAuthMethodDetail userAuthMethod : authMethods) {
            // Check whether activation ID is configured for mobile token, this configuration is set using
            // POWERAUTH_TOKEN authentication method.
            if (userAuthMethod.getAuthMethod() == AuthMethod.POWERAUTH_TOKEN) {
                final Map<String, String> config = userAuthMethod.getConfig();
                if (config != null) {
                    return config.get("activationId");
                }
            }
        }
        return null;
    }

}
