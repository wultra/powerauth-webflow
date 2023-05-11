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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wultra.core.audit.base.Audit;
import com.wultra.core.audit.base.model.AuditDetail;
import io.getlime.security.powerauth.app.nextstep.converter.HashConfigConverter;
import io.getlime.security.powerauth.app.nextstep.converter.ParameterConverter;
import io.getlime.security.powerauth.app.nextstep.repository.HashConfigRepository;
import io.getlime.security.powerauth.app.nextstep.repository.catalogue.RepositoryCatalogue;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.HashConfigEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.HashConfigDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.HashConfigStatus;
import io.getlime.security.powerauth.lib.nextstep.model.exception.HashConfigAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.HashConfigNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidConfigurationException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidRequestException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateHashConfigRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteHashConfigRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetHashConfigListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateHashConfigRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateHashConfigResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteHashConfigResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetHashConfigListResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.UpdateHashConfigResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

/**
 * This service handles persistence of hashing configurations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class HashConfigService {

    private final Logger logger = LoggerFactory.getLogger(HashConfigService.class);
    private static final String AUDIT_TYPE_CONFIGURATION = "CONFIGURATION";

    private final HashConfigRepository hashConfigRepository;
    private final Audit audit;

    private final HashConfigConverter hashConfigConverter = new HashConfigConverter();
    private final ParameterConverter parameterConverter = new ParameterConverter();

    /**
     * Hashing configuration service constructor.
     * @param repositoryCatalogue Repository catalogue.
     * @param audit Audit interface.
     */
    @Autowired
    public HashConfigService(RepositoryCatalogue repositoryCatalogue, Audit audit) {
        this.hashConfigRepository = repositoryCatalogue.getHashConfigRepository();
        this.audit = audit;
    }

    /**
     * Create a hashing configuration.
     * @param request Create hashing configuration request.
     * @return Create hashing configuration response.
     * @throws InvalidRequestException Thrown when request deserialization fails.
     * @throws HashConfigAlreadyExistsException Thrown when hashing configuration already exists.
     */
    @Transactional
    public CreateHashConfigResponse createHashConfig(CreateHashConfigRequest request) throws InvalidRequestException, HashConfigAlreadyExistsException {
        final Optional<HashConfigEntity> hashConfigOptional = hashConfigRepository.findByName(request.getHashConfigName());
        if (hashConfigOptional.isPresent()) {
            throw new HashConfigAlreadyExistsException("Hashing configuration already exists: " + request.getHashConfigName());
        }
        HashConfigEntity hashConfig = new HashConfigEntity();
        hashConfig.setName(request.getHashConfigName());
        hashConfig.setAlgorithm(request.getAlgorithm());
        hashConfig.setStatus(HashConfigStatus.ACTIVE);
        if (request.getParameters() != null) {
            try {
                final String parameters = parameterConverter.fromMap(request.getParameters());
                hashConfig.setParameters(parameters);
            } catch (JsonProcessingException ex) {
                throw new InvalidRequestException(ex);
            }
        }
        hashConfig.setTimestampCreated(new Date());
        hashConfig = hashConfigRepository.save(hashConfig);
        logger.debug("Hashing configuration was created, hashing configuration ID: {}, hashing configuration name: {}", hashConfig.getHashConfigId(), hashConfig.getName());
        audit.info("Hashing configuration was created", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("hashConfig", hashConfig)
                .build());
        final CreateHashConfigResponse response = new CreateHashConfigResponse();
        response.setHashConfigName(hashConfig.getName());
        response.setAlgorithm(hashConfig.getAlgorithm());
        response.setHashConfigStatus(hashConfig.getStatus());
        response.getParameters().putAll(request.getParameters());
        return response;
    }

    /**
     * Update a hashing configuration.
     * @param request Update hashing configuration request.
     * @return Update hashing configuration response.
     * @throws HashConfigNotFoundException Thrown when hashing configuration is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Transactional
    public UpdateHashConfigResponse updateHashConfig(UpdateHashConfigRequest request) throws HashConfigNotFoundException, InvalidRequestException {
        HashConfigEntity hashConfig = hashConfigRepository.findByName(request.getHashConfigName()).orElseThrow(() ->
                new HashConfigNotFoundException("Hashing configuration not found: " + request.getHashConfigName()));
        if (hashConfig.getStatus() != HashConfigStatus.ACTIVE && request.getHashConfigStatus() != HashConfigStatus.ACTIVE) {
            throw new HashConfigNotFoundException("Hashing configuration is not ACTIVE: " + request.getHashConfigName());
        }
        hashConfig.setAlgorithm(request.getAlgorithm());
        hashConfig.setStatus(HashConfigStatus.ACTIVE);
        if (request.getParameters() != null) {
            try {
                final String parameters = parameterConverter.fromMap(request.getParameters());
                hashConfig.setParameters(parameters);
            } catch (JsonProcessingException ex) {
                throw new InvalidRequestException(ex);
            }
        }
        hashConfig.setTimestampLastUpdated(new Date());
        hashConfig = hashConfigRepository.save(hashConfig);
        logger.debug("Hashing configuration was updated, hashing configuration ID: {}, hashing configuration name: {}", hashConfig.getHashConfigId(), hashConfig.getName());
        audit.info("Hashing configuration was updated", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("hashConfig", hashConfig)
                .build());
        final UpdateHashConfigResponse response = new UpdateHashConfigResponse();
        response.setHashConfigName(hashConfig.getName());
        response.setAlgorithm(hashConfig.getAlgorithm());
        response.setHashConfigStatus(hashConfig.getStatus());
        response.getParameters().putAll(request.getParameters());
        return response;
    }

    /**
     * Get hashing configuration list.
     * @param request Get hashing configuration list request.
     * @return Get hashing configuration list response.
     * @throws InvalidConfigurationException Thrown when Next Step configuration is invalid.
     */
    @Transactional
    public GetHashConfigListResponse getHashConfigList(GetHashConfigListRequest request) throws InvalidConfigurationException {
        final Iterable<HashConfigEntity> hashConfigs;
        if (request.isIncludeRemoved()) {
            hashConfigs = hashConfigRepository.findAll();
        } else {
            hashConfigs = hashConfigRepository.findHashConfigByStatus(HashConfigStatus.ACTIVE);
        }
        final GetHashConfigListResponse response = new GetHashConfigListResponse();
        for (HashConfigEntity hashConfig : hashConfigs) {
            final HashConfigDetail hashConfigDetail = hashConfigConverter.fromEntity(hashConfig);
            response.getHashConfigs().add(hashConfigDetail);
        }
        return response;
    }

    /**
     * Delete a hashing configuration.
     * @param request Delete hashing configuration request.
     * @return Delete hashing configuration response.
     * @throws HashConfigNotFoundException Thrown when hashing configuration is not found.
     */
    @Transactional
    public DeleteHashConfigResponse deleteHashConfig(DeleteHashConfigRequest request) throws HashConfigNotFoundException {
        HashConfigEntity hashConfig = hashConfigRepository.findByName(request.getHashConfigName()).orElseThrow(() ->
            new HashConfigNotFoundException("Hashing configuration not found: " + request.getHashConfigName()));
        if (hashConfig.getStatus() == HashConfigStatus.REMOVED) {
            throw new HashConfigNotFoundException("Hashing configuration is already REMOVED: " + request.getHashConfigName());
        }
        hashConfig.setStatus(HashConfigStatus.REMOVED);
        hashConfig.setTimestampLastUpdated(new Date());
        hashConfig = hashConfigRepository.save(hashConfig);
        logger.debug("Hashing configuration was removed, hashing configuration ID: {}, hashing configuration name: {}", hashConfig.getHashConfigId(), hashConfig.getName());
        audit.info("Hashing configuration was removed", AuditDetail.builder()
                .type(AUDIT_TYPE_CONFIGURATION)
                .param("hashConfig", hashConfig)
                .build());
        final DeleteHashConfigResponse response = new DeleteHashConfigResponse();
        response.setHashConfigName(hashConfig.getName());
        response.setHashConfigStatus(HashConfigStatus.REMOVED);
        return response;
    }

}
