/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2019 Wultra s.r.o.
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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Organization entity which adds organization context to operations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_organization")
@Data
@EqualsAndHashCode(of = "organizationId")
public class OrganizationEntity implements Serializable {

    private static final long serialVersionUID = -3682348562614758414L;

    @Id
    @Column(name = "organization_id", nullable = false)
    private String organizationId;

    @Column(name = "display_name_key")
    private String displayNameKey;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "order_number", nullable = false)
    private int orderNumber;

    @Column(name = "default_credential_name")
    private String defaultCredentialName;

    @Column(name = "default_otp_name")
    private String defaultOtpName;

}
