/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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
package io.getlime.security.powerauth.app.nextstep.service;

import io.getlime.security.powerauth.app.nextstep.repository.AuthMethodRepository;
import io.getlime.security.powerauth.app.nextstep.repository.UserPrefsRepository;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.AuthMethodEntity;
import io.getlime.security.powerauth.app.nextstep.repository.model.entity.UserPrefsEntity;
import io.getlime.security.powerauth.lib.nextstep.model.entity.AuthMethodDetail;
import io.getlime.security.powerauth.lib.nextstep.model.entity.UserAuthMethodDetail;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * This service handles querying of user authentication methods and enabling/disabling them.
 *
 * @author Roman Strobl
 */
@Service
public class AuthMethodService {

    private AuthMethodRepository authMethodRepository;
    private UserPrefsRepository userPrefsRepository;

    @Autowired
    public AuthMethodService(AuthMethodRepository authMethodRepository, UserPrefsRepository userPrefsRepository) {
        this.authMethodRepository = authMethodRepository;
        this.userPrefsRepository = userPrefsRepository;
    }

    /**
     * Lists all authentication methods supported by the Next Step server.
     *
     * @return List of all authentication methods.s
     */
    public List<AuthMethodDetail> listAuthMethods() {
        List<AuthMethodDetail> allMethods = new ArrayList<>();
        List<AuthMethodEntity> authMethodList = authMethodRepository.findAllAuthMethods();
        for (AuthMethodEntity authMethodEntity : authMethodList) {
            allMethods.add(getAuthMethodDetail(authMethodEntity));
        }
        return allMethods;
    }

    /**
     * List authentication methods enabled for given user. Methods which can be enabled/disabled are retrieved from
     * user preferences. All other methods are enabled.
     *
     * @param userId User ID
     * @return List of authentication methods enabled for given user.
     */
    public List<UserAuthMethodDetail> listAuthMethodsEnabledForUser(String userId) {
        List<UserAuthMethodDetail> enabledMethods = new ArrayList<>();
        List<AuthMethodEntity> authMethodList = authMethodRepository.findAllAuthMethods();
        UserPrefsEntity userPrefs = null;
        if (userId!=null) {
            // read user prefs only when user ID is not null, for some authentication methods user ID is not known
            userPrefs = userPrefsRepository.findUserPrefs(userId);
        }
        for (AuthMethodEntity authMethodEntity : authMethodList) {
            if (authMethodEntity.getCheckUserPrefs()) {
                // methods with user prefs require special handling
                if (userPrefs != null) {
                    // get status of methods with user prefs
                    if (userPrefs.getAuthMethodEnabled(authMethodEntity.getUserPrefsColumn())) {
                        // read configuration of method from user prefs
                        String config = userPrefs.getAuthMethodConfig(authMethodEntity.getUserPrefsColumn());
                        // add method in case it is enabled in user prefs
                        enabledMethods.add(getUserAuthMethodDetail(userId, authMethodEntity, config));
                    }
                } else {
                    // user prefs are not set - resolve methods with user prefs by their default value
                    if (authMethodEntity.getUserPrefsDefault()) {
                        // add method in case it is enabled by default
                        enabledMethods.add(getUserAuthMethodDetail(userId, authMethodEntity, null));
                    }
                }
            } else {
                // add all methods without user prefs
                enabledMethods.add(getUserAuthMethodDetail(userId, authMethodEntity, null));
            }
        }
        return enabledMethods;
    }

    /**
     * Enable or disable an authentication method for given user.
     *
     * @param userId     User ID
     * @param authMethod authentication method
     * @param enabled    true if enabled, false if disabled, null if unspecified
     */
    public void updateAuthMethodForUser(String userId, AuthMethod authMethod, Boolean enabled, String config) {
        List<AuthMethodEntity> authMethodList = authMethodRepository.findAllAuthMethods();
        boolean authMethodFound = false;
        // check whether this method supports modifications at all
        for (AuthMethodEntity authMethodEntity : authMethodList) {
            if (authMethodEntity.getAuthMethod() == authMethod) {
                authMethodFound = true;
                if (!authMethodEntity.getCheckUserPrefs()) {
                    throw new IllegalArgumentException("Authentication method " + authMethod + " does not support user preferences.");
                }
            }
        }
        if (!authMethodFound) {
            throw new IllegalArgumentException("Authentication method " + authMethod + " is not supported.");
        }
        UserPrefsEntity userPrefs = userPrefsRepository.findUserPrefs(userId);
        if (userPrefs == null) {
            // create new user prefs
            userPrefs = new UserPrefsEntity();
            userPrefs.setUserId(userId);
            // set defaults
            for (AuthMethodEntity authMethodEntity : authMethodList) {
                if (authMethodEntity.getCheckUserPrefs()) {
                    if (authMethodEntity.getAuthMethod() == authMethod) {
                        // set requested value for method which is being updated
                        userPrefs.setAuthMethodEnabled(authMethodEntity.getUserPrefsColumn(), enabled);
                    } else {
                        // set default value for other methods than the method which is being updated
                        userPrefs.setAuthMethodEnabled(authMethodEntity.getUserPrefsColumn(), authMethodEntity.getUserPrefsDefault());
                    }
                    // set authMethod configuration
                    userPrefs.setAuthMethodConfig(authMethodEntity.getUserPrefsColumn(), config);
                }
            }
        } else {
            // update existing user prefs
            for (AuthMethodEntity authMethodEntity : authMethodList) {
                if (authMethodEntity.getCheckUserPrefs()) {
                    if (authMethodEntity.getAuthMethod() == authMethod) {
                        // set requested value for method which is being updated
                        userPrefs.setAuthMethodEnabled(authMethodEntity.getUserPrefsColumn(), enabled);
                        // set authMethod configuration
                        userPrefs.setAuthMethodConfig(authMethodEntity.getUserPrefsColumn(), config);
                    }
                }
            }

        }
        // finally save created or updated userPrefs
        userPrefsRepository.save(userPrefs);
    }

    /**
     * Converts AuthMethodEntity into AuthMethodDetail which contains less fields available for the UI.
     *
     * @param authMethodEntity entity representing the authentication method.
     * @return Authentication method detail.
     */
    private AuthMethodDetail getAuthMethodDetail(AuthMethodEntity authMethodEntity) {
        if (authMethodEntity == null) {
            return null;
        }
        AuthMethodDetail authMethodDetail = new AuthMethodDetail();
        authMethodDetail.setAuthMethod(authMethodEntity.getAuthMethod());
        authMethodDetail.setHasUserInterface(authMethodEntity.getHasUserInterface());
        authMethodDetail.setDisplayNameKey(authMethodEntity.getDisplayNameKey());
        return authMethodDetail;
    }

    private UserAuthMethodDetail getUserAuthMethodDetail(String userId, AuthMethodEntity authMethodEntity, String config) {
        if (authMethodEntity == null) {
            return null;
        }
        UserAuthMethodDetail userAuthMethodDetail = new UserAuthMethodDetail();
        userAuthMethodDetail.setUserId(userId);
        userAuthMethodDetail.setAuthMethod(authMethodEntity.getAuthMethod());
        userAuthMethodDetail.setHasUserInterface(authMethodEntity.getHasUserInterface());
        userAuthMethodDetail.setDisplayNameKey(authMethodEntity.getDisplayNameKey());
        userAuthMethodDetail.setConfig(config);
        return userAuthMethodDetail;
    }
}
