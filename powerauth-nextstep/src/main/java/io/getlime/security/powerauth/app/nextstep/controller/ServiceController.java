/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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

package io.getlime.security.powerauth.app.nextstep.controller;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.app.nextstep.configuration.NextStepServerConfiguration;
import io.getlime.security.powerauth.lib.nextstep.model.response.ServiceStatusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class representing controller used for service and maintenance purpose.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Controller
@RequestMapping(value = "/api/service")
public class ServiceController {

    private final NextStepServerConfiguration nextStepServerConfiguration;
    private final BuildProperties buildProperties;

    /**
     * Controller constructor.
     * @param nextStepServerConfiguration Next step server configuration.
     * @param buildProperties Build info.
     */
    @Autowired
    public ServiceController(NextStepServerConfiguration nextStepServerConfiguration, BuildProperties buildProperties) {
        this.nextStepServerConfiguration = nextStepServerConfiguration;
        this.buildProperties = buildProperties;
    }

    /**
     * Controller resource with system information.
     * @return System status info.
     */
    @RequestMapping(value = "status", method = RequestMethod.GET)
    public @ResponseBody ObjectResponse<ServiceStatusResponse> getServiceStatus() {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Received getServiceStatus request");
        ServiceStatusResponse response = new ServiceStatusResponse();
        response.setApplicationName(nextStepServerConfiguration.getApplicationName());
        response.setApplicationDisplayName(nextStepServerConfiguration.getApplicationDisplayName());
        response.setApplicationEnvironment(nextStepServerConfiguration.getApplicationEnvironment());
        if (buildProperties != null) {
            response.setVersion(buildProperties.getVersion());
            response.setBuildTime(Date.from(buildProperties.getTime()));
        }
        response.setTimestamp(new Date());
        Logger.getLogger(this.getClass().getName()).log(Level.FINE, "The getServiceStatus request succeeded");
        return new ObjectResponse<>(response);
    }
}