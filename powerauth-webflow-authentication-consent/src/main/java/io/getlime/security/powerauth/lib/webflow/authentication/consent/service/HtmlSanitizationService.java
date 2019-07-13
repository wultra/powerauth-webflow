/*
 * Copyright 2019 Wultra s.r.o.
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
package io.getlime.security.powerauth.lib.webflow.authentication.consent.service;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Service;

/**
 * Service for sanitization of potentially unsafe HTML.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class HtmlSanitizationService {

    /**
     * Sanitize unsafe HTML text.
     * @param htmlText Unsafe HTML text.
     * @return Sanitized HTML text.
     */
    public String sanitize(String htmlText) {
        if (htmlText == null || htmlText.isEmpty()) {
            return htmlText;
        }
        // The sanitization policy corresponds with https://www.npmjs.com/package/sanitize-html#what-are-the-default-options
        // plus the 'img' tag with 'src' and 'alt' attributes.
        PolicyFactory policy = new HtmlPolicyBuilder()
                .allowAttributes("href", "name", "target").onElements("a")
                .allowAttributes("src", "alt").onElements("img")
                .allowStandardUrlProtocols()
                .allowElements("h3", "h4", "h5", "h6", "blockquote", "p", "a", "ul", "ol",
                        "nl", "li", "b", "i", "strong", "em", "strike", "code", "hr", "br", "div",
                        "table", "thead", "caption", "tbody", "tr", "th", "td", "pre", "iframe", "img"
                ).toFactory();
        return policy.sanitize(htmlText);
    }

}
