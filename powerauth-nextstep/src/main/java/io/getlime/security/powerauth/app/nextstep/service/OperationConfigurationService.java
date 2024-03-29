/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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
import io.getlime.security.powerauth.app.nextstep.converter.OperationConfigConverter;
import io.getlime.security.powerauth.app.nextstep.repository.AuthMethodRepository;
import io.getlime.security.powerauth.app.nextstep.repository.OperationConfigRepository;
import io.getlime.security.powerauth.app.nextstep.repository.OperationMethodConfigRepository;
import io.getlime.security.powerauth.app.nextstep.repository.OperationRepository;
import io.getlime.security.powerauth.app.nextstep.repository.catalogue.RepositoryCatalogue;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.AuthMethodEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationConfigEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationMethodConfigEntity;
import io.getlime.security.powerauth.lib.nextstep.model.exception.*;
import io.getlime.security.powerauth.lib.nextstep.model.request.*;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service which handles persistence of operation configurations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class OperationConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(OperationConfigurationService.class);
    private static final String AUDIT_TYPE_CONFIGURATION = "CONFIGURATION";

    private final OperationConfigRepository operationConfigRepository;
    private final OperationMethodConfigRepository operationMethodConfigRepository;
    private final OperationRepository operationRepository;
    private final AuthMethodRepository authMethodRepository;
    private final Audit audit;

    private final OperationConfigConverter configConverter = new OperationConfigConverter();

    /**
     * Service constructor.
     * @param repositoryCatalogue Repository catalogue.
     * @param audit Audit interface.
     */
    @Autowired
    public OperationConfigurationService(RepositoryCatalogue repositoryCatalogue, Audit audit) {
        this.operationConfigRepository = repositoryCatalogue.getOperationConfigRepository();
        this.operationMethodConfigRepository = repositoryCatalogue.getOperationMethodConfigRepository();
        this.operationRepository = repositoryCatalogue.getOperationRepository();
        this.authMethodRepository = repositoryCatalogue.getAuthMethodRepository();
        this.audit = audit;
    }

    /**
     * Create an operation configuration.
     * @param request Create operation configuration request.
     * @return Create operation configuration response.
     * @throws OperationConfigAlreadyExists Thrown when operation configuration already exists.
     */
    @Transactional
    public CreateOperationConfigResponse createOperationConfig(CreateOperationConfigRequest request) throws OperationConfigAlreadyExists {
        final Optional<OperationConfigEntity> operationConfigOptional = operationConfigRepository.findById(request.getOperationName());
        if (operationConfigOptional.isPresent()) {
            throw new OperationConfigAlreadyExists("Operation configuration already exists for operation: " + request.getOperationName());
        }
        OperationConfigEntity operationConfig = new OperationConfigEntity();
        operationConfig.setOperationName(request.getOperationName());
        operationConfig.setTemplateVersion(request.getTemplateVersion());
        operationConfig.setTemplateId(request.getTemplateId());
        operationConfig.setMobileTokenEnabled(request.isMobileTokenEnabled());
        operationConfig.setMobileTokenMode(request.getMobileTokenMode());
        operationConfig.setAfsEnabled(request.isAfsEnabled());
        operationConfig.setAfsConfigId(request.getAfsConfigId());
        operationConfig.setExpirationTime(request.getExpirationTime());
        operationConfig = operationConfigRepository.save(operationConfig);
        logger.debug("Operation configuration was created, operation name: {}", operationConfig.getOperationName());
        audit.info("Operation configuration was created", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("operationConfig", operationConfig)
                .build());
        final CreateOperationConfigResponse response = new CreateOperationConfigResponse();
        response.setOperationName(operationConfig.getOperationName());
        response.setTemplateVersion(operationConfig.getTemplateVersion());
        response.setTemplateId(operationConfig.getTemplateId());
        response.setMobileTokenEnabled(operationConfig.isMobileTokenEnabled());
        response.setMobileTokenMode(operationConfig.getMobileTokenMode());
        response.setAfsEnabled(operationConfig.isAfsEnabled());
        response.setAfsConfigId(operationConfig.getAfsConfigId());
        response.setExpirationTime(operationConfig.getExpirationTime());
        return response;
    }

    /**
     * Get operation configuration.
     * @param operationName Operation name.
     * @return Operation configuration.
     * @throws OperationConfigNotFoundException Thrown when operation is not configured.
     */
    @Transactional
    public GetOperationConfigDetailResponse getOperationConfig(String operationName) throws OperationConfigNotFoundException {
        final OperationConfigEntity operationConfig = operationConfigRepository.findById(operationName).orElseThrow(() ->
                new OperationConfigNotFoundException("Operation not configured, operation name: " + operationName));
        return configConverter.fromOperationConfigEntity(operationConfig);
    }

    /**
     * Get all operation configurations.
     * @return All operation configurations.
     */
    @Transactional
    public GetOperationConfigListResponse getOperationConfigList() {
        final GetOperationConfigListResponse configsResponse = new GetOperationConfigListResponse();
        final Iterable<OperationConfigEntity> allConfigs = operationConfigRepository.findAll();
        for (OperationConfigEntity operationConfig: allConfigs) {
            final GetOperationConfigDetailResponse config = configConverter.fromOperationConfigEntity(operationConfig);
            configsResponse.getOperationConfigs().add(config);
        }
        return configsResponse;
    }

    /**
     * Delete an operation configuration.
     * @param request Delete operation configuration request.
     * @return Delete operation configuration response.
     * @throws OperationConfigNotFoundException Thrown when operation configuration is not configured.
     * @throws DeleteNotAllowedException Thrown when delete action is not allowed.
     */
    @Transactional
    public DeleteOperationConfigResponse deleteOperationConfig(DeleteOperationConfigRequest request) throws OperationConfigNotFoundException, DeleteNotAllowedException {
        final OperationConfigEntity operationConfig = operationConfigRepository.findById(request.getOperationName()).orElseThrow(() ->
            new OperationConfigNotFoundException("Operation configuration not found, operation name: " + request.getOperationName()));
        final long existingOperationCount = operationRepository.countByOperationName(request.getOperationName());
        if (existingOperationCount > 0) {
            throw new DeleteNotAllowedException("Operation configuration cannot be deleted because it is used: " + request.getOperationName());
        }
        operationConfigRepository.delete(operationConfig);
        logger.debug("Operation configuration was deleted, operation name: {}", operationConfig.getOperationName());
        audit.info("Operation configuration was deleted", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("operationName", operationConfig.getOperationName())
                .build());
        final DeleteOperationConfigResponse response = new DeleteOperationConfigResponse();
        response.setOperationName(operationConfig.getOperationName());
        return response;
    }

    /**
     * Create a configuration for operation and authentication method.
     * @param request Create configuration for operation and authentication method request.
     * @return Create configuration for operation and authentication method response.
     * @throws OperationMethodConfigAlreadyExists Thrown when configuration already exists.
     * @throws OperationConfigNotFoundException Thrown when operation configuration is not found.
     * @throws AuthMethodNotFoundException Thrown when authentication method is not found.
     */
    @Transactional
    public CreateOperationMethodConfigResponse createOperationMethodConfig(CreateOperationMethodConfigRequest request) throws OperationMethodConfigAlreadyExists, OperationConfigNotFoundException, AuthMethodNotFoundException {
        final Optional<OperationConfigEntity> operationConfigOptional = operationConfigRepository.findById(request.getOperationName());
        if (operationConfigOptional.isEmpty()) {
            throw new OperationConfigNotFoundException("Operation configuration not found, operation: " + request.getOperationName());
        }
        final Optional<AuthMethodEntity> authMethodOptional = authMethodRepository.findById(request.getAuthMethod());
        if (authMethodOptional.isEmpty()) {
            throw new AuthMethodNotFoundException("Authentication method not found: " + request.getAuthMethod());
        }
        final OperationMethodConfigEntity.OperationAuthMethodKey primaryKey = new OperationMethodConfigEntity.OperationAuthMethodKey(request.getOperationName(), request.getAuthMethod());
        final Optional<OperationMethodConfigEntity> operationMethodConfigOptional = operationMethodConfigRepository.findById(primaryKey);
        if (operationMethodConfigOptional.isPresent()) {
            throw new OperationMethodConfigAlreadyExists("Configuration already exists for operation: " + request.getOperationName() + ", authentication method: " + request.getAuthMethod());
        }
        OperationMethodConfigEntity operationMethodConfig = new OperationMethodConfigEntity();
        operationMethodConfig.setPrimaryKey(primaryKey);
        operationMethodConfig.setMaxAuthFails(request.getMaxAuthFails());
        operationMethodConfig = operationMethodConfigRepository.save(operationMethodConfig);
        logger.debug("Operation and authentication method configuration was created, operation name: {}, authentication method: {}", operationMethodConfig.getPrimaryKey().getOperationName(), operationMethodConfig.getPrimaryKey().getAuthMethod());
        audit.info("Operation and method configuration was created", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("operationMethodConfig", operationMethodConfig)
                .build());
        final CreateOperationMethodConfigResponse response = new CreateOperationMethodConfigResponse();
        response.setOperationName(operationMethodConfig.getPrimaryKey().getOperationName());
        response.setAuthMethod(operationMethodConfig.getPrimaryKey().getAuthMethod());
        response.setMaxAuthFails(operationMethodConfig.getMaxAuthFails());
        return response;
    }

    @Transactional
    public GetOperationMethodConfigDetailResponse getOperationMethodConfigDetail(GetOperationMethodConfigDetailRequest request) throws OperationMethodConfigNotFoundException {
        final OperationMethodConfigEntity.OperationAuthMethodKey primaryKey = new OperationMethodConfigEntity.OperationAuthMethodKey(request.getOperationName(), request.getAuthMethod());
        final OperationMethodConfigEntity operationMethodConfig = operationMethodConfigRepository.findById(primaryKey).orElseThrow(() ->
                new OperationMethodConfigNotFoundException("Configuration not found, operation name: " + request.getOperationName() + ", authentication method: " + request.getAuthMethod()));
        final GetOperationMethodConfigDetailResponse response = new GetOperationMethodConfigDetailResponse();
        response.setOperationName(operationMethodConfig.getPrimaryKey().getOperationName());
        response.setAuthMethod(operationMethodConfig.getPrimaryKey().getAuthMethod());
        response.setMaxAuthFails(operationMethodConfig.getMaxAuthFails());
        return response;
    }

    /**
     * Delete a configuration for operation and authentication method.
     * @param request Delete configuration for operation and authentication method request.
     * @return Delete configuration for operation and authentication method response.
     * @throws OperationMethodConfigNotFoundException Thrown when configuration is not found.
     */
    @Transactional
    public DeleteOperationMethodConfigResponse deleteOperationMethodConfig(DeleteOperationMethodConfigRequest request) throws OperationMethodConfigNotFoundException {
        final OperationMethodConfigEntity.OperationAuthMethodKey primaryKey = new OperationMethodConfigEntity.OperationAuthMethodKey(request.getOperationName(), request.getAuthMethod());
        final OperationMethodConfigEntity operationMethodConfig = operationMethodConfigRepository.findById(primaryKey).orElseThrow(() ->
                new OperationMethodConfigNotFoundException("Configuration not found, operation name: " + request.getOperationName() + ", authentication method: " + request.getAuthMethod()));
        operationMethodConfigRepository.delete(operationMethodConfig);
        logger.debug("Operation and authentication method configuration was deleted, operation name: {}, authentication method: {}", operationMethodConfig.getPrimaryKey().getOperationName(), operationMethodConfig.getPrimaryKey().getAuthMethod());
        audit.info("Operation and authentication method configuration was deleted", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("operationName", operationMethodConfig.getPrimaryKey().getOperationName())
                .param("authMethod", operationMethodConfig.getPrimaryKey().getAuthMethod())
                .build());
        final DeleteOperationMethodConfigResponse response = new DeleteOperationMethodConfigResponse();
        response.setOperationName(operationMethodConfig.getPrimaryKey().getOperationName());
        response.setAuthMethod(operationMethodConfig.getPrimaryKey().getAuthMethod());
        return response;
    }

}
