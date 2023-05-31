/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2021 Wultra s.r.o.
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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserIdentityStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Entity which stores user identities.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_user_identity")
@Data
@EqualsAndHashCode(of = "userId")
public class UserIdentityEntity implements Serializable {

    private static final long serialVersionUID = -372574158382801384L;

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserIdentityStatus status;

    @Column(name = "extras")
    private String extras;

    @Column(name = "timestamp_created", nullable = false)
    private Date timestampCreated;

    @Column(name = "timestamp_last_updated")
    private Date timestampLastUpdated;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestampCreated")
    @ToString.Exclude
    private Set<UserContactEntity> contacts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @OrderBy("timestampCreated")
    @ToString.Exclude
    private Set<CredentialEntity> credentials = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @OrderBy("timestampCreated")
    @ToString.Exclude
    private Set<UserAliasEntity> aliases = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestampCreated")
    @ToString.Exclude
    private Set<UserRoleEntity> roles = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @OrderBy("timestampCreated DESC")
    @ToString.Exclude
    private Set<CredentialHistoryEntity> credentialHistory = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @OrderBy("timestampCreated DESC")
    @ToString.Exclude
    private Set<UserIdentityHistoryEntity> userIdentityHistory = new LinkedHashSet<>();

}
