package e2e.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.observer.model.*;
import patrones_farmacia.observer.controller.InventoryController;

@DisplayName("Observer Pattern - Notificaciones de Stock E2E")
class StockNotificationE2ETest {

    private InventorySubject subject;
    private InventoryController controller;
    private EmailAlert emailAlert;
    private SMSAlert smsAlert;

    @BeforeEach
    void setUp() {
        subject = new InventorySubject(5);
        controller = new InventoryController();
        emailAlert = new EmailAlert("admin@farmacia.com");
        smsAlert = new SMSAlert("+573001001001");
    }

    @Test
    @DisplayName("Debe crear sujeto de inventario exitosamente")
    void testCreateInventorySubject() {
        assertNotNull(subject);
        assertEquals(5, subject.getMinThreshold());
    }

    @Test
    @DisplayName("Debe agregar observador exitosamente")
    void testAddObserver() {
        subject.addObserver(emailAlert);
        assertDoesNotThrow(() -> subject.notifyObservers("Paracetamol", 3));
    }

    @Test
    @DisplayName("Debe agregar múltiples observadores")
    void testAddMultipleObservers() {
        subject.addObserver(emailAlert);
        subject.addObserver(smsAlert);
        assertDoesNotThrow(() -> subject.notifyObservers("Ibuprofeno", 2));
    }

    @Test
    @DisplayName("Debe remover observador exitosamente")
    void testRemoveObserver() {
        subject.addObserver(emailAlert);
        subject.removeObserver(emailAlert);
        assertDoesNotThrow(() -> subject.notifyObservers("Producto", 1));
    }

    @Test
    @DisplayName("Debe agregar producto al inventario")
    void testAddProduct() {
        subject.addProduct("Paracetamol", 10);
        assertEquals(10, subject.getStockLevel("Paracetamol"));
    }

    @Test
    @DisplayName("Debe agregar múltiples productos")
    void testAddMultipleProducts() {
        subject.addProduct("Paracetamol", 10);
        subject.addProduct("Ibuprofeno", 15);
        subject.addProduct("Amoxicilina", 8);
        assertEquals(3, subject.getAllProductNames().size());
    }

    @Test
    @DisplayName("Debe reducir stock de producto")
    void testReduceStock() {
        subject.addProduct("Paracetamol", 10);
        subject.reduceStock("Paracetamol", 3);
        assertEquals(7, subject.getStockLevel("Paracetamol"));
    }

    @Test
    @DisplayName("Debe notificar cuando stock alcanza umbral")
    void testNotifyWhenStockLow() {
        subject.addObserver(emailAlert);
        subject.addObserver(smsAlert);
        subject.addProduct("Ibuprofeno", 10);
        assertDoesNotThrow(() -> subject.reduceStock("Ibuprofeno", 7));
    }

    @Test
    @DisplayName("Debe prevenir stock negativo")
    void testPreventNegativeStock() {
        subject.addProduct("Paracetamol", 5);
        subject.reduceStock("Paracetamol", 10);
        assertEquals(0, subject.getStockLevel("Paracetamol"));
    }

    @Test
    @DisplayName("Debe incrementar stock exitosamente")
    void testIncreaseStock() {
        subject.addProduct("Ibuprofeno", 5);
        subject.increaseStock("Ibuprofeno", 10);
        assertEquals(15, subject.getStockLevel("Ibuprofeno"));
    }

    @Test
    @DisplayName("Debe obtener stock de producto")
    void testGetStock() {
        subject.addProduct("Amoxicilina", 20);
        assertEquals(20, subject.getStockLevel("Amoxicilina"));
    }

    @Test
    @DisplayName("Debe retornar -1 para producto inexistente")
    void testGetStockNonExistent() {
        assertEquals(-1, subject.getStockLevel("NoExiste"));
    }

    @Test
    @DisplayName("Debe buscar producto case-insensitive")
    void testSearchCaseInsensitive() {
        subject.addProduct("Paracetamol", 10);
        assertEquals(10, subject.getStockLevel("paracetamol"));
        assertEquals(10, subject.getStockLevel("PARACETAMOL"));
    }

    @Test
    @DisplayName("Debe retornar productos con bajo stock")
    void testGetLowStockProducts() {
        subject.addProduct("Paracetamol", 3);
        subject.addProduct("Ibuprofeno", 10);
        subject.addProduct("Amoxicilina", 2);
        assertEquals(2, subject.getLowStockProducts().size());
    }

    @Test
    @DisplayName("Debe cambiar umbral de alerta")
    void testChangeThreshold() {
        subject.setMinThreshold(10);
        assertEquals(10, subject.getMinThreshold());
    }

    @Test
    @DisplayName("Debe procesar múltiples reducciones")
    void testMultipleReductions() {
        subject.addProduct("Producto", 20);
        subject.reduceStock("Producto", 5);
        subject.reduceStock("Producto", 5);
        subject.reduceStock("Producto", 5);
        assertEquals(5, subject.getStockLevel("Producto"));
    }

    @Test
    @DisplayName("Debe procesar múltiples incrementos")
    void testMultipleIncreases() {
        subject.addProduct("Producto", 10);
        subject.increaseStock("Producto", 5);
        subject.increaseStock("Producto", 5);
        subject.increaseStock("Producto", 5);
        assertEquals(25, subject.getStockLevel("Producto"));
    }

    @Test
    @DisplayName("Debe registrar alerta por correo")
    void testRegisterEmailAlert() {
        controller.registerEmailAlert("test@farmacia.com");
        controller.addProduct("Paracetamol", 10);
        assertDoesNotThrow(() -> controller.reduceStock("Paracetamol", 8));
    }

