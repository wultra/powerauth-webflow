package io.getlime.security.powerauth.lib.nextstep.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
    @Size(min = 2, max = 256)
    private String number;
    @Size(min = 1, max = 256)
    private String accountId;
    @Size(min = 2, max = 256)
    private String name;
    private BigDecimal balance;
    private String currency;
    @NotNull
    private boolean usableForPayment;
    @Size(min = 2, max = 256)
    private String unusableForPaymentReason;

}
