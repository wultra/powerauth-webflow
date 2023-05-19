/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2021 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.service.adapter;

import io.getlime.security.powerauth.app.nextstep.converter.OperationConverter;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationEntity;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.OperationContext;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.UserContact;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AccountStatus;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.SmsDeliveryResult;
import io.getlime.security.powerauth.lib.dataadapter.model.response.CreateSmsAuthorizationResponse;
import io.getlime.security.powerauth.lib.dataadapter.model.response.SendAuthorizationSmsResponse;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OtpDeliveryResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationDetailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This service handles OTP customization.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class OtpCustomizationService {

    private final DataAdapterClient dataAdapterClient;
    private final OperationConverter operationConverter;

    private final Logger logger = LoggerFactory.getLogger(OtpCustomizationService.class);

    /**
     * Customization service for OTP.
     * @param dataAdapterClient Data Adapter client.
     * @param operationConverter Operation converter.
     */
    @Autowired
    public OtpCustomizationService(DataAdapterClient dataAdapterClient, OperationConverter operationConverter) {
        this.dataAdapterClient = dataAdapterClient;
        this.operationConverter = operationConverter;
    }

    /**
     * Create and send OTP code using Data Adapter.
     * @param userId User ID.
     * @param userContacts User contacts.
     * @param operation Operation entity.
     * @param language Language as defined in ISO-639 with 2 characters.
     * @param resend Whether OTP code is being resent.
     * @return OTP delivery result.
     */
    public OtpDeliveryResult createAndSendOtp(String userId, List<UserContact> userContacts, OperationEntity operation, String language, boolean resend) {
        final OtpDeliveryResult otpDeliveryResult = new OtpDeliveryResult();
        try {
            final GetOperationDetailResponse operationDetail = operationConverter.fromEntity(operation);
            final String organizationId = operationDetail.getOrganizationId();
            final AuthMethod authMethod = operationDetail.getChosenAuthMethod();
            final OperationContext operationContext = operationConverter.toOperationContext(operation);
            final CreateSmsAuthorizationResponse response = dataAdapterClient.createAndSendAuthorizationSms(userId, organizationId, userContacts, AccountStatus.ACTIVE, authMethod, operationContext, language, resend).getResponseObject();
            otpDeliveryResult.setOtpId(response.getMessageId());
            otpDeliveryResult.setDelivered(response.getSmsDeliveryResult() == SmsDeliveryResult.SUCCEEDED);
            otpDeliveryResult.setErrorMessage(response.getErrorMessage());
        } catch (DataAdapterClientErrorException ex) {
            logger.warn(ex.getMessage(), ex);
            otpDeliveryResult.setDelivered(false);
            // Default error message is used
        }
        return otpDeliveryResult;
    }

    /**
     * Send OTP code using Data Adapter.
     * @param userId User ID.
     * @param userContacts User contacts.
     * @param operation Operation entity.
     * @param otpId OTP ID.
     * @param otpValue OTP value.
     * @param language Language as defined in ISO-639 with 2 characters.
     * @param resend Whether OTP code is being resent.
     * @return OTP delivery result.
     */
    public OtpDeliveryResult sendOtp(String userId, List<UserContact> userContacts, OperationEntity operation, String otpId, String otpValue, String language, boolean resend) {
        final OtpDeliveryResult otpDeliveryResult = new OtpDeliveryResult();
        otpDeliveryResult.setOtpId(otpId);
        try {
            final GetOperationDetailResponse operationDetail = operationConverter.fromEntity(operation);
            final String organizationId = operationDetail.getOrganizationId();
            final AuthMethod authMethod = operationDetail.getChosenAuthMethod();
            final OperationContext operationContext = operationConverter.toOperationContext(operation);
            final SendAuthorizationSmsResponse response = dataAdapterClient.sendAuthorizationSms(userId, organizationId, userContacts, AccountStatus.ACTIVE, authMethod, operationContext, otpId, otpValue, language, resend).getResponseObject();
            otpDeliveryResult.setOtpId(response.getMessageId());
            otpDeliveryResult.setDelivered(response.getSmsDeliveryResult() == SmsDeliveryResult.SUCCEEDED);
            otpDeliveryResult.setErrorMessage(response.getErrorMessage());
        } catch (DataAdapterClientErrorException ex) {
            logger.warn(ex.getMessage(), ex);
            otpDeliveryResult.setDelivered(false);
            // Default error message is used
        }
        return otpDeliveryResult;
    }

}
