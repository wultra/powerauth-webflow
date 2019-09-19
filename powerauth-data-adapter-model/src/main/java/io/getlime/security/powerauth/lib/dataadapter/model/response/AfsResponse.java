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
package io.getlime.security.powerauth.lib.dataadapter.model.response;

import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.AuthInstrument;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Response for an anti-fraud system call.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AfsResponse {

    /**
     * AFS label specifying factors to be used during this authentication step.
     */
    private String afsLabel;

    /**
     * Authentication instruments to be used during this authentication step.
     */
    private final List<AuthInstrument> authInstruments = new ArrayList<>();

    /**
     * Extra parameters sent with the response which should be persisted together with the operation.
     */
    private final Map<String, String> extras = new LinkedHashMap<>();

    /**
     * Default constructor.
     */
    public AfsResponse() {
    }

    /**
     * Constructor with all details.
     */
    public AfsResponse(String afsLabel, List<AuthInstrument> authInstruments, Map<String, String> extras) {
        this.afsLabel = afsLabel;
        this.authInstruments.addAll(authInstruments);
        this.extras.putAll(extras);
    }

    /**
     * Get AFS label specifying factors to be used during this authentication step.
     * @return AFS label.
     */
    public String getAfsLabel() {
        return afsLabel;
    }

    /**
     * Set AFS label specifying factors to be used during this authentication step.
     * @param afsLabel AFS label.
     */
    public void setAfsLabel(String afsLabel) {
        this.afsLabel = afsLabel;
    }

    /**
     * Get authentication instruments to be used during this authentication step.
     * @return Authentication instruments.
     */
    public List<AuthInstrument> getAuthInstruments() {
        return authInstruments;
    }

    /**
     * Get extra parameters sent with the response which should be persisted together with the operation.
     * @return Extra parameters.
     */
    public Map<String, String> getExtras() {
        return extras;
    }
}
