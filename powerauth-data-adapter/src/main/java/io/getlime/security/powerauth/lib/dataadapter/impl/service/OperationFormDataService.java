package io.getlime.security.powerauth.lib.dataadapter.impl.service;

import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.entity.attribute.OperationAmountAttribute;
import io.getlime.security.powerauth.lib.nextstep.model.entity.attribute.OperationFormAttribute;
import io.getlime.security.powerauth.lib.nextstep.model.entity.attribute.OperationKeyValueAttribute;
import org.springframework.stereotype.Service;

/**
 * Service which extracts form data from an operation based on required input for SMS text.
 *
 * @author Roman Strobl
 */
@Service
public class OperationFormDataService {

    private static final String FIELD_ACCOUNT_ID = "operation.account";

    public OperationAmountAttribute getAmount(OperationFormData formData) {
        if (formData==null || formData.getParameters()==null) {
            throw new IllegalArgumentException("Argument formData is invalid");
        }
        return formData.getAmount();
    }

    public String getAccount(OperationFormData formData) {
        if (formData==null || formData.getParameters()==null) {
            throw new IllegalArgumentException("Argument formData is invalid");
        }
        OperationFormAttribute accountAttr = formData.getAttributeById(FIELD_ACCOUNT_ID);
        if (accountAttr == null) {
            return null;
        }
        if (!(accountAttr instanceof OperationKeyValueAttribute)) {
            throw new IllegalStateException("Invalid account in formData");
        }
        return ((OperationKeyValueAttribute)accountAttr).getValue();
    }

}
