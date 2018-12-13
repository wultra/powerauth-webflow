/*
 * Copyright 2017 Wultra s.r.o.
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
package io.getlime.security.powerauth.app.webflow.i18n;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
