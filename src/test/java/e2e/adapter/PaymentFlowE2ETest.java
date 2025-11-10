package e2e.adapter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.adapter.model.*;
import patrones_farmacia.adapter.controller.PaymentController;

class PaymentFlowE2ETest {

    @Test
    void testMultiplePaymentMethods() {
        CashMethod cash = new CashMethod(50000);
        CreditCardMethod card = new CreditCardMethod("1234-5678", "Cliente E2E", "999", 100000);
        EWalletMethod wallet = new EWalletMethod("WAL-001", 30000);
        AdapterPayMethod adapter = new AdapterPayMethod(cash, card, wallet);
        PaymentController controller = new PaymentController(adapter);

        // Pago en efectivo
        assertTrue(controller.processPayment("CASH", 20000));

        // Pago con tarjeta
        assertTrue(controller.processPayment("CREDIT", 50000));

        // Pago con billetera
        assertTrue(controller.processPayment("EWALLET", 10000));
    }
}