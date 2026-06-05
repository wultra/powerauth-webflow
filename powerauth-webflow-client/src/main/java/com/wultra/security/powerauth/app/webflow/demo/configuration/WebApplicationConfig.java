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
package com.wultra.security.powerauth.app.webflow.demo.configuration;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Default Web Application Configuration.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Configuration
public class WebApplicationConfig implements WebMvcConfigurer {

    /**
     * Custom object mapper to make sure that dates and other values serialize
     * correctly.
     *
     * @return A new object mapper.
     */
    private ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    /**
     * Set custom JSON converter.
     *
     * @return New custom converter with a correct object mapper.
     */
    private JacksonJsonHttpMessageConverter jacksonJsonHttpMessageConverter() {
        return new JacksonJsonHttpMessageConverter((JsonMapper) objectMapper());
    }

    /**
     * Register the JSON converters.
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(jacksonJsonHttpMessageConverter());
    }

}
