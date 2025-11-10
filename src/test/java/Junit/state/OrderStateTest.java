package Junit.state;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.state.model.*;

class OrderTest {

    @Test
    void testStateTransitions() {
        Order order = new Order("ORD-001");

        assertEquals("Pendiente", order.getStateName());

        order.process();
        assertEquals("Pagado", order.getStateName());

        order.process();
        assertEquals("Entregado", order.getStateName());

        order.process();
        assertEquals("Entregado", order.getStateName());
    }
}