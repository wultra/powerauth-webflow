package io.getlime.security.powerauth.lib.bankadapter.service;

import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationAmountAttribute;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormAttribute;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationFormData;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationKeyValueAttribute;
import org.springframework.stereotype.Service;

/**
 * Service which extracts form data from an operation based on required input for SMS text.
 *
 * @author Roman Strobl
 */
@Service
public class OperationFormDataService {

    private static final String LABEL_TO_ACCOUNT = "To Account";

    public OperationAmountAttribute getAmount(OperationFormData formData) {
        if (formData==null || formData.getParameters()==null) {
            throw new IllegalArgumentException("Argument formData is invalid.");
        }
        for (OperationFormAttribute attribute: formData.getParameters()) {
            if (attribute.getType()==OperationFormAttribute.Type.AMOUNT) {
                return (OperationAmountAttribute) attribute;
            }
        }
        return null;
    }

    public String getAccount(OperationFormData formData) {
        if (formData==null || formData.getParameters()==null) {
            throw new IllegalArgumentException("Argument formData is invalid.");
        }
        for (OperationFormAttribute attribute: formData.getParameters()) {
            if (attribute.getType()==OperationFormAttribute.Type.KEY_VALUE) {
                OperationKeyValueAttribute keyValueAttribute = (OperationKeyValueAttribute) attribute;
                if (keyValueAttribute.getLabel().equals(LABEL_TO_ACCOUNT)) {
                    return keyValueAttribute.getValue();
                }
            }
        }
        return null;
    }

}
