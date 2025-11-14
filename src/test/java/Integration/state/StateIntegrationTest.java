package Integration.state;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import patrones_farmacia.state.controller.StateController;
import patrones_farmacia.state.model.Order;
import patrones_farmacia.state.model.DeliveredState;

class StateIntegrationTest {

    @Test
    void testCreateOrderAndDefaults() {
        StateController ctrl = new StateController();
        Order o = ctrl.createOrder("ORD-001");
        assertNotNull(o);
        assertEquals("ORD-001", o.getId());
        assertEquals("PENDIENTE", o.getStateName());
        List<String> summary = ctrl.listOrdersSummary();
        assertEquals(1, summary.size());
        assertEquals("ID:ORD-001 | Estado:PENDIENTE", summary.get(0));
    }

    @Test
    void testAddProductAndFind() {
        StateController ctrl = new StateController();
        ctrl.createOrder("ORD-002");
        assertTrue(ctrl.addProductToOrder("ORD-002", "Paracetamol"));
        Order found = ctrl.findOrder("ORD-002");
        assertNotNull(found);
        List<String> products = found.getProducts();
        assertEquals(1, products.size());
        assertEquals("Paracetamol", products.get(0));
        products.add("Otro");
        List<String> productsAfter = found.getProducts();
        assertEquals(1, productsAfter.size());
    }

    @Test
    void testProcessTransitions() {
        StateController ctrl = new StateController();
        ctrl.createOrder("ORD-003");
        assertTrue(ctrl.processOrder("ORD-003"));
        assertEquals("PAGADO", ctrl.findOrder("ORD-003").getStateName());
        assertTrue(ctrl.processOrder("ORD-003"));
        assertEquals("ENTREGADO", ctrl.findOrder("ORD-003").getStateName());
        assertTrue(ctrl.processOrder("ORD-003"));
        assertEquals("ENTREGADO", ctrl.findOrder("ORD-003").getStateName());
    }

    @Test
    void testSetOrderStateAndBehavior() {
        StateController ctrl = new StateController();
        ctrl.createOrder("ORD-004");
        assertTrue(ctrl.setOrderState("ORD-004", new DeliveredState()));
        assertEquals("ENTREGADO", ctrl.findOrder("ORD-004").getStateName());
        assertTrue(ctrl.processOrder("ORD-004"));
        assertEquals("ENTREGADO", ctrl.findOrder("ORD-004").getStateName());
    }

    @Test
    void testInvalidCreateAndNotFoundOperations() {
        StateController ctrl = new StateController();
        assertThrows(IllegalArgumentException.class, () -> ctrl.createOrder(null));
        assertThrows(IllegalArgumentException.class, () -> ctrl.createOrder("   "));
        assertFalse(ctrl.addProductToOrder("NO-EXISTE", "Item"));
        assertFalse(ctrl.processOrder("NO-EXISTE"));
        assertFalse(ctrl.setOrderState("NO-EXISTE", new DeliveredState()));
        assertNull(ctrl.findOrder("NO-EXISTE"));
    }

}