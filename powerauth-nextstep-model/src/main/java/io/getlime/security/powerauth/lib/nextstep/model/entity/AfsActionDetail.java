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

import java.util.Map;

/**
 * Class representing AFS action entities.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AfsActionDetail {

    private String action;
    private int stepIndex;
    private String afsLabel;
    private boolean afsResponseApplied;
    private Map<String, Object> requestExtras;
    private Map<String, Object> responseExtras;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public void setStepIndex(int stepIndex) {
        this.stepIndex = stepIndex;
    }

    public String getAfsLabel() {
        return afsLabel;
    }

    public void setAfsLabel(String afsLabel) {
        this.afsLabel = afsLabel;
    }

    public boolean isAfsResponseApplied() {
        return afsResponseApplied;
    }

    public void setAfsResponseApplied(boolean afsResponseApplied) {
        this.afsResponseApplied = afsResponseApplied;
    }

    public Map<String, Object> getRequestExtras() {
        return requestExtras;
    }

    public void setRequestExtras(Map<String, Object> requestExtras) {
        this.requestExtras = requestExtras;
    }

    public Map<String, Object> getResponseExtras() {
        return responseExtras;
    }

    public void setResponseExtras(Map<String, Object> responseExtras) {
        this.responseExtras = responseExtras;
    }
}
