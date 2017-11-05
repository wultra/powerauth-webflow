package io.getlime.security.powerauth.lib.nextstep.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.getlime.security.powerauth.lib.nextstep.model.entity.attribute.*;

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

    // TODO - consider removing title and message in favor of OperationMessageAttribute and OperationTitleAttribute which support i18n properly
    private String title;
    private String message;
    private List<OperationFormAttribute> parameters;
    private boolean dynamicDataLoaded;
    private Map<String, String> userInput;

    public OperationFormData() {
        this.parameters = new ArrayList<>();
        this.userInput = new LinkedHashMap<>();
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
     * Set title.
     * @param titleId Title ID.
     */
    public void setTitle(String titleId) {
        OperationTitleAttribute attr = new OperationTitleAttribute();
        attr.setId(titleId);
        // temporary value until label is localized
        // TODO - migrate to OperationTitleAttribute which supports i18n
        this.title = titleId;
        saveAttribute(attr);
    }

    /**
     * Set localized title.
     * @param titleId Title ID.
     */
    public void setTitle(String titleId, String title) {
        OperationTitleAttribute attr = new OperationTitleAttribute();
        attr.setId(titleId);
        attr.setTitle(title);
        this.title = title;
        saveAttribute(attr);
    }

    /**
     * Get title.
     * @return Title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set message.
     * @param messageId Message ID.
     */
    public void setMessage(String messageId) {
        OperationMessageAttribute attr = new OperationMessageAttribute();
        attr.setId(messageId);
        // temporary value until label is localized
        // TODO - migrate to OperationMessageAttribute which supports i18n
        this.message = messageId;
        saveAttribute(attr);
    }

    /**
     * Set localized message.
     * @param messageId Message ID.
     */
    public void setMessage(String messageId, String message) {
        OperationMessageAttribute attr = new OperationMessageAttribute();
        attr.setId(messageId);
        attr.setMessage(message);
        this.message = message;
        saveAttribute(attr);
    }

    /**
     * Get message.
     * @return Message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set amount.
     * @param amountId Amount ID.
     * @param amount Amount value.
     * @param currencyId Currency ID.
     * @param currency Amount currency.
     */
    @JsonIgnore
    public void setAmount(String amountId, BigDecimal amount, String currencyId, String currency) {
        OperationAmountAttribute amountAttr = new OperationAmountAttribute();
        amountAttr.setId(amountId);
        amountAttr.setAmount(amount);
        amountAttr.setCurrencyId(currencyId);
        amountAttr.setCurrency(currency);
        saveAttribute(amountAttr);
    }

    /**
     * Get amount.
     * @return Amount.
     */
    @JsonIgnore
    public OperationAmountAttribute getAmount() {
        List<OperationFormAttribute> amountAttrs = getAttributesByType(OperationFormAttribute.Type.AMOUNT);
        if (amountAttrs.isEmpty()) {
            return null;
        }
        if (amountAttrs.size()>1) {
            throw new IllegalStateException("Multiple attributes of type AMOUNT found");
        }
        return (OperationAmountAttribute) amountAttrs.get(0);
    }


    /**
     * Set note.
     * @param noteId Note ID.
     */
    @JsonIgnore
    public void setNote(String noteId) {
        OperationNoteAttribute attr = new OperationNoteAttribute();
        attr.setId(noteId);
        saveAttribute(attr);
    }


    /**
     * Set localized note.
     * @param noteId Note ID.
     * @param note Localized note.
     */
    @JsonIgnore
    public void setNote(String noteId, String note) {
        OperationNoteAttribute attr = new OperationNoteAttribute();
        attr.setId(noteId);
        attr.setMessage(note);
        saveAttribute(attr);
    }

    /**
     * Get note.
     * @return Note.
     */
    @JsonIgnore
    public OperationNoteAttribute getNote() {
        List<OperationFormAttribute> attrs = getAttributesByType(OperationFormAttribute.Type.MESSAGE);
        if (attrs == null) {
            return null;
        }
        if (attrs.size()>1) {
            throw new IllegalStateException("Multiple attributes of type MESSAGE found");
        }
        return (OperationNoteAttribute) attrs.get(0);
    }

    /**
     * Add a bank account choice.
     * @param id Bank account choice ID.
     * @param bankAccounts List of bank accounts.
     */
    @JsonIgnore
    public void addBankAccountChoice(String id, List<BankAccountDetail> bankAccounts) {
        OperationBankAccountChoiceAttribute attr = new OperationBankAccountChoiceAttribute();
        attr.setId(id);
        attr.setBankAccounts(bankAccounts);
        saveAttribute(attr);
    }

    /**
     * Add a key-value attribute.
     * @param id Attribute ID.
     * @param value Attribute value.
     */
    @JsonIgnore
    public void addKeyValue(String id, String value) {
        OperationKeyValueAttribute attr = new OperationKeyValueAttribute();
        attr.setId(id);
        attr.setValue(value);
        saveAttribute(attr);
    }

    /**
     * Get attribute by id.
     * @param id Attribute ID.
     * @return Attribute.
     */
    @JsonIgnore
    public OperationFormAttribute getAttributeById(String id) {
        for (OperationFormAttribute attr: parameters) {
            if (attr.getId().equals(id)) {
                return attr;
            }
        }
        return null;
    }

    /**
     * Get attribute by type.
     * @param type Attribute by type.
     * @return Attribute.
     */
    @JsonIgnore
    public List<OperationFormAttribute> getAttributesByType(OperationFormAttribute.Type type) {
        List<OperationFormAttribute> attrs = new ArrayList<>();
        for (OperationFormAttribute attr: parameters) {
            if (attr.getType() == type) {
                attrs.add(attr);
            }
        }
        return attrs;
    }

    /**
     * Add a key-value user input.
     * @param key User input key.
     * @param value User input value.
     */
    public void addUserInput(String key, String value) {
        userInput.put(key, value);
    }

    /**
     * Set user input map.
     * @param userInput User input.
     */
    public void setUserInput(Map<String, String> userInput) {
        this.userInput = userInput;
    }

    /**
     * Adds attribute or updates existing attribute based on its ID.
     * @param attributeToSave Attribute to save.
     */
    private void saveAttribute(OperationFormAttribute attributeToSave) {
        if (attributeToSave == null || attributeToSave.getId() == null) {
            throw new IllegalArgumentException("Invalid attribute");
        }
        Integer existingIndex = null;
        int counter = 0;
        for (OperationFormAttribute attr: parameters) {
            if (attr.getId().equals(attributeToSave.getId())) {
                existingIndex = counter;
                break;
            }
            counter++;
        }
        if (existingIndex != null) {
            parameters.set(existingIndex, attributeToSave);
        } else {
            parameters.add(attributeToSave);
        }
    }
}
