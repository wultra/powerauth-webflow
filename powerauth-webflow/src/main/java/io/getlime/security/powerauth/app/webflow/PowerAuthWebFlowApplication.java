/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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
package io.getlime.security.powerauth.app.webflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring boot class for Web Flow server.
 *
 * @author Roman Strobl
 */
@SpringBootApplication
@EnableJpaRepositories("io.getlime.security.powerauth.lib.webflow.authentication.repository")
@ComponentScan(basePackages = "io.getlime.security.*" )
@EntityScan("io.getlime.security.*")
public class PowerAuthWebFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(PowerAuthWebFlowApplication.class, args);
    }
}
