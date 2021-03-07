package io.getlime.security.powerauth.lib.nextstep.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * Class representing details of a bank account.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
@AllArgsConstructor
public class BankAccountDetail {

    @NotBlank
    private String number;
    private String accountId;
    private String name;
    private BigDecimal balance;
    private String currency;
    private boolean usableForPayment;
    private String unusableForPaymentReason;

}
