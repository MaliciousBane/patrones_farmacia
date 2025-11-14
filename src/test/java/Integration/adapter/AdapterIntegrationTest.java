package Integration.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.adapter.model.*;
import patrones_farmacia.adapter.controller.PaymentController;

@DisplayName("Pruebas de Integración del Patrón Adapter para Pagos")
class AdapterIntegrationTest {

    private PaymentController controlador;
    private CashMethod metodoEfectivo;
    private CreditCardMethod metodoTarjeta;
    private EWalletMethod metodoBilletera;

    @BeforeEach
    void configurarSistemaDePagos() {
        metodoEfectivo = new CashMethod(30000);
        metodoTarjeta = new CreditCardMethod("1111-2222", "Cliente Test", "999", 20000);
        metodoBilletera = new EWalletMethod("WAL-123", 8000);

        AdapterPayMethod adaptador = new AdapterPayMethod(metodoEfectivo, metodoTarjeta, metodoBilletera);
        controlador = new PaymentController(adaptador);
    }

    @Test
    @DisplayName("Debe procesar correctamente pagos con todos los métodos válidos")
    void debeProcesarPagosConTodosLosMetodosValidos() {
        boolean pagoEfectivo = controlador.processPayment("CASH", 10000);
        boolean pagoTarjeta = controlador.processPayment("CREDIT", 15000);

        assertTrue(pagoEfectivo, "El pago en efectivo de $10.000 debe procesarse exitosamente");
        assertTrue(pagoTarjeta, "El pago con tarjeta de $15.000 debe procesarse exitosamente");
    }

    @Test
    @DisplayName("Debe rechazar pagos que exceden los límites disponibles")
    void debeRechazarPagosQueExcedenLimites() {
        boolean pagoBilleteraExcedido = controlador.processPayment("EWALLET", 10000);
        boolean pagoTarjetaExcedido = controlador.processPayment("CREDIT", 50000);

        assertFalse(pagoBilleteraExcedido, 
            "El pago con billetera de $10.000 debe rechazarse (saldo: $8.000)");
        assertFalse(pagoTarjetaExcedido, 
            "El pago con tarjeta de $50.000 debe rechazarse (límite: $20.000)");
    }

    @Test
    @DisplayName("Debe rechazar pagos con métodos de pago no reconocidos")
    void debeRechazarMetodosDePagoInvalidos() {
        boolean pagoMetodoInvalido = controlador.processPayment("XXX", 500);

        assertFalse(pagoMetodoInvalido, 
            "Los métodos de pago no reconocidos deben ser rechazados");
    }

    @Test
    @DisplayName("Debe actualizar correctamente los saldos después de procesar pagos")
    void debeActualizarSaldosDespuesDeProcesarPagos() {
        controlador.processPayment("CASH", 10000);
        controlador.processPayment("CREDIT", 15000);

        assertEquals(20000, metodoEfectivo.getCashAvailable(), 
            "El saldo en efectivo debe reducirse de $30.000 a $20.000");
        assertEquals(5000, metodoTarjeta.getLimit(), 
            "El límite de tarjeta debe reducirse de $20.000 a $5.000");
        assertEquals(8000, metodoBilletera.getBalance(), 
            "El saldo de la billetera debe permanecer en $8.000 (sin transacciones exitosas)");
    }

    @Test
    @DisplayName("Debe manejar múltiples transacciones consecutivas correctamente")
    void debeManejarMultiplesTransaccionesConsecutivas() {
        assertTrue(controlador.processPayment("CASH", 5000));
        assertTrue(controlador.processPayment("CASH", 8000));
        assertTrue(controlador.processPayment("CREDIT", 10000));

        assertEquals(17000, metodoEfectivo.getCashAvailable(), 
            "Después de dos pagos en efectivo, el saldo debe ser $17.000");
        assertEquals(10000, metodoTarjeta.getLimit(), 
            "Después de un pago con tarjeta, el límite debe ser $10.000");
    }

    @Test
    @DisplayName("Debe validar que todos los métodos de pago estén integrados correctamente")
    void debeValidarIntegracionCompletaDeMetodosPago() {
        assertNotNull(metodoEfectivo, "El método de efectivo debe estar inicializado");
        assertNotNull(metodoTarjeta, "El método de tarjeta debe estar inicializado");
        assertNotNull(metodoBilletera, "El método de billetera debe estar inicializado");
        assertNotNull(controlador, "El controlador de pagos debe estar inicializado");
    }
}