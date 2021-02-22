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
package io.getlime.security.powerauth.app.nextstep.repository.model.entity;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity which stores configuration of authentication methods.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_auth_method")
@Data
@EqualsAndHashCode(of = "authMethod")
public class AuthMethodEntity implements Serializable {

    private static final long serialVersionUID = -2015768978885351433L;

    @Id
    @Column(name = "auth_method", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthMethod authMethod;

    @Column(name = "order_number", nullable = false)
    private Long orderNumber;

    @Column(name = "check_user_prefs", nullable = false)
    private Boolean checkUserPrefs;

    @Column(name = "user_prefs_column")
    private Integer userPrefsColumn;

    @Column(name = "user_prefs_default")
    private Boolean userPrefsDefault;

    @Column(name = "check_auth_fails", nullable = false)
    private Boolean checkAuthFails;

    @Column(name = "max_auth_fails")
    private Long maxAuthFails;

    @Column(name = "has_user_interface", nullable = false)
    private Boolean hasUserInterface;

    @Column(name = "display_name_key")
    private String displayNameKey;

    @Column(name = "has_mobile_token", nullable = false)
    private Boolean hasMobileToken;

}
