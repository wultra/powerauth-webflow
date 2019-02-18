package io.getlime.security.powerauth.lib.nextstep.model.entity.data;


import io.getlime.security.powerauth.lib.nextstep.model.exception.InvalidOperationDataException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Roman Strobl, roman.strobl@wultra.com
 */
class OperationDataBuilderTest {

    private OperationDataBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new OperationDataBuilder();
    }

    @Test
    void testEmptyAttributes() throws InvalidOperationDataException {
        String data = builder
                .templateId(1)
                .templateVersion("A")
                .build();
        assertEquals("A1", data);
    }

    @Test
    void testAttributesBasic() throws InvalidOperationDataException {
        Calendar c = Calendar.getInstance();
        c.set(2000, Calendar.JANUARY, 1);
        String data = builder
                .templateId(1)
                .templateVersion("A")
                .attr1().accountGeneric("12345678/0100")
                .attr2().amount(new BigDecimal("100"), "EUR")
                .attr3().date(c.getTime())
                .build();
        assertEquals("A1*Q12345678/0100*A100EUR*D20000101", data);
    }

    @Test
    void testAttributesDateAndChars() throws InvalidOperationDataException {
        String data = builder
                .templateId(2)
                .templateVersion("B")
                .attr1().accountIban("DE89370400440532013000")
                .attr2().amount(new BigDecimal("100.01"), "CZK")
                .attr3().date("2000-01-01")
                .attr4().note("Příliš žluťoučký kůň úpěl ďábelské ódy")
                .build();
        assertEquals("B2*IDE89370400440532013000*A100.01CZK*D20000101*NPříliš žluťoučký kůň úpěl ďábelské ódy", data);
    }

    @Test
    void testAttributesEscaping() throws InvalidOperationDataException {
        String data = builder
                .templateId(3)
                .templateVersion("C")
                .attr1().accountIban("FR7665746739125353006311563")
                .attr2().amount(new BigDecimal("0.01"), "USD")
                .attr3().date("2011-12-31")
                .attr4().note("***text with asterisks,\\slashes,\ttabs and\nnewlines***")
                .build();
        assertEquals("C3*IFR7665746739125353006311563*A0.01USD*D20111231*N\\*\\*\\*text with asterisks,\\\\slashes, tabs and\\nnewlines\\*\\*\\*", data);
    }

    @Test
    void testAttributesOther() throws InvalidOperationDataException {
        String data = builder
                .templateId(3)
                .templateVersion("C")
                .attr1().reference("R1   2/3*4")
                .attr2().text("             -               ")
                .build();
        assertEquals("C3*RR1 2/3\\*4*N - ", data);
    }

    @Test
    void testAttributesSkip1() throws InvalidOperationDataException {
        String data = builder
                .templateId(1)
                .templateVersion("A")
                .attr1().accountGeneric("12345678/0100")
                .attr3().date("2016-06-06")
                .build();
        assertEquals("A1*Q12345678/0100**D20160606", data);
    }

    @Test
    void testAttributesSkip2() throws InvalidOperationDataException {
        String data = builder
                .templateId(1)
                .templateVersion("A")
                .attr1().accountGeneric("12345678/0100")
                .attr5().date("2016-06-06")
                .build();
        assertEquals("A1*Q12345678/0100****D20160606", data);
    }

    @Test
    void testAttributesUpdate() throws InvalidOperationDataException {
        String data = builder
                .templateId(1)
                .templateVersion("A")
                .attr1().date("2016-06-06")
                .attr1().date("2016-06-07")
                .build();
        assertEquals("A1*D20160607", data);
    }

    @Test
    void testDataInvalid1() {
        Assertions.assertThrows(InvalidOperationDataException.class, () -> {
            builder.build();
        });
    }

    @Test
    void testDataInvalid2() {
        Assertions.assertThrows(InvalidOperationDataException.class, () -> {
            builder.templateId(1).build();
        });
    }

    @Test
    void testDataInvalid3() {
        Assertions.assertThrows(InvalidOperationDataException.class, () -> {
            builder.templateVersion("A").build();
        });
    }

    @Test
    void testInvalidAmount() {
        Assertions.assertThrows(InvalidOperationDataException.class, () -> {
            builder
                    .templateId(1)
                    .templateVersion("A")
                    .attr1().accountGeneric("12345678/0100")
                    .attr2().amount(new BigDecimal("0.0099"), "EUR")
                    .build();
        });
    }

    @Test
    void testZeroAmount() {
        Assertions.assertThrows(InvalidOperationDataException.class, () -> {
            builder
                    .templateId(1)
                    .templateVersion("A")
                    .attr1().accountGeneric("12345678/0100")
                    .attr2().amount(BigDecimal.ZERO, "EUR")
                    .build();
        });
    }

    @Test
    void testNegativeAmount() {
        Assertions.assertThrows(InvalidOperationDataException.class, () -> {
            builder
                    .templateId(1)
                    .templateVersion("A")
                    .attr1().accountGeneric("12345678/0100")
                    .attr2().amount(new BigDecimal("-100"), "EUR")
                    .build();
        });
    }
}