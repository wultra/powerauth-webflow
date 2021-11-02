/*
 * Copyright 2021 Wultra s.r.o.
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

import javax.validation.constraints.PositiveOrZero;

/**
 * PowerAuth authentication context.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class PAAuthenticationContext {

    private String signatureType;
    @PositiveOrZero
    private Integer remainingAttempts;
    private boolean blocked;

}