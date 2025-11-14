package e2e.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.adapter.model.*;

@DisplayName("Adapter Pattern - Flujo de Pagos E2E")
class PaymentFlowE2ETest {

    private CashMethod cash;
    private CreditCardMethod card;
    private EWalletMethod wallet;
    private AdapterPayMethod adapter;

    @BeforeEach
    void setUp() {
        cash = new CashMethod(30000);
        card = new CreditCardMethod("1111-2222", "Cliente Premium", "123", 20000);
        wallet = new EWalletMethod("WAL-003", 5000);
        
        adapter = new AdapterPayMethod(cash, card, wallet);
    }

    // ============ PRUEBAS BÁSICAS DE VALIDACIÓN ============

    @Test
    @DisplayName("Debe procesar pago en efectivo exitosamente con saldo suficiente")
    void testProcessCashPaymentSuccessful() {
        adapter.setMode("CASH");
        boolean result = adapter.pay(10000);
        
        assertTrue(result, "El pago en efectivo debe ser exitoso");
        assertEquals(20000, cash.getCashAvailable(), "El saldo debe reducirse correctamente");
    }

    @Test
    @DisplayName("Debe procesar pago con tarjeta exitosamente dentro del límite")
    void testProcessCreditCardPaymentSuccessful() {
        adapter.setMode("CREDIT");
        boolean result = adapter.pay(15000);
        
        assertTrue(result, "El pago con tarjeta debe ser exitoso");
        assertEquals(5000, card.getLimit(), "El límite debe reducirse correctamente");
    }

    @Test
    @DisplayName("Debe procesar pago con billetera digital exitosamente")
    void testProcessEWalletPaymentSuccessful() {
        adapter.setMode("EWALLET");
        boolean result = adapter.pay(3000);
        
        assertTrue(result, "El pago con billetera debe ser exitoso");
        assertEquals(2000, wallet.getBalance(), "El saldo debe reducirse correctamente");
    }

    // ============ PRUEBAS DE RECHAZO POR SALDO INSUFICIENTE ============

    @Test
    @DisplayName("Debe rechazar pago en efectivo por saldo insuficiente")
    void testRejectCashPaymentInsufficientBalance() {
        adapter.setMode("CASH");
        boolean result = adapter.pay(35000);
        
        assertFalse(result, "Debe rechazar pago que excede el saldo en efectivo");
        assertEquals(30000, cash.getCashAvailable(), "El saldo no debe cambiar");
    }

    @Test
    @DisplayName("Debe rechazar pago con billetera por saldo insuficiente")
    void testRejectEWalletPaymentInsufficientBalance() {
        adapter.setMode("EWALLET");
        boolean result = adapter.pay(6000);
        
        assertFalse(result, "Debe rechazar pago que excede el saldo de la billetera");
        assertEquals(5000, wallet.getBalance(), "El saldo no debe cambiar");
    }

    @Test
    @DisplayName("Debe rechazar pago con tarjeta por límite excedido")
    void testRejectCreditCardPaymentLimitExceeded() {
        adapter.setMode("CREDIT");
        boolean result = adapter.pay(50000);
        
        assertFalse(result, "Debe rechazar pago que excede el límite de la tarjeta");
        assertEquals(20000, card.getLimit(), "El límite no debe cambiar");
    }

    // ============ PRUEBAS DE MÉTODOS INVÁLIDOS ============

    @Test
    @DisplayName("Debe rechazar método de pago inválido")
    void testRejectInvalidPaymentMethod() {
        adapter.setMode("INVALID");
        boolean result = adapter.pay(1000);
        
        assertFalse(result, "Debe rechazar método de pago no reconocido");
        assertEquals(30000, cash.getCashAvailable(), "Fondos no deben afectarse");
    }

    @Test
    @DisplayName("Debe rechazar método de pago con cadena vacía")
    void testRejectEmptyPaymentMethod() {
        adapter.setMode("");
        boolean result = adapter.pay(1000);
        
        assertFalse(result, "Debe rechazar método vacío");
    }

    @Test
    @DisplayName("Debe retornar nombre correcto del método de pago")
    void testGetPaymentMethodName() {
        adapter.setMode("CASH");
        assertEquals("Efectivo", adapter.getName(), "Debe retornar nombre en español");
        
        adapter.setMode("CREDIT");
        assertEquals("Tarjeta de Crédito", adapter.getName(), "Debe retornar nombre de tarjeta");
        
        adapter.setMode("EWALLET");
        assertEquals("E-Wallet", adapter.getName(), "Debe retornar nombre de billetera");
    }

    // ============ PRUEBAS DE MONTOS ============

    @Test
    @DisplayName("Debe rechazar pago con monto negativo")
    void testRejectNegativePaymentAmount() {
        adapter.setMode("CASH");
        boolean result = adapter.pay(-5000);
        
        assertFalse(result, "Debe rechazar monto negativo");
        assertEquals(30000, cash.getCashAvailable(), "El saldo no debe cambiar");
    }

