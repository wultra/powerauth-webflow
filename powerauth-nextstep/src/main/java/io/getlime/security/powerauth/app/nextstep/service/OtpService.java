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
package io.getlime.security.powerauth.app.nextstep.service;

import com.wultra.core.audit.base.Audit;
import com.wultra.core.audit.base.model.AuditDetail;
import io.getlime.security.powerauth.app.nextstep.converter.OtpValueConverter;
import io.getlime.security.powerauth.app.nextstep.converter.UserContactConverter;
import io.getlime.security.powerauth.app.nextstep.repository.OtpRepository;
import io.getlime.security.powerauth.app.nextstep.repository.catalogue.RepositoryCatalogue;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.*;
import io.getlime.security.powerauth.app.nextstep.service.adapter.OtpCustomizationService;
import io.getlime.security.powerauth.app.nextstep.service.catalogue.ServiceCatalogue;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.UserContact;
import io.getlime.security.powerauth.lib.nextstep.model.entity.*;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.OtpStatus;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserIdentityStatus;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OtpGenerationAlgorithm;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;

/**
 * This service handles persistence of one time passwords.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class OtpService {

    private final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final String AUDIT_TYPE_AUTHENTICATION = "AUTHENTICATION";

    private final OtpRepository otpRepository;
    private final ServiceCatalogue serviceCatalogue;
    private final OtpValueConverter otpValueConverter;
    private final UserContactConverter userContactConverter = new UserContactConverter(); //TODO: Review autowiring
    private final Audit audit;

    /**
     * OTP service constructor.
     * @param repositoryCatalogue Repository catalogue.
     * @param serviceCatalogue Service catalogue.
     * @param otpValueConverter OTP value converter.
     * @param audit Audit interface.
     */
    @Autowired
    public OtpService(RepositoryCatalogue repositoryCatalogue, @Lazy ServiceCatalogue serviceCatalogue, OtpValueConverter otpValueConverter, Audit audit) {
        this.serviceCatalogue = serviceCatalogue;
        this.otpRepository = repositoryCatalogue.getOtpRepository();
        this.otpValueConverter = otpValueConverter;
        this.audit = audit;
    }

    /**
     * Create an OTP.
     * @param request Create OTP request.
     * @return Create OTP response.
     * @throws OtpDefinitionNotFoundException Thrown when OTP definition is not found.
     * @throws UserNotActiveException Thrown when user is not active.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws CredentialNotActiveException Thrown when credential is not active.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws OtpGenAlgorithmNotSupportedException Thrown when OTP generation algorithm is not supported.
     * @throws InvalidConfigurationException Thrown when OTP policy is not configured properly.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws OperationAlreadyFinishedException Thrown when operation is already failed.
     * @throws EncryptionException Thrown when encryption fails.
     */
    @Transactional
    public CreateOtpResponse createOtp(CreateOtpRequest request) throws OtpDefinitionNotFoundException, UserNotActiveException, CredentialDefinitionNotFoundException, OperationNotFoundException, InvalidRequestException, OtpGenAlgorithmNotSupportedException, InvalidConfigurationException, OperationAlreadyFinishedException, OperationAlreadyFailedException, CredentialNotActiveException, CredentialNotFoundException, EncryptionException {
        final OtpDefinitionService otpDefinitionService = serviceCatalogue.getOtpDefinitionService();
        final OtpDefinitionEntity otpDefinition = otpDefinitionService.findActiveOtpDefinition(request.getOtpName());
        final String userId = request.getUserId();
        final String credentialName = request.getCredentialName();
        final String otpData = request.getOtpData();
        final String operationId = request.getOperationId();
        return createOtpInternal(otpDefinition, userId, credentialName, otpData, operationId);
    }

    /**
     * Create and send and OTP. Depending on configuration the OTP is created in Next Step or in Data Adapter.
     * OTP delivery is always done via Data Adapter.
     * @param request Create and send an OTP.
     * @return Create and send OTP response.
     * @throws OtpDefinitionNotFoundException Thrown when OTP definition is not found.
     * @throws UserNotActiveException Thrown when user is not active.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws CredentialNotActiveException Thrown when credential is not active.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws OtpGenAlgorithmNotSupportedException Thrown when OTP generation algorithm is not supported.
     * @throws InvalidConfigurationException Thrown when OTP policy is not configured properly.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws OperationAlreadyFinishedException Thrown when operation is already failed.
     * @throws EncryptionException Thrown when encryption fails.
     */
    @Transactional
    public CreateAndSendOtpResponse createAndSendOtp(CreateAndSendOtpRequest request) throws OtpDefinitionNotFoundException, CredentialNotFoundException, CredentialNotActiveException, InvalidRequestException, InvalidConfigurationException, OtpGenAlgorithmNotSupportedException, CredentialDefinitionNotFoundException, OperationAlreadyFinishedException, OperationAlreadyFailedException, OperationNotFoundException, UserNotActiveException, EncryptionException {
        final OtpDefinitionService otpDefinitionService = serviceCatalogue.getOtpDefinitionService();
        final OperationPersistenceService operationPersistenceService = serviceCatalogue.getOperationPersistenceService();
        final CredentialDefinitionService credentialDefinitionService = serviceCatalogue.getCredentialDefinitionService();
        final OtpCustomizationService otpCustomizationService = serviceCatalogue.getOtpCustomizationService();

        final OtpDefinitionEntity otpDefinition = otpDefinitionService.findActiveOtpDefinition(request.getOtpName());
        final String userId = request.getUserId();
        final String credentialName = request.getCredentialName();
        final String otpData = request.getOtpData();
        final String operationId = request.getOperationId();
        final String language = request.getLanguage();
        final boolean dataAdapterProxyEnabled = otpDefinition.isDataAdapterProxyEnabled();
        boolean resend = false;
        // Operation is required, otherwise Data Adapter would not have enough context to generate SMS message
        final OperationEntity operation = operationPersistenceService.getOperation(operationId);
        final Optional<OtpEntity> existingOtp = otpRepository.findFirstByOperationOrderByTimestampCreatedDesc(operation);
        if (existingOtp.isPresent()) {
            resend = true;
        }
        CredentialDefinitionEntity credentialDefinition = null;
        if (credentialName != null) {
            credentialDefinition = credentialDefinitionService.findActiveCredentialDefinition(credentialName);
        }

        final List<UserContact> contacts = getUserContacts(userId);

        if (dataAdapterProxyEnabled) {
            // Create and send OTP code via Data Adapter
            final OtpDeliveryResult result = otpCustomizationService.createAndSendOtp(userId, contacts, operation, language, resend);
            // Store a local OTP record so that OTP can be found during authentication
            final OtpEntity otp = new OtpEntity();
            otp.setOtpId(result.getOtpId());
            otp.setOtpDefinition(otpDefinition);
            otp.setCredentialDefinition(credentialDefinition);
            otp.setUserId(userId);
            otp.setOperation(operation);
            otp.setStatus(OtpStatus.EXTERNAL);
            otp.setTimestampCreated(new Date());
            otpRepository.save(otp);
            final CreateAndSendOtpResponse response = new CreateAndSendOtpResponse();
            response.setOtpName(otpDefinition.getName());
            response.setUserId(userId);
            response.setOtpId(result.getOtpId());
            // Derive status based on whether OTP code was generated and delivered
            if (result.getOtpId() != null && result.isDelivered()) {
                response.setOtpStatus(OtpStatus.ACTIVE);
            } else {
                response.setOtpStatus(OtpStatus.BLOCKED);
            }
            response.setDelivered(result.isDelivered());
            response.setErrorMessage(result.getErrorMessage());
            return response;
        } else {
            // Create OTP in Next Step and send it via Data Adapter
            final CreateOtpResponse otpResponse = createOtpInternal(otpDefinition, userId, credentialName, otpData, operationId);
            final OtpDeliveryResult result = otpCustomizationService.sendOtp(userId, contacts, operation, otpResponse.getOtpId(), otpResponse.getOtpValue(), language, resend);
            final CreateAndSendOtpResponse response = new CreateAndSendOtpResponse();
            response.setOtpName(otpDefinition.getName());
            response.setUserId(userId);
            response.setOtpId(otpResponse.getOtpId());
            response.setOtpStatus(otpResponse.getOtpStatus());
            response.setDelivered(result.isDelivered());
            response.setErrorMessage(result.getErrorMessage());
            return response;
        }
    }

    /**
     * Create an OTP.
     * @param otpDefinition OTP definition.
     * @param userId User ID.
     * @param credentialName Credential definition name.
     * @param otpData OTP data.
     * @param operationId Operation ID.
     * @return Create OTP response.
     * @throws UserNotActiveException Thrown when user is not active.
     * @throws CredentialDefinitionNotFoundException Thrown when credential definition is not found.
     * @throws CredentialNotFoundException Thrown when credential is not found.
     * @throws CredentialNotActiveException Thrown when credential is not active.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws OtpGenAlgorithmNotSupportedException Thrown when OTP generation algorithm is not supported.
     * @throws InvalidConfigurationException Thrown when OTP policy is not configured properly.
     * @throws OperationAlreadyFinishedException Thrown when operation is already finished.
     * @throws OperationAlreadyFinishedException Thrown when operation is already failed.
     * @throws EncryptionException Thrown when encryption fails.
     */
    private CreateOtpResponse createOtpInternal(OtpDefinitionEntity otpDefinition, String userId, String credentialName, String otpData, String operationId) throws UserNotActiveException, CredentialDefinitionNotFoundException, OperationNotFoundException, InvalidRequestException, OtpGenAlgorithmNotSupportedException, InvalidConfigurationException, OperationAlreadyFinishedException, OperationAlreadyFailedException, CredentialNotActiveException, CredentialNotFoundException, EncryptionException {
        final UserIdentityLookupService userIdentityLookupService = serviceCatalogue.getUserIdentityLookupService();
        final CredentialService credentialService = serviceCatalogue.getCredentialService();
        final OtpGenerationService otpGenerationService = serviceCatalogue.getOtpGenerationService();
        final IdGeneratorService idGeneratorService = serviceCatalogue.getIdGeneratorService();
        final OperationPersistenceService operationPersistenceService = serviceCatalogue.getOperationPersistenceService();
        final CredentialDefinitionService credentialDefinitionService = serviceCatalogue.getCredentialDefinitionService();

        UserIdentityEntity user = null;
        if (userId != null) {
            Optional<UserIdentityEntity> userOptional = userIdentityLookupService.findUserOptional(userId);
            if (userOptional.isPresent()) {
                user = userOptional.get();
                if(user.getStatus() != UserIdentityStatus.ACTIVE) {
                    throw new UserNotActiveException("User identity is not ACTIVE: " + user.getUserId());
                }
            }
        }
        CredentialDefinitionEntity credentialDefinition = null;
        if (user != null && credentialName != null) {
            credentialDefinition = credentialDefinitionService.findActiveCredentialDefinition(credentialName);
            // Make sure the credential exists and it is active
            credentialService.findActiveCredential(credentialDefinition, user);
        }
        OperationEntity operation = null;
        if (operationId != null) {
            operation = operationPersistenceService.getOperation(operationId);
            // Remove obsolete OTPs for this operation
            List<OtpEntity> otpsToRemove = new ArrayList<>();
            try (final Stream<OtpEntity> existingOtps = otpRepository.findAllByOperationAndStatus(operation, OtpStatus.ACTIVE)) {
                existingOtps.forEach(otp -> {
                    logger.debug("Existing OTP was removed due to new OTP: {}", otp.getOtpId());
                    audit.info("OTP was removed due to new OTP", AuditDetail.builder()
                            .type(AUDIT_TYPE_AUTHENTICATION)
                            .param("userId", userId)
                            .param("otpId", otp.getOtpId())
                            .build());
                    otp.setStatus(OtpStatus.REMOVED);
                    otpsToRemove.add(otp);
                });
            }
            otpRepository.saveAll(otpsToRemove);
            if (operation.getResult() == AuthResult.DONE) {
                throw new OperationAlreadyFinishedException("Cannot create OTP, because operation is already finished: " + operation.getOperationId());
            }
            if (operation.getResult() == AuthResult.FAILED) {
                throw new OperationAlreadyFailedException("Cannot create OTP, because operation is already failed: " + operation.getOperationId());
            }
        }
        String otpDataToUse = null;
        if (otpData != null) {
            otpDataToUse = otpData;
        } else if (operation != null) {
            otpDataToUse = operation.getOperationData();
        } else if (otpDefinition.getOtpPolicy().getGenAlgorithm() == OtpGenerationAlgorithm.OTP_DATA_DIGEST) {
            throw new InvalidRequestException("OTP data is not available for OTP definition: " + otpDefinition.getName());
        }
        final OtpValueDetail otpValueDetail = otpGenerationService.generateOtpValue(otpData, otpDefinition.getOtpPolicy());
        final String otpId = idGeneratorService.generateOtpId();
        OtpEntity otp = new OtpEntity();
        otp.setOtpId(otpId);
        otp.setOtpDefinition(otpDefinition);
        otp.setUserId(userId);
        otp.setCredentialDefinition(credentialDefinition);
        otp.setOperation(operation);
        final OtpValue otpValueDb = otpValueConverter.toDBValue(otpValueDetail.getOtpValue(), otpId, otpDefinition);
        otp.setValue(otpValueDb.getValue());
        otp.setEncryptionAlgorithm(otpValueDb.getEncryptionAlgorithm());
        otp.setSalt(otpValueDetail.getSalt());
        otp.setStatus(OtpStatus.ACTIVE);
        otp.setOtpData(otpDataToUse);
        otp.setAttemptCounter(0);
        otp.setFailedAttemptCounter(0);
        otp.setTimestampCreated(new Date());
        final Long expirationTime = otpDefinition.getOtpPolicy().getExpirationTime();
        if (expirationTime != null) {
            final Calendar cal = GregorianCalendar.getInstance();
            cal.add(Calendar.SECOND, expirationTime.intValue());
            otp.setTimestampExpires(cal.getTime());
        }
        otp = otpRepository.save(otp);
        logger.debug("OTP was created, user ID: {}, OTP ID: {}", userId, otp.getOtpId());
        audit.info("OTP was created", AuditDetail.builder()
                .type(AUDIT_TYPE_AUTHENTICATION)
                .param("userId", userId)
                .param("otpId", otp.getOtpId())
                .param("operationId", otp.getOperation() != null ? otp.getOperation().getOperationId() : null)
                .build());
        final CreateOtpResponse response = new CreateOtpResponse();
        response.setOtpName(otp.getOtpDefinition().getName());
        response.setUserId(userId);
        response.setOtpId(otp.getOtpId());
        response.setOtpValue(otpValueDetail.getOtpValue());
        response.setOtpStatus(otp.getStatus());
        return response;
    }

    /**
     * Get OTP list for an operation.
     * @param request Get OTP list request.
     * @return Get OTP list response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    @Transactional
    public GetOtpListResponse getOtpList(GetOtpListRequest request) throws OperationNotFoundException {
        final OperationPersistenceService operationPersistenceService = serviceCatalogue.getOperationPersistenceService();
        final OperationEntity operation = operationPersistenceService.getOperation(request.getOperationId());
        final GetOtpListResponse response = new GetOtpListResponse();
        try (final Stream<OtpEntity> otps = otpRepository.findAllByOperationOrderByTimestampCreatedDesc(operation)) {
            response.setOperationId(operation.getOperationId());
            otps.forEach(otp -> {
                if (!request.isIncludeRemoved() && otp.getStatus() == OtpStatus.REMOVED) {
                    return;
                }
                try {
                    final OtpDetail otpDetail = getOtpDetail(otp);
                    response.getOtpDetails().add(otpDetail);
                } catch (EncryptionException | InvalidConfigurationException ex) {
                    logger.error(ex.getMessage(), ex);
                    audit.error(ex.getMessage(), ex);
                }
            });
        }
        return response;
    }

    /**
     * Get OTP detail.
     * @param request Get OTP detail request.
     * @return Get OTP detail response.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws OtpNotFoundException Thrown when OTP is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    @Transactional
    public GetOtpDetailResponse getOtpDetail(GetOtpDetailRequest request) throws OperationNotFoundException, OtpNotFoundException, InvalidRequestException, InvalidConfigurationException, EncryptionException {
        final OtpEntity otp = findOtp(request.getOtpId(), request.getOperationId());
        final OtpDetail otpDetail = getOtpDetail(otp);
        final GetOtpDetailResponse response = new GetOtpDetailResponse();
        response.setOperationId(request.getOperationId());
        response.setOtpDetail(otpDetail);
        return response;
    }

    /**
     * Delete an OTP.
     * @param request Delete OTP request.
     * @return Delete OTP response.
     * @throws OtpNotFoundException Thrown when OTP is not found.
     * @throws OperationNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Transactional
    public DeleteOtpResponse deleteOtp(DeleteOtpRequest request) throws OtpNotFoundException, OperationNotFoundException, InvalidRequestException {
        final OtpEntity otp = findOtp(request.getOtpId(), request.getOperationId());
        if (otp.getStatus() == OtpStatus.REMOVED) {
            throw new OtpNotFoundException("OTP is already removed: " + request.getOtpId() + ", operation ID: " + request.getOperationId());
        }
        otp.setStatus(OtpStatus.REMOVED);
        otpRepository.save(otp);
        logger.debug("OTP was removed, OTP ID: {}", otp.getOtpId());
        audit.info("OTP was removed", AuditDetail.builder()
                .type(AUDIT_TYPE_AUTHENTICATION)
                .param("otpId", otp.getOtpId())
                .build());
        final DeleteOtpResponse response = new DeleteOtpResponse();
        response.setOtpId(otp.getOtpId());
        if (otp.getOperation() != null) {
            response.setOperationId(otp.getOperation().getOperationId());
        }
        response.setOtpStatus(otp.getStatus());
        return response;
    }

    /**
     * Find an OTP by an otp ID and/or operation ID. This method is not transactional.
     * @param otpId Otp ID.
     * @param operationId Operation ID.
     * @return OTP entity.
     * @throws OtpNotFoundException Thrown when operation is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     * @throws OperationNotFoundException Thrown when operation is not found.
     */
    public OtpEntity findOtp(String otpId, String operationId) throws OtpNotFoundException, InvalidRequestException, OperationNotFoundException {
        final OperationPersistenceService operationPersistenceService = serviceCatalogue.getOperationPersistenceService();
        final OtpEntity otp;
        if (otpId != null) {
            otp = otpRepository.findById(otpId).orElseThrow(() ->
                    new OtpNotFoundException("OTP not found: " + otpId));
            if (operationId != null) {
                if (otp.getOperation() == null) {
                    throw new InvalidRequestException("OTP was not created within an operation: " + otpId);
                }
                if (!operationId.equals(otp.getOperation().getOperationId())) {
                    throw new InvalidRequestException("Operation ID mismatch for an OTP: " + otpId + ", operation ID: " + operationId);
                }
            }
        } else if (operationId != null) {
            final OperationEntity operation = operationPersistenceService.getOperation(operationId);
            otp = otpRepository.findFirstByOperationOrderByTimestampCreatedDesc(operation).orElseThrow(() ->
                    new OtpNotFoundException("No OTP found for operation: " + operation.getOperationId()));
        } else {
            throw new InvalidRequestException("Missing otp ID or operation ID");
        }
        return otp;
    }

    /**
     * Get OTP detail for given OTP and operation.
     * @param otp OTP entity.
     * @return Operation detail.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     * @throws EncryptionException Thrown when decryption fails.
     */
    private OtpDetail getOtpDetail(OtpEntity otp) throws InvalidConfigurationException, EncryptionException {
        final OtpDetail otpDetail = new OtpDetail();
        otpDetail.setOtpName(otp.getOtpDefinition().getName());
        if (otp.getUserId() != null) {
            otpDetail.setUserId(otp.getUserId());
        }
        otpDetail.setOtpId(otp.getOtpId());
        if (otp.getOperation() != null) {
            otpDetail.setOperationId(otp.getOperation().getOperationId());
        }
        final Integer remainingAttempts = resolveRemainingAttempts(otp);
        otpDetail.setRemainingAttempts(remainingAttempts);
        otpDetail.setOtpData(otp.getOtpData());
        final OtpValue otpValueDb = new OtpValue(otp.getEncryptionAlgorithm(), otp.getValue());
        final String value = otpValueConverter.fromDBValue(otpValueDb, otp.getOtpId(), otp.getOtpDefinition());
        otpDetail.setOtpValue(value);
        if (otp.getCredentialDefinition() != null) {
            otpDetail.setCredentialName(otp.getCredentialDefinition().getName());
        }
        otpDetail.setAttemptCounter(otp.getAttemptCounter());
        otpDetail.setFailedAttemptCounter(otp.getFailedAttemptCounter());
        otpDetail.setOtpStatus(otp.getStatus());
        otpDetail.setTimestampCreated(otp.getTimestampCreated());
        otpDetail.setTimestampVerified(otp.getTimestampVerified());
        otpDetail.setTimestampBlocked(otp.getTimestampBlocked());
        otpDetail.setTimestampExpires(otp.getTimestampExpires());
        return otpDetail;
    }

    /**
     * Resolve remaining attempts for an OTP.
     * @param otp OTP entity.
     * @return Remaining attempts.
     */
    private Integer resolveRemainingAttempts(OtpEntity otp) {
        final StepResolutionService stepResolutionService = serviceCatalogue.getStepResolutionService();
        final OtpPolicyEntity otpPolicy = otp.getOtpDefinition().getOtpPolicy();
        Integer remainingAttempts = null;
        if (otpPolicy.getAttemptLimit() != null) {
            remainingAttempts = otpPolicy.getAttemptLimit() - otp.getFailedAttemptCounter();
        }
        if (otp.getOperation() != null) {
            final OperationEntity operationEntity = otp.getOperation();
            final Integer remainingAttemptsOperation = stepResolutionService.getNumberOfRemainingAttempts(operationEntity);
            if (remainingAttemptsOperation != null && (remainingAttempts == null || remainingAttemptsOperation < remainingAttempts)) {
                return remainingAttemptsOperation;
            } else {
                return remainingAttempts;
            }
        } else {
            return remainingAttempts;
        }
    }

    /**
     * Obtain contacts for given user ID.
     * @param userId User ID.
     * @return Contacts for given user.
     */
    private List<UserContact> getUserContacts(String userId) {
        final List<UserContact> contacts = new ArrayList<>();
        try {
            final GetUserContactListRequest userContactListRequest = new GetUserContactListRequest();
            userContactListRequest.setUserId(userId);
            final GetUserContactListResponse userContactList = serviceCatalogue.getUserContactService().getUserContactList(userContactListRequest);
            for (UserContactDetail ucd: userContactList.getContacts()) {
                final UserContact userContact = userContactConverter.toUserContact(ucd);
                if (userContact != null) {
                    contacts.add(userContact);
                }
            }
        } catch (UserNotFoundException ex) {
            // Should not happen, the method is called with user ID that exists
            logger.error("User ID was not found in method that should have granted user ID existence, user ID: {}", userId, ex);
        }
        return contacts;
    }

}