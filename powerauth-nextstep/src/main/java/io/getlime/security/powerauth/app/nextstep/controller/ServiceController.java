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

package io.getlime.security.powerauth.app.nextstep.controller;

import com.wultra.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.app.nextstep.configuration.NextStepServerConfiguration;
import io.getlime.security.powerauth.lib.nextstep.model.response.ServiceStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * Class representing controller used for service and maintenance purpose.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@RestController
@RequestMapping("api/service")
@Validated
public class ServiceController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    private final NextStepServerConfiguration nextStepServerConfiguration;
    private BuildProperties buildProperties;

    /**
     * REST controller constructor.
     * @param nextStepServerConfiguration Next step server configuration.
     */
    @Autowired
    public ServiceController(NextStepServerConfiguration nextStepServerConfiguration) {
        this.nextStepServerConfiguration = nextStepServerConfiguration;
    }

    /**
     * Set build information.
     * @param buildProperties Build properties.
     */
    @Autowired(required = false)
    public void setBuildProperties(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    /**
     * Controller resource with system information.
     * @return System status info.
     */
    @SecurityRequirements
    @Operation(summary = "Get service status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service status sent in response"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping("status")
    public ObjectResponse<ServiceStatusResponse> getServiceStatus() {
        logger.debug("Received getServiceStatus request");
        final ServiceStatusResponse response = new ServiceStatusResponse();
        response.setApplicationName(nextStepServerConfiguration.getApplicationName());
        response.setApplicationDisplayName(nextStepServerConfiguration.getApplicationDisplayName());
        response.setApplicationEnvironment(nextStepServerConfiguration.getApplicationEnvironment());
        if (buildProperties != null) {
            response.setVersion(buildProperties.getVersion());
            response.setBuildTime(Date.from(buildProperties.getTime()));
        }
        response.setTimestamp(new Date());
        logger.debug("The getServiceStatus request succeeded");
        return new ObjectResponse<>(response);
    }
}