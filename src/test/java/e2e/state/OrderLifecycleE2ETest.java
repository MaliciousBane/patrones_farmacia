package e2e.state;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.state.model.Order;

class OrderLifecycleE2ETest {

    @Test
    void shouldTransitionThroughAllOrderStates() {
        Order order = new Order("ORD-E2E");
        
        assertEquals("Pendiente", order.getStateName(), 
                     "La orden debe comenzar en estado Pendiente");
        
        order.process();
        order.process();
        
        assertEquals("Entregado", order.getStateName(), 
                     "La orden debe transicionar a estado Entregado después de procesar");
    }
}