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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Hashtable;

/**
 * Configuration of Next Step server.
 *
 * @author Roman Strobl
 */
@Configuration
@ConfigurationProperties("ext")
@ComponentScan(basePackages = {"io.getlime.security.powerauth"})
@Slf4j
public class NextStepServerConfiguration {

    private final AuditFactory auditFactory;

    /**
     * Data Adapter service URL.
     */
    @Value("${powerauth.dataAdapter.service.url}")
    private String dataAdapterServiceUrl;

    @Value("${powerauth.service.url}")
    private String powerAuthRestUrl;

    @Value("${powerauth.service.restClientConfig.responseTimeout}")
    private Duration powerAuthServiceTimeout;

    @Value("${powerauth.service.restClientConfig.maxIdleTime}")
    private Duration powerAuthServiceMaxIdleTime;

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
     * Connection to LDAP setting.
     */
    @Value("${powerauth.nextstep.ldap.url}")
    private String ldapUrl;

    @Value("${powerauth.nextstep.ldap.base}")
    private String ldapBase;

    @Value("${powerauth.nextstep.ldap.managerDn}")
    private String managerDn;

    @Value("${powerauth.nextstep.ldap.managerPassword}")
    private String managerPassword;

    @Value("${powerauth.nextstep.ldap.pooled:false}")
    private boolean ldapPooled;

    @Value("${powerauth.nextstep.ldap.anonymousReadOnly:false}")
    private boolean ldapAnonymousReadOnly;

    @Value("${powerauth.nextstep.ldap.userSearchBase}")
    private String userSearchBase;

    @Value("${powerauth.nextstep.ldap.userSearchFilter}")
    private String userSearchFilter;

    @Value("${powerauth.nextstep.ldap.connectTimeout:5000}")
    private int ldapConnectTimeoutMs;

    @Value("${powerauth.nextstep.ldap.readTimeout:5000}")
    private int ldapReadTimeoutMs;

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
     * Get configured base filter for finding user in LDAP.
     * @return userSearchBase .
     */
    public String getUserSearchBase() {
        return userSearchBase;
    }

    /**
     * Get configured filter for finding user in LDAP.
     * @return userSearchFilter
     */
    public String getUserSearchFilter() {
        return userSearchFilter;
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
    public PowerAuthClient powerAuthClient() throws PowerAuthClientException {
        final PowerAuthRestClientConfiguration config = new PowerAuthRestClientConfiguration();
        config.setPowerAuthClientToken(powerAuthClientToken);
        config.setPowerAuthClientSecret(powerAuthClientSecret);
        config.setAcceptInvalidSslCertificate(powerAuthAcceptInvalidSslCertificate);
        config.setResponseTimeout(powerAuthServiceTimeout);
        config.setMaxIdleTime(powerAuthServiceMaxIdleTime);
        return new PowerAuthRestClient(powerAuthRestUrl, config);
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

    /**
     * Configure LDAP context source
     * @return LDAP context source.
     */
    @ConditionalOnProperty(prefix = "powerauth.nextstep.ldap", name = "enabled", havingValue = "true")
    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource cs = new LdapContextSource();
        cs.setUrl(ldapUrl);
        cs.setBase(ldapBase);
        if (StringUtils.hasText(managerDn)) cs.setUserDn(managerDn);
        if (StringUtils.hasText(managerPassword)) cs.setPassword(managerPassword);
        cs.setPooled(ldapPooled);
        cs.setAnonymousReadOnly(ldapAnonymousReadOnly);

        Hashtable<String, Object> env = new Hashtable<>();
        env.put("com.sun.jndi.ldap.connect.timeout", String.valueOf(ldapConnectTimeoutMs));
        env.put("com.sun.jndi.ldap.read.timeout", String.valueOf(ldapReadTimeoutMs));
        cs.setBaseEnvironmentProperties(env);

        cs.afterPropertiesSet();
        return cs;
    }

    @ConditionalOnProperty(prefix = "powerauth.nextstep.ldap", name = "enabled", havingValue = "true")
    @Bean
    public LdapTemplate ldapTemplate(LdapContextSource cs) {
        return new LdapTemplate(cs);
    }

}
