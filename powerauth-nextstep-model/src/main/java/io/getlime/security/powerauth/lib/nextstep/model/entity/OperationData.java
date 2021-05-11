package io.getlime.security.powerauth.lib.nextstep.model.entity;

import io.getlime.security.powerauth.lib.nextstep.model.entity.data.OperationDataAttribute;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidOperationDataException;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Operation data contains structured data used for signatures.
 *
 * See <a href="https://developers.wultra.com/docs/develop/powerauth-webflow/Operation-Data">operation data documentation</a>.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class OperationData {

    /**
     * Maximum number of attributes for operation data.
     */
    public static final int OPERATION_DATA_ATTRIBUTE_COUNT = 5;

    @NotBlank
    @Size(min = 1, max = 256)
    private final String templateVersion;
    @NotBlank
    @Size(min = 1, max = 256)
    private final Integer templateId;
    @NotNull
    private final Map<Integer, OperationDataAttribute> attributes = new LinkedHashMap<>();

    /**
     * Operation data constructor.
     *
     * @param templateVersion Template version.
     * @param templateId Template ID.
     */
    public OperationData(String templateVersion, Integer templateId) {
        this.templateVersion = templateVersion;
        this.templateId = templateId;
    }

    /**
     * Add operation data attribute.
     * @param attributeIndex Index of attribute starting by 1.
     * @param attribute Operation data attribute.
     */
    public void addAttribute(int attributeIndex, OperationDataAttribute attribute) {
        if (attributeIndex < 1 || attributeIndex > OPERATION_DATA_ATTRIBUTE_COUNT) {
            throw new IllegalArgumentException("Invalid attribute ID, value is expected in range 1-5.");
        }
        attributes.put(attributeIndex, attribute);
    }

    /**
     * Get formatted operation data in a single line asterisk-separated string.
     * @return Formatted operation data.
     * @throws InvalidOperationDataException Thrown when operation data is invalid.
     */
    public String formattedValue() throws InvalidOperationDataException {
        validateOperationData();
        StringBuilder sb = new StringBuilder();
        sb.append(templateVersion);
        sb.append(templateId);
        if (hasMoreAttributes(0)) {
            sb.append("*");
        }
        for (int i = 1; i <= OPERATION_DATA_ATTRIBUTE_COUNT; i++) {
            OperationDataAttribute attr = attributes.get(i);
            if (attr != null) {
                sb.append(attr.formattedValue());
            }
            if (hasMoreAttributes(i)) {
                sb.append("*");
            }
        }
        return sb.toString();
    }

    /**
     * Return whether operation data has any attributes after given attribute index.
     * @param attributeIndex Attribute index.
     * @return True if additional attributes are present, otherwise false.
     */
    private boolean hasMoreAttributes(int attributeIndex) {
        if (attributeIndex == OPERATION_DATA_ATTRIBUTE_COUNT) {
            return false;
        }
        for (int i = attributeIndex + 1; i <= OPERATION_DATA_ATTRIBUTE_COUNT; i++) {
            if (attributes.get(i) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validate operation data.
     */
    private void validateOperationData() throws InvalidOperationDataException {
        if (templateVersion == null) {
            throw new InvalidOperationDataException("Template ID is missing.");
        }
        if (templateId == null) {
            throw new InvalidOperationDataException("Template ID is missing.");
        }
    }
}
