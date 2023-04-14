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
package io.getlime.security.powerauth.app.webflow.i18n;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Service which converts resource bundle messages for given locale to JSON and provides access to the MessageSource.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class I18NService {

    private ObjectMapper objectMapper;

    @Resource
    private ReloadableResourceBundleMessageSourceWithListing messageSource;

    /**
     * Default constructor.
     */
    public I18NService() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Generates JSON representation of all messages for given locale.
     *
     * @param locale Requested locale.
     * @return JSON representation of messages.
     */
    public String generateMessages(Locale locale) {
        try {
            return objectMapper.writeValueAsString(messageSource.getAllProperties(locale));
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * Get the message source for all locales.
     *
     * @return Message source.
     */
    public AbstractMessageSource getMessageSource() {
        return messageSource;
    }

}
