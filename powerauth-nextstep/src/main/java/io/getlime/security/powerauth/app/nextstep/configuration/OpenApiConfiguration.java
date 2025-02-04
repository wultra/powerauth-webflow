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
package io.getlime.security.powerauth.app.nextstep.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.servlet.ServletContext;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class used for setting up Open API documentation.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Configuration
@OpenAPIDefinition(
        servers = @Server(url="/powerauth-nextstep"),
        info = @Info(
                title = "PowerAuth Next Step RESTful API Documentation",
                version = "1.0",
                license = @License(
                        name = "APL 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                ),
                description = "Documentation for the PowerAuth Next Step RESTful API published by the PowerAuth Next Step Server.",
                contact = @Contact(
                        name = "Wultra s.r.o.",
                        url = "https://www.wultra.com"
                )
        )
)
public class OpenApiConfiguration {

    @Bean
    public GroupedOpenApi pushApiGroup() {
        String[] packages = {"io.getlime.security.powerauth"};

        return GroupedOpenApi.builder()
                .group("nextstep")
                .packagesToScan(packages)
                .build();
    }

    @Bean
    public OpenAPI openAPI(final ServletContext servletContext, final SecurityConfig securityConfig) {
        final io.swagger.v3.oas.models.servers.Server server = new io.swagger.v3.oas.models.servers.Server()
                .url(servletContext.getContextPath())
                .description("Default Server URL");

        final OpenAPI openAPI = new OpenAPI()
                .servers(List.of(server));

        if (SecurityConfig.AuthType.OIDC == securityConfig.getAuthType()) {
            final Components components = new Components()
                    .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT"));

            openAPI.components(components);
            openAPI.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
        }

        return openAPI;
    }

}
