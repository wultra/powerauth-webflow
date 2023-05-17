/*
 * Copyright 2023 Lime - HighTech Solutions s.r.o.
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

package io.getlime.security.powerauth.lib.dataadapter.model.entity;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * Class representing user contact object.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Data
public class UserContact {

    public enum Type {
        PHONE,
        EMAIL,
        OTHER
    }

    @NotBlank
    @Size(min = 2, max = 256)
    private String contactName;
    @NotNull
    private Type contactType;
    @NotBlank
    @Size(min = 2, max = 256)
    private String contactValue;
    @NotNull
    private boolean primary;
    @NotNull
    private Date timestampCreated;
    private Date timestampLastUpdated;

}
