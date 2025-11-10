package Integration.observer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.observer.model.*;
import patrones_farmacia.chainOfResponsability.controller.FinalizeSaleHandler;
import patrones_farmacia.chainOfResponsability.controller.PaymentValidationHandler;
import patrones_farmacia.chainOfResponsability.controller.StockValidationHandler;
import patrones_farmacia.chainOfResponsability.model.*;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;
import patrones_farmacia.facade.model.Sale;

class ObserverAndChainIntegrationTest {

    @Test
    void testLowStockTriggersNotificationAndChainCompletes() {
        InventorySubject subject = new InventorySubject(5);
        FarmaObserver obs = new EmailAlert("alertas@farmacia.com");
        subject.addObserver(obs);
        subject.addProduct("Dolex", 10);

        // Bajar stock
        subject.increaseStock("Dolex", 3);

        // Simular venta validada con Chain
        FCreator creator = new FCreator();
        Medicine med = creator.createMedicine(FCreator.Type.BRAND, "Dolex", "GSK", 6000);
        Sale sale = new Sale("INT-002", "Cliente");
        sale.addMedicine(med);

        Handler stock = new StockValidationHandler(null);
        Handler payment = new PaymentValidationHandler(null);
        Handler finish = new FinalizeSaleHandler();
        stock.setNext(payment);
        payment.setNext(finish);

        assertTrue(stock.handle(sale));
    }
}