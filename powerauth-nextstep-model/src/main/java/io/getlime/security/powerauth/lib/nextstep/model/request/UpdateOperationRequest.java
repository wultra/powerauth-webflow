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
package io.getlime.security.powerauth.lib.nextstep.model.request;

import io.getlime.security.powerauth.lib.nextstep.model.entity.ApplicationContext;
import io.getlime.security.powerauth.lib.nextstep.model.entity.KeyValueParameter;
import io.getlime.security.powerauth.lib.nextstep.model.entity.PAAuthenticationContext;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthInstrument;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthStepResult;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Request object used for updating an operation.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Data
public class UpdateOperationRequest {

    @NotBlank
    @Size(min = 1, max = 256)
    private String operationId;
    @Size(min = 1, max = 256)
    private String userId;
    @Size(min = 2, max = 256)
    private String organizationId;
    private AuthMethod authMethod;
    private final List<AuthInstrument> authInstruments = new ArrayList<>();
    private AuthStepResult authStepResult;
    private AuthMethod targetAuthMethod;
    @Size(min = 2, max = 256)
    private String authStepResultDescription;
    private final List<KeyValueParameter> params = new ArrayList<>();
    private ApplicationContext applicationContext;
    @Size(min = 36, max = 36)
    private String authenticationId;
    private PAAuthenticationContext authenticationContext;

}
