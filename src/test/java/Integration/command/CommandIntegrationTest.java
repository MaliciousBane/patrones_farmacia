package Integration.command;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.command.controller.*;
import patrones_farmacia.command.model.SaleReceiver;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.state.model.Order;

class CommandIntegrationTest {

    @Test
    void testCommandExecutionAndUndo() {

        SaleReceiver receiver = new SaleReceiver();
        CashierInvoker invoker = new CashierInvoker();

        Sale sale = new Sale("CMD-200", "Cliente Test");

        RegisterSaleCommand register = new RegisterSaleCommand(receiver, sale);
        CancelSaleCommand cancel = new CancelSaleCommand(receiver, "CMD-200");


        invoker.executeCommand(register);
        assertEquals(1, receiver.getAllSales().size());

        invoker.executeCommand(cancel);
        assertEquals(0, receiver.getAllSales().size());
    }

    @Test
    void testCancelNonExistingSale() {
        SaleReceiver receiver = new SaleReceiver();
        CashierInvoker invoker = new CashierInvoker();

        CancelSaleCommand cancel = new CancelSaleCommand(receiver, "NO-EXISTE");

        assertDoesNotThrow(() -> invoker.executeCommand(cancel));
        assertEquals(0, receiver.getAllSales().size());
    }
}