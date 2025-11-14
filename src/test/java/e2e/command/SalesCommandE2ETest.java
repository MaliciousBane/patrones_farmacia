package e2e.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.command.controller.*;
import patrones_farmacia.command.model.SaleReceiver;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;
import java.util.List;

@DisplayName("Command Pattern - Operaciones de Ventas E2E")
class SalesCommandE2ETest {

    private SaleReceiver receiver;
    private CashierInvoker invoker;
    private FCreator creator;
    private Medicine med1, med2, med3;

    @BeforeEach
    void setUp() {
        receiver = new SaleReceiver();
        invoker = new CashierInvoker();
        creator = new FCreator();
        med1 = creator.createMedicine(FCreator.Type.GENERIC, "Amoxicilina", "GenFarma", 4500);
        med2 = creator.createMedicine(FCreator.Type.BRANDED, "Ibupirac", "Farmalab", 8900);
        med3 = creator.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "GenFarma", 2500);
    }

    @Test
    @DisplayName("Debe registrar venta exitosamente")
    void testRegisterSaleSuccessfully() {
        Sale sale = new Sale("CMD-001", "Cliente Test");
        sale.addMedicine(med1);
        invoker.executeCommand(new RegisterSaleCommand(receiver, sale));
        assertEquals(1, receiver.getAllSales().size());
        assertEquals("CMD-001", receiver.getAllSales().get(0).getId());
    }

    @Test
    @DisplayName("Debe registrar múltiples ventas")
    void testRegisterMultipleSalesSequentially() {
        Sale s1 = new Sale("CMD-002", "Cliente 1");
        s1.addMedicine(med1);
        Sale s2 = new Sale("CMD-003", "Cliente 2");
        s2.addMedicine(med2);
        Sale s3 = new Sale("CMD-004", "Cliente 3");
        s3.addMedicine(med3);
        invoker.executeCommand(new RegisterSaleCommand(receiver, s1));
        invoker.executeCommand(new RegisterSaleCommand(receiver, s2));
        invoker.executeCommand(new RegisterSaleCommand(receiver, s3));
        assertEquals(3, receiver.getAllSales().size());
    }

    @Test
    @DisplayName("Debe registrar venta con múltiples medicinas")
    void testRegisterSaleWithMultipleMedicines() {
        Sale sale = new Sale("CMD-005", "Cliente VIP");
        sale.addMedicine(med1);
        sale.addMedicine(med2);
        sale.addMedicine(med3);
        invoker.executeCommand(new RegisterSaleCommand(receiver, sale));
        assertEquals(3, receiver.getAllSales().get(0).getItems().size());
        assertEquals(15900, receiver.getAllSales().get(0).getTotal());
    }

    @Test
    @DisplayName("Debe cancelar venta registrada")
    void testCancelRegisteredSale() {
        Sale sale = new Sale("CMD-006", "Cliente Cancelar");
        sale.addMedicine(med1);
        invoker.executeCommand(new RegisterSaleCommand(receiver, sale));
        invoker.executeCommand(new CancelSaleCommand(receiver, "CMD-006"));
        assertEquals(0, receiver.getAllSales().size());
    }

    @Test
    @DisplayName("Debe rechazar cancelación inexistente")
    void testRejectCancelNonExistentSale() {
        assertFalse(receiver.cancelSale("CMD-INEXISTENTE"));
        assertEquals(0, receiver.getAllSales().size());
    }

    @Test
    @DisplayName("Debe cancelar venta específica entre múltiples")
    void testCancelSpecificSaleAmongMultiple() {
        Sale s1 = new Sale("CMD-007", "Cliente 1");
        s1.addMedicine(med1);
        Sale s2 = new Sale("CMD-008", "Cliente 2");
        s2.addMedicine(med2);
        Sale s3 = new Sale("CMD-009", "Cliente 3");
        s3.addMedicine(med3);
        invoker.executeCommand(new RegisterSaleCommand(receiver, s1));
        invoker.executeCommand(new RegisterSaleCommand(receiver, s2));
        invoker.executeCommand(new RegisterSaleCommand(receiver, s3));
        invoker.executeCommand(new CancelSaleCommand(receiver, "CMD-008"));
        assertEquals(2, receiver.getAllSales().size());
        assertTrue(receiver.getAllSales().stream().anyMatch(s -> s.getId().equals("CMD-007")));
        assertFalse(receiver.getAllSales().stream().anyMatch(s -> s.getId().equals("CMD-008")));
    }

    @Test
    @DisplayName("Debe devolver producto de venta")
    void testReturnProductFromSale() {
        Sale sale = new Sale("CMD-010", "Cliente Devolución");
        sale.addMedicine(med1);
        sale.addMedicine(med2);
        invoker.executeCommand(new RegisterSaleCommand(receiver, sale));
        invoker.executeCommand(new ReturnProductCommand(receiver, "CMD-010", med1));
        assertEquals(1, receiver.getAllSales().get(0).getItems().size());
        assertEquals("Ibupirac", receiver.getAllSales().get(0).getItems().get(0).getName());
    }

    @Test
    @DisplayName("Debe rechazar devolución inexistente")
    void testRejectReturnNonExistentProduct() {
        Sale sale = new Sale("CMD-011", "Cliente Rechazo");
        sale.addMedicine(med1);
        invoker.executeCommand(new RegisterSaleCommand(receiver, sale));
        Medicine nonExistent = creator.createMedicine(FCreator.Type.GENERIC, "NoExiste", "Lab", 1000);
        assertFalse(receiver.returnProduct("CMD-011", nonExistent));
        assertEquals(1, receiver.getAllSales().get(0).getItems().size());
    }

    @Test
    @DisplayName("Debe devolver todos los productos")
    void testReturnAllProductsFromSale() {
        Sale sale = new Sale("CMD-012", "Cliente Devolución Total");
        sale.addMedicine(med1);
        sale.addMedicine(med2);
        sale.addMedicine(med3);
        invoker.executeCommand(new RegisterSaleCommand(receiver, sale));
        invoker.executeCommand(new ReturnProductCommand(receiver, "CMD-012", med1));
        invoker.executeCommand(new ReturnProductCommand(receiver, "CMD-012", med2));
        invoker.executeCommand(new ReturnProductCommand(receiver, "CMD-012", med3));
        assertEquals(0, receiver.getAllSales().get(0).getItems().size());
    }

    @Test
    @DisplayName("Debe deshacer último comando")
    void testUndoLastRegisterCommand() {
        Sale sale = new Sale("CMD-013", "Cliente Undo");
        sale.addMedicine(med1);
        invoker.executeCommand(new RegisterSaleCommand(receiver, sale));
        invoker.undoLast();
        assertEquals(0, receiver.getAllSales().size());
    }

    @Test
    @DisplayName("Debe deshacer múltiples comandos")
    void testUndoMultipleCommandsInReverseOrder() {
        Sale s1 = new Sale("CMD-014", "Cliente 1");
        s1.addMedicine(med1);
        Sale s2 = new Sale("CMD-015", "Cliente 2");
        s2.addMedicine(med2);
        Sale s3 = new Sale("CMD-016", "Cliente 3");
        s3.addMedicine(med3);
        invoker.executeCommand(new RegisterSaleCommand(receiver, s1));
        invoker.executeCommand(new RegisterSaleCommand(receiver, s2));
        invoker.executeCommand(new RegisterSaleCommand(receiver, s3));
        invoker.undoLast();
        invoker.undoLast();
        invoker.undoLast();
        assertEquals(0, receiver.getAllSales().size());
    }

    @Test
    @DisplayName("Debe ignorar undo sin historial")
    void testUndoWithoutHistory() {
        assertDoesNotThrow(() -> invoker.undoLast());
        assertEquals(0, receiver.getAllSales().size());
    }

    @Test
    @DisplayName("Debe ejecutar flujo: registrar -> devolver -> cancelar")
    void testCompleteFlowRegisterReturnCancel() {
        Sale sale = new Sale("CMD-017", "Cliente Flujo");
        sale.addMedicine(med1);
        sale.addMedicine(med2);
        invoker.executeCommand(new RegisterSaleCommand(receiver, sale));
        assertEquals(2, receiver.getAllSales().get(0).getItems().size());
        invoker.executeCommand(new ReturnProductCommand(receiver, "CMD-017", med1));
        assertEquals(1, receiver.getAllSales().get(0).getItems().size());
        invoker.executeCommand(new CancelSaleCommand(receiver, "CMD-017"));
        assertEquals(0, receiver.getAllSales().size());
    }

    @Test
    @DisplayName("Debe registrar, cancelar y registrar nuevamente")
    void testRegisterCancelThenRegisterAgain() {
        Sale s1 = new Sale("CMD-018", "Cliente Primera");
        s1.addMedicine(med1);
        invoker.executeCommand(new RegisterSaleCommand(receiver, s1));
        invoker.executeCommand(new CancelSaleCommand(receiver, "CMD-018"));
        assertEquals(0, receiver.getAllSales().size());
        Sale s2 = new Sale("CMD-019", "Cliente Segunda");
        s2.addMedicine(med2);
        invoker.executeCommand(new RegisterSaleCommand(receiver, s2));
        assertEquals("CMD-019", receiver.getAllSales().get(0).getId());
    }

    @Test
    @DisplayName("Debe buscar venta case-insensitive")
    void testCaseInsensitiveSaleSearch() {
        Sale sale = new Sale("cmd-020", "Cliente");
        sale.addMedicine(med1);
        invoker.executeCommand(new RegisterSaleCommand(receiver, sale));
        assertTrue(receiver.cancelSale("CMD-020"));
    }

    @Test
    @DisplayName("Debe devolver producto case-insensitive")
    void testCaseInsensitiveProductReturn() {
        Sale sale = new Sale("CMD-021", "Cliente");
        sale.addMedicine(med1);
        invoker.executeCommand(new RegisterSaleCommand(receiver, sale));
        Medicine medicineLowercase = creator.createMedicine(FCreator.Type.GENERIC, "amoxicilina", "GenFarma", 4500);
        assertTrue(receiver.returnProduct("CMD-021", medicineLowercase));
        assertEquals(0, receiver.getAllSales().get(0).getItems().size());
    }

    @Test
    @DisplayName("Debe preservar datos de venta")
    void testPreserveSaleDataAfterReturn() {
        Sale sale = new Sale("CMD-022", "Cliente Integridad");
        String idBefore = sale.getId();
        String clientBefore = sale.getClient();
        sale.addMedicine(med1);
        sale.addMedicine(med2);
        invoker.executeCommand(new RegisterSaleCommand(receiver, sale));
        invoker.executeCommand(new ReturnProductCommand(receiver, "CMD-022", med1));
        Sale retrieved = receiver.getAllSales().get(0);
        assertEquals(idBefore, retrieved.getId());
        assertEquals(clientBefore, retrieved.getClient());
    }

    @Test
    @DisplayName("Debe preservar medicinas no devueltas")
    void testPreserveMedicinesNotReturned() {
        Sale sale = new Sale("CMD-023", "Cliente Medicinas");
        sale.addMedicine(med1);
        sale.addMedicine(med2);
        invoker.executeCommand(new RegisterSaleCommand(receiver, sale));
        invoker.executeCommand(new ReturnProductCommand(receiver, "CMD-023", med1));
        Medicine remaining = receiver.getAllSales().get(0).getItems().get(0);
        assertEquals("Ibupirac", remaining.getName());
        assertEquals(8900, remaining.getPrice());
    }

    @Test
    @DisplayName("Debe procesar múltiples comandos rápidamente")
    void testRapidCommandExecution() {
        for(int i = 0; i < 10; i++) {
            Sale sale = new Sale("CMD-STRESS-" + i, "Cliente " + i);
            sale.addMedicine(med1);
            invoker.executeCommand(new RegisterSaleCommand(receiver, sale));
        }
        assertEquals(10, receiver.getAllSales().size());
    }

    @Test
    @DisplayName("Debe alternar comandos correctamente")
    void testAlternatingCommands() {
        for(int i = 0; i < 5; i++) {
            Sale sale = new Sale("CMD-ALT-" + i, "Cliente " + i);
            sale.addMedicine(med1);
            invoker.executeCommand(new RegisterSaleCommand(receiver, sale));
            invoker.executeCommand(new CancelSaleCommand(receiver, "CMD-ALT-" + i));
        }
        assertEquals(0, receiver.getAllSales().size());
    }

    @Test
    @DisplayName("Debe validar registro de venta")
    void testValidateSaleRegistration() {
        Sale sale = new Sale("CMD-024", "Cliente Validación");
        sale.addMedicine(med1);
        double totalBefore = sale.getTotal();
        invoker.executeCommand(new RegisterSaleCommand(receiver, sale));
        Sale registered = receiver.getAllSales().get(0);
        assertEquals(sale.getId(), registered.getId());
        assertEquals(sale.getClient(), registered.getClient());
        assertEquals(totalBefore, registered.getTotal());
        assertEquals(1, registered.getItems().size());
    }

    @Test
    @DisplayName("Debe retornar lista vacía sin ventas")
    void testEmptySalesList() {
        List<Sale> sales = receiver.getAllSales();
        assertTrue(sales.isEmpty());
        assertEquals(0, sales.size());
    }
}