package Integration.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.observer.model.InventorySubject;
import patrones_farmacia.observer.model.FarmaObserver;
import patrones_farmacia.observer.controller.InventoryController;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

class ObserverIntegrationTest {

    private InventorySubject subject;
    private InventoryController controller;

    @BeforeEach
    void init() {
        subject = new InventorySubject(5);
        controller = new InventoryController();
    }

    @Test
    void observersAreNotifiedWhenStockFallsBelowThreshold() {
        AtomicReference<String> lastName = new AtomicReference<>("");
        AtomicInteger lastStock = new AtomicInteger(-1);
        FarmaObserver o = (name, stock) -> {
            lastName.set(name);
            lastStock.set(stock);
        };
        subject.addObserver(o);
        subject.addProduct("ProdA", 10);
        subject.reduceStock("ProdA", 7);
        assertEquals("ProdA", lastName.get());
        assertEquals(3, lastStock.get());
    }

    @Test
    void removeObserverPreventsFurtherNotifications() {
        AtomicInteger counter = new AtomicInteger(0);
        FarmaObserver o = (name, stock) -> counter.incrementAndGet();
        subject.addObserver(o);
        subject.addProduct("X", 6);
        subject.reduceStock("X", 2);
        assertTrue(counter.get() > 0);
        int before = counter.get();
        subject.removeObserver(o);
        subject.reduceStock("X", 1);
        assertEquals(before, counter.get());
    }

    @Test
    void addAndIncreaseAndGetStockAndAllProducts() {
        subject.addProduct("P1", 3);
        subject.addProduct("P2", 8);
        assertEquals(3, subject.getStockLevel("P1"));
        subject.increaseStock("P1", 5);
        assertEquals(8, subject.getStockLevel("P1"));
        List<String> names = subject.getAllProductNames();
        assertTrue(names.contains("P1"));
        assertTrue(names.contains("P2"));
    }

    @Test
    void getLowStockProductsRespectsThresholdAndSetMinThreshold() {
        subject.addProduct("L1", 5);
        subject.addProduct("L2", 6);
        List<String> low1 = subject.getLowStockProducts();
        assertTrue(low1.contains("L1"));
        assertFalse(low1.contains("L2"));
        subject.setMinThreshold(6);
        List<String> low2 = subject.getLowStockProducts();
        assertTrue(low2.contains("L1"));
        assertTrue(low2.contains("L2"));
    }

    @Test
    void reducingNonExistingProductReturnsNoChangeAndGetStockLevelIsMinusOne() {
        subject.addProduct("Exists", 4);
        subject.reduceStock("NoExiste", 2);
        assertEquals(-1, subject.getStockLevel("NoExiste"));
        assertEquals(4, subject.getStockLevel("Exists"));
    }

    @Test
    void multipleObserversReceiveNotifications() {
        AtomicInteger a = new AtomicInteger(0);
        AtomicInteger b = new AtomicInteger(0);
        FarmaObserver o1 = (n, s) -> a.incrementAndGet();
        FarmaObserver o2 = (n, s) -> b.incrementAndGet();
        subject.addObserver(o1);
        subject.addObserver(o2);
        subject.addProduct("Multi", 6);
        subject.reduceStock("Multi", 3);
        assertEquals(1, a.get());
        assertEquals(1, b.get());
    }

    @Test
    void controllerIntegrationRegistersBuiltInObserversAndThresholdManagementWorks() {
        controller.setThreshold(2);
        controller.registerEmailAlert("e@mail");
        controller.registerSMSAlert("300");
        controller.addProduct("C1", 4);
        assertEquals(4, controller.getStock("C1"));
        controller.reduceStock("C1", 3);
        assertTrue(controller.getLowStockProducts().contains("C1"));
        controller.restock("C1", 5);
        assertEquals(6, controller.getStock("C1"));
    }
}