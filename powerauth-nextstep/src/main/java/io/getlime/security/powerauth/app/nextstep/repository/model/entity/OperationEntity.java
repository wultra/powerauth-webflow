/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2017 Wultra s.r.o.
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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserAccountStatus;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Entity which stores status of an operation, its parameters and last result.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Entity
@Table(name = "ns_operation")
@Data
@EqualsAndHashCode(of = "operationId")
public class OperationEntity implements Serializable {

    private static final long serialVersionUID = -8991119412441607003L;

    @Id
    @Column(name = "operation_id", nullable = false)
    private String operationId;

    @Column(name = "operation_name", nullable = false)
    private String operationName;

    @Column(name = "operation_form_data")
    private String operationFormData;

    @Column(name = "operation_data", nullable = false)
    private String operationData;

    @Column(name = "user_id")
    private String userId;

    @ManyToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "organization_id")
    private OrganizationEntity organization;

    @Column(name = "application_id")
    private String applicationId;

    @Column(name = "application_name")
    private String applicationName;

    @Column(name = "application_description")
    private String applicationDescription;

    @Column(name = "application_original_scopes")
    private String applicationOriginalScopes;

    @Column(name = "application_extras")
    private String applicationExtras;

    @Column(name = "user_account_status")
    @Enumerated(EnumType.STRING)
    private UserAccountStatus userAccountStatus;

    @Column(name = "external_operation_name")
    private String externalOperationName;

    @Column(name = "external_transaction_id")
    private String externalTransactionId;

    @Column(name = "result")
    @Enumerated(EnumType.STRING)
    private AuthResult result;

    @Column(name = "timestamp_created", nullable = false)
    private Date timestampCreated;

    @Column(name = "timestamp_expires")
    private Date timestampExpires;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "operation", cascade = CascadeType.ALL)
    @OrderBy("result_id")
    private List<OperationHistoryEntity> operationHistory;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "operation", cascade = CascadeType.ALL)
    @OrderBy("timestamp_created")
    @Fetch(value = FetchMode.SUBSELECT)
    private List<OperationAfsActionEntity> afsActions;

    /**
     * Is the operation expired?
     *
     * @return true if expired
     */
    public boolean isExpired() {
        return new Date().after(timestampExpires);
    }

    /**
     * Returns current OperationHistoryEntity. Null value is return in case there is no history for this operation.
     *
     * @return Current OperationHistoryEntity
     */
    public OperationHistoryEntity getCurrentOperationHistoryEntity() {
        if (operationHistory == null || operationHistory.isEmpty()) {
            return null;
        }
        return operationHistory.get(operationHistory.size() - 1);
    }

}
