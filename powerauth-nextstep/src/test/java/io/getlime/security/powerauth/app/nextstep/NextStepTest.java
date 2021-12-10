/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2021 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.nextstep;

import com.wultra.core.audit.base.database.DatabaseAudit;
import io.getlime.security.powerauth.app.nextstep.configuration.NextStepClientFactory;
import io.getlime.security.powerauth.app.nextstep.configuration.NextStepTestConfiguration;
import io.getlime.security.powerauth.lib.nextstep.client.NextStepClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Next Step tests.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.properties")
@Sql(scripts = "/db_schema.sql")
public class NextStepTest implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    protected NextStepClient nextStepClient;

    @Autowired
    protected NextStepClientFactory nextStepClientFactory;

    @Autowired
    protected NextStepTestConfiguration nextStepTestConfiguration;

    @LocalServerPort
    protected int port;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext1) throws BeansException {
        NextStepTest.applicationContext = applicationContext1;
    }

    @AfterAll
    public static void cleanup() {
        // Flush audit data to database before the test application and H2 database are terminated
        applicationContext.getBean(DatabaseAudit.class).flush();
    }

    @Test
    public void testContextLoads() {
        assertTrue(port > 1024);
    }

}
