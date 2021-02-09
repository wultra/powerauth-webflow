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

import javax.persistence.*;
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
@EqualsAndHashCode(of = {"userId", "timestampCreated"})
public class UserIdentityHistoryEntity implements Serializable {

    private static final long serialVersionUID = 2982236221553997424L;

    @Id
    @SequenceGenerator(name = "ns_user_identity_history", sequenceName = "ns_user_identity_history_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "ns_user_identity_history")
    @Column(name = "user_identity_history_id", nullable = false)
    private Long userIdentityId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false, nullable = false)
    private UserIdentityEntity userId;

    @Column(name = "status", nullable = false)
    private UserIdentityStatus status;

    @Column(name = "roles")
    private String roles;

    @Column(name = "extras")
    private String extras;

    @Column(name = "timestamp_created", nullable = false)
    private Date timestampCreated;

}
