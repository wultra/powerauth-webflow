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

package io.getlime.security.powerauth.app.webauth.configuration;

import io.getlime.security.powerauth.lib.credentials.client.CredentialStoreClient;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the Web Auth application, see default values in application.properties.
 *
 * @author Roman Strobl
 */
@Configuration
@ComponentScan(basePackages = {"io.getlime.security.powerauth"})
public class WebAuthServerConfiguration {

    /**
     * Dynamic URL for CSS stylesheet to allow external styling of Web Auth UI.
     */
    @Value("${powerauth.webauth.page.stylesheet.url}")
    private String stylesheetUrl;

    /**
     * Dynamic page title
     */
    @Value("${powerauth.webauth.page.title}")
    private String pageTitle;

    /**
     * Get the dynamic URL for CSS stylesheet to allow external styling of Web Auth UI.
     * @return Dynamic URL for CSS stylesheet.
     */
    public String getStylesheetUrl() {
        return stylesheetUrl;
    }

    /**
     * Get dynamic page title.
     * @return Page title.
     */
    public String getPageTitle() {
        return pageTitle;
    }
}
