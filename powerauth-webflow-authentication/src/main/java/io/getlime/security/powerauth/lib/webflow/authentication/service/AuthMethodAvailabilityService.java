package io.getlime.security.powerauth.lib.webflow.authentication.service;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.powerauth.soap.ActivationStatus;
import io.getlime.powerauth.soap.GetActivationListForUserResponse;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepServiceException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthMethodDetail;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetAuthMethodsResponse;
import io.getlime.security.powerauth.soap.spring.client.PowerAuthServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for getting information about current availability of authentication methods.
 *
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
@Service
public class AuthMethodAvailabilityService {

    private final NextStepClient nextStepClient;
    private final PowerAuthServiceClient powerAuthServiceClient;

    @Autowired
    public AuthMethodAvailabilityService(NextStepClient nextStepClient, PowerAuthServiceClient powerAuthServiceClient) {
        this.nextStepClient = nextStepClient;
        this.powerAuthServiceClient = powerAuthServiceClient;
    }

    /**
     * Returns whether authentication method is currently available.
     *
     * @param authMethod Authentication method.
     * @param userId User ID.
     * @param operationId Operation ID.
     * @return Whether authentication method is available.
     */
    public boolean isAuthMethodEnabledForUser(AuthMethod authMethod, String userId, String operationId) {
        try {
            ObjectResponse<GetAuthMethodsResponse> response = nextStepClient.getAuthMethodsEnabledForUser(userId);
            List<AuthMethodDetail> enabledAuthMethods = response.getResponseObject().getAuthMethods();
            for (AuthMethodDetail authMethodDetail: enabledAuthMethods) {
                if (authMethodDetail.getAuthMethod() == authMethod) {
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
     * Returns whether Mobile Token authentication method is currently available by querying the PowerAuth backend for ACTIVE activations.
     * @param userId User ID.
     * @param operationId Operation ID.
     * @return Whether Mobile Token authentication method is available.
     */
    private boolean isMobileTokenAuthMethodAvailable(String userId, String operationId) {
        // check whether user has an ACTIVE activation
        List<GetActivationListForUserResponse.Activations> allActivations = powerAuthServiceClient.getActivationListForUser(userId);
        for (GetActivationListForUserResponse.Activations activation: allActivations) {
            if (activation.getActivationStatus() == ActivationStatus.ACTIVE) {
                // user has an active activation - method can be used
                // TODO - filter applications based on activationId, ACTIVE activation may come from another application, see #122
                return true;
            }
        }
        return false;
    }
}
