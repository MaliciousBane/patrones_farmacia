package e2e.facade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.facade.controller.*;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;
import patrones_farmacia.adapter.model.*;

@DisplayName("Facade Pattern - Proceso Completo de Ventas E2E")
class SaleProcessE2ETest {

    private InventSystem invent;
    private FacadeSale facade;
    private FCreator creator;
    private Medicine med1, med2, med3;

    @BeforeEach
    void setUp() {
        creator = new FCreator();
        invent = new InventSystem();
        CashMethod cash = new CashMethod(100000);
        CreditCardMethod card = new CreditCardMethod("1234-5678", "Farmacia Principal", "456", 50000);
        EWalletMethod wallet = new EWalletMethod("WAL-FARM-001", 30000);
        AdapterPayMethod adapter = new AdapterPayMethod(cash, card, wallet);
        PaySystem paySystem = new PaySystem(adapter);
        ReceiptSystem receipt = new ReceiptSystem();
        facade = new FacadeSale(invent, paySystem, receipt);
        
        med1 = creator.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "GenFarma", 3500);
        med2 = creator.createMedicine(FCreator.Type.BRANDED, "Dolex", "GSK", 6000);
        med3 = creator.createMedicine(FCreator.Type.GENERIC, "Ibupirac", "Farmalab", 8900);
        
