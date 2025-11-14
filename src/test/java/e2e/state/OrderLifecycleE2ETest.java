package e2e.state;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.state.model.*;
import patrones_farmacia.state.controller.StateController;

@DisplayName("State Pattern - Ciclo de Vida de Órdenes E2E")
class OrderLifecycleE2ETest {

    private StateController controller;
    private Order order;

    @BeforeEach
    void setUp() {
        controller = new StateController();
    }

    @Test
    @DisplayName("Debe crear orden en estado pendiente")
    void testCreateOrderInPendientState() {
        Order ord = controller.createOrder("ORD-001");
        assertNotNull(ord);
        assertEquals("PENDIENTE", ord.getStateName());
    }

    @Test
    @DisplayName("Debe transicionar a estado pagado")
    void testTransitionToPaidState() {
        Order ord = controller.createOrder("ORD-002");
        controller.processOrder("ORD-002");
        assertEquals("PAGADO", ord.getStateName());
    }

    @Test
    @DisplayName("Debe transicionar a estado entregado")
    void testTransitionToDeliveredState() {
        Order ord = controller.createOrder("ORD-003");
        controller.processOrder("ORD-003");
        controller.processOrder("ORD-003");
        assertEquals("ENTREGADO", ord.getStateName());
    }

    @Test
    @DisplayName("Debe mantener estado entregado")
    void testRemainInDeliveredState() {
        Order ord = controller.createOrder("ORD-004");
        controller.processOrder("ORD-004");
        controller.processOrder("ORD-004");
        controller.processOrder("ORD-004");
        assertEquals("ENTREGADO", ord.getStateName());
    }

    @Test
    @DisplayName("Debe pasar por todos los estados")
    void testTransitionThroughAllOrderStates() {
        Order ord = controller.createOrder("ORD-E2E");
        assertEquals("PENDIENTE", ord.getStateName());
        ord.process();
        assertEquals("PAGADO", ord.getStateName());
        ord.process();
        assertEquals("ENTREGADO", ord.getStateName());
    }

    @Test
    @DisplayName("Debe agregar producto a orden")
    void testAddProductToOrder() {
        controller.createOrder("ORD-005");
        assertTrue(controller.addProductToOrder("ORD-005", "Paracetamol"));
    }

    @Test
    @DisplayName("Debe agregar múltiples productos")
    void testAddMultipleProductsToOrder() {
        controller.createOrder("ORD-006");
        assertTrue(controller.addProductToOrder("ORD-006", "Paracetamol"));
        assertTrue(controller.addProductToOrder("ORD-006", "Ibuprofeno"));
        assertTrue(controller.addProductToOrder("ORD-006", "Amoxicilina"));
    }

    @Test
    @DisplayName("Debe rechazar producto en orden inexistente")
    void testRejectProductInNonExistentOrder() {
        assertFalse(controller.addProductToOrder("ORD-FAKE", "Paracetamol"));
    }

    @Test
    @DisplayName("Debe procesar orden exitosamente")
    void testProcessOrderSuccessfully() {
        controller.createOrder("ORD-007");
        assertTrue(controller.processOrder("ORD-007"));
    }

    @Test
    @DisplayName("Debe rechazar procesamiento de orden inexistente")
    void testRejectProcessNonExistentOrder() {
        assertFalse(controller.processOrder("ORD-FAKE"));
    }

    @Test
    @DisplayName("Debe cambiar estado forzadamente")
    void testForceStateChange() {
        controller.createOrder("ORD-008");
        assertTrue(controller.setOrderState("ORD-008", new DeliveredState()));
        Order ord = controller.createOrder("ORD-009");
        controller.setOrderState("ORD-009", new DeliveredState());
        assertEquals("ENTREGADO", controller.findOrder("ORD-009").getStateName());
    }

    @Test
    @DisplayName("Debe listar órdenes en resumen")
    void testListOrdersSummary() {
        controller.createOrder("ORD-010");
        controller.createOrder("ORD-011");
        controller.createOrder("ORD-012");
        var summary = controller.listOrdersSummary();
        assertEquals(3, summary.size());
        assertTrue(summary.stream().anyMatch(s -> s.contains("ORD-010")));
    }

    @Test
    @DisplayName("Debe encontrar orden por ID")
    void testFindOrderById() {
        controller.createOrder("ORD-013");
        Order found = controller.findOrder("ORD-013");
        assertNotNull(found);
        assertEquals("ORD-013", found.getId());
    }

    @Test
    @DisplayName("Debe buscar orden case-insensitive")
    void testFindOrderCaseInsensitive() {
        controller.createOrder("ORD-014");
        Order found = controller.findOrder("ord-014");
        assertNotNull(found);
        assertEquals("ORD-014", found.getId());
    }

    @Test
    @DisplayName("Debe retornar nulo para ID inexistente")
    void testReturnNullForNonExistentId() {
        Order found = controller.findOrder("ORD-INEXISTENTE");
        assertNull(found);
    }

    @Test
    @DisplayName("Debe rechazar ID nulo en creación")
    void testRejectNullIdOnCreation() {
        assertThrows(IllegalArgumentException.class, () -> controller.createOrder(null));
    }

    @Test
    @DisplayName("Debe rechazar ID vacío en creación")
    void testRejectEmptyIdOnCreation() {
        assertThrows(IllegalArgumentException.class, () -> controller.createOrder(""));
    }

    @Test
    @DisplayName("Debe rechazar ID con solo espacios")
    void testRejectWhitespaceIdOnCreation() {
        assertThrows(IllegalArgumentException.class, () -> controller.createOrder("   "));
    }

