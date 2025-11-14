package Integration.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.adapter.model.*;
import patrones_farmacia.adapter.controller.PaymentController;

@DisplayName("Pruebas de Integración del Patrón Adapter para Pagos - Mejoradas")
class AdapterIntegrationTest {

    private PaymentController controlador;
    private AdapterPayMethod adaptador;
    private CashMethod metodoEfectivo;
    private CreditCardMethod metodoTarjeta;
    private EWalletMethod metodoBilletera;

    @BeforeEach
    void configurarSistemaDePagos() {
        metodoEfectivo = new CashMethod(30000);
        metodoTarjeta = new CreditCardMethod("1111-2222", "Cliente Test", "999", 20000);
        metodoBilletera = new EWalletMethod("WAL-123", 8000);
        adaptador = new AdapterPayMethod(metodoEfectivo, metodoTarjeta, metodoBilletera);
        controlador = new PaymentController(adaptador);
    }

    @Test
    @DisplayName("Procesa pagos válidos con todos los métodos y actualiza saldos")
    void debeProcesarPagosConTodosLosMetodosValidosYActualizarSaldos() {
        assertTrue(controlador.processPayment("EWALLET", 8000));
        assertTrue(controlador.processPayment("CASH", 10000));
        assertTrue(controlador.processPayment("CREDIT", 15000));
        assertEquals(20000.0, metodoEfectivo.getCashAvailable(), 0.001);
        assertEquals(5000.0, metodoTarjeta.getLimit(), 0.001);
        assertEquals(0.0, metodoBilletera.getBalance(), 0.001);
    }

    @Test
    @DisplayName("Rechaza pagos que exceden límites disponibles")
    void debeRechazarPagosQueExcedenLimites() {
        assertFalse(controlador.processPayment("EWALLET", 10000));
        assertFalse(controlador.processPayment("CREDIT", 50000));
        assertFalse(controlador.processPayment("CASH", 40000));
        assertEquals(30000.0, metodoEfectivo.getCashAvailable(), 0.001);
        assertEquals(20000.0, metodoTarjeta.getLimit(), 0.001);
        assertEquals(8000.0, metodoBilletera.getBalance(), 0.001);
    }

    @Test
    @DisplayName("Rechaza métodos de pago no reconocidos")
    void debeRechazarMetodosDePagoInvalidos() {
        assertFalse(controlador.processPayment("XXX", 500));
        assertFalse(controlador.processPayment("", 100));
    }

    @Test
    @DisplayName("Permite múltiples transacciones consecutivas y mantiene saldos correctos")
    void debeManejarMultiplesTransaccionesConsecutivas() {
        assertTrue(controlador.processPayment("CASH", 5000));
        assertTrue(controlador.processPayment("CASH", 8000));
        assertTrue(controlador.processPayment("CREDIT", 10000));
        assertEquals(17000.0, metodoEfectivo.getCashAvailable(), 0.001);
        assertEquals(10000.0, metodoTarjeta.getLimit(), 0.001);
    }

    @Test
    @DisplayName("Soporta modos en minúsculas y diferentes capitalizaciones")
    void debeSoportarModoEnMinusculas() {
        assertTrue(controlador.processPayment("credit", 5000));
        assertEquals(15000.0, metodoTarjeta.getLimit(), 0.001);
        assertTrue(controlador.processPayment("eWallet", 2000));
        assertEquals(6000.0, metodoBilletera.getBalance(), 0.001);
    }

    @Test
    @DisplayName("Valida nombres de modo en el adaptador")
    void debeRetornarNombresDeMetodoCorrectamente() {
        adaptador.setMode("CASH");
        assertEquals("Efectivo", adaptador.getName());
        adaptador.setMode("credit");
        assertEquals("Tarjeta de Crédito", adaptador.getName());
        adaptador.setMode("ewallet");
        assertEquals("E-Wallet", adaptador.getName());
    }

    @Test
    @DisplayName("Procesa montos cero sin alterar saldos")
    void debeProcesarMontoCeroSinCambiarSaldos() {
        assertTrue(controlador.processPayment("CASH", 0));
        assertTrue(controlador.processPayment("CREDIT", 0));
        assertTrue(controlador.processPayment("EWALLET", 0));
        assertEquals(30000.0, metodoEfectivo.getCashAvailable(), 0.001);
        assertEquals(20000.0, metodoTarjeta.getLimit(), 0.001);
        assertEquals(8000.0, metodoBilletera.getBalance(), 0.001);
    }

    @Test
    @DisplayName("Verifica que todos los componentes estén inicializados")
    void debeValidarIntegracionCompletaDeMetodosPago() {
        assertNotNull(metodoEfectivo);
        assertNotNull(metodoTarjeta);
        assertNotNull(metodoBilletera);
        assertNotNull(adaptador);
        assertNotNull(controlador);
    }

    @Test
    @DisplayName("Permite agotar exactamente el límite de crédito")
    void debeAgotarExactamenteLimiteTarjeta() {
        assertTrue(controlador.processPayment("CREDIT", 5000));
        assertTrue(controlador.processPayment("CREDIT", 15000));
        assertEquals(0.0, metodoTarjeta.getLimit(), 0.001);
        assertFalse(controlador.processPayment("CREDIT", 1));
    }

    @Test
    @DisplayName("Permite realizar múltiples pagos pequeños en e-wallet hasta agotarla")
    void debeAgotarBilleteraConPagosPequenos() {
        for (int i = 0; i < 8; i++) {
            assertTrue(controlador.processPayment("EWALLET", 1000));
        }
        assertEquals(0.0, metodoBilletera.getBalance(), 0.001);
        assertFalse(controlador.processPayment("EWALLET", 1));
    }

    @Test
    @DisplayName("Comportamiento con montos negativos incrementa saldos según implementación actual")
    void montosNegativosIncrementanSaldos() {
        double beforeCash = metodoEfectivo.getCashAvailable();
        double beforeLimit = metodoTarjeta.getLimit();
        double beforeWallet = metodoBilletera.getBalance();
        assertTrue(controlador.processPayment("CASH", -1000));
        assertTrue(controlador.processPayment("CREDIT", -2000));
        assertTrue(controlador.processPayment("EWALLET", -500));
        assertEquals(beforeCash + 1000, metodoEfectivo.getCashAvailable(), 0.001);
        assertEquals(beforeLimit + 2000, metodoTarjeta.getLimit(), 0.001);
        assertEquals(beforeWallet + 500, metodoBilletera.getBalance(), 0.001);
    }

    @Test
    @DisplayName("Cambia de modo varias veces y mantiene correcto nombre-reportado después de la última acción")
    void cambiaDeModoYVerificaNombreFinal() {
        assertTrue(controlador.processPayment("CASH", 1000));
        assertTrue(controlador.processPayment("credit", 2000));
        assertTrue(controlador.processPayment("ewallet", 3000));
        assertEquals("E-Wallet", adaptador.getName());
    }
}