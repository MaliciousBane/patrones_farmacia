package Junit.adapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.adapter.model.*;
import patrones_farmacia.adapter.controller.PaymentController;

class PaymentControllerTest {

    private PaymentController controller;
    private AdapterPayMethod adapter;

    @BeforeEach
    void setUp() {
        CashMethod cash = new CashMethod(100000);
        CreditCardMethod card = new CreditCardMethod("1234", "Andrés", "999", 200000);
        EWalletMethod wallet = new EWalletMethod("WAL-01", 50000);
        adapter = new AdapterPayMethod(cash, card, wallet);
        controller = new PaymentController(adapter);
    }

    @Test
    void testCashPaymentSuccess() {
        assertTrue(controller.processPayment("CASH", 20000));
    }

    @Test
    void testCardPaymentFailsOverLimit() {
        assertFalse(controller.processPayment("CREDIT", 999999));
    }

    @Test
    void testWalletPayment() {
        assertTrue(controller.processPayment("EWALLET", 30000));
    }
}