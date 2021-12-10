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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wultra.core.audit.base.Audit;
import com.wultra.core.audit.base.AuditFactory;
import com.wultra.security.powerauth.client.PowerAuthClient;
import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import com.wultra.security.powerauth.rest.client.PowerAuthRestClient;
import com.wultra.security.powerauth.rest.client.PowerAuthRestClientConfiguration;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClient;
import io.getlime.security.powerauth.lib.dataadapter.client.DataAdapterClientErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration of Next Step server.
 *
 * @author Roman Strobl
 */
@Configuration
@ConfigurationProperties("ext")
@ComponentScan(basePackages = {"io.getlime.security.powerauth"})
public class NextStepServerConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(NextStepServerConfiguration.class);
    private final AuditFactory auditFactory;

    /**
     * Data Adapter service URL.
     */
    @Value("${powerauth.dataAdapter.service.url}")
    private String dataAdapterServiceUrl;

    @Value("${powerauth.service.url}")
    private String powerAuthRestUrl;

    @Value("${powerauth.service.security.clientToken}")
    private String powerAuthClientToken;

    @Value("${powerauth.service.security.clientSecret}")
    private String powerAuthClientSecret;

    @Value("${powerauth.service.ssl.acceptInvalidSslCertificate}")
    private boolean powerAuthAcceptInvalidSslCertificate;

    /**
     * Operation expiration time in seconds.
     */
    @Value("${powerauth.nextstep.operation.expirationTimeInSeconds}")
    private int operationExpirationTime;

    @Value("${powerauth.nextstep.identity.credential.useOriginalUsername}")
    private boolean useOriginalUsername;

    @Value("${powerauth.nextstep.identity.credential.generateUsernameMaxAttempts}")
    private int generateUsernameMaxAttempts;

    @Value("${powerauth.nextstep.pa.operations.enabled}")
    private boolean powerAuthOperationSupportEnabled;

    @Value("${powerauth.nextstep.e2eEncryption.key}")
    private String e2eEncryptionKey;

    @Value("${powerauth.nextstep.db.master.encryption.key}")
    private String masterDbEncryptionKey;

    /**
     * Application name.
     */
    @Value("${powerauth.nextstep.service.applicationName}")
    private String applicationName;

    /**
     * Application display name.
     */
    @Value("${powerauth.nextstep.service.applicationDisplayName}")
    private String applicationDisplayName;

    /**
     * Application environment.
     */
    @Value("${powerauth.nextstep.service.applicationEnvironment}")
    private String applicationEnvironment;

    /**
     * Configuration constructor.
     * @param auditFactory Audit factory.
     */
    @Autowired
    public NextStepServerConfiguration(AuditFactory auditFactory) {
        this.auditFactory = auditFactory;
    }

    /**
     * Get the operation expiration time.
     *
     * @return expiration time for operations in seconds
     */
    public int getOperationExpirationTime() {
        return operationExpirationTime;
    }

    /**
     * Get whether original username for a removed credential when the credential is recreated.
     * @return Whether original username for a removed credential when the credential is recreated.
     */
    public boolean isUseOriginalUsername() {
        return useOriginalUsername;
    }

    /**
     * Get maximum number of attempts when generating username.
     * @return Maximum number of attempts when generating username.
     */
    public int getGenerateUsernameMaxAttempts() {
        return generateUsernameMaxAttempts;
    }

    /**
     * Get whether PowerAuth operations support is enabled.
     * @return Whether PowerAuth operations support is enabled.
     */
    public boolean isPowerAuthOperationSupportEnabled() {
        return powerAuthOperationSupportEnabled;
    }

    /**
     * Get end-to-end encryption key.
     * @return End-to-end encryption key
     */
    public String getE2eEncryptionKey() {
        return e2eEncryptionKey;
    }

    /**
     * Get master DB encryption key.
     * @return Master DB encryption key.
     */
    public String getMasterDbEncryptionKey() {
        return masterDbEncryptionKey;
    }

    /**
     * Get application name.
     * @return Application name.
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * Get application display name.
     * @return Application display name.
     */
    public String getApplicationDisplayName() {
        return applicationDisplayName;
    }

    /**
     * Get application environment.
     * @return Application environment.
     */
    public String getApplicationEnvironment() {
        return applicationEnvironment;
    }

    /**
     * Default data adapter client.
     *
     * @return Data adapter client.
     */
    @Bean
    public DataAdapterClient defaultDataAdapterClient() {
        try {
            return new DataAdapterClient(dataAdapterServiceUrl);
        } catch (DataAdapterClientErrorException ex) {
            logger.error(ex.getMessage(), ex);
            audit().error(ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Initialize PowerAuth REST client.
     * @return PowerAuth REST client.
     */
    @Bean
    public PowerAuthClient powerAuthClient() {
        final PowerAuthRestClientConfiguration config = new PowerAuthRestClientConfiguration();
        config.setPowerAuthClientToken(powerAuthClientToken);
        config.setPowerAuthClientSecret(powerAuthClientSecret);
        config.setAcceptInvalidSslCertificate(powerAuthAcceptInvalidSslCertificate);
        try {
            return new PowerAuthRestClient(powerAuthRestUrl, config);
        } catch (PowerAuthClientException ex) {
            logger.error(ex.getMessage(), ex);
            audit().error(ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Prepare and configure object mapper.
     * @return Object mapper.
     */
    @Bean
    public ObjectMapper objectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    /**
     * Prepare audit interface.
     * @return Audit interface.
     */
    @Bean
    public Audit audit() {
        return auditFactory.getAudit();
    }

}
