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
package io.getlime.security.powerauth.lib.mtoken.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request to register for sending push messages.
 * @author Petr Dvorak, petr@wultra.com
 */
@Getter
@Setter
public class PushRegisterRequest {

    /**
     * Platform
     */
    @NotNull
    @Schema(description = "Platform.")
    private Platform platform;

    /**
     *
     */
    @NotBlank
    @Schema(description = "Push registration token.")
    private String token;

    public enum Platform {
        @JsonProperty("ios")
        IOS,

        @JsonProperty("android")
        ANDROID
    }

}
