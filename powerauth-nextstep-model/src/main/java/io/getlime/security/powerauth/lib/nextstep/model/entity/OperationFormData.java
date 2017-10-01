package io.getlime.security.powerauth.lib.nextstep.model.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Operation form data represents data visible to the user during the operation and collected responses.
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class OperationFormData {

    private String title;
    private String message;
    private List<OperationFormAttribute> parameters;
    private boolean dynamicDataLoaded;
    private Map<String, String > userInput;

    public OperationFormData() {
        this.parameters = new ArrayList<>();
        this.userInput = new LinkedHashMap<>();
    }

    /**
     * Get operation title.
     * @return Operation title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set operation title.
     * @param title Operation title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get operation message.
     * @return Operation message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set operation message.
     * @param message Operation message.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get form attributes.
     * @return Form attributes.
     */
    public List<OperationFormAttribute> getParameters() {
        return parameters;
    }

    /**
     * Whether dynamic data is loaded.
     * @return True if dynamic data is loaded, otherwise false.
     */
    public boolean isDynamicDataLoaded() {
        return dynamicDataLoaded;
    }

    /**
     * Set whether dynamic data is loaded.
     * @param dynamicDataLoaded True if dynamic data is loaded, otherwise false.
     */
    public void setDynamicDataLoaded(boolean dynamicDataLoaded) {
        this.dynamicDataLoaded = dynamicDataLoaded;
    }

    /**
     * Get key-value map with user input.
     * @return User input.
     */
    public Map<String, String> getUserInput() {
        return userInput;
    }

    /**
     * Add an amount.
     * @param label Amount label.
     * @param amount Amount value.
     * @param currency Amount currency.
     */
    public void addAmount(String label, BigDecimal amount, String currency) {
        OperationAmountAttribute attr = new OperationAmountAttribute();
        attr.setLabel(label);
        attr.setAmount(amount);
        attr.setCurrency(currency);
        parameters.add(attr);
    }

    /**
     * Add a key-value attribute.
     * @param label Attribute label.
     * @param value Attribute value.
     */
    public void addKeyValue(String label, String value) {
        OperationKeyValueAttribute attr = new OperationKeyValueAttribute();
        attr.setLabel(label);
        attr.setValue(value);
        parameters.add(attr);
    }

    /**
     * Add a message.
     * @param label Message label.
     * @param message Message value.
     */
    public void addMessage(String label, String message) {
        OperationMessageAttribute attr = new OperationMessageAttribute();
        attr.setLabel(label);
        attr.setMessage(message);
        parameters.add(attr);
    }

    /**
     * Add a bank account choice.
     * @param label Bank account choice label.
     * @param bankAccounts List of bank accounts.
     * @param chosenBankAccountNumber Chosen bank account number.
     */
    public void addBankAccountChoice(String label, List<BankAccountDetail> bankAccounts, String chosenBankAccountNumber) {
        OperationBankAccountChoiceAttribute attr = new OperationBankAccountChoiceAttribute();
        attr.setLabel(label);
        attr.setBankAccounts(bankAccounts);
        attr.setChosenBankAccountNumber(chosenBankAccountNumber);
        parameters.add(attr);
    }

    /**
     * Add a key-value user input.
     * @param key User input key.
     * @param value User input value.
     */
    public void addUserInput(String key, String value) {
        userInput.put(key, value);
    }
}
