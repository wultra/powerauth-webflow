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
package io.getlime.security.powerauth.app.nextstep.repository.model.entity;

import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthMethod;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
    private Integer maxAuthFails;

    @Column(name = "has_user_interface", nullable = false)
    private Boolean hasUserInterface;

    @Column(name = "display_name_key")
    private String displayNameKey;

    @Column(name = "has_mobile_token", nullable = false)
    private Boolean hasMobileToken;

}
