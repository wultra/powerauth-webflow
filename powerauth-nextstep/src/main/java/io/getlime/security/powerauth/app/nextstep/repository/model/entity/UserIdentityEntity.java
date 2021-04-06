/*
 * Copyright 2021 Wultra s.r.o.
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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
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
