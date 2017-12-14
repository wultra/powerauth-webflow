package io.getlime.security.powerauth.lib.dataadapter.impl.service;

import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.entity.attribute.OperationAmountFieldAttribute;
import io.getlime.security.powerauth.lib.nextstep.model.entity.attribute.OperationFormFieldAttribute;
import io.getlime.security.powerauth.lib.nextstep.model.entity.attribute.OperationKeyValueFieldAttribute;
import org.springframework.stereotype.Service;

/**
 * Service which extracts form data from an operation based on required input for SMS text.
 *
 * @author Roman Strobl
 */
@Service
public class OperationFormDataService {

    private static final String FIELD_ACCOUNT_ID = "operation.account";

    /**
     * Extract amount from operation form data.
     * @param formData Operation form data.
     * @return Operation amount attribute.
     */
    public OperationAmountFieldAttribute getAmount(OperationFormData formData) {
        if (formData==null || formData.getParameters()==null) {
            throw new IllegalArgumentException("Argument formData is invalid");
        }
        return formData.getAmount();
    }

    /**
     * Extract account from operation form data.
     * @param formData Operation form data.
     * @return Operation to account value.
     */
    public String getAccount(OperationFormData formData) {
        if (formData==null || formData.getParameters()==null) {
            throw new IllegalArgumentException("Argument formData is invalid");
        }
        OperationFormFieldAttribute accountAttr = formData.getAttributeById(FIELD_ACCOUNT_ID);
        if (accountAttr == null) {
            return null;
        }
        if (!(accountAttr instanceof OperationKeyValueFieldAttribute)) {
            throw new IllegalStateException("Invalid account in formData");
        }
        return ((OperationKeyValueFieldAttribute)accountAttr).getValue();
    }

}
