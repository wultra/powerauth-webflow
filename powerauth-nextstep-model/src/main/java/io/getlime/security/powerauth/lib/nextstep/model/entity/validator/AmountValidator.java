package io.getlime.security.powerauth.lib.nextstep.model.entity.validator;

import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidOperationDataException;

import java.math.BigDecimal;

/**
 * Validator for amount attribute.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class AmountValidator {

    /**
     * Validate amount attribute.
     * @param amount Amount attribute value.
     * @throws InvalidOperationDataException Thrown in case amount is invalid.
     */
    public static void validateAmount(BigDecimal amount) throws InvalidOperationDataException {
        if (amount == null) {
            throw new InvalidOperationDataException("Amount was not set");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationDataException("Amount is zero or negative: " + amount);
        }
        // Reject amount with more than 2 decimal places.
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new InvalidOperationDataException("Invalid amount: " + amount);
        }
    }
}
