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

package io.getlime.security.powerauth.app.tppengine.errorhandling.exception;

/**
 * Exception thrown in case TPP was not found.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class TppNotFoundException extends Exception {

    private String licenseInfo;

    public TppNotFoundException() {
    }

    public TppNotFoundException(String message, String licenseInfo) {
        super(message);
        this.licenseInfo = licenseInfo;
    }

    public String getLicenseInfo() {
        return licenseInfo;
    }
}