    @Test
    @DisplayName("Debe rechazar pago con monto cero")
    void testRejectZeroPaymentAmount() {
        adapter.setMode("CASH");
        boolean result = adapter.pay(0);
        
        assertFalse(result, "Debe rechazar monto cero");
        assertEquals(30000, cash.getCashAvailable(), "El saldo no debe cambiar");
    }

    @Test
    @DisplayName("Debe procesar pago exacto al saldo disponible")
    void testProcessPaymentExactBalance() {
        adapter.setMode("CASH");
        boolean result = adapter.pay(30000);
        
        assertTrue(result, "Debe procesar pago exacto al saldo");
        assertEquals(0, cash.getCashAvailable(), "El saldo debe ser cero");
    }

    @Test
    @DisplayName("Debe procesar pago con monto muy pequeño")
    void testProcessMinimalPaymentAmount() {
        adapter.setMode("CASH");
        boolean result = adapter.pay(1);
        
        assertTrue(result, "Debe procesar monto mínimo");
        assertEquals(29999, cash.getCashAvailable(), "Debe reducir por 1");
    }

    // ============ PRUEBAS DE MÚLTIPLES TRANSACCIONES ============

    @Test
    @DisplayName("Debe procesar múltiples pagos en efectivo secuencialmente")
    void testMultipleSequentialCashPayments() {
        adapter.setMode("CASH");
        
        assertTrue(adapter.pay(5000), "Primer pago debe exitoso");
        assertEquals(25000, cash.getCashAvailable(), "Saldo después de primer pago");
        
        assertTrue(adapter.pay(10000), "Segundo pago debe exitoso");
        assertEquals(15000, cash.getCashAvailable(), "Saldo después de segundo pago");
        
        assertTrue(adapter.pay(15000), "Tercer pago debe exitoso");
        assertEquals(0, cash.getCashAvailable(), "Saldo final debe ser cero");
    }

    @Test
    @DisplayName("Debe procesar múltiples pagos con diferentes métodos")
    void testMultiplePaymentMethodsSequential() {
        adapter.setMode("CASH");
        assertTrue(adapter.pay(5000), "Pago en efectivo debe exitoso");
        assertEquals(25000, cash.getCashAvailable());
        
        adapter.setMode("CREDIT");
        assertTrue(adapter.pay(8000), "Pago con tarjeta debe exitoso");
        assertEquals(12000, card.getLimit());
        
        adapter.setMode("EWALLET");
        assertTrue(adapter.pay(2000), "Pago con billetera debe exitoso");
        assertEquals(3000, wallet.getBalance());
    }

    @Test
    @DisplayName("Debe mantener independencia entre métodos de pago")
    void testPaymentMethodsIndependence() {
        adapter.setMode("CASH");
        adapter.pay(5000);
        assertEquals(25000, cash.getCashAvailable());
        
        adapter.setMode("CREDIT");
        adapter.pay(3000);
        assertEquals(17000, card.getLimit());
        
        adapter.setMode("CASH");
        assertEquals(25000, cash.getCashAvailable(), "Cash no afectado por tarjeta");
        
        adapter.setMode("CREDIT");
        assertEquals(17000, card.getLimit(), "Tarjeta no afectada por efectivo");
        
        adapter.setMode("EWALLET");
        assertEquals(5000, wallet.getBalance(), "Billetera no debe cambiar");
    }

    @Test
    @DisplayName("Debe rechazar pago después de agotar saldo")
    void testRejectPaymentAfterBalanceExhausted() {
        adapter.setMode("CASH");
        
        assertTrue(adapter.pay(30000), "Debe procesar pago completo");
        assertEquals(0, cash.getCashAvailable(), "Saldo debe ser cero");
        
        boolean result = adapter.pay(1);
        assertFalse(result, "Debe rechazar pago cuando saldo es cero");
        assertEquals(0, cash.getCashAvailable(), "Saldo debe permanecer en cero");
    }

    // ============ PRUEBAS DE INTEGRIDAD DE DATOS ============

    @Test
    @DisplayName("Debe preservar información de la tarjeta después del pago")
    void testPreserveCardInformationAfterPayment() {
        String cardNumberBefore = card.getCardNumber();
        String cardHolderBefore = card.getOwnerName();
        String cardCVVBefore = card.getCVV();
        
        adapter.setMode("CREDIT");
        adapter.pay(5000);
        
        assertEquals(cardNumberBefore, card.getCardNumber(), "Número de tarjeta debe preservarse");
        assertEquals(cardHolderBefore, card.getOwnerName(), "Titular debe preservarse");
        assertEquals(cardCVVBefore, card.getCVV(), "CVV debe preservarse");
    }

    @Test
    @DisplayName("Debe preservar información de la billetera digital después del pago")
    void testPreserveWalletInformationAfterPayment() {
        String walletIdBefore = wallet.getAccountNumber();
        
        adapter.setMode("EWALLET");
        adapter.pay(1000);
        
        assertEquals(walletIdBefore, wallet.getAccountNumber(), "ID de billetera debe preservarse");
    }

