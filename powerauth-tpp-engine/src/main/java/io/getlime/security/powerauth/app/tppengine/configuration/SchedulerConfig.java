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
package io.getlime.security.powerauth.app.tppengine.configuration;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Scheduler configuration.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@EnableSchedulerLock(defaultLockAtLeastFor = "100ms", defaultLockAtMostFor = "1m")
@Configuration
@Slf4j
public class SchedulerConfig {

    /**
     * Defines a bean with the lock provider for <a href="https://github.com/lukas-krecan/ShedLock">ShedLock</a>.
     * @param dataSource Data source
     * @return Scheduler lock provider
     */
    @Bean
    public LockProvider lockProviderDefaultDataSource(final DataSource dataSource) {
        logger.info("Initializing scheduler lock provider.");
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .usingDbTime()
                        .withJdbcTemplate(new JdbcTemplate(dataSource))
                        .build()
        );
    }
}
