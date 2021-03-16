/*
 * Copyright 2021 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep;

import io.getlime.security.powerauth.app.nextstep.configuration.NextStepClientFactory;
import io.getlime.security.powerauth.app.nextstep.configuration.NextStepTestConfiguration;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import org.junit.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertTrue;

/**
 * Next Step tests.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NextStepTest {

    protected NextStepClient nextStepClient;

    @Autowired
    protected NextStepClientFactory nextStepClientFactory;

    @Autowired
    protected NextStepTestConfiguration nextStepTestConfiguration;

    @LocalServerPort
    protected int port;

    @Test
    public void testContextLoads() {
        assertTrue(port > 1024);
    }
}