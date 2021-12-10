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

package io.getlime.security.powerauth.lib.dataadapter.model.response;

import java.util.Date;

/**
 * Response object for a system status call.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class ServiceStatusResponse {

    private String applicationName;
    private String applicationDisplayName;
    private String applicationEnvironment;
    private String version;
    private Date buildTime;
    private Date timestamp;

    /**
     * Get the application name.
     * @return Application name.
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * Set the application name.
     * @param applicationName Application name.
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * Get the application display name.
     * @return Application display name.
     */
    public String getApplicationDisplayName() {
        return applicationDisplayName;
    }

    /**
     * Set the application display name.
     * @param applicationDisplayName Application display name.
     */
    public void setApplicationDisplayName(String applicationDisplayName) {
        this.applicationDisplayName = applicationDisplayName;
    }

    /**
     * Get application environment name.
     * @return Environment name.
     */
    public String getApplicationEnvironment() {
        return applicationEnvironment;
    }

    /**
     * Set application environment name.
     * @param applicationEnvironment Environment name.
     */
    public void setApplicationEnvironment(String applicationEnvironment) {
        this.applicationEnvironment = applicationEnvironment;
    }

    /**
     * Get version.
     * @return version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set version.
     * @param version Version.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Get build time.
     * @return Build time.
     */
    public Date getBuildTime() {
        return buildTime;
    }

    /**
     * Set build time.
     * @param buildTime Build time.
     */
    public void setBuildTime(Date buildTime) {
        this.buildTime = buildTime;
    }

    /**
     * Get current timestamp.
     * @return Timestamp.
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Set current timestamp.
     * @param timestamp Timestamp.
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}