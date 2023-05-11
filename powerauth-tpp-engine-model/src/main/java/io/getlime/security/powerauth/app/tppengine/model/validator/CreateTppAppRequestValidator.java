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

package io.getlime.security.powerauth.app.tppengine.model.validator;

import io.getlime.security.powerauth.app.tppengine.model.request.CreateTppAppRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Class to validate {@link io.getlime.security.powerauth.app.tppengine.model.request.CreateTppAppRequest}
 * objects.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class CreateTppAppRequestValidator {

    public static List<String> validate(CreateTppAppRequest source) {
        return validate(source, null);
    }

    public static List<String> validate(CreateTppAppRequest source, Set<String> permittedScopes) {
        List<String> errors = new ArrayList<>();
        final String tppLicense = source.getTppLicense();
        if (tppLicense == null || tppLicense.isEmpty()) {
            errors.add("License information is not present.");
        }

        final String tppName = source.getTppName();
        if (tppName == null || tppName.isEmpty()) {
            errors.add("Third-party name is not present.");
        }

        final String appName = source.getAppName();
        if (appName == null || appName.isEmpty()) {
            errors.add("Application name is not present.");
        }

        final String appType = source.getAppType();
        if (appType == null || appType.isEmpty()) {
            errors.add("Application type is not present.");
        } else {
            if (!"web".equals(appType) && !"native".equals(appType)) {
                errors.add("Invalid application type - must be 'native' or 'web'.");
            }
        }

        final String[] redirectUris = source.getRedirectUris();
        if (redirectUris == null || redirectUris.length == 0) {
            errors.add("You must provide at least one redirect URI.");
        } else {
            for (final String redirectUri : redirectUris) {
                try {
                    new URI(redirectUri);
                } catch (URISyntaxException e) {
                    errors.add("You provided an invalid redirect URI: " + redirectUri);
                }
            }
        }

        final String[] scopes = source.getScopes();
        if (scopes == null || scopes.length == 0) {
            errors.add("You must provide at least one OAuth 2.1 scope.");
        } else {
            for (final String scope : scopes) {
                // validate scope against the basic regexp pattern
                if (!scope.matches("^[A-Za-z0-9]+$")) {
                    errors.add("You provided an invalid scope: " + scope);
                } else {
                    // validate scope against the permitted scopes
                    if (permittedScopes != null && !permittedScopes.contains(scope)) {
                        errors.add("You provided an invalid scope: " + scope);
                    }
                }
            }
        }

        return errors.isEmpty() ? null : errors;
    }

}
