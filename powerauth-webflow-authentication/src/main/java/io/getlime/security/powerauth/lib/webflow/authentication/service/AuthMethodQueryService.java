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
package io.getlime.security.powerauth.lib.webflow.authentication.service;

import com.wultra.security.powerauth.client.PowerAuthClient;
import com.wultra.security.powerauth.client.v3.ActivationStatus;
import com.wultra.security.powerauth.client.v3.GetActivationListForUserResponse;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthStep;
import io.getlime.security.powerauth.lib.nextstep.model.entity.UserAuthMethodDetail;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetMobileTokenConfigResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetUserAuthMethodsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for getting information about current availability of authentication methods.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class AuthMethodQueryService {

    private static final Logger logger = LoggerFactory.getLogger(AuthMethodQueryService.class);

    private final NextStepClient nextStepClient;
    private final PowerAuthClient powerAuthClient;

    /**
     * Service constructor.
     * @param nextStepClient Next step client.
     * @param powerAuthClient PowerAuth 2.0 client.
     */
    @Autowired
    public AuthMethodQueryService(NextStepClient nextStepClient, PowerAuthClient powerAuthClient) {
        this.nextStepClient = nextStepClient;
        this.powerAuthClient = powerAuthClient;
    }

    /**
     * Returns whether authentication method is currently available.
     o
     * @param authMethod Authentication method.
     * @param userId User ID.
     * @param operationId Operation ID.
     * @return Whether authentication method is available.
     */
    public boolean isAuthMethodEnabled(AuthMethod authMethod, String userId, String operationId) {
        try {
            ObjectResponse<GetUserAuthMethodsResponse> response = nextStepClient.getAuthMethodsEnabledForUser(userId);
            List<UserAuthMethodDetail> enabledAuthMethods = response.getResponseObject().getUserAuthMethods();
            for (UserAuthMethodDetail authMethodDetail: enabledAuthMethods) {
                if (authMethodDetail.getAuthMethod() == authMethod) {
                    // Authentication methods without UI are not available
                    if (!authMethodDetail.getHasUserInterface()) {
                        return false;
                    }
                    // AuthMethod POWERAUTH_TOKEN requires special logic - activation could be BLOCKED at any time
                    if (authMethod != AuthMethod.POWERAUTH_TOKEN) {
                        return true;
                    } else {
                        return isMobileTokenAvailable(userId, operationId);
                    }
                }
            }
            return false;
        } catch (NextStepServiceException e) {
            logger.error("Error occurred in Next Step server", e);
            return false;
        }
    }

    /**
     * Get the configured activationId for mobile token. Null value is returned when activationId is not configured or configuration is invalid.
     * @param userId user ID.
     * @return Activation ID.
     * @throws NextStepServiceException Thrown when Next Step request fails.
     */
    public String getActivationIdForMobileTokenAuthMethod(String userId) throws NextStepServiceException {
        String configuredActivation = null;
        ObjectResponse<GetUserAuthMethodsResponse> response = nextStepClient.getAuthMethodsEnabledForUser(userId);
        GetUserAuthMethodsResponse userAuthMethods = response.getResponseObject();
        for (UserAuthMethodDetail authMethodDetail : userAuthMethods.getUserAuthMethods()) {
            if (authMethodDetail.getAuthMethod() == AuthMethod.POWERAUTH_TOKEN) {
                Map<String, String> config = authMethodDetail.getConfig();
                if (config != null) {
                    String activationId = config.get("activationId");
                    if (activationId != null && !activationId.isEmpty()) {
                        // set successfully parsed activationId from configuration
                        configuredActivation = activationId;
                    }
                }
            }
        }
        return configuredActivation;
    }

    /**
     * Get information whether mobile token is available. Following checks are performed:
     * <ul>
     * <li>Non-SCA operations: POWERAUTH_TOKEN method is available as a next step for the operation.</li>
     * <li>SCA operations: Operation is among pending operations for mobile token.</li>
     * <li>User has an ACTIVE activation in PowerAuth server and it matches configured activation ID in Next Step.</li>
     * </ul>
     *
     * @param userId User ID.
     * @param operationId Operation ID.
     * @return Whether Mobile Token is currently available for given user ID and operation ID.
     * @throws NextStepServiceException Thrown when Next Step request fails.
     */
    public boolean isMobileTokenAvailable(String userId, String operationId) throws NextStepServiceException {
        // Non-SCA usage: check whether POWERAUTH_TOKEN method is available as next step for operation (used in operation review step)
        ObjectResponse<GetOperationDetailResponse> objectResponseOperation = nextStepClient.getOperationDetail(operationId);
        GetOperationDetailResponse operation = objectResponseOperation.getResponseObject();
        boolean mobileTokenAvailableAsNextStep = false;
        for (AuthStep step: operation.getSteps()) {
            if (step.getAuthMethod() == AuthMethod.POWERAUTH_TOKEN) {
                mobileTokenAvailableAsNextStep = true;
                break;
            }
        }

        // SCA usage: check whether mobile token is enabled using configuration for user ID, operation name and chosen authentication method
        if (!mobileTokenAvailableAsNextStep) {
            // Retrieve pending mobile token operations for given user and check that operation with given operation ID is among them
            if (operation.getChosenAuthMethod() != null) {
                ObjectResponse<GetMobileTokenConfigResponse> objectResponse = nextStepClient.getMobileTokenConfig(userId, operation.getOperationName(), operation.getChosenAuthMethod());
                GetMobileTokenConfigResponse configResponse = objectResponse.getResponseObject();
                if (!configResponse.isMobileTokenEnabled()) {
                    return false;
                }
            }
        }

        // Retrieve activation ID configured for mobile token
        String configuredActivationId = getActivationIdForMobileTokenAuthMethod(userId);

        // Check whether user has an ACTIVE activation and it matches configured activation
        List<GetActivationListForUserResponse.Activations> allActivations = powerAuthClient.getActivationListForUser(userId);
        for (GetActivationListForUserResponse.Activations activation : allActivations) {
            if (activation.getActivationStatus() == ActivationStatus.ACTIVE && activation.getActivationId().equals(configuredActivationId)) {
                // User has an active activation and it is the configured activation - mobile token is available
                return true;
            }
        }
        return false;
    }

}