        invent.addToStock(med1);
        invent.addToStock(med2);
        invent.addToStock(med3);
    }

    @Test
    @DisplayName("Debe completar venta completa con fachada")
    void testCompleteEntireSaleProcessThroughFacade() {
        Sale sale = new Sale("FAC-001", "Cliente Principal");
        sale.addMedicine(med1);
        assertTrue(facade.doSale(sale, "CASH"));
    }

    @Test
    @DisplayName("Debe procesar venta con múltiples medicinas")
    void testProcessSaleWithMultipleMedicines() {
        Sale sale = new Sale("FAC-002", "Cliente VIP");
        sale.addMedicine(med1);
        sale.addMedicine(med2);
        sale.addMedicine(med3);
        assertTrue(facade.doSale(sale, "CASH"));
        assertEquals(18400, sale.getTotal());
    }

    @Test
    @DisplayName("Debe rechazar venta con medicina no disponible")
    void testRejectSaleWithUnavailableMedicine() {
        Medicine unavailable = creator.createMedicine(FCreator.Type.GENERIC, "Inexistente", "LabX", 5000);
        Sale sale = new Sale("FAC-003", "Cliente Rechazo");
        sale.addMedicine(unavailable);
        assertFalse(facade.doSale(sale, "CASH"));
    }

    @Test
    @DisplayName("Debe rechazar venta si pago falla")
    void testRejectSaleIfPaymentFails() {
        CashMethod emptyCash = new CashMethod(1000);
        AdapterPayMethod emptyAdapter = new AdapterPayMethod(emptyCash, null, null);
        PaySystem emptyPaySystem = new PaySystem(emptyAdapter);
        FacadeSale emptyFacade = new FacadeSale(invent, emptyPaySystem, new ReceiptSystem());
        
        Sale sale = new Sale("FAC-004", "Cliente Sin Fondos");
        sale.addMedicine(med1);
        assertFalse(emptyFacade.doSale(sale, "CASH"));
    }

    @Test
    @DisplayName("Debe procesar pago en efectivo")
    void testProcessPaymentInCash() {
        Sale sale = new Sale("FAC-005", "Cliente Efectivo");
        sale.addMedicine(med1);
        assertTrue(facade.doSale(sale, "CASH"));
    }

    @Test
    @DisplayName("Debe procesar pago con tarjeta crédito")
    void testProcessPaymentWithCreditCard() {
        Sale sale = new Sale("FAC-006", "Cliente Tarjeta");
        sale.addMedicine(med2);
        assertTrue(facade.doSale(sale, "CREDIT"));
    }

    @Test
    @DisplayName("Debe procesar pago con billetera digital")
    void testProcessPaymentWithEWallet() {
        Sale sale = new Sale("FAC-007", "Cliente Billetera");
        sale.addMedicine(med3);
        assertTrue(facade.doSale(sale, "EWALLET"));
    }

    @Test
    @DisplayName("Debe rechazar método de pago inválido")
    void testRejectPaymentWithInvalidMethod() {
        Sale sale = new Sale("FAC-008", "Cliente Inválido");
        sale.addMedicine(med1);
        assertFalse(facade.doSale(sale, "INVALID"));
    }

    @Test
    @DisplayName("Debe remover medicina del inventario")
    void testRemoveMedicineFromStockAfterSale() {
        Sale sale = new Sale("FAC-009", "Cliente Inventario");
        sale.addMedicine(med1);
        assertTrue(facade.doSale(sale, "CASH"));
        assertFalse(invent.verifyStock(med1));
    }

    @Test
    @DisplayName("Debe mantener medicina en stock si venta falla")
    void testKeepMedicinesInStockAfterFailedSale() {
        CashMethod emptyCash = new CashMethod(100);
        AdapterPayMethod emptyAdapter = new AdapterPayMethod(emptyCash, null, null);
        PaySystem emptyPaySystem = new PaySystem(emptyAdapter);
        FacadeSale emptyFacade = new FacadeSale(invent, emptyPaySystem, new ReceiptSystem());
        
        Sale sale = new Sale("FAC-010", "Cliente Fallo");
        sale.addMedicine(med1);
        assertFalse(emptyFacade.doSale(sale, "CASH"));
        assertTrue(invent.verifyStock(med1));
    }

    @Test
    @DisplayName("Debe procesar medicina duplicada")
    void testProcessSaleWithDuplicateMedicine() {
        Sale sale = new Sale("FAC-011", "Cliente Duplicado");
        sale.addMedicine(med1);
        sale.addMedicine(med1);
        assertTrue(facade.doSale(sale, "CASH"));
        assertEquals(7000, sale.getTotal());
    }

    @Test
    @DisplayName("Debe procesar múltiples ventas secuencialmente")
    void testProcessMultipleSalesSequentially() {
        Sale sale1 = new Sale("FAC-012", "Cliente 1");
        sale1.addMedicine(med1);
        assertTrue(facade.doSale(sale1, "CASH"));
        invent.addToStock(med1);
        
        Sale sale2 = new Sale("FAC-013", "Cliente 2");
        sale2.addMedicine(med2);
        assertTrue(facade.doSale(sale2, "CASH"));
    }

    @Test
    @DisplayName("Debe validar inventario antes de pago")
    void testVerifyStockBeforeProcessingPayment() {
        Medicine unavailable = creator.createMedicine(FCreator.Type.GENERIC, "No Existe", "Lab", 1000);
        Sale sale = new Sale("FAC-015", "Cliente Validación");
        sale.addMedicine(unavailable);
        assertFalse(facade.doSale(sale, "CASH"));
    }

    @Test
    @DisplayName("Debe validar método case-insensitive")
    void testPaymentMethodCaseInsensitive() {
        Sale sale1 = new Sale("FAC-016", "Cliente Mayúsculas");
        sale1.addMedicine(med1);
        assertTrue(facade.doSale(sale1, "CASH"));
        invent.addToStock(med1);
        
        Sale sale2 = new Sale("FAC-017", "Cliente Minúsculas");
        sale2.addMedicine(med1);
        assertTrue(facade.doSale(sale2, "cash"));
    }

    @Test
    @DisplayName("Debe procesar venta exacta en límite tarjeta")
    void testProcessSaleExactCardLimit() {
        Medicine expensive = creator.createMedicine(FCreator.Type.BRANDED, "Premium", "Lab", 50000);
        invent.addToStock(expensive);
        Sale sale = new Sale("FAC-018", "Cliente Límite");
        sale.addMedicine(expensive);
        assertTrue(facade.doSale(sale, "CREDIT"));
    }

    @Test
    @DisplayName("Debe rechazar venta que excede tarjeta")
    void testRejectSaleExceedingCardLimit() {
        Medicine tooExpensive = creator.createMedicine(FCreator.Type.BRANDED, "Muy Caro", "Lab", 60000);
        invent.addToStock(tooExpensive);
        Sale sale = new Sale("FAC-019", "Cliente Exceso");
        sale.addMedicine(tooExpensive);
        assertFalse(facade.doSale(sale, "CREDIT"));
    }

    @Test
    @DisplayName("Debe procesar saldo exacto en billetera")
    void testProcessSaleWithExactWalletBalance() {
        Medicine exactPrice = creator.createMedicine(FCreator.Type.GENERIC, "Exacto", "Lab", 30000);
        invent.addToStock(exactPrice);
        Sale sale = new Sale("FAC-020", "Cliente Exacto");
        sale.addMedicine(exactPrice);
        assertTrue(facade.doSale(sale, "EWALLET"));
    }

    @Test
    @DisplayName("Debe generar recibo después de venta")
    void testGenerateReceiptAfterSuccessfulSale() {
        Sale sale = new Sale("FAC-021", "Cliente Recibo");
        sale.addMedicine(med1);
        assertTrue(facade.doSale(sale, "CASH"));
        assertEquals(1, sale.getItems().size());
    }

    @Test
    @DisplayName("Debe preservar ID de venta")
    void testPreserveSaleIdAfterProcessing() {
        Sale sale = new Sale("FAC-UNIQUE-022", "Cliente ID");
        sale.addMedicine(med1);
        String idBefore = sale.getId();
        facade.doSale(sale, "CASH");
        assertEquals(idBefore, sale.getId());
    }

    @Test
    @DisplayName("Debe preservar nombre cliente")
    void testPreserveClientNameAfterProcessing() {
        Sale sale = new Sale("FAC-023", "Cliente Test");
        sale.addMedicine(med1);
        String clientBefore = sale.getClient();
        facade.doSale(sale, "CASH");
        assertEquals(clientBefore, sale.getClient());
    }

    @Test
    @DisplayName("Debe procesar medicinas con precios variados")
    void testProcessSaleWithDifferentPrices() {
        Sale sale = new Sale("FAC-024", "Cliente Precios");
        sale.addMedicine(med1);
        sale.addMedicine(med2);
        sale.addMedicine(med3);
        assertTrue(facade.doSale(sale, "CASH"));
        assertEquals(18400, sale.getTotal());
    }

    @Test
    @DisplayName("Debe manejar venta vacía")
    void testHandleEmptySale() {
        Sale sale = new Sale("FAC-025", "Cliente Vacío");
        assertFalse(facade.doSale(sale, "CASH"));
    }

    @Test
    @DisplayName("Debe procesar venta con una medicina")
    void testProcessSaleWithSingleMedicine() {
        Sale sale = new Sale("FAC-026", "Cliente Individual");
        sale.addMedicine(med1);
        assertTrue(facade.doSale(sale, "CASH"));
        assertEquals(3500, sale.getTotal());
    }

    @Test
    @DisplayName("Debe funcionar con múltiples fachadas")
    void testMultipleFacadeInstances() {
        InventSystem invent2 = new InventSystem();
        invent2.addToStock(med1);
        invent2.addToStock(med2);
        CashMethod cash2 = new CashMethod(50000);
        AdapterPayMethod adapter2 = new AdapterPayMethod(cash2, null, null);
        PaySystem pay2 = new PaySystem(adapter2);
        FacadeSale facade2 = new FacadeSale(invent2, pay2, new ReceiptSystem());
        
        Sale sale1 = new Sale("FAC-027-A", "Cliente Fachada 1");
        sale1.addMedicine(med1);
        assertTrue(facade.doSale(sale1, "CASH"));
        
        Sale sale2 = new Sale("FAC-027-B", "Cliente Fachada 2");
        sale2.addMedicine(med1);
        assertTrue(facade2.doSale(sale2, "CASH"));
    }

    @Test
    @DisplayName("Debe validar stock case-insensitive")
    void testVerifyStockCaseInsensitive() {
        assertTrue(invent.verifyStock(med1));
        Medicine same = creator.createMedicine(FCreator.Type.GENERIC, "PARACETAMOL", "GenFarma", 3500);
        assertTrue(invent.verifyStock(same));
    }

    @Test
    @DisplayName("Debe procesar flujo completo")
    void testCompleteFlowWithoutErrors() {
        Sale sale = new Sale("FAC-028", "Cliente Flujo");
        sale.addMedicine(med1);
        sale.addMedicine(med2);
        assertTrue(facade.doSale(sale, "CASH"));
        assertEquals(9500, sale.getTotal());
        assertFalse(invent.verifyStock(med1));
        assertFalse(invent.verifyStock(med2));
        assertTrue(invent.verifyStock(med3));
    }

    @Test
    @DisplayName("Debe recuperarse de fallo y procesar siguiente")
    void testRecoverFromFailedSaleAndProcessNextSuccessfully() {
        Sale failedSale = new Sale("FAC-029-FAIL", "Cliente Fallo");
        failedSale.addMedicine(med1);
        Medicine notInStock = creator.createMedicine(FCreator.Type.GENERIC, "No Stock", "Lab", 5000);
        failedSale.addMedicine(notInStock);
        assertFalse(facade.doSale(failedSale, "CASH"));
        
        Sale successSale = new Sale("FAC-029-SUCCESS", "Cliente Éxito");
        successSale.addMedicine(med2);
        assertTrue(facade.doSale(successSale, "CASH"));
        assertTrue(invent.verifyStock(med1));
    }
}