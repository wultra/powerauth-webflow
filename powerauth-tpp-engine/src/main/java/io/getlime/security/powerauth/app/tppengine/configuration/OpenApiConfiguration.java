/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class used for setting up Open API documentation.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Configuration
@OpenAPIDefinition(
        servers = @Server(url="/powerauth-tpp-engine"),
        info = @Info(
                title = "PowerAuth TPP Engine RESTful API Documentation",
                version = "1.0",
                license = @License(
                        name = "APL 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                ),
                description = "Documentation for the PowerAuth TPP Engine RESTful API published by the PowerAuth TPP Engine Server.",
                contact = @Contact(
                        name = "Wultra s.r.o.",
                        url = "https://www.wultra.com"
                )
        )
)
public class OpenApiConfiguration {

    @Bean
    public GroupedOpenApi pushApiGroup() {
        String[] packages = {"io.getlime.security.powerauth.app.tppengine"};

        return GroupedOpenApi.builder()
                .group("tppengine")
                .packagesToScan(packages)
                .build();
    }

}
