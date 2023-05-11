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

package io.getlime.security.powerauth.app.webflow.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.getlime.security.powerauth.app.webflow.i18n.ReloadableResourceBundleMessageSourceWithListing;
import io.getlime.security.powerauth.rest.api.spring.annotation.support.PowerAuthAnnotationInterceptor;
import io.getlime.security.powerauth.rest.api.spring.annotation.support.PowerAuthEncryptionArgumentResolver;
import io.getlime.security.powerauth.rest.api.spring.annotation.support.PowerAuthWebArgumentResolver;
import io.getlime.security.powerauth.rest.api.spring.filter.PowerAuthRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.List;

/**
 * Default Spring Web MVC configuration.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private WebFlowServerConfiguration configuration;

    /* Register PowerAuth Server Beans */

    /**
     * Register PowerAuth web argument resolver.
     * @return PowerAuth web argument resolver.
     */
    @Bean
    public PowerAuthWebArgumentResolver powerAuthWebArgumentResolver() {
        return new PowerAuthWebArgumentResolver();
    }

    /**
     * Register PowerAuth encryption argument resolver.
     * @return PowerAuth encryption argument resolver.
     */
    @Bean
    public PowerAuthEncryptionArgumentResolver powerAuthEncryptionArgumentResolver() {
        return new PowerAuthEncryptionArgumentResolver();
    }

    /**
     * Register PowerAuth interceptor.
     * @return PowerAuth interceptor.
     */
    @Bean
    public PowerAuthAnnotationInterceptor powerAuthInterceptor() {
        return new PowerAuthAnnotationInterceptor();
    }

    /**
     * Register PowerAuth filter.
     * @return PowerAuth filter.
     */
    @Bean
    public FilterRegistrationBean<PowerAuthRequestFilter> powerAuthFilterRegistration() {
        FilterRegistrationBean<PowerAuthRequestFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new PowerAuthRequestFilter());
        registrationBean.setMatchAfter(true);
        return registrationBean;
    }

    /**
     * LocaleResolver resolves the locale based on cookie called 'lang'. Default locale is English.
     *
     * @return Locale resolver
     */
    @Bean
    public LocaleResolver localeResolver() {
        return new CookieLocaleResolver("lang");
    }

    /**
     * LocaleChangeInterceptor changes the locale based on value of parameter lang.
     *
     * @return Locale change interceptor
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    /**
     * MessageSource provides access to internationalized messages. Custom implementation of MessageSource is used
     * to allow reloading of messages and listing all message properties.
     *
     * @return Message source
     */
    @Bean
    public ReloadableResourceBundleMessageSourceWithListing messageSource() {
        ReloadableResourceBundleMessageSourceWithListing messageSource = new ReloadableResourceBundleMessageSourceWithListing();
        messageSource.setBasename(configuration.getResourcesLocation() + "messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(Math.toIntExact(configuration.getResourcesCacheDuration().getSeconds()));
        return messageSource;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(powerAuthWebArgumentResolver());
        argumentResolvers.add(powerAuthEncryptionArgumentResolver());
    }

    /**
     * Add resource handlers to registry. Used to publish custom folder as external resources.
     * By default, resources in "classpath:/resources/" are used.
     *
     * @param registry Registry.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/ext-resources/**").addResourceLocations(configuration.getResourcesLocation());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(powerAuthInterceptor());
        registry.addInterceptor(localeChangeInterceptor());
    }

    /**
     * Custom object mapper to make sure that dates and other values serialize
     * correctly.
     *
     * @return A new object mapper.
     */
    private ObjectMapper objectMapper() {
        Jackson2ObjectMapperFactoryBean bean = new Jackson2ObjectMapperFactoryBean();
        bean.setIndentOutput(true);
        bean.afterPropertiesSet();
        ObjectMapper objectMapper = bean.getObject();
        objectMapper.registerModule(new JavaTimeModule());
        // replacement for ISO8601DateFormat which is deprecated
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return objectMapper;
    }

    /**
     * Set custom JSON converter.
     *
     * @return New custom converter with a correct object mapper.
     */
    private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        return converter;
    }

    /**
     * Register the JSON converters.
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(mappingJackson2HttpMessageConverter());
    }

}
