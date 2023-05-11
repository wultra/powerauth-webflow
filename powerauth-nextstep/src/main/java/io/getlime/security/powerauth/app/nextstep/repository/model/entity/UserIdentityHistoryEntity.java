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
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * Entity which stores user identity history.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_user_identity_history")
@Data
@EqualsAndHashCode(of = {"user", "timestampCreated"})
public class UserIdentityHistoryEntity implements Serializable {

    private static final long serialVersionUID = 2982236221553997424L;

    @Id
    @SequenceGenerator(name = "ns_user_identity_history", sequenceName = "ns_user_identity_history_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "ns_user_identity_history")
    @Column(name = "user_identity_history_id", nullable = false)
    private Long userIdentityId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", updatable = false, nullable = false)
    private UserIdentityEntity user;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserIdentityStatus status;

    @Column(name = "roles")
    private String roles;

    @Column(name = "extras")
    private String extras;

    @Column(name = "timestamp_created", nullable = false)
    private Date timestampCreated;

}
