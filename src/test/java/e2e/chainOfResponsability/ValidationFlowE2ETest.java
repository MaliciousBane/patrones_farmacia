package e2e.chainOfResponsability;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.chainOfResponsability.controller.FinalizeSaleHandler;
import patrones_farmacia.chainOfResponsability.controller.PaymentValidationHandler;
import patrones_farmacia.chainOfResponsability.controller.StockValidationHandler;
import patrones_farmacia.chainOfResponsability.model.*;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;

class ValidationFlowE2ETest {

    @Test
    void testValidationChainForSale() {
        FCreator creator = new FCreator();
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "Amoxicilina", "GenFarma", 4500);
        Sale sale = new Sale("CHAIN-E2E", "Cliente E2E");
        sale.addMedicine(med);

        Handler stock = new StockValidationHandler(null);
        Handler pay = new PaymentValidationHandler(null);
        Handler finalizer = new FinalizeSaleHandler();
        stock.setNext(pay);
        pay.setNext(finalizer);

        assertTrue(stock.handle(sale));
    }
}