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
package io.getlime.security.powerauth.lib.nextstep.model.entity;

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.CredentialStatus;
import lombok.Data;
import java.util.Date;
import java.util.Optional;

/**
 * Class represents details of a credential in external system.
 *
 * @author Zdenek Cerny, zdenek.cerny@wultra.com
 */
@Data
public class ExternalCredentialDetail {

    private CredentialStatus credentialStatus;
    private Integer failedAttempts;

    public Optional<CredentialStatus> getCredentialStatusOpt() {
        return Optional.ofNullable(credentialStatus);
    }

    public Optional<Integer> getFailedAttemptsOpt() {
        return Optional.ofNullable(failedAttempts);
    }
}
