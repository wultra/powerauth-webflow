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
package com.wultra.security.powerauth.app.nextstep.configuration;

import tools.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Default Web Application Configuration.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Configuration
public class WebApplicationConfig implements WebMvcConfigurer {

    private final JsonMapper objectMapper;

    /**
     * Configuration constructor.
     * @param objectMapper Object mapper.
     */
    @Autowired
    public WebApplicationConfig(JsonMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    /**
     * Set custom JSON converter.
     *
     * @return New custom converter with a correct object mapper.
     */
    private JacksonJsonHttpMessageConverter jacksonJsonHttpMessageConverter() {
        return new JacksonJsonHttpMessageConverter(objectMapper);
    }

    /**
     * Register the JSON converters.
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(jacksonJsonHttpMessageConverter());
    }

}
