/*
 * Copyright 2017 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.converter;

import io.getlime.security.powerauth.app.nextstep.repository.model.entity.OperationConfigEntity;
import io.getlime.security.powerauth.lib.nextstep.model.response.GetOperationConfigResponse;

/**
 * Converter for operation configuration.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OperationConfigConverter {

    /**
     * Convert operation config entity to operation config response.
     * @param operationConfig Operation config entity.
     * @return Operation config response.
     */
    public GetOperationConfigResponse fromOperationConfigEntity(OperationConfigEntity operationConfig) {
        GetOperationConfigResponse response = new GetOperationConfigResponse();
        response.setOperationName(operationConfig.getOperationName());
        response.setTemplateVersion(operationConfig.getTemplateVersion());
        response.setTemplateId(operationConfig.getTemplateId());
        response.setMobileTokenMode(operationConfig.getMobileTokenMode());
        return response;
    }
}
