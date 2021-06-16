/*
 * Copyright 2017 Wultra s.r.o.
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

import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.UserAccountStatus;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.AuthResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
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
