package Integration.adapter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.adapter.model.*;
import patrones_farmacia.adapter.controller.PaymentController;

class AdapterIntegrationTest {

    @Test
    void testAdapterIntegratesMultiplePaymentSystems() {
        CashMethod cash = new CashMethod(50000);
        CreditCardMethod card = new CreditCardMethod("1234-5678", "Cliente Integración", "999", 100000);
        EWalletMethod wallet = new EWalletMethod("WAL-001", 25000);

        AdapterPayMethod adapter = new AdapterPayMethod(cash, card, wallet);
        PaymentController controller = new PaymentController(adapter);

        assertTrue(controller.processPayment("CASH", 20000));
        assertTrue(controller.processPayment("CREDIT", 30000));
        assertTrue(controller.processPayment("EWALLET", 15000));
    }
}