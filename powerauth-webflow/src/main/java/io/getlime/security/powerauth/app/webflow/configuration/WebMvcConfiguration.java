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

package io.getlime.security.powerauth.app.webflow.configuration;

import io.getlime.security.powerauth.app.webflow.i18n.ReloadableResourceBundleMessageSourceWithListing;
import io.getlime.security.powerauth.rest.api.spring.annotation.PowerAuthAnnotationInterceptor;
import io.getlime.security.powerauth.rest.api.spring.annotation.PowerAuthWebArgumentResolver;
import io.getlime.security.powerauth.rest.api.spring.filter.PowerAuthRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.List;
import java.util.Locale;

/**
 * Default Spring Web MVC configuration.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    private WebFlowServerConfiguration configuration;

    /* Register PowerAuth 2.0 Server Beans */

    @Bean
    public PowerAuthWebArgumentResolver powerAuthWebArgumentResolver() {
        return new PowerAuthWebArgumentResolver();
    }

    @Bean
    public PowerAuthAnnotationInterceptor powerAuthInterceptor() {
        return new PowerAuthAnnotationInterceptor();
    }

    @Bean
    public FilterRegistrationBean powerAuthFilterRegistration() {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
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
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setCookieName("lang");
        resolver.setDefaultLocale(Locale.ENGLISH);
        return resolver;
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
        return messageSource;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(powerAuthWebArgumentResolver());
        super.addArgumentResolvers(argumentResolvers);
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
        super.addInterceptors(registry);
    }

}
