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

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Credential generation parameters.
 */
@Data
public class CredentialGenerationParam {

    @Positive
    private int length;
    @NotNull
    private boolean includeSmallLetters;
    @Positive
    private Integer smallLettersCount;
    @NotNull
    private boolean includeCapitalLetters;
    @Positive
    private Integer capitalLettersCount;
    @NotNull
    private boolean includeDigits;
    @Positive
    private Integer digitsCount;
    @NotNull
    private boolean includeSpecialChars;
    @Positive
    private Integer specialCharsCount;

}
