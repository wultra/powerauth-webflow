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
package io.getlime.security.powerauth.app.dataadapter;

import io.getlime.security.powerauth.crypto.lib.config.PowerAuthConfiguration;
import io.getlime.security.powerauth.provider.CryptoProviderUtilFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

import java.security.Security;

/**
 * Spring Boot servlet initializer.
 * @author Roman Strobl, roman.strobl@lime-company.eu
 */
public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        // Register BC provider
        Security.addProvider(new BouncyCastleProvider());

        // Tell PowerAuth components to use BC provider
        PowerAuthConfiguration.INSTANCE.setKeyConvertor(CryptoProviderUtilFactory.getCryptoProviderUtils());

        return application.sources(DataAdapterApplication.class);
    }

}
