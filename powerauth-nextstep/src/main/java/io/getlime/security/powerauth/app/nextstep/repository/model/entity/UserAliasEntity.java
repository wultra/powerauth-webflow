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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserAliasStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Entity which stores user aliases.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_user_alias")
@Data
@EqualsAndHashCode(of = {"name", "user"})
public class UserAliasEntity implements Serializable {

    private static final long serialVersionUID = -6855066974507308862L;

    @Id
    @SequenceGenerator(name = "ns_user_alias", sequenceName = "ns_user_alias_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "ns_user_alias")
    @Column(name = "user_alias_id", nullable = false)
    private Long userAliasId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private UserIdentityEntity user;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserAliasStatus status;

    @Column(name = "extras")
    private String extras;

    @Column(name = "timestamp_created", nullable = false)
    private Date timestampCreated;

    @Column(name = "timestamp_last_updated")
    private Date timestampLastUpdated;

}
