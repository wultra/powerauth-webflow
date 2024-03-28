/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2024 Wultra s.r.o.
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

package io.getlime.security.powerauth.app.tppengine.controller;

import io.getlime.security.powerauth.app.tppengine.configuration.TppEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Home controller to display welcome page.
 *
 * @author Jan Pesek, jan.pesek@wultra.com
 */
@Controller
public class HomeController {

    private final BuildProperties buildProperties;
    private final TppEngineConfiguration tppEngineConfiguration;

    public HomeController(@Autowired(required = false) final BuildProperties buildProperties,
                          final TppEngineConfiguration tppEngineConfiguration) {
        this.buildProperties = buildProperties;
        this.tppEngineConfiguration = tppEngineConfiguration;
    }

    @SuppressWarnings("SameReturnValue")
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home(Model model) {
        if (buildProperties != null) {
            model.addAttribute("version", buildProperties.getVersion());
            model.addAttribute("buildTime", buildProperties.getTime());
        }

        model.addAttribute("applicationName", tppEngineConfiguration.getApplicationName());
        model.addAttribute("applicationDisplayName", tppEngineConfiguration.getApplicationDisplayName());
        model.addAttribute("applicationEnvironment", tppEngineConfiguration.getApplicationEnvironment());

        return "index";
    }

}
