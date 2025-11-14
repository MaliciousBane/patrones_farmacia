package e2e.adapter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.adapter.model.*;
import patrones_farmacia.adapter.controller.PaymentController;

class PaymentFlowE2ETest {

    @Test
    void shouldProcessValidPaymentsAndRejectInvalidOnes() {
        CashMethod cash = new CashMethod(30000);
        CreditCardMethod card = new CreditCardMethod("1111-2222", "Cliente", "123", 20000);
        EWalletMethod wallet = new EWalletMethod("WAL-003", 5000);
        
        AdapterPayMethod adapter = new AdapterPayMethod(cash, card, wallet);
        PaymentController controller = new PaymentController(adapter);

        assertTrue(controller.processPayment("CASH", 10000), 
                   "El pago en efectivo debe exitoso con saldo suficiente");
        assertTrue(controller.processPayment("CREDIT", 15000), 
                   "El pago con tarjeta debe ser exitoso dentro del límite");

        assertFalse(controller.processPayment("EWALLET", 6000), 
                    "El pago con billetera debe fallar por saldo insuficiente");
        assertFalse(controller.processPayment("CREDIT", 50000), 
                    "El pago con tarjeta debe fallar por exceder el límite");
        assertFalse(controller.processPayment("INVALID", 1000), 
                    "El método de pago inválido debe ser rechazado");

        assertEquals(20000, cash.getCashAvailable(), 
                     "El saldo en efectivo debe ser 30000 - 10000");
        assertEquals(5000, card.getLimit(), 
                     "El límite de la tarjeta debe ser 20000 - 15000");
        assertEquals(5000, wallet.getBalance(), 
                     "El saldo de la billetera debe permanecer sin cambios");
    }
}