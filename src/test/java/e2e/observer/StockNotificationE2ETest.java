package e2e.observer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.observer.model.*;

class StockNotificationE2ETest {

    @Test
    void testLowStockAlertsAreSent() {
        InventorySubject subject = new InventorySubject(5);
        subject.addObserver(new EmailAlert("admin@farmacia.com"));
        subject.addObserver(new SMSAlert("+573001001001"));
        subject.addProduct("Ibuprofeno", 10);

        assertDoesNotThrow(() -> subject.addProduct("Ibuprofeno", 2));
    }
}
