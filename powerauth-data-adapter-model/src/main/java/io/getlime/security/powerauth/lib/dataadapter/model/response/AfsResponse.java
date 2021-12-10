/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.dataadapter.model.response;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Response for an anti-fraud system call.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AfsResponse {

    /**
     * Whether AFS response should be applied in current authentication step.
     */
    private boolean afsResponseApplied = false;

    /**
     * AFS label specifying factors to be used during this authentication step.
     */
    private String afsLabel;

    /**
     * Configuration of authentication options available for the user.
     */
    private AuthStepOptions authStepOptions = new AuthStepOptions();

    /**
     * Extra parameters sent with the response which should be persisted together with the operation.
     */
    private final Map<String, Object> extras = new LinkedHashMap<>();

    /**
     * Default constructor.
     */
    public AfsResponse() {
    }

    /**
     * Constructor with all details.
     * @param afsResponseApplied Whether AFS response should be applied in Web Flow.
     * @param afsLabel AFS label to be stored with the operation.
     * @param authStepOptions Authentication step options for current step. Use null value case afsResponseApplied = false.
     * @param extras AFS extras.
     */
    public AfsResponse(boolean afsResponseApplied, String afsLabel, AuthStepOptions authStepOptions, Map<String, Object> extras) {
        this.afsResponseApplied = afsResponseApplied;
        this.afsLabel = afsLabel;
        this.authStepOptions = authStepOptions;
        this.extras.putAll(extras);
    }

    /**
     * Get whether AFS response should be applied in current authentication step.
     * @return Whether AFS response should be applied in current authentication step.
     */
    public boolean isAfsResponseApplied() {
        return afsResponseApplied;
    }

    /**
     * Set whether AFS response should be applied in current authentication step.
     * @param afsResponseApplied Whether AFS response should be applied in current authentication step.
     */
    public void setAfsResponseApplied(boolean afsResponseApplied) {
        this.afsResponseApplied = afsResponseApplied;
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
     * Get authentication step options available for the user.
     * @return Authentication step options available for the user.
     */
    public AuthStepOptions getAuthStepOptions() {
        return authStepOptions;
    }

    /**
     * Set authentication step options available for the user.
     * @param authStepOptions Authentication step options available for the user.
     */
    public void setAuthStepOptions(AuthStepOptions authStepOptions) {
        this.authStepOptions = authStepOptions;
    }

    /**
     * Get extra parameters sent with the response which should be persisted together with the operation.
     * @return Extra parameters.
     */
    public Map<String, Object> getExtras() {
        return extras;
    }
}
