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

package io.getlime.security.powerauth.app.tppengine.model.validator;

import com.sun.jndi.toolkit.url.Uri;
import io.getlime.security.powerauth.app.tppengine.model.request.CreateTppAppRequest;

import java.net.MalformedURLException;
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

        final String[] redirectUris = source.getRedirectUris();
        if (redirectUris == null || redirectUris.length == 0) {
            errors.add("You must provide at least one redirect URI.");
        } else {
            for (final String redirectUri : redirectUris) {
                try {
                    new Uri(redirectUri);
                } catch (MalformedURLException e) {
                    errors.add("You provided an invalid redirect URI: " + redirectUri);
                }
            }
        }

        final String[] scopes = source.getScopes();
        if (scopes == null || scopes.length == 0) {
            errors.add("You must provide at least one OAuth 2.0 scope.");
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
