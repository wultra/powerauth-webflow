/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.powerauth.soap.ActivationStatus;
import io.getlime.powerauth.soap.GetActivationListForUserResponse;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.model.entity.UserAuthMethodDetail;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.exception.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetUserAuthMethodsResponse;
import io.getlime.security.powerauth.soap.spring.client.PowerAuthServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for getting information about current availability of authentication methods.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Service
public class AuthMethodQueryService {

    private final NextStepClient nextStepClient;
    private final PowerAuthServiceClient powerAuthServiceClient;

    @Autowired
    public AuthMethodQueryService(NextStepClient nextStepClient, PowerAuthServiceClient powerAuthServiceClient, ObjectMapper objectMapper) {
        this.nextStepClient = nextStepClient;
        this.powerAuthServiceClient = powerAuthServiceClient;
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
                        return isMobileTokenAuthMethodAvailable(userId, operationId);
                    }
                }
            }
            return false;
        } catch (NextStepServiceException e) {
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
                String activationId = config.get("activationId");
                if (activationId != null && !activationId.isEmpty()) {
                    // set successfully parsed activationId from configuration
                    configuredActivation = activationId;
                }
            }
        }
        return configuredActivation;
    }

    /**
     * Returns whether Mobile Token authentication method is currently available by querying the PowerAuth backend for ACTIVE activations.
     * @param userId User ID.
     * @param operationId Operation ID.
     * @return Whether Mobile Token authentication method is available.
     */
    private boolean isMobileTokenAuthMethodAvailable(String userId, String operationId) throws NextStepServiceException {
        String configuredActivationId = getActivationIdForMobileTokenAuthMethod(userId);
        if (configuredActivationId == null) {
            return false;
        }
        // check whether user has an ACTIVE activation and it matches configured activation
        List<GetActivationListForUserResponse.Activations> allActivations = powerAuthServiceClient.getActivationListForUser(userId);
        for (GetActivationListForUserResponse.Activations activation: allActivations) {
            if (activation.getActivationStatus() == ActivationStatus.ACTIVE && activation.getActivationId().equals(configuredActivationId)) {
                // user has an active activation and it is the configured activation - method can be used
                return true;
            }
        }
        return false;
    }

}
