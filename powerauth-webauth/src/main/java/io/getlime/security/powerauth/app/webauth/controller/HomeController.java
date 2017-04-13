/*
 * Copyright 2016 Lime - HighTech Solutions s.r.o.
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
package io.getlime.security.powerauth.app.webauth.controller;

import io.getlime.security.powerauth.app.webauth.configuration.WebAuthServerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * Simple controller, redirects to the main HTML page with JavaScript content
 *
 * @author Roman Strobl
 */
@Controller
public class HomeController {

    private WebAuthServerConfiguration webAuthConfig;

    @Autowired
    public HomeController(WebAuthServerConfiguration webAuthConfig) {
        this.webAuthConfig = webAuthConfig;
    }

    /**
     * Redirects to the index.html template file
     * @param model Model is used to store a link to the stylesheet which can be externalized
     * @return index page
     * @throws Exception thrown when page is not found
     */
    @RequestMapping(value = "/")
    public String index(Map<String, Object> model) throws Exception {
        model.put("stylesheet", webAuthConfig.getStylesheetUrl());
        return "index";
    }

}
