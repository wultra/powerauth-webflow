package io.getlime.security.powerauth.lib.dataadapter.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.getlime.security.powerauth.lib.dataadapter.model.entity.attribute.*;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.BannerType;
import io.getlime.security.powerauth.lib.dataadapter.model.enumeration.ValueFormatType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Operation form data represents data visible to the user during the operation and collected responses.
 * @author Petr Dvorak, petr@wultra.com
 */
public class FormData {

    private MessageAttribute title;
    private MessageAttribute greeting;
    private MessageAttribute summary;
    private List<FormFieldConfig> config;
    private List<FormBanner> banners;
    private List<Attribute> parameters;
    private Map<String, String> userInput;

    /**
     * Default constructor.
     */
    public FormData() {
        this.config = new ArrayList<>();
        this.parameters = new ArrayList<>();
        this.banners = new ArrayList<>();
        this.userInput = new LinkedHashMap<>();
    }

    /**
     * Get form configuration.
     * @return Form configuration.
     */
    public List<FormFieldConfig> getConfig() {
        return config;
    }

    /**
     * Get form attributes.
     * @return Form attributes.
     */
    public List<Attribute> getParameters() {
        return parameters;
    }

    /**
     * Get form banners.
     * @return Form banners.
     */
    public List<FormBanner> getBanners() {
        return banners;
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
    public void setTitle(MessageAttribute title) {
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
    public MessageAttribute addTitle(String titleId) {
        MessageAttribute attr = new MessageAttribute();
        attr.setId(titleId);
        this.title = attr;
        return attr;
    }

    /**
     * Get title form attribute.
     * @return Title form attribute.
     */
    public MessageAttribute getTitle() {
        return title;
    }

    /**
     * Set greeting form attribute.
     * @param greeting Greeting form attribute.
     */
    public void setGreeting(MessageAttribute greeting) {
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
    public MessageAttribute addGreeting(String greetingId) {
        MessageAttribute attr = new MessageAttribute();
        attr.setId(greetingId);
        this.greeting = attr;
        return attr;
    }

    /**
     * Get summary form attribute.
     * @return Summary form attribute.
     */
    public MessageAttribute getGreeting() {
        return greeting;
    }

    /**
     * Set summary form attribute.
     * @param summary Summary form attribute.
     */
    public void setSummary(MessageAttribute summary) {
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
    public MessageAttribute addSummary(String summaryId) {
        MessageAttribute attr = new MessageAttribute();
        attr.setId(summaryId);
        this.summary = attr;
        return attr;
    }

    /**
     * Get summary form attribute.
     * @return Summary form attribute.
     */
    public MessageAttribute getSummary() {
        return summary;
    }


    /**
     * Add an amount.
     * @param attrId Attribute ID.
     * @param amount Amount value.
     * @param currencyId Currency ID.
     * @param currency Amount currency.
     * @return Operation amount field attribute.
     */
    @JsonIgnore
    public AmountAttribute addAmount(String attrId, BigDecimal amount, String currencyId, String currency) {
        AmountAttribute amountAttr = new AmountAttribute();
        amountAttr.setId(attrId);
        amountAttr.setAmount(amount);
        amountAttr.setCurrencyId(currencyId);
        amountAttr.setCurrency(currency);
        saveAttribute(amountAttr);
        return amountAttr;
    }

    /**
     * Add an amount before existing field.
     * @param attrId Attribute ID.
     * @param amount Amount value.
     * @param currencyId Currency ID.
     * @param currency Amount currency.
     * @param existingField Existing field.
     * @return Operation amount field attribute.
     */
    @JsonIgnore
    public AmountAttribute addAmountBeforeField(String attrId, BigDecimal amount, String currencyId, String currency, Attribute existingField) {
        AmountAttribute amountAttr = new AmountAttribute();
        amountAttr.setId(attrId);
        amountAttr.setAmount(amount);
        amountAttr.setCurrencyId(currencyId);
        amountAttr.setCurrency(currency);
        addAttributeBeforeField(amountAttr, existingField);
        return amountAttr;
    }

    /**
     * Get amount.
     * @return Amount.
     */
    @JsonIgnore
    public AmountAttribute getAmount() {
        List<Attribute> amountAttrs = getAttributesByType(Attribute.Type.AMOUNT);
        if (amountAttrs.isEmpty()) {
            return null;
        }
        if (amountAttrs.size()>1) {
            throw new IllegalStateException("Multiple attributes of type AMOUNT found");
        }
        return (AmountAttribute) amountAttrs.get(0);
    }

    /**
     * Add localized note.
     * @param attrId Attribute ID.
     * @param note Localized note.
     * @return Operation note field attribute.
     */
    @JsonIgnore
    public NoteAttribute addNote(String attrId, String note) {
        NoteAttribute attr = new NoteAttribute();
        attr.setId(attrId);
        attr.setNote(note);
        saveAttribute(attr);
        return attr;
    }

    /**
     * Add localized note before existing field.
     * @param attrId Attribute ID.
     * @param note Localized note.
     * @param existingField Existing field.
     * @return Operation note field attribute.
     */
    @JsonIgnore
    public NoteAttribute addNoteBeforeField(String attrId, String note, Attribute existingField) {
        NoteAttribute noteAttr = new NoteAttribute();
        noteAttr.setId(attrId);
        noteAttr.setNote(note);
        addAttributeBeforeField(noteAttr, existingField);
        return noteAttr;
    }

    /**
     * Add a formatted note.
     * @param noteId Attribute ID.
     * @param note Note.
     * @param valueFormatType Value format type.
     * @return Operation note field attribute.
     */
    @JsonIgnore
    public NoteAttribute addNote(String noteId, String note, ValueFormatType valueFormatType) {
        NoteAttribute attr = new NoteAttribute(valueFormatType);
        attr.setId(noteId);
        attr.setNote(note);
        saveAttribute(attr);
        return attr;
    }

    /**
     * Add a formatted note before existing field.
     * @param attrId Attribute ID.
     * @param note Note.
     * @param valueFormatType Value format type.
     * @param existingField Existing field.
     * @return Operation note field attribute.
     */
    @JsonIgnore
    public NoteAttribute addNoteBeforeField(String attrId, String note, ValueFormatType valueFormatType, Attribute existingField) {
        NoteAttribute noteAttr = new NoteAttribute(valueFormatType);
        noteAttr.setId(attrId);
        noteAttr.setNote(note);
        addAttributeBeforeField(noteAttr, existingField);
        return noteAttr;
    }

    /**
     * Get note.
     * @return Note.
     */
    @JsonIgnore
    public NoteAttribute getNote() {
        List<Attribute> attrs = getAttributesByType(Attribute.Type.NOTE);
        if (attrs == null) {
            return null;
        }
        if (attrs.size()>1) {
            throw new IllegalStateException("Multiple attributes of type NOTE found");
        }
        return (NoteAttribute) attrs.get(0);
    }

    /**
     * Add a bank account choice.
     * @param attrId Attribute ID.
     * @param bankAccounts List of bank accounts.
     * @param enabled Whether choice is enabled.
     * @param defaultValue Default bank account value.
     * @return Bank account choice field attribute.
     */
    @JsonIgnore
    public BankAccountChoiceAttribute addBankAccountChoice(String attrId, List<BankAccount> bankAccounts, boolean enabled, String defaultValue) {
        BankAccountChoiceAttribute attr = new BankAccountChoiceAttribute();
        attr.setId(attrId);
        attr.setBankAccounts(bankAccounts);
        attr.setEnabled(enabled);
        attr.setDefaultValue(defaultValue);
        saveAttribute(attr);
        return attr;
    }

    /**
     * Add a bank account choice before existing field.
     * @param attrId Attribute ID.
     * @param bankAccounts List of bank accounts.
     * @param enabled Whether choice is enabled.
     * @param defaultValue Default bank account value.
     * @param existingField Existing field.
     * @return Bank account choice field attribute.
     */
    @JsonIgnore
    public BankAccountChoiceAttribute addBankAccountChoiceBeforeField(String attrId, List<BankAccount> bankAccounts, boolean enabled, String defaultValue, Attribute existingField) {
        BankAccountChoiceAttribute attr = new BankAccountChoiceAttribute();
        attr.setId(attrId);
        attr.setBankAccounts(bankAccounts);
        attr.setEnabled(enabled);
        attr.setDefaultValue(defaultValue);
        addAttributeBeforeField(attr, existingField);
        return attr;
    }

    /**
     * Add a key-value attribute.
     * @param attrId Attribute ID.
     * @param value Attribute value.
     * @return Key-value field attribute.
     */
    @JsonIgnore
    public KeyValueAttribute addKeyValue(String attrId, String value) {
        KeyValueAttribute attr = new KeyValueAttribute();
        attr.setId(attrId);
        attr.setValue(value);
        saveAttribute(attr);
        return attr;
    }

    /**
     * Add a key-value attribute before an existing field.
     * @param attrId Attribute ID.
     * @param value Attribute value.
     * @param existingField Existing field.
     * @return Key-value field attribute.
     */
    @JsonIgnore
    public KeyValueAttribute addKeyValueBeforeField(String attrId, String value, Attribute existingField) {
        KeyValueAttribute keyValueAttr = new KeyValueAttribute();
        keyValueAttr.setId(attrId);
        keyValueAttr.setValue(value);
        addAttributeBeforeField(keyValueAttr, existingField);
        return keyValueAttr;
    }

    /**
     * Add a formatted key-value attribute.
     * @param attrId Attribute ID.
     * @param value Attribute value.
     * @param valueFormatType Value format type.
     * @return Key-value field attribute.
     */
    @JsonIgnore
    public KeyValueAttribute addKeyValue(String attrId, String value, ValueFormatType valueFormatType) {
        KeyValueAttribute attr = new KeyValueAttribute(valueFormatType);
        attr.setId(attrId);
        attr.setValue(value);
        saveAttribute(attr);
        return attr;
    }

    /**
     * Add a formatted key-value attribute before existing field.
     * @param attrId Attribute ID.
     * @param value Attribute value.
     * @param valueFormatType Value format type.
     * @param existingField Existing field.
     * @return Key-value field attribute.
     */
    @JsonIgnore
    public KeyValueAttribute addKeyValueBeforeField(String attrId, String value, ValueFormatType valueFormatType, Attribute existingField) {
        KeyValueAttribute keyValueAttr = new KeyValueAttribute(valueFormatType);
        keyValueAttr.setId(attrId);
        keyValueAttr.setValue(value);
        addAttributeBeforeField(keyValueAttr, existingField);
        return keyValueAttr;
    }

    /**
     * Add a banner above the form.
     * @param bannerType Banner type.
     * @param bannerId Banner ID.
     * @return Form banner.
     */
    @JsonIgnore
    public FormBanner addBanner(BannerType bannerType, String bannerId) {
        FormBanner banner = new FormBanner(bannerType);
        banner.setId(bannerId);
        banners.add(banner);
        return banner;
    }

    /**
     * Add a banner before a field.
     * @param bannerType Banner type.
     * @param attrId Attribute ID.
     * @param existingField Operation form field attribute before which banner should be added.
     * @return Banner attribute.
     */
    public BannerAttribute addBannerBeforeField(BannerType bannerType, String attrId, Attribute existingField) {
        BannerAttribute bannerAttr = new BannerAttribute(bannerType);
        bannerAttr.setId(attrId);
        addAttributeBeforeField(bannerAttr, existingField);
        return bannerAttr;
    }

    /**
     * Add party information.
     * @param attrId Attribute ID.
     * @param partyInfo Party information.
     * @return Party information.
     */
    @JsonIgnore
    public PartyInfoAttribute addPartyInfo(String attrId, PartyInfo partyInfo) {
        PartyInfoAttribute partyInfoAttr = new PartyInfoAttribute(attrId, partyInfo);
        saveAttribute(partyInfoAttr);
        return partyInfoAttr;
    }

    /**
     * Add party information before an existing field.
     * @param attrId Attribute ID.
     * @param partyInfo Party information.
     * @param existingField Existing field.
     * @return Party information attribute.
     */
    @JsonIgnore
    public PartyInfoAttribute addPartyInfoBeforeField(String attrId, PartyInfo partyInfo, Attribute existingField) {
        PartyInfoAttribute partyInfoAttr = new PartyInfoAttribute(attrId, partyInfo);
        addAttributeBeforeField(partyInfoAttr, existingField);
        return partyInfoAttr;
    }

    /**
     * Get attribute by id.
     * @param id Attribute ID.
     * @return Attribute.
     */
    @JsonIgnore
    public Attribute getAttributeById(String id) {
        for (Attribute attr: parameters) {
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
    public List<Attribute> getAttributesByType(Attribute.Type type) {
        List<Attribute> attrs = new ArrayList<>();
        for (Attribute attr: parameters) {
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
     * Add attribute or update existing attribute based on its ID.
     * @param attrToSave Attribute to save.
     */
    private void saveAttribute(Attribute attrToSave) {
        if (attrToSave == null || attrToSave.getId() == null) {
            throw new IllegalArgumentException("Invalid attribute");
        }
        Integer existingIndex = null;
        int counter = 0;
        for (Attribute attr: parameters) {
            if (attr.getId().equals(attrToSave.getId())) {
                existingIndex = counter;
                break;
            }
            counter++;
        }
        if (existingIndex != null) {
            parameters.set(existingIndex, attrToSave);
        } else {
            parameters.add(attrToSave);
        }
    }

    /**
     * Add attribute before existing field.
     * @param addedAttr Added attribute.
     * @param existingField Existing field.
     */
    private void addAttributeBeforeField(Attribute addedAttr, Attribute existingField) {
        if (getAttributeById(addedAttr.getId()) != null) {
            throw new IllegalArgumentException("Field is already present in operation form data: "+addedAttr.getId());
        }
        if (!parameters.contains(existingField)) {
            throw new IllegalArgumentException("Field is missing in operation form data: "+existingField.getId());
        }
        int fieldIndex = parameters.indexOf(existingField);
        parameters.add(fieldIndex, addedAttr);
    }
}
