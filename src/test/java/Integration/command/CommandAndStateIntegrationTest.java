package Integration.command;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.command.controller.*;
import patrones_farmacia.command.model.SaleReceiver;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.state.model.Order;

class CommandAndStateIntegrationTest {

    @Test
    void testRegisterSaleAndAdvanceOrderState() {
        SaleReceiver receiver = new SaleReceiver();
        CashierInvoker invoker = new CashierInvoker();

        Sale sale = new Sale("CMD-001", "Cliente Test");
        RegisterSaleCommand cmd = new RegisterSaleCommand(receiver, sale);
        invoker.executeCommand(cmd);

        Order order = new Order("ORD-001");
        assertEquals("Pendiente", order.getStateName());
        order.process();
        assertEquals("Pagado", order.getStateName());
    }
}