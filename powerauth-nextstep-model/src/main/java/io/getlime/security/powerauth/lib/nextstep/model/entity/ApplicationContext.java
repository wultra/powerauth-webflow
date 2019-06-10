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
package io.getlime.security.powerauth.lib.nextstep.model.entity;

/**
 * Application context for OAuth 2.0 consent screen.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class ApplicationContext {

    private String id;
    private String name;
    private String description;
    private ApplicationExtras extras;

    /**
     * Default constructor.
     */
    public ApplicationContext() {
    }

    /**
     * Constructor with all details.
     * @param id Application identifier.
     * @param name Application name.
     * @param description Application description.
     * @param extras Extra information for OAuth 2.0 consent screen.
     */
    public ApplicationContext(String id, String name, String description, ApplicationExtras extras) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.extras = extras;
    }

    /**
     * Get application identifier.
     * @return Application identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * Set application identifier.
     * @param id Application identifier.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get application name.
     * @return Application name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set application name.
     * @param name Application name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get application description.
     * @return Application description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set application description.
     * @param description Application description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get extra information for OAuth 2.0 consent screen.
     * @return Extra information for OAuth 2.0 consent screen.
     */
    public ApplicationExtras getExtras() {
        return extras;
    }

    /**
     * Set extra information for OAuth 2.0 consent screen.
     * @param extras Extra information for OAuth 2.0 consent screen.
     */
    public void setExtras(ApplicationExtras extras) {
        this.extras = extras;
    }
}
