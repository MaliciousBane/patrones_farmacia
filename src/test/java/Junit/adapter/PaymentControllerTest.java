package Junit.adapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.adapter.model.*;
import patrones_farmacia.adapter.controller.PaymentController;

@DisplayName("Pruebas del Controlador de Pagos")
class PaymentControllerTest {

    private PaymentController controlador;
    private AdapterPayMethod adaptadorPagos;

    @BeforeEach
    void configurarPrueba() {
        CashMethod efectivo = new CashMethod(100000);
        CreditCardMethod tarjeta = new CreditCardMethod("1234", "Andrés", "999", 200000);
        EWalletMethod billetera = new EWalletMethod("WAL-01", 50000);
        adaptadorPagos = new AdapterPayMethod(efectivo, tarjeta, billetera);
        controlador = new PaymentController(adaptadorPagos);
    }

    @Test
    @DisplayName("Debe procesar correctamente un pago en efectivo dentro del saldo disponible")
    void debeProcesarPagoEnEfectivoExitosamente() {
        double montoPago = 20000;
        
        boolean resultado = controlador.processPayment("CASH", montoPago);
        
        assertTrue(resultado, "El pago en efectivo de $20.000 debe procesarse exitosamente");
    }

    @Test
    @DisplayName("Debe rechazar un pago con tarjeta que excede el límite disponible")
    void debeRechazarPagoConTarjetaSobreLimite() {
        double montoExcesivo = 999999;
        
        boolean resultado = controlador.processPayment("CREDIT", montoExcesivo);
        
        assertFalse(resultado, "El pago con tarjeta de $999.999 debe ser rechazado por exceder el límite de $200.000");
    }

    @Test
    @DisplayName("Debe procesar correctamente un pago con billetera electrónica")
    void debeProcesarPagoConBilleteraElectronica() {
        double montoPago = 30000;
        
        boolean resultado = controlador.processPayment("EWALLET", montoPago);
        
        assertTrue(resultado, "El pago con billetera electrónica de $30.000 debe procesarse exitosamente");
    }

    @Test
    @DisplayName("Debe rechazar un pago en efectivo que excede el saldo disponible")
    void debeRechazarPagoEnEfectivoSinSaldoSuficiente() {
        double montoExcesivo = 150000;
        
        boolean resultado = controlador.processPayment("CASH", montoExcesivo);
        
        assertFalse(resultado, "El pago en efectivo de $150.000 debe ser rechazado por exceder el saldo de $100.000");
    }
}