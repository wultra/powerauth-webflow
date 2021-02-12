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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.security.powerauth.app.nextstep.repository.HashingConfigRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.HashingConfigEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.HashingConfigDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.HashingConfigStatus;
import io.getlime.security.powerauth.lib.nextstep.model.exception.HashingConfigAlreadyExistsException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.HashingConfigNotFoundException;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidRequestException;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateHashConfigRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.DeleteHashConfigRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.GetHashConfigListRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.CreateHashConfigResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.DeleteHashConfigResponse;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetHashConfigListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * This service handles persistence of hashing configurations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class HashingConfigService {

    private final HashingConfigRepository hashingConfigRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Logger logger = LoggerFactory.getLogger(HashingConfigService.class);

    @Autowired
    public HashingConfigService(HashingConfigRepository hashingConfigRepository) {
        this.hashingConfigRepository = hashingConfigRepository;
    }

    @Transactional
    public CreateHashConfigResponse createHashConfig(CreateHashConfigRequest request) throws InvalidRequestException, HashingConfigAlreadyExistsException {
        Optional<HashingConfigEntity> hashConfigOptional = hashingConfigRepository.findByName(request.getHashConfigName());
        if (hashConfigOptional.isPresent()) {
            throw new HashingConfigAlreadyExistsException("Hashing configuration already exists: " + request.getHashConfigName());
        }
        HashingConfigEntity hashConfig = new HashingConfigEntity();
        hashConfig.setName(request.getHashConfigName());
        hashConfig.setAlgorithm(request.getAlgorithm());
        hashConfig.setStatus(HashingConfigStatus.ACTIVE);
        // TODO - create converter
        if (request.getParameters() != null) {
            try {
                String parameters = objectMapper.writeValueAsString(request.getParameters());
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
        response.getParameters().putAll(request.getParameters());
        return response;
    }

    @Transactional
    public GetHashConfigListResponse getHashConfigList(GetHashConfigListRequest request) throws InvalidRequestException {
        Iterable<HashingConfigEntity> hashConfigs;
        if (request.isIncludeRemoved()) {
            hashConfigs = hashingConfigRepository.findAll();
        } else {
            hashConfigs = hashingConfigRepository.findHashingConfigByStatus(HashingConfigStatus.ACTIVE);
        }
        GetHashConfigListResponse response = new GetHashConfigListResponse();
        for (HashingConfigEntity hashConfig: hashConfigs) {
            // TODO - use converter
            HashingConfigDetail hashingConfigDetail = new HashingConfigDetail();
            hashingConfigDetail.setHashConfigName(hashConfig.getName());
            hashingConfigDetail.setAlgorithm(hashConfig.getAlgorithm());
            hashingConfigDetail.setHashConfigStatus(hashConfig.getStatus());
            hashingConfigDetail.setTimestampCreated(hashConfig.getTimestampCreated());
            try {
                Map<String, String> parameters = objectMapper.readValue(hashConfig.getParameters(), new TypeReference<Map<String, String>>() {});
                hashingConfigDetail.getParameters().putAll(parameters);
            } catch (JsonProcessingException ex) {
                throw new InvalidRequestException(ex);
            }
            response.getHashConfigs().add(hashingConfigDetail);
        }
        return response;
    }

    @Transactional
    public DeleteHashConfigResponse deleteHashConfig(DeleteHashConfigRequest request) throws HashingConfigNotFoundException {
        Optional<HashingConfigEntity> hashConfigOptional = hashingConfigRepository.findByName(request.getHashConfigName());
        if (!hashConfigOptional.isPresent()) {
            throw new HashingConfigNotFoundException("Hashing configuration not found: " + request.getHashConfigName());
        }
        HashingConfigEntity hashConfig = hashConfigOptional.get();
        hashConfig.setStatus(HashingConfigStatus.REMOVED);
        hashingConfigRepository.save(hashConfig);
        DeleteHashConfigResponse response = new DeleteHashConfigResponse();
        response.setHashConfigName(hashConfig.getName());
        return response;
    }

}
