package Junit.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.adapter.model.*;
import patrones_farmacia.adapter.controller.PaymentController;
import patrones_farmacia.adapter.view.PaymentConsole;
import java.lang.reflect.Field;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

@DisplayName("Adapter Pattern - Payment Controller & Adapter E2E ampliado")
class PaymentControllerTest {

    private CashMethod cash;
    private EWalletMethod wallet;
    private CreditCardMethod card;
    private AdapterPayMethod adapter;
    private PaymentController controller;
    private PrintStream originalOut;
    private InputStream originalIn;
    private ByteArrayOutputStream baos;

    @BeforeEach
    void setUp() {
        cash = new CashMethod(10000);
        wallet = new EWalletMethod("WAL-001", 30000);
        card = new CreditCardMethod("1234-0000-0000-0000", "Owner", "123", 50000);
        adapter = new AdapterPayMethod(cash, card, wallet);
        controller = new PaymentController(adapter);
        originalOut = System.out;
        originalIn = System.in;
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    private void setAdapterMode(String mode) {
        adapter.setMode(mode);
    }

    @Test
    @DisplayName("Cash pay success reduces cash available")
    void testCashPaymentSuccess() {
        setAdapterMode("CASH");
        assertTrue(adapter.pay(5000));
        assertEquals(5000, cash.getCashAvailable(), 0.01);
    }

    @Test
    @DisplayName("Cash pay exact boundary reduces to zero")
    void testCashExactBoundary() {
        setAdapterMode("CASH");
        assertTrue(adapter.pay(10000));
        assertEquals(0, cash.getCashAvailable(), 0.01);
    }

    @Test
    @DisplayName("Cash pay fails when insufficient funds")
    void testCashPaymentFailInsufficient() {
        cash = new CashMethod(1000);
        adapter = new AdapterPayMethod(cash, card, wallet);
        setAdapterMode("CASH");
        assertFalse(adapter.pay(2000));
        assertEquals(1000, cash.getCashAvailable(), 0.01);
    }

    @Test
    @DisplayName("Cash multiple payments reduce cash sequentially")
    void testCashMultiplePaymentsReduceSequentially() {
        setAdapterMode("CASH");
        assertTrue(adapter.pay(4000));
        assertTrue(adapter.pay(3000));
        assertEquals(3000, cash.getCashAvailable(), 0.01);
    }

    @Test
    @DisplayName("EWallet transfer success and repeated payments until depletion")
    void testEWalletSequenceAndDepletion() {
        setAdapterMode("EWALLET");
        assertTrue(adapter.pay(15000));
        assertTrue(adapter.pay(10000));
        assertFalse(adapter.pay(10000));
    }

    @Test
    @DisplayName("EWallet transfer fails when insufficient funds")
    void testEWalletFailInsufficient() {
        wallet = new EWalletMethod("WAL-002", 100);
        adapter = new AdapterPayMethod(cash, card, wallet);
        setAdapterMode("EWALLET");
        assertFalse(adapter.pay(500));
    }

    @Test
    @DisplayName("Credit card pay success within limit")
    void testCreditCardSuccessWithinLimit() {
        setAdapterMode("CREDIT");
        assertTrue(adapter.pay(40000));
    }

    @Test
    @DisplayName("Credit card pay fails when exceeding limit")
    void testCreditCardFailExceedLimit() {
        setAdapterMode("CREDIT");
        assertFalse(adapter.pay(60000));
    }

    @Test
    @DisplayName("Credit card edge limit accepted exactly")
    void testCreditCardEdgeLimitExact() {
        setAdapterMode("CREDIT");
        assertTrue(adapter.pay(50000));
    }

    @Test
    @DisplayName("Credit card accepts zero amount")
    void testCreditCardZeroPayment() {
        setAdapterMode("CREDIT");
        assertTrue(adapter.pay(0));
    }

    @Test
    @DisplayName("Adapter getName is not empty and matches modes")
    void testAdapterGetName() {
        setAdapterMode("CASH");
        assertEquals("Efectivo", adapter.getName());
        setAdapterMode("CREDIT");
        assertEquals("Tarjeta de Crédito", adapter.getName());
        setAdapterMode("EWALLET");
        assertEquals("E-Wallet", adapter.getName());
    }

    @Test
    @DisplayName("Adapter handles null sub-methods gracefully")
    void testAdapterNullMethods() {
        AdapterPayMethod a = new AdapterPayMethod(null, null, null);
        a.setMode("CASH");
        assertFalse(a.pay(1));
        a.setMode("EWALLET");
        assertFalse(a.pay(1));
        a.setMode("CREDIT");
        assertFalse(a.pay(1));
    }

    @Test
    @DisplayName("Mode selection is case-insensitive (setMode normalizes)")
    void testModeCaseInsensitive() {
        adapter.setMode("cash");
        assertTrue(adapter.pay(1000));
        adapter.setMode("credit");
        assertTrue(adapter.pay(1000));
        adapter.setMode("ewallet");
        assertTrue(adapter.pay(1000));
    }

    @Test
    @DisplayName("Adapter can switch modes and pay repeatedly")
    void testAdapterSwitchModesSequentially() {
        setAdapterMode("CASH");
        assertTrue(adapter.pay(1000));
        setAdapterMode("EWALLET");
        assertTrue(adapter.pay(1000));
        setAdapterMode("CREDIT");
        assertTrue(adapter.pay(1000));
    }

    @Test
    @DisplayName("Invalid mode returns false")
    void testInvalidModeReturnsFalse() throws Exception {
        Field f = adapter.getClass().getDeclaredField("currentMode");
        f.setAccessible(true);
        f.set(adapter, "INVALID");
        assertFalse(adapter.pay(1000));
    }

    @Test
    @DisplayName("Null mode returns false")
    void testNullModeReturnsFalse() throws Exception {
        Field f = adapter.getClass().getDeclaredField("currentMode");
        f.setAccessible(true);
        f.set(adapter, null);
        assertFalse(adapter.pay(1));
        assertEquals("Desconocido", adapter.getName());
    }

    @Test
    @DisplayName("Negative payment amount treated as invalid")
    void testNegativeAmountReturnsFalse() {
        setAdapterMode("CASH");
        assertFalse(adapter.pay(-100));
    }

    @Test
    @DisplayName("PaymentController stores adapter reference")
    void testPaymentControllerAdapterReference() throws Exception {
        Field f = controller.getClass().getDeclaredField("adapter");
        f.setAccessible(true);
        Object inside = f.get(controller);
        assertSame(adapter, inside);
    }

    @Test
    @DisplayName("PaymentController.processPayment uses adapter and returns result")
    void testPaymentControllerProcessPayment() {
        assertTrue(controller.processPayment("CASH", 1000));
        assertFalse(controller.processPayment("CREDIT", 1000000));
    }

    @Test
    @DisplayName("Model getters work as expected")
    void testModelGetters() {
        assertEquals(10000, cash.getCashAvailable(), 0.01);
        assertEquals("WAL-001", wallet.getAccountNumber());
        assertEquals(30000, wallet.getBalance(), 0.01);
        assertEquals("Owner", card.getOwnerName());
        assertEquals(50000, card.getLimit(), 0.01);
        assertEquals("1234-0000-0000-0000", card.getCardNumber());
        assertEquals("123", card.getCVV());
    }

    @Test
    @DisplayName("PaymentConsole.run completes with exit option (simulated input)")
    void testPaymentConsoleRunCompletes() throws Exception {
        String input = "1\n1000\n4\n0\n";
        InputStream oldIn = System.in;
        PrintStream oldOut = System.out;
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
            System.setIn(in);
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos2));
            PaymentConsole pc = new PaymentConsole();
            pc.run();
            String out = baos2.toString();
            assertTrue(out.length() > 0);
        } finally {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }
    }

    @Test
    @DisplayName("Concurrent payments on different adapters do not throw")
    void concurrentPaymentsDoNotThrow() throws Exception {
        ExecutorService es = Executors.newFixedThreadPool(3);
        List<Callable<Boolean>> tasks = new ArrayList<>();
        tasks.add(() -> { AdapterPayMethod a = new AdapterPayMethod(new CashMethod(2000), card, wallet); a.setMode("CASH"); return a.pay(1000); });
        tasks.add(() -> { AdapterPayMethod a = new AdapterPayMethod(cash, new CreditCardMethod("n","o","p",1000), wallet); a.setMode("CREDIT"); return a.pay(500); });
        tasks.add(() -> { AdapterPayMethod a = new AdapterPayMethod(cash, card, new EWalletMethod("ACC",2000)); a.setMode("EWALLET"); return a.pay(1500); });
        List<Future<Boolean>> results = es.invokeAll(tasks);
        for (Future<Boolean> f : results) assertNotNull(f.get());
        es.shutdownNow();
    }

    @Test
    @DisplayName("Adapter prints message on unsupported mode path")
    void adapterPrintsUnsupportedModeMessage() {
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        PrintStream old = System.out;
        try {
            System.setOut(new PrintStream(baos2));
            Field f = adapter.getClass().getDeclaredField("currentMode");
            f.setAccessible(true);
            f.set(adapter, "UNKNOWN_MODE");
            assertFalse(adapter.pay(10));
            String out = baos2.toString();
            assertTrue(out.contains("Modo de pago no soportado"));
        } catch (Exception e) {
            fail("No debe lanzar excepción: " + e.getMessage());
        } finally {
            System.setOut(old);
        }
    }

    @Test
    @DisplayName("Adapter.getName returns Desconocido for null and unknown")
    void adapterGetNameUnknownAndNull() throws Exception {
        Field f = adapter.getClass().getDeclaredField("currentMode");
        f.setAccessible(true);
        f.set(adapter, "UNKNOWN");
        assertEquals("Desconocido", adapter.getName());
        f.set(adapter, null);
        assertEquals("Desconocido", adapter.getName());
    }

    @Test
    @DisplayName("EWallet and Cash state mutated after payments")
    void stateMutationAfterPayments() {
        setAdapterMode("EWALLET");
        assertTrue(adapter.pay(5000));
        assertEquals(25000, wallet.getBalance(), 0.01);
        setAdapterMode("CASH");
        assertTrue(adapter.pay(1000));
        assertEquals(9000, cash.getCashAvailable(), 0.01);
    }

    @Test
    @DisplayName("CashMethod prints message on success")
    void cashMethodPrintsMessageSuccess() {
        CashMethod cm = new CashMethod(5000);
        assertTrue(cm.cashPay(2000));
        String out = baos.toString();
        assertTrue(out.contains("Pago en efectivo realizado"));
    }

    @Test
    @DisplayName("CashMethod prints message on failure")
    void cashMethodPrintsMessageFailure() {
        CashMethod cm = new CashMethod(100);
        assertFalse(cm.cashPay(500));
        String out = baos.toString();
        assertTrue(out.contains("Fondos insuficientes"));
    }

    @Test
    @DisplayName("CreditCardMethod prints message on success")
    void creditCardPrintsMessageSuccess() {
        CreditCardMethod cc = new CreditCardMethod("1111", "Test", "111", 1000);
        assertTrue(cc.makePayment(500));
        String out = baos.toString();
        assertTrue(out.contains("Pago con tarjeta realizado"));
    }

    @Test
    @DisplayName("CreditCardMethod prints message on failure")
    void creditCardPrintsMessageFailure() {
        CreditCardMethod cc = new CreditCardMethod("1111", "Test", "111", 100);
        assertFalse(cc.makePayment(500));
        String out = baos.toString();
        assertTrue(out.contains("Límite de crédito insuficiente"));
    }

    @Test
    @DisplayName("EWalletMethod prints message on success")
    void eWalletPrintsMessageSuccess() {
        EWalletMethod ew = new EWalletMethod("ACC-123", 5000);
        assertTrue(ew.transferCash(2000));
        String out = baos.toString();
        assertTrue(out.contains("Transferencia desde E-Wallet exitosa"));
    }

    @Test
    @DisplayName("EWalletMethod prints message on failure")
    void eWalletPrintsMessageFailure() {
        EWalletMethod ew = new EWalletMethod("ACC-123", 100);
        assertFalse(ew.transferCash(500));
        String out = baos.toString();
        assertTrue(out.contains("Saldo insuficiente"));
    }

    @Test
    @DisplayName("PaymentController prints procesando message")
    void paymentControllerPrintsProcessando() {
        controller.processPayment("CASH", 100);
        String out = baos.toString();
        assertTrue(out.contains("Procesando pago por"));
    }

    @Test
    @DisplayName("Adapter setMode null converts to null")
    void adapterSetModeNull() {
        adapter.setMode(null);
        assertEquals("Desconocido", adapter.getName());
        assertFalse(adapter.pay(100));
    }

    @Test
    @DisplayName("Multiple mode switches maintain state consistency")
    void multipleModeSwitches() {
        for (int i = 0; i < 10; i++) {
            adapter.setMode("CASH");
            assertTrue(adapter.pay(10));
            adapter.setMode("CREDIT");
            assertTrue(adapter.pay(10));
            adapter.setMode("EWALLET");
            assertTrue(adapter.pay(10));
        }
        assertEquals(9700, cash.getCashAvailable(), 0.01);
        assertEquals(9700, card.getLimit(), 0.01);
        assertEquals(29700, wallet.getBalance(), 0.01);
    }

    @Test
    @DisplayName("CreditCard multiple payments reduce limit sequentially")
    void creditCardMultiplePaymentsReduceLimit() {
        CreditCardMethod cc = new CreditCardMethod("1111", "Owner", "111", 1000);
        assertTrue(cc.makePayment(100));
        assertTrue(cc.makePayment(200));
        assertTrue(cc.makePayment(300));
        assertEquals(400, cc.getLimit(), 0.01);
    }

    @Test
    @DisplayName("EWallet multiple transfers reduce balance sequentially")
    void eWalletMultipleTransfersReduceBalance() {
        EWalletMethod ew = new EWalletMethod("ACC-001", 1000);
        assertTrue(ew.transferCash(100));
        assertTrue(ew.transferCash(200));
        assertTrue(ew.transferCash(300));
        assertEquals(400, ew.getBalance(), 0.01);
    }

    @Test
    @DisplayName("AdapterPayMethod with one null payment method fails appropriately")
    void adapterWithOneNullMethod() {
        AdapterPayMethod a = new AdapterPayMethod(new CashMethod(1000), null, null);
        a.setMode("CASH");
        assertTrue(a.pay(500));
        a.setMode("CREDIT");
        assertFalse(a.pay(100));
        a.setMode("EWALLET");
        assertFalse(a.pay(100));
    }

    @Test
    @DisplayName("PaymentController processPayment with invalid mode returns false")
    void paymentControllerInvalidMode() {
        assertFalse(controller.processPayment("INVALID", 1000));
    }

    @Test
    @DisplayName("Payment zero amount always succeeds for credit card")
    void zeroPaymentCreditCard() {
        CreditCardMethod cc = new CreditCardMethod("1111", "Owner", "111", 100);
        assertTrue(cc.makePayment(0));
        assertEquals(100, cc.getLimit(), 0.01);
    }

    @Test
    @DisplayName("Payment zero amount always succeeds for cash")
    void zeroPaymentCash() {
        CashMethod cm = new CashMethod(100);
        assertTrue(cm.cashPay(0));
        assertEquals(100, cm.getCashAvailable(), 0.01);
    }

    @Test
    @DisplayName("Payment zero amount always succeeds for ewallet")
    void zeroPaymentEWallet() {
        EWalletMethod ew = new EWalletMethod("ACC-001", 100);
        assertTrue(ew.transferCash(0));
        assertEquals(100, ew.getBalance(), 0.01);
    }

    @Test
    @DisplayName("Adapter with mixed case mode names works correctly")
    void mixedCaseModeNames() {
        adapter.setMode("CaSh");
        assertTrue(adapter.pay(100));
        adapter.setMode("CrEdIt");
        assertTrue(adapter.pay(100));
        adapter.setMode("eWaLLet");
        assertTrue(adapter.pay(100));
    }

    @Test
    @DisplayName("PaymentController getAdapter returns correct reference")
    void paymentControllerGetAdapter() throws Exception {
        Field f = controller.getClass().getDeclaredField("adapter");
        f.setAccessible(true);
        Object ref = f.get(controller);
        assertSame(adapter, ref);
    }

    @Test
    @DisplayName("Concurrent adapter operations maintain consistency")
    void concurrentAdapterOperations() throws Exception {
        ExecutorService es = Executors.newFixedThreadPool(5);
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            tasks.add(() -> {
                AdapterPayMethod a = new AdapterPayMethod(new CashMethod(100), new CreditCardMethod("c", "c", "c", 100), new EWalletMethod("a", 100));
                a.setMode("CASH");
                a.pay(10);
                a.setMode("CREDIT");
                a.pay(10);
                a.setMode("EWALLET");
                a.pay(10);
                return true;
            });
        }
        List<Future<Boolean>> results = es.invokeAll(tasks);
        for (Future<Boolean> f : results) assertTrue(f.get());
        es.shutdownNow();
    }

    @Test
    @DisplayName("CreditCard edge case payment equal to limit")
    void creditCardPaymentEqualToLimit() {
        CreditCardMethod cc = new CreditCardMethod("1111", "Owner", "111", 500);
        assertTrue(cc.makePayment(500));
        assertEquals(0, cc.getLimit(), 0.01);
    }

    @Test
    @DisplayName("CreditCard payment just over limit fails")
    void creditCardPaymentJustOverLimit() {
        CreditCardMethod cc = new CreditCardMethod("1111", "Owner", "111", 500);
        assertFalse(cc.makePayment(500.01));
        assertEquals(500, cc.getLimit(), 0.01);
    }

    @Test
    @DisplayName("EWallet balance exactly equal to amount succeeds")
    void eWalletExactBalance() {
        EWalletMethod ew = new EWalletMethod("ACC-001", 500);
        assertTrue(ew.transferCash(500));
        assertEquals(0, ew.getBalance(), 0.01);
    }

    @Test
    @DisplayName("EWallet balance just under amount fails")
    void eWalletUnderBalance() {
        EWalletMethod ew = new EWalletMethod("ACC-001", 500);
        assertFalse(ew.transferCash(500.01));
        assertEquals(500, ew.getBalance(), 0.01);
    }
}