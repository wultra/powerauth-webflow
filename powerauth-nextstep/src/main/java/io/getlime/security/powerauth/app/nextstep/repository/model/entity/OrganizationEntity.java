/*
 * Copyright 2019 Wultra s.r.o.
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

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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
