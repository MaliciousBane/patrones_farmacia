package Junit.state;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.state.controller.StateController;
import patrones_farmacia.state.model.*;
import patrones_farmacia.state.view.StateConsole;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

@DisplayName("State Pattern - Órdenes y Controller E2E ampliadas")
class OrderStateTest {

    private StateController controller;

    @BeforeEach
    void init() {
        controller = new StateController();
    }

    @Test
    @DisplayName("Crear orden valida ID y retorna objeto con estado PENDIENTE")
    void createOrderValidAndState() {
        Order o = controller.createOrder("ORD-100");
        assertNotNull(o);
        assertEquals("PENDIENTE", o.getStateName());
        assertEquals("ORD-100", o.getId());
    }

    @Test
    @DisplayName("Crear orden con ID nulo lanza excepción")
    void createOrderNullIdThrows() {
        assertThrows(IllegalArgumentException.class, () -> controller.createOrder(null));
    }

    @Test
    @DisplayName("Crear orden con ID vacío lanza excepción")
    void createOrderEmptyIdThrows() {
        assertThrows(IllegalArgumentException.class, () -> controller.createOrder("   "));
    }

    @Test
    @DisplayName("Agregar producto a orden existente retorna true y muestra producto")
    void addProductToExistingOrder() {
        controller.createOrder("ORD-101");
        boolean r = controller.addProductToOrder("ORD-101", "Paracetamol");
        assertTrue(r);
        Order found = controller.findOrder("ORD-101");
        assertEquals(1, found.getProducts().size());
        assertTrue(found.getProducts().contains("Paracetamol"));
    }

    @Test
    @DisplayName("Agregar producto a orden inexistente retorna false")
    void addProductToNonExistentOrder() {
        assertFalse(controller.addProductToOrder("NO-ORD", "X"));
    }

    @Test
    @DisplayName("Procesar orden cambia estado a PAGADO")
    void processChangesToPaid() {
        controller.createOrder("ORD-102");
        assertTrue(controller.processOrder("ORD-102"));
        assertEquals("PAGADO", controller.findOrder("ORD-102").getStateName());
    }

    @Test
    @DisplayName("Procesar dos veces cambia a ENTREGADO")
    void processTwiceToDelivered() {
        controller.createOrder("ORD-103");
        controller.processOrder("ORD-103");
        controller.processOrder("ORD-103");
        assertEquals("ENTREGADO", controller.findOrder("ORD-103").getStateName());
    }

    @Test
    @DisplayName("Procesar orden inexistente retorna false")
    void processNonExistentReturnsFalse() {
        assertFalse(controller.processOrder("ORD-XXX"));
    }

    @Test
    @DisplayName("Forzar estado de orden existente funciona")
    void forceStateChangeWorks() {
        controller.createOrder("ORD-104");
        boolean ok = controller.setOrderState("ORD-104", new DeliveredState());
        assertTrue(ok);
        assertEquals("ENTREGADO", controller.findOrder("ORD-104").getStateName());
    }

    @Test
    @DisplayName("Forzar estado en orden inexistente retorna false")
    void forceStateOnMissingOrder() {
        assertFalse(controller.setOrderState("MISSING", new PayState()));
    }

    @Test
    @DisplayName("Listar resumen de órdenes contiene IDs y estados")
    void listOrdersSummaryContainsInfo() {
        controller.createOrder("ORD-105");
        controller.createOrder("ORD-106");
        controller.processOrder("ORD-106");
        List<String> summary = controller.listOrdersSummary();
        assertEquals(2, summary.size());
        assertTrue(summary.stream().anyMatch(s -> s.contains("ORD-105")));
        assertTrue(summary.stream().anyMatch(s -> s.contains("PENDIENTE") || s.contains("PAGADO")));
    }

    @Test
    @DisplayName("Buscar orden es case-insensitive")
    void findOrderCaseInsensitive() {
        controller.createOrder("ord-107");
        Order found = controller.findOrder("ORD-107");
        assertNotNull(found);
        assertEquals("ord-107", found.getId());
    }

    @Test
    @DisplayName("Múltiples órdenes mantienen estados independientes")
    void multipleOrdersIndependentStates() {
        controller.createOrder("A1");
        controller.createOrder("A2");
        controller.processOrder("A1");
        assertEquals("PAGADO", controller.findOrder("A1").getStateName());
        assertEquals("PENDIENTE", controller.findOrder("A2").getStateName());
    }

    @Test
    @DisplayName("Productos se preservan después de procesar")
    void productsPreservedAfterProcessing() {
        controller.createOrder("ORD-108");
        controller.addProductToOrder("ORD-108", "X");
        controller.addProductToOrder("ORD-108", "Y");
        controller.processOrder("ORD-108");
        Order o = controller.findOrder("ORD-108");
        assertEquals(2, o.getProducts().size());
        assertTrue(o.getProducts().contains("X"));
        assertTrue(o.getProducts().contains("Y"));
    }

    @Test
    @DisplayName("Order.getProducts devuelve copia defensiva")
    void getProductsReturnsDefensiveCopy() {
        controller.createOrder("ORD-109");
        controller.addProductToOrder("ORD-109", "P1");
        Order o = controller.findOrder("ORD-109");
        List<String> list = o.getProducts();
        list.add("MALICIOUS");
        assertEquals(1, o.getProducts().size());
    }

