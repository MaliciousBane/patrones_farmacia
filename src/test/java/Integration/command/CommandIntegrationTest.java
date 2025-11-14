package Integration.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.command.controller.*;
import patrones_farmacia.command.model.SaleReceiver;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;

class CommandIntegrationTest {

    private SaleReceiver receiver;
    private CashierInvoker invoker;
    private FCreator creator;
    private Medicine med1;
    private Medicine med2;
    private Medicine med3;

    @BeforeEach
    void init() {
        receiver = new SaleReceiver();
        invoker = new CashierInvoker();
        creator = new FCreator();
        med1 = creator.createMedicine(FCreator.Type.BRAND, "Dolex", "GSK", 5000);
        med2 = creator.createMedicine(FCreator.Type.GENERIC, "Ibuprofeno", "GenFarma", 1500);
        med3 = creator.createMedicine(FCreator.Type.GENERIC, "Aspirina", "Bayer", 1200);
    }

    @Test
    void registerThenUndoRemovesSale() {
        Sale sale = new Sale("CMD-201", "Cliente A");
        sale.addMedicine(med1);
        RegisterSaleCommand register = new RegisterSaleCommand(receiver, sale);
        invoker.executeCommand(register);
        assertEquals(1, receiver.getAllSales().size());
        invoker.undoLast();
        assertEquals(0, receiver.getAllSales().size());
    }

    @Test
    void registerAndCancelSequenceProducesEmptySalesAndHistoryAdjusts() {
        Sale sale = new Sale("CMD-202", "Cliente B");
        sale.addMedicine(med1);
        RegisterSaleCommand register = new RegisterSaleCommand(receiver, sale);
        CancelSaleCommand cancel = new CancelSaleCommand(receiver, "CMD-202");
        invoker.executeCommand(register);
        invoker.executeCommand(cancel);
        assertEquals(0, receiver.getAllSales().size());
        invoker.undoLast();
        assertEquals(0, receiver.getAllSales().size());
        invoker.undoLast();
        assertEquals(0, receiver.getAllSales().size());
    }

    @Test
    void returnProductRemovesItemWhenPresentAndFailsWhenMissing() {
        Sale sale = new Sale("CMD-203", "Cliente C");
        sale.addMedicine(med1);
        RegisterSaleCommand register = new RegisterSaleCommand(receiver, sale);
        invoker.executeCommand(register);
        ReturnProductCommand ret = new ReturnProductCommand(receiver, "CMD-203", med1);
        invoker.executeCommand(ret);
        assertEquals(0, receiver.getAllSales().get(0).getItems().size());
        boolean result = receiver.returnProduct("CMD-203", med2);
        assertFalse(result);
    }

    @Test
    void cancelNonExistingSaleDoesNotThrowAndLeavesStateEmpty() {
        CancelSaleCommand cancel = new CancelSaleCommand(receiver, "NO-EXISTE");
        assertDoesNotThrow(() -> invoker.executeCommand(cancel));
        assertEquals(0, receiver.getAllSales().size());
    }

    @Test
    void multipleRegistersAndUndoLastKeepsPreviousSale() {
        Sale s1 = new Sale("CMD-301", "C1");
        Sale s2 = new Sale("CMD-302", "C2");
        s1.addMedicine(med1);
        s2.addMedicine(med2);
        RegisterSaleCommand r1 = new RegisterSaleCommand(receiver, s1);
        RegisterSaleCommand r2 = new RegisterSaleCommand(receiver, s2);
        invoker.executeCommand(r1);
        invoker.executeCommand(r2);
        assertEquals(2, receiver.getAllSales().size());
        invoker.undoLast();
        assertEquals(1, receiver.getAllSales().size());
        assertEquals("CMD-301", receiver.getAllSales().get(0).getId());
    }

    @Test
    void undoOnEmptyHistoryDoesNotThrow() {
        assertDoesNotThrow(() -> invoker.undoLast());
        assertEquals(0, receiver.getAllSales().size());
    }

    @Test
    void returnUndoDoesNotRestoreProduct() {
        Sale sale = new Sale("CMD-401", "Cliente D");
        sale.addMedicine(med1);
        sale.addMedicine(med2);
        RegisterSaleCommand register = new RegisterSaleCommand(receiver, sale);
        invoker.executeCommand(register);
        ReturnProductCommand ret = new ReturnProductCommand(receiver, "CMD-401", med1);
        invoker.executeCommand(ret);
        assertEquals(1, receiver.getAllSales().get(0).getItems().size());
        invoker.undoLast();
        assertEquals(1, receiver.getAllSales().get(0).getItems().size());
    }

    @Test
    void cancelExistingSaleRemainsCancelledAfterUndo() {
        Sale sale = new Sale("CMD-402", "Cliente E");
        sale.addMedicine(med3);
        invoker.executeCommand(new RegisterSaleCommand(receiver, sale));
        invoker.executeCommand(new CancelSaleCommand(receiver, "CMD-402"));
        assertEquals(0, receiver.getAllSales().size());
        invoker.undoLast();
        assertEquals(0, receiver.getAllSales().size());
    }

    @Test
    void complexSequenceLeavesConsistentState() {
        Sale s1 = new Sale("CMD-501", "X");
        Sale s2 = new Sale("CMD-502", "Y");
        s1.addMedicine(med1);
        s1.addMedicine(med2);
        s2.addMedicine(med3);
        invoker.executeCommand(new RegisterSaleCommand(receiver, s1));
        invoker.executeCommand(new RegisterSaleCommand(receiver, s2));
        invoker.executeCommand(new ReturnProductCommand(receiver, "CMD-501", med2));
        invoker.executeCommand(new CancelSaleCommand(receiver, "CMD-502"));
        assertEquals(1, receiver.getAllSales().size());
        assertEquals("CMD-501", receiver.getAllSales().get(0).getId());
        assertEquals(1, receiver.getAllSales().get(0).getItems().size());
        invoker.undoLast();
        invoker.undoLast();
        assertEquals(1, receiver.getAllSales().size());
        assertEquals("CMD-501", receiver.getAllSales().get(0).getId());
    }
}