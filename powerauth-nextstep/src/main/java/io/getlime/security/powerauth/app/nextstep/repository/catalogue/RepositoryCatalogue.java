/*
 * Copyright 2021 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.repository.catalogue;

import io.getlime.security.powerauth.app.nextstep.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Repository catalogue with all repositories for easier autowiring.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class RepositoryCatalogue {

    private ApplicationRepository applicationRepository;
    private AuthenticationRepository authenticationRepository;
    private AuthMethodRepository authMethodRepository;
    private CredentialDefinitionRepository credentialDefinitionRepository;
    private CredentialPolicyRepository credentialPolicyRepository;
    private CredentialRepository credentialRepository;
    private HashConfigRepository hashConfigRepository;
    private OperationAfsActionRepository operationAfsActionRepository;
    private OperationConfigRepository operationConfigRepository;
    private OperationHistoryRepository operationHistoryRepository;
    private OperationMethodConfigRepository operationMethodConfigRepository;
    private OperationRepository operationRepository;
    private OrganizationRepository organizationRepository;
    private OtpDefinitionRepository otpDefinitionRepository;
    private OtpPolicyRepository otpPolicyRepository;
    private OtpRepository otpRepository;
    private RoleRepository roleRepository;
    private StepDefinitionRepository stepDefinitionRepository;
    private UserIdentityRepository userIdentityRepository;
    private UserPrefsRepository userPrefsRepository;
    private UserRoleRepository userRoleRepository;

    /**
     * Set application repository.
     * @param applicationRepository Application repository.
     */
    @Autowired
    public void setApplicationRepository(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    /**
     * Set authentication repository.
     * @param authenticationRepository Authentication repository.
     */
    @Autowired
    public void setAuthenticationRepository(AuthenticationRepository authenticationRepository) {
        this.authenticationRepository = authenticationRepository;
    }

    /**
     * Set authentication method repository.
     * @param authMethodRepository Authentication method repository.
     */
    @Autowired
    public void setAuthMethodRepository(AuthMethodRepository authMethodRepository) {
        this.authMethodRepository = authMethodRepository;
    }

    /**
     * Set credential definition repository.
     * @param credentialDefinitionRepository Credential definition repository.
     */
    @Autowired
    public void setCredentialDefinitionRepository(CredentialDefinitionRepository credentialDefinitionRepository) {
        this.credentialDefinitionRepository = credentialDefinitionRepository;
    }

    /**
     * Set credential policy repository.
     * @param credentialPolicyRepository Credential policy repository.
     */
    @Autowired
    public void setCredentialPolicyRepository(CredentialPolicyRepository credentialPolicyRepository) {
        this.credentialPolicyRepository = credentialPolicyRepository;
    }

    /**
     * Set credential repository.
     * @param credentialRepository Credential repository.
     */
    @Autowired
    public void setCredentialRepository(CredentialRepository credentialRepository) {
        this.credentialRepository = credentialRepository;
    }

    /**
     * Set hashing configuration repository.
     * @param hashConfigRepository Hashing configuration repository.
     */
    @Autowired
    public void setHashConfigRepository(HashConfigRepository hashConfigRepository) {
        this.hashConfigRepository = hashConfigRepository;
    }

    /**
     * Set operation AFS action repository.
     * @param operationAfsActionRepository Operation AFS action repository.
     */
    @Autowired
    public void setOperationAfsActionRepository(OperationAfsActionRepository operationAfsActionRepository) {
        this.operationAfsActionRepository = operationAfsActionRepository;
    }

    /**
     * Set operation configuration repository.
     * @param operationConfigRepository Operation configuration repository.
     */
    @Autowired
    public void setOperationConfigRepository(OperationConfigRepository operationConfigRepository) {
        this.operationConfigRepository = operationConfigRepository;
    }

    /**
     * Set operation history repository.
     * @param operationHistoryRepository Operation history repository.
     */
    @Autowired
    public void setOperationHistoryRepository(OperationHistoryRepository operationHistoryRepository) {
        this.operationHistoryRepository = operationHistoryRepository;
    }

    /**
     * Set operation and authentication method configuration repository.
     * @param operationMethodConfigRepository Operation and authentication method configuration repository.
     */
    @Autowired
    public void setOperationMethodConfigRepository(OperationMethodConfigRepository operationMethodConfigRepository) {
        this.operationMethodConfigRepository = operationMethodConfigRepository;
    }

    /**
     * Set operation repository.
     * @param operationRepository Operation repository.
     */
    @Autowired
    public void setOperationRepository(OperationRepository operationRepository) {
        this.operationRepository = operationRepository;
    }

    /**
     * Set organization repository.
     * @param organizationRepository Organization repository.
     */
    @Autowired
    public void setOrganizationRepository(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    /**
     * Set OTP definition repository.
     * @param otpDefinitionRepository OTP definition repository.
     */
    @Autowired
    public void setOtpDefinitionRepository(OtpDefinitionRepository otpDefinitionRepository) {
        this.otpDefinitionRepository = otpDefinitionRepository;
    }

    /**
     * Set OTP policy repository.
     * @param otpPolicyRepository OTP policy repository.
     */
    @Autowired
    public void setOtpPolicyRepository(OtpPolicyRepository otpPolicyRepository) {
        this.otpPolicyRepository = otpPolicyRepository;
    }

    /**
     * Set OTP repository.
     * @param otpRepository OTP repository.
     */
    @Autowired
    public void setOtpRepository(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    /**
     * Set role repository.
     * @param roleRepository Role repository.
     */
    @Autowired
    public void setRoleRepository(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Set step definition repository.
     * @param stepDefinitionRepository Step definition repository.
     */
    @Autowired
    public void setStepDefinitionRepository(StepDefinitionRepository stepDefinitionRepository) {
        this.stepDefinitionRepository = stepDefinitionRepository;
    }

    /**
     * Set user identity repository.
     * @param userIdentityRepository User identity repository.
     */
    @Autowired
    public void setUserIdentityRepository(UserIdentityRepository userIdentityRepository) {
        this.userIdentityRepository = userIdentityRepository;
    }

    /**
     * Set user preferences repository.
     * @param userPrefsRepository User preferences repository.
     */
    @Autowired
    public void setUserPrefsRepository(UserPrefsRepository userPrefsRepository) {
        this.userPrefsRepository = userPrefsRepository;
    }

    /**
     * Set user role repository.
     * @param userRoleRepository User role repository.
     */
    @Autowired
    public void setUserRoleRepository(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    /**
     * Get application repository.
     * @return Application repository.
     */
    public ApplicationRepository getApplicationRepository() {
        return applicationRepository;
    }

    /**
     * Get authentication repository.
     * @return Authentication repository.
     */
    public AuthenticationRepository getAuthenticationRepository() {
        return authenticationRepository;
    }

    /**
     * Get authentication method repository.
     * @return Authentication method repository.
     */
    public AuthMethodRepository getAuthMethodRepository() {
        return authMethodRepository;
    }

    /**
     * Get credential definition repository.
     * @return Credential definition repository.
     */
    public CredentialDefinitionRepository getCredentialDefinitionRepository() {
        return credentialDefinitionRepository;
    }

    /**
     * Get credential policy repository.
     * @return Credential policy repository.
     */
    public CredentialPolicyRepository getCredentialPolicyRepository() {
        return credentialPolicyRepository;
    }

    /**
     * Get credential repository.
     * @return Credential repository.
     */
    public CredentialRepository getCredentialRepository() {
        return credentialRepository;
    }

    /**
     * Get hashing configuration repository.
     * @return Hashing configuration repository.
     */
    public HashConfigRepository getHashConfigRepository() {
        return hashConfigRepository;
    }

    /**
     * Get operation AFS action repository.
     * @return Operation AFS action repository.
     */
    public OperationAfsActionRepository getOperationAfsActionRepository() {
        return operationAfsActionRepository;
    }

    /**
     * Get operation configuration repository.
     * @return Operation configuration repository.
     */
    public OperationConfigRepository getOperationConfigRepository() {
        return operationConfigRepository;
    }

    /**
     * Get operation history repository.
     * @return Operation history repository.
     */
    public OperationHistoryRepository getOperationHistoryRepository() {
        return operationHistoryRepository;
    }

    /**
     * Get operation and authentication method repository.
     * @return Operation and authentication method repository.
     */
    public OperationMethodConfigRepository getOperationMethodConfigRepository() {
        return operationMethodConfigRepository;
    }

    /**
     * Get operation repository.
     * @return Operation repository.
     */
    public OperationRepository getOperationRepository() {
        return operationRepository;
    }

    /**
     * Get organization repository.
     * @return Organization repository.
     */
    public OrganizationRepository getOrganizationRepository() {
        return organizationRepository;
    }

    /**
     * Get OTP definition repository.
     * @return OTP definition repository.
     */
    public OtpDefinitionRepository getOtpDefinitionRepository() {
        return otpDefinitionRepository;
    }

    /**
     * Get OTP policy repository.
     * @return OTP policy repository.
     */
    public OtpPolicyRepository getOtpPolicyRepository() {
        return otpPolicyRepository;
    }

    /**
     * Get OTP repository.
     * @return OTP repository.
     */
    public OtpRepository getOtpRepository() {
        return otpRepository;
    }

    /**
     * Get role repository.
     * @return Role repository.
     */
    public RoleRepository getRoleRepository() {
        return roleRepository;
    }

    /**
     * Get step definition repository.
     * @return Step definition repository.
     */
    public StepDefinitionRepository getStepDefinitionRepository() {
        return stepDefinitionRepository;
    }

    /**
     * Get user identity repository.
     * @return User identity repository.
     */
    public UserIdentityRepository getUserIdentityRepository() {
        return userIdentityRepository;
    }

    /**
     * Get user preferences repository.
     * @return User preferences repository.
     */
    public UserPrefsRepository getUserPrefsRepository() {
        return userPrefsRepository;
    }

    /**
     * Get user role repository.
     * @return User role repository.
     */
    public UserRoleRepository getUserRoleRepository() {
        return userRoleRepository;
    }

}