    @Test
    @DisplayName("Procesar más de necesario mantiene ENTREGADO")
    void processMoreKeepsDelivered() {
        controller.createOrder("ORD-110");
        controller.processOrder("ORD-110");
        controller.processOrder("ORD-110");
        controller.processOrder("ORD-110");
        assertEquals("ENTREGADO", controller.findOrder("ORD-110").getStateName());
    }

    @Test
    @DisplayName("Crear varias órdenes y verificar resumen contiene todas")
    void createManyOrdersAndVerifySummary() {
        for (int i = 0; i < 5; i++) controller.createOrder("B-" + i);
        List<String> sum = controller.listOrdersSummary();
        assertEquals(5, sum.size());
    }

    @Test
    @DisplayName("PendientState.manage imprime y cambia a PayState")
    void pendientStateManageBehavior() {
        Order o = new Order("S-1");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream old = System.out;
        try {
            System.setOut(new PrintStream(baos));
            o.process();
            String out = baos.toString();
            assertTrue(out.contains("confirmado") || out.contains("confirmada"));
            assertEquals("PAGADO", o.getStateName());
        } finally {
            System.setOut(old);
        }
    }

    @Test
    @DisplayName("PayState.manage imprime y cambia a DeliveredState")
    void payStateManageBehavior() {
        Order o = new Order("S-2");
        o.setState(new PayState());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream old = System.out;
        try {
            System.setOut(new PrintStream(baos));
            o.process();
            String out = baos.toString();
            assertTrue(out.contains("pagado") || out.contains("PAGADO"));
            assertEquals("ENTREGADO", o.getStateName());
        } finally {
            System.setOut(old);
        }
    }

    @Test
    @DisplayName("DeliveredState.manage imprime y mantiene ENTREGADO")
    void deliveredStateManageBehavior() {
        Order o = new Order("S-3");
        o.setState(new DeliveredState());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream old = System.out;
        try {
            System.setOut(new PrintStream(baos));
            o.process();
            String out = baos.toString();
            assertTrue(out.contains("ya fue entregado") || out.contains("entregado"));
            assertEquals("ENTREGADO", o.getStateName());
        } finally {
            System.setOut(old);
        }
    }

    @Test
    @DisplayName("StateController.createOrder imprime mensaje de creación")
    void controllerCreateOrderPrints() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream old = System.out;
        try {
            System.setOut(new PrintStream(baos));
            controller.createOrder("PRINT-1");
            String out = baos.toString();
            assertTrue(out.contains("Pedido creado") || out.contains("Pedido"));
        } finally {
            System.setOut(old);
        }
    }

    @Test
    @DisplayName("StateController.processOrder imprime flujo completo")
    void controllerProcessOrderPrintsFlow() {
        controller.createOrder("FLOW-1");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream old = System.out;
        try {
            System.setOut(new PrintStream(baos));
            controller.processOrder("FLOW-1");
            controller.processOrder("FLOW-1");
            String out = baos.toString();
            assertTrue(out.contains("Procesando pedido"));
            assertTrue(out.contains("Nuevo estado"));
        } finally {
            System.setOut(old);
        }
    }

    @Test
    @DisplayName("findOrder devuelve null cuando no existe")
    void findOrderReturnsNullWhenNotFound() {
        assertNull(controller.findOrder("NON-EX"));
    }

    @Test
    @DisplayName("setOrderState con distintos estados actualiza correctamente")
    void setOrderStateWithDifferentStates() {
        controller.createOrder("ST-1");
        assertTrue(controller.setOrderState("ST-1", new PayState()));
        assertEquals("PAGADO", controller.findOrder("ST-1").getStateName());
        assertTrue(controller.setOrderState("ST-1", new DeliveredState()));
        assertEquals("ENTREGADO", controller.findOrder("ST-1").getStateName());
    }

    @Test
    @DisplayName("StateConsole.run ejecuta secuencia sin excepciones y produce salida")
    void stateConsoleRunProducesOutput() {
        StateConsole console = new StateConsole();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream old = System.out;
        try {
            System.setOut(new PrintStream(baos));
            console.run();
            String out = baos.toString();
            assertTrue(out.contains("Pedido creado") || out.contains("Procesando pedido") || out.length() > 0);
        } finally {
            System.setOut(old);
        }
    }

    @Test
    @DisplayName("Order.id se preserva correctamente y es case-sensitive en almacenamiento")
    void orderIdPreservedAndCaseStored() {
        controller.createOrder("Case-Id");
        Order o = controller.findOrder("case-id");
        assertNotNull(o);
        assertEquals("Case-Id", o.getId());
    }

    @Test
    @DisplayName("Crear ordenes con nombres similares no colapsan")
    void createOrdersSimilarNames() {
        controller.createOrder("SIM-1");
        controller.createOrder("sim-1-2");
        assertNotNull(controller.findOrder("SIM-1"));
        assertNotNull(controller.findOrder("sim-1-2"));
    }

    @Test
    @DisplayName("Procesar sin productos aun cambia estado normalmente")
    void processWithoutProductsStillTransitions() {
        controller.createOrder("NOPROD-1");
        assertTrue(controller.processOrder("NOPROD-1"));
        assertEquals("PAGADO", controller.findOrder("NOPROD-1").getStateName());
    }

    @Test
    @DisplayName("Repeated processing of many orders remains consistent")
    void repeatedProcessingManyOrders() {
        for (int i = 0; i < 10; i++) {
            String id = "RP-" + i;
            controller.createOrder(id);
            controller.processOrder(id);
            controller.processOrder(id);
            assertEquals("ENTREGADO", controller.findOrder(id).getStateName());
        }
    }
}