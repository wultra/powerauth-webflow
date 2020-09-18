/*
 * Copyright 2019 Wultra s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getlime.security.powerauth.app.tppengine.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class used for setting up Open API documentation.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Configuration
@OpenAPIDefinition(
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
