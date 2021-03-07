/*
 * Copyright 2019 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.nextstep.model.entity;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class representing AFS action entities.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class AfsActionDetail {

    @NotNull
    private String action;
    @Positive
    private int stepIndex;
    private String afsLabel;
    private boolean afsResponseApplied;
    @NotNull
    private final Map<String, Object> requestExtras = new LinkedHashMap<>();
    @NotNull
    private final Map<String, Object> responseExtras = new LinkedHashMap<>();

}
