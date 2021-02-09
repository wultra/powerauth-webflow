package io.getlime.security.powerauth.lib.nextstep.model.entity;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Class representing details of a bank account.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class BankAccountDetail {

    @NotNull
    private String number;
    @NotNull
    private String accountId;
    private String name;
    @NotNull
    private BigDecimal balance;
    private String currency;
    private boolean usableForPayment;
    private String unusableForPaymentReason;

}
