/*
 * Copyright 2012 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.getlime.security.powerauth.app.nextstep.converter.HashingConfigConverter;
import io.getlime.security.powerauth.app.nextstep.converter.ParameterConverter;
import io.getlime.security.powerauth.app.nextstep.repository.HashingConfigRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.HashConfigEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.HashingConfigDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.HashConfigStatus;
import io.getlime.security.powerauth.lib.nextstep.model.exception.HashingConfigAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.HashingConfigNotFoundException;
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

import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;

/**
 * This service handles persistence of hashing configurations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class HashingConfigService {

    private final Logger logger = LoggerFactory.getLogger(HashingConfigService.class);

    private final HashingConfigRepository hashingConfigRepository;
    private final HashingConfigConverter hashingConfigConverter = new HashingConfigConverter();

    private final ParameterConverter parameterConverter = new ParameterConverter();

    /**
     * Hashing configuration service constructor.
     * @param hashingConfigRepository Hashing configuration repository.
     */
    @Autowired
    public HashingConfigService(HashingConfigRepository hashingConfigRepository) {
        this.hashingConfigRepository = hashingConfigRepository;
    }

    /**
     * Create a hashing configuration.
     * @param request Create hashing configuration request.
     * @return Create hashing configuration response.
     * @throws InvalidRequestException Thrown when request deserialization fails.
     * @throws HashingConfigAlreadyExistsException Thrown when hashing configuration already exists.
     */
    @Transactional
    public CreateHashConfigResponse createHashConfig(CreateHashConfigRequest request) throws InvalidRequestException, HashingConfigAlreadyExistsException {
        Optional<HashConfigEntity> hashConfigOptional = hashingConfigRepository.findByName(request.getHashConfigName());
        if (hashConfigOptional.isPresent()) {
            throw new HashingConfigAlreadyExistsException("Hashing configuration already exists: " + request.getHashConfigName());
        }
        HashConfigEntity hashConfig = new HashConfigEntity();
        hashConfig.setName(request.getHashConfigName());
        hashConfig.setAlgorithm(request.getAlgorithm());
        hashConfig.setStatus(HashConfigStatus.ACTIVE);
        if (request.getParameters() != null) {
            try {
                String parameters = parameterConverter.fromMap(request.getParameters());
                hashConfig.setParameters(parameters);
            } catch (JsonProcessingException ex) {
                throw new InvalidRequestException(ex);
            }
        }
        hashConfig.setTimestampCreated(new Date());
        hashingConfigRepository.save(hashConfig);
        CreateHashConfigResponse response = new CreateHashConfigResponse();
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
     * @throws HashingConfigNotFoundException Thrown when hashing configuration is not found.
     * @throws InvalidRequestException Thrown when request is invalid.
     */
    @Transactional
    public UpdateHashConfigResponse updateHashConfig(UpdateHashConfigRequest request) throws HashingConfigNotFoundException, InvalidRequestException {
        Optional<HashConfigEntity> hashConfigOptional = hashingConfigRepository.findByName(request.getHashConfigName());
        if (!hashConfigOptional.isPresent()) {
            throw new HashingConfigNotFoundException("Hashing configuration not found: " + request.getHashConfigName());
        }
        HashConfigEntity hashConfig = hashConfigOptional.get();
        if (hashConfig.getStatus() != HashConfigStatus.ACTIVE && request.getHashConfigStatus() != HashConfigStatus.ACTIVE) {
            throw new HashingConfigNotFoundException("Hashing configuration is not ACTIVE: " + request.getHashConfigName());
        }
        hashConfig.setName(request.getHashConfigName());
        hashConfig.setAlgorithm(request.getAlgorithm());
        hashConfig.setStatus(HashConfigStatus.ACTIVE);
        if (request.getParameters() != null) {
            try {
                String parameters = parameterConverter.fromMap(request.getParameters());
                hashConfig.setParameters(parameters);
            } catch (JsonProcessingException ex) {
                throw new InvalidRequestException(ex);
            }
        }
        hashConfig.setTimestampCreated(new Date());
        hashingConfigRepository.save(hashConfig);
        UpdateHashConfigResponse response = new UpdateHashConfigResponse();
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
     * @throws InvalidRequestException Thrown when request deserialization fails.
     */
    @Transactional
    public GetHashConfigListResponse getHashConfigList(GetHashConfigListRequest request) throws InvalidRequestException {
        Iterable<HashConfigEntity> hashConfigs;
        if (request.isIncludeRemoved()) {
            hashConfigs = hashingConfigRepository.findAll();
        } else {
            hashConfigs = hashingConfigRepository.findHashingConfigByStatus(HashConfigStatus.ACTIVE);
        }
        GetHashConfigListResponse response = new GetHashConfigListResponse();
        for (HashConfigEntity hashConfig : hashConfigs) {
            HashingConfigDetail hashingConfigDetail = hashingConfigConverter.fromEntity(hashConfig);
            response.getHashConfigs().add(hashingConfigDetail);
        }
        return response;
    }

    /**
     * Delete a hashing configuration.
     * @param request Delete hashing configuration request.
     * @return Delete hashing configuration response.
     * @throws HashingConfigNotFoundException Thrown when hashing configuration is not found.
     */
    @Transactional
    public DeleteHashConfigResponse deleteHashConfig(DeleteHashConfigRequest request) throws HashingConfigNotFoundException {
        Optional<HashConfigEntity> hashConfigOptional = hashingConfigRepository.findByName(request.getHashConfigName());
        if (!hashConfigOptional.isPresent()) {
            throw new HashingConfigNotFoundException("Hashing configuration not found: " + request.getHashConfigName());
        }
        HashConfigEntity hashConfig = hashConfigOptional.get();
        if (hashConfig.getStatus() != HashConfigStatus.REMOVED) {
            hashConfig.setStatus(HashConfigStatus.REMOVED);
            hashingConfigRepository.save(hashConfig);
        }
        DeleteHashConfigResponse response = new DeleteHashConfigResponse();
        response.setHashConfigName(hashConfig.getName());
        response.setHashConfigStatus(HashConfigStatus.REMOVED);
        return response;
    }

}
