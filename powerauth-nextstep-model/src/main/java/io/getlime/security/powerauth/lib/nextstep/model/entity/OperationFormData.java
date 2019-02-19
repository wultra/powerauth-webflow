package io.getlime.security.powerauth.lib.nextstep.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.getlime.security.powerauth.lib.nextstep.model.entity.attribute.*;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.BannerType;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.ValueFormatType;
import io.getlime.security.powerauth.lib.nextstep.model.entity.validator.AmountValidator;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidOperationDataException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Operation form data represents data visible to the user during the operation and collected responses.
 * @author Petr Dvorak, petr@wultra.com
 */
public class OperationFormData {

    private OperationFormMessageAttribute title;
    private OperationFormMessageAttribute greeting;
    private OperationFormMessageAttribute summary;
    private List<OperationFormFieldConfig> config;
    private List<OperationFormBanner> banners;
    private List<OperationFormFieldAttribute> parameters;
    private boolean dynamicDataLoaded;
    private Map<String, String> userInput;

    /**
     * Default constructor.
     */
    public OperationFormData() {
        this.config = new ArrayList<>();
        this.parameters = new ArrayList<>();
        this.banners = new ArrayList<>();
        this.userInput = new LinkedHashMap<>();
    }

    /**
     * Get form configuration.
     * @return Form configuration.
     */
    public List<OperationFormFieldConfig> getConfig() {
        return config;
    }

    /**
     * Get form attributes.
     * @return Form attributes.
     */
    public List<OperationFormFieldAttribute> getParameters() {
        return parameters;
    }

