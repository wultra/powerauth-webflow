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
package io.getlime.security.powerauth.app.nextstep.service.catalogue;

import io.getlime.security.powerauth.app.nextstep.service.*;
import io.getlime.security.powerauth.app.nextstep.service.adapter.AuthenticationCustomizationService;
import io.getlime.security.powerauth.app.nextstep.service.adapter.OperationCustomizationService;
import io.getlime.security.powerauth.app.nextstep.service.adapter.OtpCustomizationService;
import io.getlime.security.powerauth.app.nextstep.service.adapter.UserLookupCustomizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Service catalogue with all services for easier autowiring.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class ServiceCatalogue {

    private ApplicationService applicationService;
    private AuthenticationService authenticationService;
    private AuthMethodChangeService authMethodChangeService;
    private AuthMethodService authMethodService;
    private CredentialCounterService credentialCounterService;
    private CredentialDefinitionService credentialDefinitionService;
    private CredentialGenerationService credentialGenerationService;
    private CredentialHistoryService credentialHistoryService;
    private CredentialPolicyService credentialPolicyService;
    private CredentialProtectionService credentialProtectionService;
    private CredentialService credentialService;
    private CredentialValidationService credentialValidationService;
    private EndToEndEncryptionService endToEndEncryptionService;
    private HashConfigService hashConfigService;
    private IdGeneratorService idGeneratorService;
    private MobileTokenConfigurationService mobileTokenConfigurationService;
    private OperationConfigurationService operationConfigurationService;
    private OperationPersistenceService operationPersistenceService;
    private OrganizationService organizationService;
    private OtpDefinitionService otpDefinitionService;
    private OtpGenerationService otpGenerationService;
    private OtpPolicyService otpPolicyService;
    private OtpService otpService;
    private PowerAuthOperationService powerAuthOperationService;
    private RoleService roleService;
    private StepDefinitionService stepDefinitionService;
    private StepResolutionService stepResolutionService;
    private UserAliasService userAliasService;
    private UserContactService userContactService;
    private UserIdentityLookupService userIdentityLookupService;
    private UserIdentityService userIdentityService;
    private UserRoleService userRoleService;

    private AuthenticationCustomizationService authenticationCustomizationService;
    private OperationCustomizationService operationCustomizationService;
    private OtpCustomizationService otpCustomizationService;
    private UserLookupCustomizationService userLookupCustomizationService;

    /**
     * Set application service.
     * @param applicationService Application service.
     */
    @Autowired
    public void setApplicationService(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * Set authentication service.
     * @param authenticationService Authentication service.
     */
    @Autowired
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Set authentication method change service.
     * @param authMethodChangeService Authentication method change service.
     */
    @Autowired
    public void setAuthMethodChangeService(AuthMethodChangeService authMethodChangeService) {
        this.authMethodChangeService = authMethodChangeService;
    }

    /**
     * Set authentication method service.
     * @param authMethodService Authentication method service.
     */
    @Autowired
    public void setAuthMethodService(AuthMethodService authMethodService) {
        this.authMethodService = authMethodService;
    }

    /**
     * Set credential counter service.
     * @param credentialCounterService Credential counter service.
     */
    @Autowired
    public void setCredentialCounterService(CredentialCounterService credentialCounterService) {
        this.credentialCounterService = credentialCounterService;
    }

    /**
     * Set credential definition service.
     * @param credentialDefinitionService Credential definition service.
     */
    @Autowired
    public void setCredentialDefinitionService(CredentialDefinitionService credentialDefinitionService) {
        this.credentialDefinitionService = credentialDefinitionService;
    }

    /**
     * Set credential generation service.
     * @param credentialGenerationService Credential generation service.
     */
    @Autowired
    public void setCredentialGenerationService(CredentialGenerationService credentialGenerationService) {
        this.credentialGenerationService = credentialGenerationService;
    }

    /**
     * Set credential history service.
     * @param credentialHistoryService Credential history service.
     */
    @Autowired
    public void setCredentialHistoryService(CredentialHistoryService credentialHistoryService) {
        this.credentialHistoryService = credentialHistoryService;
    }

    /**
     * Set credential policy service.
     * @param credentialPolicyService Credential policy service.
     */
    @Autowired
    public void setCredentialPolicyService(CredentialPolicyService credentialPolicyService) {
        this.credentialPolicyService = credentialPolicyService;
    }

    /**
     * Set credential protection service.
     * @param credentialProtectionService Credential protection service.
     */
    @Autowired
    public void setCredentialProtectionService(CredentialProtectionService credentialProtectionService) {
        this.credentialProtectionService = credentialProtectionService;
    }

    /**
     * Set credential service.
     * @param credentialService Credential service.
     */
    @Autowired
    public void setCredentialService(CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    /**
     * Set credential validation service.
     * @param credentialValidationService Credential validation service.
     */
    @Autowired
    public void setCredentialValidationService(CredentialValidationService credentialValidationService) {
        this.credentialValidationService = credentialValidationService;
    }

    /**
     * Set end-to-end encryption service.
     * @param endToEndEncryptionService End-to-end encryption service.
     */
    @Autowired
    public void setEndToEndEncryptionService(EndToEndEncryptionService endToEndEncryptionService) {
        this.endToEndEncryptionService = endToEndEncryptionService;
    }

    /**
     * Set hashing configuration service.
     * @param hashConfigService Hashing configuration service.
     */
    @Autowired
    public void setHashConfigService(HashConfigService hashConfigService) {
        this.hashConfigService = hashConfigService;
    }

    /**
     * Set ID generator service.
     * @param idGeneratorService ID generator service.
     */
    @Autowired
    public void setIdGeneratorService(IdGeneratorService idGeneratorService) {
        this.idGeneratorService = idGeneratorService;
    }

    /**
     * Set mobile token configuration service.
     * @param mobileTokenConfigurationService Mobile token configuration service.
     */
    @Autowired
    public void setMobileTokenConfigurationService(MobileTokenConfigurationService mobileTokenConfigurationService) {
        this.mobileTokenConfigurationService = mobileTokenConfigurationService;
    }

    /**
     * Set operation configuration service.
     * @param operationConfigurationService Operation configuration service.
     */
    @Autowired
    public void setOperationConfigurationService(OperationConfigurationService operationConfigurationService) {
        this.operationConfigurationService = operationConfigurationService;
    }

    /**
     * Set operation persistence service.
     * @param operationPersistenceService Operation persistence service.
     */
    @Autowired
    public void setOperationPersistenceService(OperationPersistenceService operationPersistenceService) {
        this.operationPersistenceService = operationPersistenceService;
    }

    /**
     * Set organization service.
     * @param organizationService Organization service.
     */
    @Autowired
    public void setOrganizationService(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    /**
     * Set OTP definition service.
     * @param otpDefinitionService OTP definition service.
     */
    @Autowired
    public void setOtpDefinitionService(OtpDefinitionService otpDefinitionService) {
        this.otpDefinitionService = otpDefinitionService;
    }

    /**
     * Set OTP generation service.
     * @param otpGenerationService OTP generation service.
     */
    @Autowired
    public void setOtpGenerationService(OtpGenerationService otpGenerationService) {
        this.otpGenerationService = otpGenerationService;
    }

    /**
     * Set OTP policy service.
     * @param otpPolicyService OTP policy service.
     */
    @Autowired
    public void setOtpPolicyService(OtpPolicyService otpPolicyService) {
        this.otpPolicyService = otpPolicyService;
    }

    /**
     * Set OTP service.
     * @param otpService OTP service.
     */
    @Autowired
    public void setOtpService(OtpService otpService) {
        this.otpService = otpService;
    }

    /**
     * Set PowerAuth operation service.
     * @param powerAuthOperationService PowerAuth operation service.
     */
    @Autowired
    public void setPowerAuthOperationService(PowerAuthOperationService powerAuthOperationService) {
        this.powerAuthOperationService = powerAuthOperationService;
    }

    /**
     * Set role service.
     * @param roleService Role service.
     */
    @Autowired
    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Set step definition service.
     * @param stepDefinitionService Step definition service.
     */
    @Autowired
    public void setStepDefinitionService(StepDefinitionService stepDefinitionService) {
        this.stepDefinitionService = stepDefinitionService;
    }

    /**
     * Set step resolution service.
     * @param stepResolutionService Step resolution service.
     */
    @Autowired
    public void setStepResolutionService(StepResolutionService stepResolutionService) {
        this.stepResolutionService = stepResolutionService;
    }

    /**
     * Set user alias service.
     * @param userAliasService User alias service.
     */
    @Autowired
    public void setUserAliasService(UserAliasService userAliasService) {
        this.userAliasService = userAliasService;
    }

    /**
     * Set user contact service.
     * @param userContactService User contact service.
     */
    @Autowired
    public void setUserContactService(UserContactService userContactService) {
        this.userContactService = userContactService;
    }

    /**
     * Set user identity lookup service.
     * @param userIdentityLookupService User identity lookup service.
     */
    @Autowired
    public void setUserIdentityLookupService(UserIdentityLookupService userIdentityLookupService) {
        this.userIdentityLookupService = userIdentityLookupService;
    }

    /**
     * Set user identity service.
     * @param userIdentityService User identity service.
     */
    @Autowired
    public void setUserIdentityService(UserIdentityService userIdentityService) {
        this.userIdentityService = userIdentityService;
    }

    /**
     * Set user role service.
     * @param userRoleService User role service.
     */
    @Autowired
    public void setUserRoleService(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    /**
     * Set authentication customization service.
     * @param authenticationCustomizationService Authentication customization service.
     */
    @Autowired
    public void setAuthenticationCustomizationService(AuthenticationCustomizationService authenticationCustomizationService) {
        this.authenticationCustomizationService = authenticationCustomizationService;
    }

    /**
     * Set operation customization service.
     * @param operationCustomizationService Operation customization service.
     */
    @Autowired
    public void setOperationCustomizationService(OperationCustomizationService operationCustomizationService) {
        this.operationCustomizationService = operationCustomizationService;
    }

    /**
     * Set OTP customization service.
     * @param otpCustomizationService OTP customization service.
     */
    @Autowired
    public void setOtpCustomizationService(OtpCustomizationService otpCustomizationService) {
        this.otpCustomizationService = otpCustomizationService;
    }

    /**
     * Set user lookup customization service.
     * @param userLookupCustomizationService User lookup customization service.
     */
    @Autowired
    public void setUserLookupCustomizationService(UserLookupCustomizationService userLookupCustomizationService) {
        this.userLookupCustomizationService = userLookupCustomizationService;
    }

    /**
     * Get application service.
     * @return Application service.
     */
    public ApplicationService getApplicationService() {
        return applicationService;
    }

    /**
     * Get authentication service.
     * @return Authentication service.
     */
    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    /**
     * Get authentication method change service.
     * @return Authentication method change service.
     */
    public AuthMethodChangeService getAuthMethodChangeService() {
        return authMethodChangeService;
    }

    /**
     * Get authentication method service.
     * @return Authentication method service.
     */
    public AuthMethodService getAuthMethodService() {
        return authMethodService;
    }

    /**
     * Get credential counter service.
     * @return Credential counter service.
     */
    public CredentialCounterService getCredentialCounterService() {
        return credentialCounterService;
    }

    /**
     * Get credential definition service.
     * @return Credential definition service.
     */
    public CredentialDefinitionService getCredentialDefinitionService() {
        return credentialDefinitionService;
    }

    /**
     * Get credential generation service.
     * @return Credential generation service.
     */
    public CredentialGenerationService getCredentialGenerationService() {
        return credentialGenerationService;
    }

    /**
     * Get credential history service.
     * @return Credential history service.
     */
    public CredentialHistoryService getCredentialHistoryService() {
        return credentialHistoryService;
    }

    /**
     * Get credential policy service.
     * @return Credential policy service.
     */
    public CredentialPolicyService getCredentialPolicyService() {
        return credentialPolicyService;
    }

    /**
     * Get credential protection service.
     * @return Credential protection service.
     */
    public CredentialProtectionService getCredentialProtectionService() {
        return credentialProtectionService;
    }

    /**
     * Get credential service.
     * @return Credential service.
     */
    public CredentialService getCredentialService() {
        return credentialService;
    }

    /**
     * Get credential validation service.
     * @return Credential validation service.
     */
    public CredentialValidationService getCredentialValidationService() {
        return credentialValidationService;
    }

    /**
     * Get end-to-end encryption service.
     * @return End-to-end encryption service.
     */
    public EndToEndEncryptionService getEndToEndEncryptionService() {
        return endToEndEncryptionService;
    }

    /**
     * Get hashing configuration service.
     * @return Hashing configuration service.
     */
    public HashConfigService getHashConfigService() {
        return hashConfigService;
    }

    /**
     * Get ID generator service.
     * @return ID generator service.
     */
    public IdGeneratorService getIdGeneratorService() {
        return idGeneratorService;
    }

    /**
     * Get mobile token configuration service.
     * @return Mobile token configuration service.
     */
    public MobileTokenConfigurationService getMobileTokenConfigurationService() {
        return mobileTokenConfigurationService;
    }

    /**
     * Get operation configuration service.
     * @return Operation configuration service.
     */
    public OperationConfigurationService getOperationConfigurationService() {
        return operationConfigurationService;
    }

    /**
     * Get operation persistence service.
     * @return Operation persistence service.
     */
    public OperationPersistenceService getOperationPersistenceService() {
        return operationPersistenceService;
    }

    /**
     * Get organization service.
     * @return Organization service.
     */
    public OrganizationService getOrganizationService() {
        return organizationService;
    }

    /**
     * Get OTP definition service.
     * @return OTP definition service.
     */
    public OtpDefinitionService getOtpDefinitionService() {
        return otpDefinitionService;
    }

    /**
     * Get OTP generation service.
     * @return OTP generation service.
     */
    public OtpGenerationService getOtpGenerationService() {
        return otpGenerationService;
    }

    /**
     * Get OTP policy service.
     * @return OTP policy service.
     */
    public OtpPolicyService getOtpPolicyService() {
        return otpPolicyService;
    }

    /**
     * Get OTP service.
     * @return OTP service.
     */
    public OtpService getOtpService() {
        return otpService;
    }

    /**
     * Get PowerAuth operation service.
     * @return PowerAuth operation service.
     */
    public PowerAuthOperationService getPowerAuthOperationService() {
        return powerAuthOperationService;
    }

    /**
     * Get role service.
     * @return Role service.
     */
    public RoleService getRoleService() {
        return roleService;
    }

    /**
     * Get step definition service.
     * @return Step definition service.
     */
    public StepDefinitionService getStepDefinitionService() {
        return stepDefinitionService;
    }

    /**
     * Get step resolution service.
     * @return Step resolution service.
     */
    public StepResolutionService getStepResolutionService() {
        return stepResolutionService;
    }

    /**
     * Get user alias service.
     * @return User alias service.
     */
    public UserAliasService getUserAliasService() {
        return userAliasService;
    }

    /**
     * Get user contact service.
     * @return User contact service.
     */
    public UserContactService getUserContactService() {
        return userContactService;
    }

    /**
     * Get user identity lookup service.
     * @return User identity lookup service.
     */
    public UserIdentityLookupService getUserIdentityLookupService() {
        return userIdentityLookupService;
    }

    /**
     * Get user identity service.
     * @return User identity service.
     */
    public UserIdentityService getUserIdentityService() {
        return userIdentityService;
    }

    /**
     * Get user role service.
     * @return User role service.
     */
    public UserRoleService getUserRoleService() {
        return userRoleService;
    }

    /**
     * Get authentication customization service.
     * @return Authentication customization service.
     */
    public AuthenticationCustomizationService getAuthenticationCustomizationService() {
        return authenticationCustomizationService;
    }

    /**
     * Get operation customization service.
     * @return Operation customization service.
     */
    public OperationCustomizationService getOperationCustomizationService() {
        return operationCustomizationService;
    }

    /**
     * Get OTP customization service.
     * @return OTP customization service.
     */
    public OtpCustomizationService getOtpCustomizationService() {
        return otpCustomizationService;
    }

    /**
     * Get user lookup customization service.
     * @return User lookup customization service.
     */
    public UserLookupCustomizationService getUserLookupCustomizationService() {
        return userLookupCustomizationService;
    }

}