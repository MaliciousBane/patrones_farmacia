package Integration.state;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.state.model.Order;

class StateIntegrationTest {

    @Test
    void testOrderStateProgression() {
        Order order = new Order("ORD-INT-001");
        assertEquals("Pendiente", order.getStateName());

        order.process();
        assertEquals("Pagado", order.getStateName());

        order.process();
        assertEquals("Entregado", order.getStateName());

        order.process();
        assertEquals("Entregado", order.getStateName());
    }
}