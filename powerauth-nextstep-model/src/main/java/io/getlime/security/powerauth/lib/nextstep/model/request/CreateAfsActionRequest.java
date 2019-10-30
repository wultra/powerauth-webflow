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
package io.getlime.security.powerauth.lib.nextstep.model.request;

import java.util.Date;

/**
 * Request object used for creating an anti-fraud system action in Next Step.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class CreateAfsActionRequest {

    private String operationId;
    private String afsAction;
    private int stepIndex;
    private String requestAfsExtras;
    private boolean afsResponseApplied;
    private String afsLabel;
    private String responseAfsExtras;
    private Date timestampCreated;

    /**
     * Default constructor.
     */
    public CreateAfsActionRequest() {
    }

    /**
     * Constructor with all AFS action details.
     * @param operationId Operation ID.
     * @param afsAction Action in AFS request.
     * @param stepIndex Step index for current action.
     * @param requestAfsExtras AFS request extras.
     * @param afsResponseApplied Whether AFS response is applied.
     * @param afsLabel AFS label from response.
     * @param responseAfsExtras AFS response extras.
     */
    public CreateAfsActionRequest(String operationId, String afsAction, int stepIndex, String requestAfsExtras, boolean afsResponseApplied, String afsLabel, String responseAfsExtras) {
        this.operationId = operationId;
        this.afsAction = afsAction;
        this.stepIndex = stepIndex;
        this.requestAfsExtras = requestAfsExtras;
        this.afsResponseApplied = afsResponseApplied;
        this.afsLabel = afsLabel;
        this.responseAfsExtras = responseAfsExtras;
        this.timestampCreated = new Date();
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getAfsAction() {
        return afsAction;
    }

    public void setAfsAction(String afsAction) {
        this.afsAction = afsAction;
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public void setStepIndex(int stepIndex) {
        this.stepIndex = stepIndex;
    }

    public String getRequestAfsExtras() {
        return requestAfsExtras;
    }

    public void setRequestAfsExtras(String requestAfsExtras) {
        this.requestAfsExtras = requestAfsExtras;
    }

    public boolean isAfsResponseApplied() {
        return afsResponseApplied;
    }

    public void setAfsResponseApplied(boolean afsResponseApplied) {
        this.afsResponseApplied = afsResponseApplied;
    }

    public String getAfsLabel() {
        return afsLabel;
    }

    public void setAfsLabel(String afsLabel) {
        this.afsLabel = afsLabel;
    }

    public String getResponseAfsExtras() {
        return responseAfsExtras;
    }

    public void setResponseAfsExtras(String responseAfsExtras) {
        this.responseAfsExtras = responseAfsExtras;
    }

    public Date getTimestampCreated() {
        return timestampCreated;
    }

    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }
}
