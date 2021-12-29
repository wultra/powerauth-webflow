/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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
        // plus the 'img' tag with 'src' and 'alt' attributes and class attributes for various tags.
        PolicyFactory policy = new HtmlPolicyBuilder()
                .allowAttributes("href", "name", "target", "class").onElements("a")
                .allowAttributes("src", "alt", "class").onElements("img")
                .allowAttributes("class").onElements("div")
                .allowAttributes("class").onElements("p")
                .allowStandardUrlProtocols()
                .allowElements("h3", "h4", "h5", "h6", "blockquote", "p", "a", "ul", "ol",
                        "nl", "li", "b", "i", "strong", "em", "strike", "code", "hr", "br", "div",
                        "table", "thead", "caption", "tbody", "tr", "th", "td", "pre", "iframe", "img"
                ).toFactory();
        return policy.sanitize(htmlText);
    }

}
