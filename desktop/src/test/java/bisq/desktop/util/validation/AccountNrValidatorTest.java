package bisq.desktop.util.validation;

import bisq.core.locale.Res;
import bisq.core.payment.validation.AccountNrValidator;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AccountNrValidatorTest {

    @Before
    public void setup() {
        Locale.setDefault(new Locale("en", "US"));
        Res.setBaseCurrencyCode("XMR");
        Res.setBaseCurrencyName("Monero");
    }

    @Test
    public void testValidationForArgentina() {
        AccountNrValidator validator = new AccountNrValidator("AR");

        assertTrue(validator.validate("4009041813520").isValid);
        assertTrue(validator.validate("035-005198/5").isValid);
    }
}
