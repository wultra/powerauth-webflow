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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.HashAlgorithm;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.HashConfigStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
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

    @Serial
    private static final long serialVersionUID = 5186710016544178844L;

    @Id
    @SequenceGenerator(name = "ns_hashing_config", sequenceName = "ns_hashing_config_seq", allocationSize = 1)
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
