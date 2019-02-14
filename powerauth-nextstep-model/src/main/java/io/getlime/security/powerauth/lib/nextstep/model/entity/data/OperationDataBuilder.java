package io.getlime.security.powerauth.lib.nextstep.model.entity.data;

import io.getlime.security.powerauth.lib.nextstep.model.entity.OperationData;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidOperationDataException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import static io.getlime.security.powerauth.lib.nextstep.model.entity.OperationData.OPERATION_DATA_ATTRIBUTE_COUNT;

/**
 * Builder for operation data.
 *
 * See <a href='https://github.com/wultra/powerauth-webflow/wiki/Off-line-Signatures-QR-Code#operation-data'>operation data documentation</a>.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OperationDataBuilder {

    private String templateVersion;
    private Integer templateId;
    private Map<Integer, OperationDataAttribute> attributes;

    /**
     * Default constructor.
     */
    public OperationDataBuilder() {
        this.attributes = new TreeMap<>();
    }

    /**
     * Set template version.
     * @param templateVersion Template version.
     * @return Operation data builder.
     */
    public OperationDataBuilder templateVersion(String templateVersion) {
        this.templateVersion = templateVersion;
        return this;
    }

    /**
     * Set template ID.
     * @param templateId Template ID.
     * @return Operation data builder.
     */
    public OperationDataBuilder templateId(int templateId) {
        this.templateId = templateId;
        return this;
    }

    /**
     * Switch to attribute builder for attribute with index 1.
     * @return Operation attribute builder.
     */
    public OperationAttributeBuilder attr1() {
        return new OperationAttributeBuilder(1);
    }

    /**
     * Switch to attribute builder for attribute with index 2.
     * @return Operation attribute builder.
     */
    public OperationAttributeBuilder attr2() {
        return new OperationAttributeBuilder(2);
    }

    /**
     * Switch to attribute builder for attribute with index 3.
     * @return Operation attribute builder.
     */
    public OperationAttributeBuilder attr3() {
        return new OperationAttributeBuilder(3);
    }

    /**
     * Switch to attribute builder for attribute with index 4.
     * @return Operation attribute builder.
     */
    public OperationAttributeBuilder attr4() {
        return new OperationAttributeBuilder(4);
    }

    /**
     * Switch to attribute builder for attribute with index 5.
     * @return Operation attribute builder.
     */
    public OperationAttributeBuilder attr5() {
        return new OperationAttributeBuilder(5);
    }

    /**
     * Build operation data string.
     * @return Operation data string.
     * @throws InvalidOperationDataException Thrown when operation data is invalid.
     */
    public String build() throws InvalidOperationDataException {
        OperationData operationData = new OperationData(templateVersion, templateId);
        for (int i = 1; i <= OPERATION_DATA_ATTRIBUTE_COUNT; i++) {
            operationData.addAttribute(i, attributes.get(i));
        }
        return operationData.formattedValue();
    }

    /**
     * Operation attribute builder.
     */
    public class OperationAttributeBuilder {

        private int attributeId;

        /**
         * Constructor for operation attributes.
         * @param attributeIndex Attribute index.
         */
        OperationAttributeBuilder(int attributeIndex) {
            this.attributeId = attributeIndex;
        }

        /**
         * Set amount and currency.
         * @param amount Amount.
         * @param currency Currency.
         * @return Operation data builder.
         */
        public OperationDataBuilder amount(BigDecimal amount, String currency) throws InvalidOperationDataException {
            OperationAmountAttribute amountField = new OperationAmountAttribute(amount, currency);
            attributes.put(attributeId, amountField);
            return OperationDataBuilder.this;
        }

        /**
         * Set IBAN account.
         * @param iban IBAN account.
         * @return Operation data builder.
         */
        public OperationDataBuilder accountIban(String iban) {
            OperationIbanAccountAttribute accountField = new OperationIbanAccountAttribute(iban);
            attributes.put(attributeId, accountField);
            return OperationDataBuilder.this;
        }

        /**
         * Set generic account.
         * @param account Generic account.
         * @return Operation data builder.
         */
        public OperationDataBuilder accountGeneric(String account) {
            OperationGenericAccountAttribute accountField = new OperationGenericAccountAttribute(account);
            attributes.put(attributeId, accountField);
            return OperationDataBuilder.this;
        }

        /**
         * Set date.
         * @param date Date.
         * @return Operation data builder.
         */
        public OperationDataBuilder date(Date date) {
            OperationDateAttribute dataField = new OperationDateAttribute(date);
            attributes.put(attributeId, dataField);
            return OperationDataBuilder.this;
        }

        /**
         * Set date from string.
         * @param date String with date in format yyyy-MM-dd (month index starts by 1).
         * @return Operation data builder.
         */
        public OperationDataBuilder date(String date) {
            OperationDateAttribute dataField = new OperationDateAttribute(date);
            attributes.put(attributeId, dataField);
            return OperationDataBuilder.this;
        }

        /**
         * Set note.
         * @param note Note.
         * @return Operation data builder.
         */
        public OperationDataBuilder note(String note) {
            OperationNoteAttribute noteField = new OperationNoteAttribute(note);
            attributes.put(attributeId, noteField);
            return OperationDataBuilder.this;
        }

        /**
         * Set reference.
         * @param reference Reference.
         * @return Operation data builder.
         */
        public OperationDataBuilder reference(String reference) {
            OperationReferenceAttribute referenceField = new OperationReferenceAttribute(reference);
            attributes.put(attributeId, referenceField);
            return OperationDataBuilder.this;
        }

        /**
         * Set text.
         * @param text Text.
         * @return Operation data builder.
         */
        public OperationDataBuilder text(String text) {
            OperationNoteAttribute textField = new OperationNoteAttribute(text);
            attributes.put(attributeId, textField);
            return OperationDataBuilder.this;
        }
    }

}
