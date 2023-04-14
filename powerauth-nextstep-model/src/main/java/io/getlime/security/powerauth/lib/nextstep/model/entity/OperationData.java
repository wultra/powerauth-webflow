/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2018 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.getlime.security.powerauth.lib.nextstep.model.entity;

import io.getlime.security.powerauth.lib.nextstep.model.entity.data.OperationDataAttribute;
import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidOperationDataException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

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