    // ============ PRUEBAS DE FLUJO COMPLETO ============

    @Test
    @DisplayName("Debe procesar flujo completo: efectivo -> tarjeta -> billetera")
    void testCompletePaymentFlow() {
        // Fase 1: Pago en efectivo
        adapter.setMode("CASH");
        assertTrue(adapter.pay(8000), "Fase 1: Efectivo");
        assertEquals(22000, cash.getCashAvailable());
        
        // Fase 2: Pago con tarjeta
        adapter.setMode("CREDIT");
        assertTrue(adapter.pay(12000), "Fase 2: Tarjeta");
        assertEquals(8000, card.getLimit());
        
        // Fase 3: Pago con billetera
        adapter.setMode("EWALLET");
        assertTrue(adapter.pay(4000), "Fase 3: Billetera");
        assertEquals(1000, wallet.getBalance());
        
        // Verificar estado final
        assertEquals(22000, cash.getCashAvailable(), "Efectivo final correcto");
        assertEquals(8000, card.getLimit(), "Tarjeta final correcta");
        assertEquals(1000, wallet.getBalance(), "Billetera final correcta");
    }

    @Test
    @DisplayName("Debe recuperarse de fallos en pagos")
    void testRecoveryFromPaymentFailures() {
        adapter.setMode("CASH");
        
        // Intento fallido
        assertFalse(adapter.pay(35000), "Debe fallar por saldo insuficiente");
        assertEquals(30000, cash.getCashAvailable(), "Saldo intacto después del fallo");
        
        // Pago exitoso posterior
        assertTrue(adapter.pay(10000), "Debe procesar después del fallo");
        assertEquals(20000, cash.getCashAvailable(), "Saldo correcto después del fallo");
    }

    // ============ PRUEBAS DE VALIDACIÓN DE TARJETA ============

    @Test
    @DisplayName("Debe validar formato de número de tarjeta")
    void testCreditCardNumberValidation() {
        assertNotNull(card.getCardNumber(), "Número de tarjeta no debe ser nulo");
        assertTrue(card.getCardNumber().contains("-"), "Número debe estar formateado");
        assertEquals("1111-2222", card.getCardNumber(), "Formato correcto");
    }

    @Test
    @DisplayName("Debe validar CVV de tarjeta")
    void testCreditCardCVVValidation() {
        assertNotNull(card.getCVV(), "CVV no debe ser nulo");
        assertEquals("123", card.getCVV(), "CVV debe coincidir");
    }

    @Test
    @DisplayName("Debe validar titular de tarjeta")
    void testCreditCardOwnerValidation() {
        assertNotNull(card.getOwnerName(), "Titular no debe ser nulo");
        assertEquals("Cliente Premium", card.getOwnerName(), "Titular debe coincidir");
    }

    // ============ PRUEBAS DE CAMBIO DE MODO ============

    @Test
    @DisplayName("Debe cambiar de modo correctamente")
    void testPaymentModeSwitch() {
        adapter.setMode("CASH");
        assertEquals("Efectivo", adapter.getName(), "Debe estar en modo CASH");
        
        adapter.setMode("CREDIT");
        assertEquals("Tarjeta de Crédito", adapter.getName(), "Debe estar en modo CREDIT");
        
        adapter.setMode("EWALLET");
        assertEquals("E-Wallet", adapter.getName(), "Debe estar en modo EWALLET");
    }

    @Test
    @DisplayName("Debe ser case-insensitive en el modo de pago")
    void testPaymentModeCaseInsensitive() {
        adapter.setMode("cash");
        boolean result = adapter.pay(1000);
        assertTrue(result, "Debe procesar 'cash' en minúsculas");
        
        adapter.setMode("CREDIT");
        adapter.setMode("credit");
        result = adapter.pay(1000);
        assertTrue(result, "Debe procesar 'credit' en minúsculas");
    }

    // ============ PRUEBAS DE ESTRÉS ============

    @Test
    @DisplayName("Debe procesar múltiples transacciones pequeñas")
    void testMultipleSmallTransactions() {
        adapter.setMode("CASH");
        
        for (int i = 0; i < 10; i++) {
            assertTrue(adapter.pay(1000), "Transacción " + i + " debe exitosa");
        }
        
        assertEquals(20000, cash.getCashAvailable(), "Debe haber procesado 10 transacciones de 1000");
    }

    @Test
    @DisplayName("Debe procesar pagos en todos los métodos simultáneamente")
    void testAllPaymentMethodsWork() {
        // Verificar que cada método funciona
        adapter.setMode("CASH");
        assertTrue(adapter.pay(100), "CASH debe funcionar");
        
        adapter.setMode("CREDIT");
        assertTrue(adapter.pay(100), "CREDIT debe funcionar");
        
        adapter.setMode("EWALLET");
        assertTrue(adapter.pay(100), "EWALLET debe funcionar");
        
        // Verificar saldos
        assertEquals(29900, cash.getCashAvailable());
        assertEquals(19900, card.getLimit());
        assertEquals(4900, wallet.getBalance());
    }
}