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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring boot class for Web Flow server.
 *
 * @author Roman Strobl
 */
@SpringBootApplication
@EnableJpaRepositories("io.getlime.security.powerauth.lib.webflow.authentication.repository")
@ComponentScan(basePackages = {"io.getlime.security.*", "com.wultra.core.audit.base"})
@EntityScan("io.getlime.security.*")
@EnableScheduling
public class PowerAuthWebFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(PowerAuthWebFlowApplication.class, args);
    }
}
