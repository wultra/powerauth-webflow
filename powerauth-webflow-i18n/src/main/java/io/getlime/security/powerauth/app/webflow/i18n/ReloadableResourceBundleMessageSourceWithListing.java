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

import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;
import java.util.Properties;

/**
 * Custom ResourceBundleMessageSource which is both reloadable and supports listing. Cache is disabled to allow
 * immediate reload of messages.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class ReloadableResourceBundleMessageSourceWithListing extends ReloadableResourceBundleMessageSource {

    Properties getAllProperties(Locale locale) {
        clearCacheIncludingAncestors();
        PropertiesHolder propertiesHolder = getMergedProperties(locale);
        return propertiesHolder.getProperties();
    }
}
