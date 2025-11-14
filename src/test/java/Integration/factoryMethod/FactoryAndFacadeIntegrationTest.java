package Integration.factoryMethod;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.facade.controller.*;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;
import patrones_farmacia.adapter.model.*;

@DisplayName("Pruebas de Integración de Factory Method y Facade")
class FactoryAndFacadeIntegrationTest {

    private FCreator creadorMedicamentos;
    private InventSystem sistemaInventario;
    private FacadeSale fachadaVenta;

    @BeforeEach
    void configurarSistemasIntegrados() {
        creadorMedicamentos = new FCreator();
        sistemaInventario = new InventSystem();
        
        CashMethod efectivo = new CashMethod(50000);
        CreditCardMethod tarjeta = new CreditCardMethod("123", "Cliente", "999", 100000);
        EWalletMethod billetera = new EWalletMethod("WAL-001", 20000);
        AdapterPayMethod adaptadorPagos = new AdapterPayMethod(efectivo, tarjeta, billetera);
        
        PaySystem sistemaPagos = new PaySystem(adaptadorPagos);
        ReceiptSystem sistemaRecibos = new ReceiptSystem();
        
        fachadaVenta = new FacadeSale(sistemaInventario, sistemaPagos, sistemaRecibos);
    }

    @Test
    @DisplayName("Debe crear medicamento con Factory y procesarlo con Facade")
    void debeCrearMedicamentoYProcesarConFacade() {
        Medicine medicamento = creadorMedicamentos.createMedicine(
            FCreator.Type.GENERIC, 
            "Ibuprofeno", 
            "GenFarma", 
            2500
        );

        sistemaInventario.addToStock(medicamento);

        Sale venta = new Sale("INT-001", "Cliente Integración");
        venta.addMedicine(medicamento);

        boolean resultado = fachadaVenta.doSale(venta, "CASH");

        assertTrue(resultado, 
            "La venta debe procesarse exitosamente integrando Factory Method y Facade");
    }

    @Test
    @DisplayName("Debe integrar creación de múltiples medicamentos con procesamiento de venta")
    void debeIntegrarMultiplesMedicamentosConVenta() {
        Medicine generico = creadorMedicamentos.createMedicine(
            FCreator.Type.GENERIC, "Paracetamol", "TecnoFarma", 3000
        );
        Medicine marca = creadorMedicamentos.createMedicine(
            FCreator.Type.BRAND, "Dolex Forte", "GSK", 6500
        );

        sistemaInventario.addToStock(generico);
        sistemaInventario.addToStock(marca);

        Sale venta = new Sale("INT-002", "María González");
        venta.addMedicine(generico);
        venta.addMedicine(marca);

        boolean resultado = fachadaVenta.doSale(venta, "CASH");

        assertTrue(resultado, 
            "Debe procesar ventas con múltiples medicamentos creados por Factory Method");
    }

    @Test
    @DisplayName("Debe procesar venta con medicamento de marca usando tarjeta de crédito")
    void debeProcesarVentaMedicamentoMarcaConTarjeta() {
        Medicine medicamentoMarca = creadorMedicamentos.createMedicine(
            FCreator.Type.BRAND, 
            "Aspirina Protect", 
            "Bayer", 
            15000
        );

        sistemaInventario.addToStock(medicamentoMarca);

        Sale venta = new Sale("INT-003", "Roberto Díaz");
        venta.addMedicine(medicamentoMarca);

        boolean resultado = fachadaVenta.doSale(venta, "CREDIT");

        assertTrue(resultado, 
            "Debe procesar venta de medicamento de marca con pago por tarjeta");
    }

    @Test
    @DisplayName("Debe procesar venta con medicamento genérico usando billetera electrónica")
    void debeProcesarVentaMedicamentoGenericoConBilletera() {
        Medicine medicamentoGenerico = creadorMedicamentos.createMedicine(
            FCreator.Type.GENERIC, 
            "Amoxicilina", 
            "GenFarma", 
            8000
        );

        sistemaInventario.addToStock(medicamentoGenerico);

        Sale venta = new Sale("INT-004", "Laura Méndez");
        venta.addMedicine(medicamentoGenerico);

        boolean resultado = fachadaVenta.doSale(venta, "EWALLET");

        assertTrue(resultado, 
            "Debe procesar venta de medicamento genérico con pago por billetera electrónica");
    }

    @Test
    @DisplayName("Debe validar el flujo completo desde creación hasta venta finalizada")
    void debeValidarFlujoCompletoDesdCreacionHastaVenta() {
        Medicine med1 = creadorMedicamentos.createMedicine(
            FCreator.Type.GENERIC, "Acetaminofén", "TecnoFarma", 2800
        );
        Medicine med2 = creadorMedicamentos.createMedicine(
            FCreator.Type.BRAND, "Advil", "Pfizer", 9500
        );

        sistemaInventario.addToStock(med1);
        sistemaInventario.addToStock(med2);

        Sale venta = new Sale("INT-005", "Pedro Ramírez");
        venta.addMedicine(med1);
        venta.addMedicine(med2);

        
        boolean resultado = fachadaVenta.doSale(venta, "CASH");

        assertTrue(resultado, 
            "El flujo completo debe ejecutarse: Factory → Inventario → Venta → Pago → Recibo");
        assertNotNull(med1, "El primer medicamento debe estar creado");
        assertNotNull(med2, "El segundo medicamento debe estar creado");
    }

    @Test
    @DisplayName("Debe integrar diferentes tipos de medicamentos con diferentes métodos de pago")
    void debeIntegrarTiposMedicamentosConMetodosPago() {
        Medicine medicamento1 = creadorMedicamentos.createMedicine(
            FCreator.Type.GENERIC, "Loratadina", "GenFarma", 4500
        );
        sistemaInventario.addToStock(medicamento1);

        Sale venta1 = new Sale("INT-006", "Cliente A");
        venta1.addMedicine(medicamento1);
        assertTrue(fachadaVenta.doSale(venta1, "CASH"), 
            "Venta con medicamento genérico y efectivo debe procesarse");

        Medicine medicamento2 = creadorMedicamentos.createMedicine(
            FCreator.Type.BRAND, "Claritin", "Bayer", 18000
        );
        sistemaInventario.addToStock(medicamento2);

        Sale venta2 = new Sale("INT-007", "Cliente B");
        venta2.addMedicine(medicamento2);
        assertTrue(fachadaVenta.doSale(venta2, "CREDIT"), 
            "Venta con medicamento de marca y tarjeta debe procesarse");
    }
}