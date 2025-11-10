package e2e.command;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.command.controller.*;
import patrones_farmacia.command.model.SaleReceiver;
import patrones_farmacia.facade.model.Sale;

class SalesCommandE2ETest {

    @Test
    void testRegisterAndCancelSalesUsingCommands() {
        SaleReceiver receiver = new SaleReceiver();
        CashierInvoker invoker = new CashierInvoker();

        Sale sale = new Sale("CMD-E2E", "Cliente E2E");
        RegisterSaleCommand register = new RegisterSaleCommand(receiver, sale);
        CancelSaleCommand cancel = new CancelSaleCommand(receiver, "CMD-E2E");

        assertDoesNotThrow(() -> {
            invoker.executeCommand(register);
            invoker.executeCommand(cancel);
        });
    }
}