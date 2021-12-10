/*
 * Copyright 2016 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.webflow.configuration;

import io.getlime.security.powerauth.rest.api.spring.application.PowerAuthApplicationConfiguration;
import io.getlime.security.powerauth.rest.api.spring.model.ActivationContext;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Default implementation of PowerAuthApplicationConfiguration interface.
 *
 * @author Petr Dvorak
 */
@Configuration
public class ApplicationConfiguration implements PowerAuthApplicationConfiguration {

    @Override
    public Map<String, Object> statusServiceCustomObject(ActivationContext activationContext) {
        return null;
    }

}
