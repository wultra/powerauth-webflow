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

import io.getlime.security.powerauth.app.nextstep.service.DefaultLdapVerifierService;
import io.getlime.security.powerauth.app.nextstep.service.LdapVerifierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * LDAP configuration for Next Step server.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@Configuration
@ConditionalOnProperty(name = "powerauth.nextstep.ldap.enabled", havingValue = "true")
@EnableConfigurationProperties(NextStepLdapConfigurationProperties.class)
@Slf4j
public class NextStepLdapConfiguration {

    @Bean
    public LdapVerifierService ldapVerifierService(final NextStepLdapConfigurationProperties configuration, final LdapTemplate ldapTemplate) {
        logger.info("Initializing DefaultLdapVerifierService");
        return new DefaultLdapVerifierService(configuration, ldapTemplate);
    }

    @Bean
    public LdapContextSource contextSource(final NextStepLdapConfigurationProperties configuration) {
        final LdapContextSource cs = new LdapContextSource();
        cs.setUrl(configuration.getUrl());
        cs.setBase(configuration.getBase());
        if (StringUtils.hasText(configuration.getManagerDn())) {
            cs.setUserDn(configuration.getManagerDn());
        }
        if (StringUtils.hasText(configuration.getManagerPassword())) {
            cs.setPassword(configuration.getManagerPassword());
        }
        cs.setPooled(configuration.isPooled());
        cs.setAnonymousReadOnly(configuration.isAnonymousReadOnly());

        final Map<String, Object> env = Map.of(
            "com.sun.jndi.ldap.connect.timeout", String.valueOf(configuration.getConnectTimeout().toMillis()),
            "com.sun.jndi.ldap.read.timeout", String.valueOf(configuration.getReadTimeout().toMillis()));
        cs.setBaseEnvironmentProperties(env);

        cs.afterPropertiesSet();
        return cs;
    }

    @Bean
    public LdapTemplate ldapTemplate(final LdapContextSource cs) {
        return new LdapTemplate(cs);
    }
}
