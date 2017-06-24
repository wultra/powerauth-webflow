package io.getlime.security.powerauth.lib.nextstep.model.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class OperationDisplayDetails {

    private String title;
    private String message;
    private List<OperationDisplayAttribute> parameters;

    public OperationDisplayDetails() {
        this.parameters = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<OperationDisplayAttribute> getParameters() {
        return parameters;
    }

    public void addAmount(String label, BigDecimal amount, String currency) {
        OperationAmountDisplayAttribute attr = new OperationAmountDisplayAttribute();
        attr.setLabel(label);
        attr.setAmount(amount);
        attr.setCurrency(currency);
        parameters.add(attr);
    }

    public void addKeyValue(String label, String value) {
        OperationKeyValueDisplayAttribute attr = new OperationKeyValueDisplayAttribute();
        attr.setLabel(label);
        attr.setValue(value);
        parameters.add(attr);
    }

    public void addMessage(String label, String message) {
        OperationMessageDisplayAttribute attr = new OperationMessageDisplayAttribute();
        attr.setLabel(label);
        attr.setMessage(message);
        parameters.add(attr);
    }

}
