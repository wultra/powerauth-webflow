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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.HashAlgorithm;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.HashConfigStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Entity which stores hashing configurations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_hashing_config")
@Data
@EqualsAndHashCode(of = "name")
public class HashConfigEntity implements Serializable {

    private static final long serialVersionUID = 5186710016544178844L;

    @Id
    @SequenceGenerator(name = "ns_hashing_config", sequenceName = "ns_hashing_config_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "ns_hashing_config")
    @Column(name = "hashing_config_id", nullable = false)
    private Long hashConfigId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "algorithm", nullable = false)
    @Enumerated(EnumType.STRING)
    private HashAlgorithm algorithm;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private HashConfigStatus status;

    @Column(name = "parameters")
    private String parameters;

    @Column(name = "timestamp_created", nullable = false)
    private Date timestampCreated;

    @Column(name = "timestamp_last_updated")
    private Date timestampLastUpdated;

}
