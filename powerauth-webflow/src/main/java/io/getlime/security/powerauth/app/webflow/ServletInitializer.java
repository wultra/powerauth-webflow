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
package io.getlime.security.powerauth.app.webflow;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.security.Security;

/**
 * Servlet initializer which handles application startup in a web container.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class ServletInitializer extends SpringBootServletInitializer {

    /**
     * Configure servlet initializer - set up Bouncy Castle crypto provider.
     * @param application Application.
     * @return Spring application builder.
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        // Register BC provider
        Security.addProvider(new BouncyCastleProvider());

        return application.sources(PowerAuthWebFlowApplication.class);
    }

}