    /**
     * Get form banners.
     * @return Form banners.
     */
    public List<OperationFormBanner> getBanners() {
        return banners;
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
     * Set title form attribute.
     * @param title Title form attribute.
     */
    public void setTitle(OperationFormMessageAttribute title) {
        if (title == null) {
            // avoid JSON mapping null title
            return;
        }
        this.title = title;
    }

    /**
     * Set title.
     * @param titleId Title ID.
     * @return Operation form message attribute.
     */
    @JsonIgnore
    public OperationFormMessageAttribute addTitle(String titleId) {
        OperationFormMessageAttribute attr = new OperationFormMessageAttribute();
        attr.setId(titleId);
        this.title = attr;
        return attr;
    }

    /**
     * Get title form attribute.
     * @return Title form attribute.
     */
    public OperationFormMessageAttribute getTitle() {
        return title;
    }

    /**
     * Set greeting form attribute.
     * @param greeting Greeting form attribute.
     */
    public void setGreeting(OperationFormMessageAttribute greeting) {
        if (greeting == null) {
            // avoid JSON mapping null title
            return;
        }
        this.greeting = greeting;
    }

    /**
     * Set greeting.
     * @param greetingId Greeting ID.
     * @return Operation form message attribute.
     */
    @JsonIgnore
    public OperationFormMessageAttribute addGreeting(String greetingId) {
        OperationFormMessageAttribute attr = new OperationFormMessageAttribute();
        attr.setId(greetingId);
        this.greeting = attr;
        return attr;
    }

    /**
     * Get summary form attribute.
     * @return Summary form attribute.
     */
    public OperationFormMessageAttribute getGreeting() {
        return greeting;
    }

    /**
     * Set summary form attribute.
     * @param summary Summary form attribute.
     */
    public void setSummary(OperationFormMessageAttribute summary) {
        if (summary == null) {
            // avoid JSON mapping null title
            return;
        }
        this.summary = summary;
    }

    /**
     * Set summary.
     * @param summaryId Message ID.
     * @return Operation form message attribute.
     */
    @JsonIgnore
    public OperationFormMessageAttribute addSummary(String summaryId) {
        OperationFormMessageAttribute attr = new OperationFormMessageAttribute();
        attr.setId(summaryId);
        this.summary = attr;
        return attr;
    }

    /**
     * Get summary form attribute.
     * @return Summary form attribute.
     */
    public OperationFormMessageAttribute getSummary() {
        return summary;
    }

    /**
     * Set amount.
     * @param amountId Amount ID.
     * @param amount Amount value.
     * @param currencyId Currency ID.
     * @param currency Amount currency.
     * @return Operation amount field attribute.
     * @throws InvalidOperationDataException Thrown in case amount is invalid.
     */
    @JsonIgnore
    public OperationAmountFieldAttribute addAmount(String amountId, BigDecimal amount, String currencyId, String currency) throws InvalidOperationDataException {
        AmountValidator.validateAmount(amount);
        OperationAmountFieldAttribute amountAttr = new OperationAmountFieldAttribute();
        amountAttr.setId(amountId);
        amountAttr.setAmount(amount);
        amountAttr.setCurrencyId(currencyId);
        amountAttr.setCurrency(currency);
        saveAttribute(amountAttr);
        return amountAttr;
    }

    /**
     * Get amount.
     * @return Amount.
     */
    @JsonIgnore
    public OperationAmountFieldAttribute getAmount() {
        List<OperationFormFieldAttribute> amountAttrs = getAttributesByType(OperationFormFieldAttribute.Type.AMOUNT);
        if (amountAttrs.isEmpty()) {
            return null;
        }
        if (amountAttrs.size()>1) {
            throw new IllegalStateException("Multiple attributes of type AMOUNT found");
        }
        return (OperationAmountFieldAttribute) amountAttrs.get(0);
    }

    /**
     * Set localized note.
     * @param noteId Note ID.
     * @param note Localized note.
     * @return Operation note field attribute.
     */
    @JsonIgnore
    public OperationNoteFieldAttribute addNote(String noteId, String note) {
        OperationNoteFieldAttribute attr = new OperationNoteFieldAttribute();
        attr.setId(noteId);
        attr.setNote(note);
        saveAttribute(attr);
        return attr;
    }

    /**
     * Set a formatted note.
     * @param noteId Note ID.
     * @param note Note.
     * @param valueFormatType Value format type.
     * @return Operation note field attribute.
     */
    @JsonIgnore
    public OperationNoteFieldAttribute addNote(String noteId, String note, ValueFormatType valueFormatType) {
        OperationNoteFieldAttribute attr = new OperationNoteFieldAttribute(valueFormatType);
        attr.setId(noteId);
        attr.setNote(note);
        saveAttribute(attr);
        return attr;
    }

    /**
     * Get note.
     * @return Note.
     */
    @JsonIgnore
    public OperationNoteFieldAttribute getNote() {
        List<OperationFormFieldAttribute> attrs = getAttributesByType(OperationFormFieldAttribute.Type.NOTE);
        if (attrs == null) {
            return null;
        }
        if (attrs.size()>1) {
            throw new IllegalStateException("Multiple attributes of type NOTE found");
        }
        return (OperationNoteFieldAttribute) attrs.get(0);
    }

    /**
     * Add a bank account choice.
     * @param id Bank account choice ID.
     * @param bankAccounts List of bank accounts.
     * @param enabled Whether choice is enabled.
     * @param defaultValue Default bank account value.
     * @return Bank account choice field attribute.
     */
    @JsonIgnore
    public OperationBankAccountChoiceFieldAttribute addBankAccountChoice(String id, List<BankAccountDetail> bankAccounts, boolean enabled, String defaultValue) {
        OperationBankAccountChoiceFieldAttribute attr = new OperationBankAccountChoiceFieldAttribute();
        attr.setId(id);
        attr.setBankAccounts(bankAccounts);
        attr.setEnabled(enabled);
        attr.setDefaultValue(defaultValue);
        saveAttribute(attr);
        return attr;
    }

    /**
     * Add a key-value attribute.
     * @param id Attribute ID.
     * @param value Attribute value.
     * @return Key-value field attribute.
     */
    @JsonIgnore
    public OperationKeyValueFieldAttribute addKeyValue(String id, String value) {
        OperationKeyValueFieldAttribute attr = new OperationKeyValueFieldAttribute();
        attr.setId(id);
        attr.setValue(value);
        saveAttribute(attr);
        return attr;
    }

    /**
     * Add a formatted key-value attribute.
     * @param id Attribute ID.
     * @param value Attribute value.
     * @param valueFormatType Value format type.
     * @return Key-value field attribute.
     */
    @JsonIgnore
    public OperationKeyValueFieldAttribute addKeyValue(String id, String value, ValueFormatType valueFormatType) {
        OperationKeyValueFieldAttribute attr = new OperationKeyValueFieldAttribute(valueFormatType);
        attr.setId(id);
        attr.setValue(value);
        saveAttribute(attr);
        return attr;
    }

    /**
     * Add a heading attribute.
     * @param id Attribute ID.
     * @param value Attribute value.
     * @return A heading field attribute.
     */
    @JsonIgnore
    public OperationHeadingFieldAttribute addHeading(String id, String value) {
        OperationHeadingFieldAttribute attr = new OperationHeadingFieldAttribute();
        attr.setId(id);
        attr.setValue(value);
        saveAttribute(attr);
        return attr;
    }

    /**
     * Add a formatted heading attribute.
     * @param id Attribute ID.
     * @param value Attribute value.
     * @param valueFormatType Value format type.
     * @return A heading field attribute.
     */
    @JsonIgnore
    public OperationHeadingFieldAttribute addHeading(String id, String value, ValueFormatType valueFormatType) {
        OperationHeadingFieldAttribute attr = new OperationHeadingFieldAttribute(valueFormatType);
        attr.setId(id);
        attr.setValue(value);
        saveAttribute(attr);
        return attr;
    }

    /**
     * Add party information.
     * @param id Attribute ID.
     * @param partyInfo Party information.
     * @return A party information attribute.
     */
    public OperationPartyInfoFieldAttribute addPartyInfo(String id, PartyInfo partyInfo) {
        OperationPartyInfoFieldAttribute attr = new OperationPartyInfoFieldAttribute();
        attr.setId(id);
        attr.setPartyInfo(partyInfo);
        saveAttribute(attr);
        return attr;
    }

    /**
     * Add a banner above the form.
     * @param bannerType Banner type.
     * @param bannerId Banner ID.
     */
    @JsonIgnore
    public void addBanner(BannerType bannerType, String bannerId) {
        OperationFormBanner banner = new OperationFormBanner(bannerType);
        banner.setId(bannerId);
        banners.add(banner);
    }

    /**
     * Add a banner before a field.
     * @param bannerType Banner type.
     * @param bannerId Banner ID.
     * @param existingField Operation form field attribute before which banner should be added.
     */
    public void addBannerBeforeField(BannerType bannerType, String bannerId, OperationFormFieldAttribute existingField) {
        if (!parameters.contains(existingField)) {
            throw new IllegalArgumentException("Field is missing in operation form data: "+existingField.getId());
        }
        OperationBannerFieldAttribute banner = new OperationBannerFieldAttribute(bannerType);
        banner.setId(bannerId);
        int fieldIndex = parameters.indexOf(existingField);
        parameters.add(fieldIndex, banner);
    }

    /**
     * Get attribute by id.
     * @param id Attribute ID.
     * @return Attribute.
     */
    @JsonIgnore
    public OperationFormFieldAttribute getAttributeById(String id) {
        for (OperationFormFieldAttribute attr: parameters) {
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
    public List<OperationFormFieldAttribute> getAttributesByType(OperationFormFieldAttribute.Type type) {
        List<OperationFormFieldAttribute> attrs = new ArrayList<>();
        for (OperationFormFieldAttribute attr: parameters) {
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
    private void saveAttribute(OperationFormFieldAttribute attributeToSave) {
        if (attributeToSave == null || attributeToSave.getId() == null) {
            throw new IllegalArgumentException("Invalid attribute");
        }
        Integer existingIndex = null;
        int counter = 0;
        for (OperationFormFieldAttribute attr: parameters) {
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