    @Test
    @DisplayName("Debe preservar ID de orden")
    void testPreserveOrderId() {
        Order ord = controller.createOrder("ORD-UNIQUE-001");
        String idBefore = ord.getId();
        controller.processOrder("ORD-UNIQUE-001");
        assertEquals(idBefore, ord.getId());
    }

    @Test
    @DisplayName("Debe procesar múltiples órdenes secuencialmente")
    void testProcessMultipleOrdersSequentially() {
        for(int i = 1; i <= 5; i++) {
            String ordId = "ORD-SEQ-" + i;
            controller.createOrder(ordId);
            controller.addProductToOrder(ordId, "Producto" + i);
            controller.processOrder(ordId);
        }
        var summary = controller.listOrdersSummary();
        assertEquals(5, summary.size());
    }

    @Test
    @DisplayName("Debe mantener productos después de procesamiento")
    void testPreserveProductsAfterProcessing() {
        controller.createOrder("ORD-015");
        controller.addProductToOrder("ORD-015", "Paracetamol");
        controller.addProductToOrder("ORD-015", "Ibuprofeno");
        controller.processOrder("ORD-015");
        Order ord = controller.findOrder("ORD-015");
        assertEquals(2, ord.getProducts().size());
    }

    @Test
    @DisplayName("Debe obtener estado correcto después de cambio forzado")
    void testGetCorrectStateAfterForcedChange() {
        controller.createOrder("ORD-016");
        controller.setOrderState("ORD-016", new PayState());
        Order ord = controller.findOrder("ORD-016");
        assertEquals("PAGADO", ord.getStateName());
    }

    @Test
    @DisplayName("Debe flujo completo: crear -> agregar -> procesar")
    void testCompleteFlowCreateAddProcess() {
        controller.createOrder("ORD-017");
        assertTrue(controller.addProductToOrder("ORD-017", "Paracetamol"));
        assertTrue(controller.processOrder("ORD-017"));
        Order ord = controller.findOrder("ORD-017");
        assertEquals("PAGADO", ord.getStateName());
        assertEquals(1, ord.getProducts().size());
    }

    @Test
    @DisplayName("Debe flujo completo hasta entrega")
    void testCompleteFlowToDelivery() {
        controller.createOrder("ORD-018");
        controller.addProductToOrder("ORD-018", "Paracetamol");
        controller.addProductToOrder("ORD-018", "Ibuprofeno");
        controller.processOrder("ORD-018");
        controller.processOrder("ORD-018");
        Order ord = controller.findOrder("ORD-018");
        assertEquals("ENTREGADO", ord.getStateName());
    }

    @Test
    @DisplayName("Debe manejar múltiples órdenes independientes")
    void testMultipleIndependentOrders() {
        Order o1 = controller.createOrder("ORD-A");
        Order o2 = controller.createOrder("ORD-B");
        Order o3 = controller.createOrder("ORD-C");
        
        controller.processOrder("ORD-A");
        controller.processOrder("ORD-B");
        controller.processOrder("ORD-B");
        
        assertEquals("PAGADO", o1.getStateName());
        assertEquals("ENTREGADO", o2.getStateName());
        assertEquals("PENDIENTE", o3.getStateName());
    }

    @Test
    @DisplayName("Debe validar transiciones de estado")
    void testValidateStateTransitions() {
        Order ord = new Order("ORD-019");
        assertDoesNotThrow(ord::process);
        assertEquals("PAGADO", ord.getStateName());
        assertDoesNotThrow(ord::process);
        assertEquals("ENTREGADO", ord.getStateName());
    }

    @Test
    @DisplayName("Debe resumen incluya todos los estados")
    void testSummaryIncludeAllStates() {
        controller.createOrder("ORD-020");
        Order o2 = controller.createOrder("ORD-021");
        Order o3 = controller.createOrder("ORD-022");
        
        controller.processOrder("ORD-021");
        controller.processOrder("ORD-022");
        controller.processOrder("ORD-022");
        
        var summary = controller.listOrdersSummary();
        assertTrue(summary.stream().anyMatch(s -> s.contains("PENDIENTE")));
        assertTrue(summary.stream().anyMatch(s -> s.contains("PAGADO")));
        assertTrue(summary.stream().anyMatch(s -> s.contains("ENTREGADO")));
    }

    @Test
    @DisplayName("Debe rechazar cambio de estado en orden nula")
    void testRejectStateChangeOnNullOrder() {
        assertFalse(controller.setOrderState("ORD-FAKE", new PayState()));
    }

    @Test
    @DisplayName("Debe obtener lista vacía sin órdenes")
    void testEmptyOrdersList() {
        var summary = controller.listOrdersSummary();
        assertTrue(summary.isEmpty());
        assertEquals(0, summary.size());
    }

    @Test
    @DisplayName("Debe crear orden con nombre especial")
    void testCreateOrderWithSpecialName() {
        Order ord = controller.createOrder("ORD-2024-001");
        assertNotNull(ord);
        assertEquals("ORD-2024-001", ord.getId());
    }

    @Test
    @DisplayName("Debe procesar orden con muchos productos")
    void testProcessOrderWithManyProducts() {
        controller.createOrder("ORD-023");
        for(int i = 0; i < 10; i++) {
            controller.addProductToOrder("ORD-023", "Producto" + i);
        }
        Order ord = controller.findOrder("ORD-023");
        assertEquals(10, ord.getProducts().size());
        controller.processOrder("ORD-023");
        assertEquals("PAGADO", ord.getStateName());
    }
}