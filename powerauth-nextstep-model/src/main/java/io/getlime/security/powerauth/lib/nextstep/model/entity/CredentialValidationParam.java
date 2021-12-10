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

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * Credential validation parameters.
 */
@Data
public class CredentialValidationParam {

    @NotNull
    private boolean includeWhitespaceRule;
    @NotNull
    private boolean includeUsernameRule;
    @NotNull
    private boolean includeAllowedCharacterRule;
    @Size(min = 1, max = 256)
    private String allowedChars;
    @NotNull
    private boolean includeAllowedRegexRule;
    @Size(min = 1, max = 256)
    private String allowedRegex;
    @NotNull
    private boolean includeIllegalCharacterRule;
    @Size(min = 1, max = 256)
    private String illegalChars;
    @NotNull
    private boolean includeIllegalRegexRule;
    @Size(min = 1, max = 256)
    private String illegalRegex;
    @NotNull
    private boolean includeCharacterRule;
    @NotNull
    private boolean includeSmallLetters;
    @Positive
    private Integer smallLettersMin;
    @NotNull
    private boolean includeCapitalLetters;
    @Positive
    private Integer capitalLettersMin;
    @NotNull
    private boolean includeAlphabeticalLetters;
    @Positive
    private Integer alphabeticalLettersMin;
    @NotNull
    private boolean includeDigits;
    @Positive
    private Integer digitsMin;
    @NotNull
    private boolean includeSpecialChars;
    @Positive
    private Integer specialCharsMin;

}