    @Test
    @DisplayName("Debe registrar alerta por SMS")
    void testRegisterSMSAlert() {
        controller.registerSMSAlert("+573001234567");
        controller.addProduct("Ibuprofeno", 10);
        assertDoesNotThrow(() -> controller.reduceStock("Ibuprofeno", 7));
    }

    @Test
    @DisplayName("Debe obtener stock desde controlador")
    void testGetStockFromController() {
        controller.addProduct("Amoxicilina", 15);
        assertEquals(15, controller.getStock("Amoxicilina"));
    }

    @Test
    @DisplayName("Debe obtener productos bajo stock")
    void testGetLowStockFromController() {
        controller.addProduct("Producto1", 2);
        controller.addProduct("Producto2", 20);
        controller.addProduct("Producto3", 3);
        assertEquals(2, controller.getLowStockProducts().size());
    }

    @Test
    @DisplayName("Debe obtener todos los productos")
    void testGetAllProductsFromController() {
        controller.addProduct("Med1", 10);
        controller.addProduct("Med2", 15);
        controller.addProduct("Med3", 20);
        assertEquals(3, controller.getAllProducts().size());
    }

    @Test
    @DisplayName("Debe cambiar umbral desde controlador")
    void testChangeThresholdFromController() {
        controller.setThreshold(20);
        assertEquals(20, controller.getThreshold());
    }

    @Test
    @DisplayName("Debe reabastecer producto")
    void testRestockProduct() {
        controller.addProduct("Producto", 10);
        controller.restock("Producto", 15);
        assertEquals(25, controller.getStock("Producto"));
    }

    @Test
    @DisplayName("Debe manejar nombres especiales")
    void testSpecialNames() {
        subject.addProduct("Paracetamol-500mg", 10);
        subject.addProduct("Ibupirac Plus+", 15);
        assertEquals(10, subject.getStockLevel("Paracetamol-500mg"));
        assertEquals(15, subject.getStockLevel("Ibupirac Plus+"));
    }

    @Test
    @DisplayName("Debe mantener independencia de observadores")
    void testObserverIndependence() {
        subject.addObserver(new EmailAlert("email1@test.com"));
        subject.addObserver(new EmailAlert("email2@test.com"));
        subject.addObserver(new SMSAlert("+123456789"));
        assertDoesNotThrow(() -> subject.notifyObservers("Producto", 2));
    }

    @Test
    @DisplayName("Debe notificar exactamente en umbral")
    void testNotifyAtThreshold() {
        subject.addObserver(emailAlert);
        subject.setMinThreshold(5);
        subject.addProduct("Producto", 10);
        assertDoesNotThrow(() -> subject.reduceStock("Producto", 5));
    }

    @Test
    @DisplayName("Debe mantener integridad de datos")
    void testDataIntegrity() {
        subject.addProduct("Paracetamol", 100);
        subject.reduceStock("Paracetamol", 20);
        subject.increaseStock("Paracetamol", 10);
        subject.reduceStock("Paracetamol", 15);
        assertEquals(75, subject.getStockLevel("Paracetamol"));
    }

    @Test
    @DisplayName("Debe rechazar reducción de inexistente")
    void testRejectReduceNonExistent() {
        subject.reduceStock("NoExiste", 5);
        assertEquals(-1, subject.getStockLevel("NoExiste"));
    }

    @Test
    @DisplayName("Debe procesar stock muy alto")
    void testVeryHighStock() {
        subject.addProduct("Producto", 999999);
        assertEquals(999999, subject.getStockLevel("Producto"));
    }

    @Test
    @DisplayName("Debe procesar stock muy bajo")
    void testVeryLowStock() {
        subject.addProduct("Producto", 1);
        subject.reduceStock("Producto", 1);
        assertEquals(0, subject.getStockLevel("Producto"));
    }

    @Test
    @DisplayName("Debe mantener lista de productos")
    void testMaintainProductList() {
        subject.addProduct("Med1", 10);
        subject.addProduct("Med2", 20);
        subject.addProduct("Med3", 30);
        subject.reduceStock("Med1", 5);
        subject.increaseStock("Med2", 5);
        assertEquals(3, subject.getAllProductNames().size());
    }

    @Test
    @DisplayName("Debe crear observadores correctamente")
    void testCreateObservers() {
        EmailAlert email = new EmailAlert("test@mail.com");
        SMSAlert sms = new SMSAlert("+573001234567");
        assertTrue(email.toString().contains("test@mail.com"));
        assertTrue(sms.toString().contains("+573001234567"));
    }

    @Test
    @DisplayName("Debe flujo completo")
    void testCompleteFlow() {
        subject.addObserver(emailAlert);
        subject.addObserver(smsAlert);
        subject.addProduct("Ibuprofeno", 15);
        subject.setMinThreshold(5);
        subject.reduceStock("Ibuprofeno", 11);
        assertEquals(4, subject.getStockLevel("Ibuprofeno"));
        assertTrue(subject.getLowStockProducts().contains("Ibuprofeno"));
    }

    @Test
    @DisplayName("Debe manejar múltiples instancias")
    void testMultipleInstances() {
        InventorySubject s1 = new InventorySubject(5);
        InventorySubject s2 = new InventorySubject(10);
        s1.addProduct("Producto", 8);
        s2.addProduct("Producto", 8);
        s1.reduceStock("Producto", 4);
        s2.reduceStock("Producto", 4);
        assertEquals(4, s1.getStockLevel("Producto"));
        assertEquals(4, s2.getStockLevel("Producto"));
    }
}