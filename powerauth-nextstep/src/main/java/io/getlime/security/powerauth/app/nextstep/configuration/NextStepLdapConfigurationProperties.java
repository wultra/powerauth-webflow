/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2025 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * LDAP configuration properties.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@ConfigurationProperties(prefix = "powerauth.nextstep.ldap")
@Getter
@Setter
public class NextStepLdapConfigurationProperties {

    private String url;

    private String base;

    private String managerDn;

    private String managerPassword;

    private boolean pooled;

    private boolean anonymousReadOnly;

    private String userSearchBase;

    private String userSearchFilter;

    private Duration connectTimeout;

    private Duration readTimeout;
}
