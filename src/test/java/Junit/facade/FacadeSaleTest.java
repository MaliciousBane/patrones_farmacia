package Junit.facade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.facade.controller.*;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.adapter.model.*;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;

@DisplayName("Pruebas del Patrón Facade para Ventas")
class FacadeSaleTest {

    private FacadeSale fachada;
    private InventSystem sistemaInventario;
    private FCreator creadorMedicamentos;

    @BeforeEach
    void configurarSistemas() {
        sistemaInventario = new InventSystem();
        
        CashMethod efectivo = new CashMethod(100000);
        CreditCardMethod tarjeta = new CreditCardMethod("1234", "Farmacia Central", "999", 200000);
        EWalletMethod billetera = new EWalletMethod("WALLET-01", 50000);
        AdapterPayMethod adaptadorPagos = new AdapterPayMethod(efectivo, tarjeta, billetera);
        
        PaySystem sistemaPagos = new PaySystem(adaptadorPagos);
        ReceiptSystem sistemaRecibos = new ReceiptSystem();
        
        fachada = new FacadeSale(sistemaInventario, sistemaPagos, sistemaRecibos);
        creadorMedicamentos = new FCreator();
    }

    @Test
    @DisplayName("Debe procesar exitosamente una venta completa con pago en efectivo")
    void debeProcesarVentaCompletaConEfectivo() {
        Medicine medicamento = creadorMedicamentos.createMedicine(
            FCreator.Type.GENERIC, 
            "Amoxicilina", 
            "GenFarma", 
            8000
        );
        sistemaInventario.addToStock(medicamento);

        Sale venta = new Sale("FAC-001", "Carmen Rodríguez");
        venta.addMedicine(medicamento);

        boolean resultado = fachada.doSale(venta, "CASH");

        assertTrue(resultado, "La venta debe procesarse exitosamente con pago en efectivo");
    }

    @Test
    @DisplayName("Debe procesar venta con tarjeta de crédito correctamente")
    void debeProcesarVentaConTarjetaCredito() {
        Medicine medicamento = creadorMedicamentos.createMedicine(
            FCreator.Type.BRAND, 
            "Aspirina Protect", 
            "Bayer", 
            15000
        );
        sistemaInventario.addToStock(medicamento);

        Sale venta = new Sale("FAC-002", "Jorge Morales");
        venta.addMedicine(medicamento);

        boolean resultado = fachada.doSale(venta, "CREDIT");

        assertTrue(resultado, "La venta debe procesarse correctamente con tarjeta de crédito");
    }

    @Test
    @DisplayName("Debe procesar venta con múltiples medicamentos")
    void debeProcesarVentaConMultiplesMedicamentos() {
        Medicine medicamento1 = creadorMedicamentos.createMedicine(
            FCreator.Type.GENERIC, 
            "Paracetamol", 
            "TecnoFarma", 
            3500
        );
        Medicine medicamento2 = creadorMedicamentos.createMedicine(
            FCreator.Type.GENERIC, 
            "Ibuprofeno", 
            "GenFarma", 
            4200
        );
        
        sistemaInventario.addToStock(medicamento1);
        sistemaInventario.addToStock(medicamento2);

        Sale venta = new Sale("FAC-003", "Patricia Gómez");
        venta.addMedicine(medicamento1);
        venta.addMedicine(medicamento2);

        boolean resultado = fachada.doSale(venta, "CASH");

        assertTrue(resultado, "La venta con múltiples medicamentos debe procesarse correctamente");
    }

    @Test
    @DisplayName("Debe procesar venta con billetera electrónica")
    void debeProcesarVentaConBilleteraElectronica() {
        Medicine medicamento = creadorMedicamentos.createMedicine(
            FCreator.Type.BRAND, 
            "Dolex Gripa", 
            "GSK", 
            12000
        );
        sistemaInventario.addToStock(medicamento);

        Sale venta = new Sale("FAC-004", "Ricardo Herrera");
        venta.addMedicine(medicamento);

        boolean resultado = fachada.doSale(venta, "EWALLET");

        assertTrue(resultado, "La venta debe procesarse correctamente con billetera electrónica");
    }
}