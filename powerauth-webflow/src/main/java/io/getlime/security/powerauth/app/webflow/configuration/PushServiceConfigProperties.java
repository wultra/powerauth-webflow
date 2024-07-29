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
package io.getlime.security.powerauth.app.webflow.configuration;

import com.wultra.core.rest.client.base.RestClientConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Push Server.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@Configuration
@ConfigurationProperties(prefix = "powerauth.push.service")
@Getter @Setter
public class PushServiceConfigProperties {

    private String url;

    private RestClientConfiguration restClientConfig;
}
