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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserRoleStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Entity which assigns user roles.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_user_role")
@Data
@EqualsAndHashCode(of = {"userId", "role"})
public class UserRoleEntity implements Serializable {

    private static final long serialVersionUID = -248437038124901685L;

    @Id
    @SequenceGenerator(name = "ns_user_role", sequenceName = "ns_user_role_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "ns_user_role")
    @Column(name = "user_role_id", nullable = false)
    private Long userRoleId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false, nullable = false)
    private UserIdentityEntity userId;

    @ManyToOne
    @JoinColumn(name = "role_id", referencedColumnName = "role_id", insertable = false, updatable = false, nullable = false)
    private RoleEntity role;

    @Column(name = "status", nullable = false)
    private UserRoleStatus status;

    @Column(name = "timestamp_created", nullable = false)
    private Date timestampCreated;

    @Column(name = "timestamp_last_updated")
    private Date timestampLastUpdated;

}
