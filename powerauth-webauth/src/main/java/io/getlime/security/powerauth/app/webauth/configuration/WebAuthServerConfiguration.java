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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the Web Auth application.
 *
 * @author Roman Strobl
 */
@Configuration
@ComponentScan(basePackages = {"io.getlime.security.powerauth"})
public class WebAuthServerConfiguration {

    /**
     * Dynamic URL for external CSS stylesheet.
     */
    @Value("${powerauth.webauth.page.custom-css.url}")
    private String customStyleSheetUrl;

    /**
     * External location for the main CSS file.
     * Note: Default value is "classpath:/resources/.
     */
    @Value("${powerauth.webauth.page.ext-css.location}")
    private String stylesheetLocation;

    /**
     * Dynamic page title.
     */
    @Value("${powerauth.webauth.page.title}")
    private String pageTitle;

    /**
     * Get custom external stylesheet URL.
     * @return External stylesheet URL.
     */
    public String getCustomStyleSheetUrl() {
        return customStyleSheetUrl;
    }

    /**
     * Get the dynamic location for the main CSS stylesheet to allow external styling of Web Auth UI.
     * @return Dynamic location for CSS stylesheet. By default, "classpath:/resources/".
     */
    public String getStylesheetLocation() {
        return stylesheetLocation;
    }

    /**
     * Get dynamic page title.
     * @return Page title.
     */
    public String getPageTitle() {
        return pageTitle;
    }
}
