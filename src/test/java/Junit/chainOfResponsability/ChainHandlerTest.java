package Junit.chainOfResponsability;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.chainOfResponsability.controller.FinalizeSaleHandler;
import patrones_farmacia.chainOfResponsability.controller.PaymentValidationHandler;
import patrones_farmacia.chainOfResponsability.controller.StockValidationHandler;
import patrones_farmacia.chainOfResponsability.model.*;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.factoryMethod.model.Medicine;
import patrones_farmacia.factoryMethod.controller.FCreator;

class ChainHandlerTest {

    @Test
    void testChainSuccessFlow() {
        FCreator creator = new FCreator();
        Sale sale = new Sale("CHAIN001", "Cliente");
        sale.addMedicine(creator.createMedicine(FCreator.Type.GENERIC, "Dolex", "GSK", 3000));

        Handler stock = new StockValidationHandler(null);
        Handler payment = new PaymentValidationHandler(null);
        Handler finish = new FinalizeSaleHandler();
        stock.setNext(payment);
        payment.setNext(finish);

        assertTrue(stock.handle(sale));
    }
}