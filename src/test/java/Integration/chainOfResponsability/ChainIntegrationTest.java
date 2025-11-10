package Integration.chainOfResponsability;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.chainOfResponsability.controller.FinalizeSaleHandler;
import patrones_farmacia.chainOfResponsability.controller.PaymentValidationHandler;
import patrones_farmacia.chainOfResponsability.controller.StockValidationHandler;
import patrones_farmacia.chainOfResponsability.model.*;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;
import patrones_farmacia.facade.model.Sale;

class ChainIntegrationTest {

    @Test
    void testFullValidationChain() {
        // Crear medicamento y venta
        FCreator creator = new FCreator();
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "Ibuprofeno", "GenFarma", 2500);
        Sale sale = new Sale("CHAIN-001", "Cliente Integración");
        sale.addMedicine(med);

        // Construir la cadena de responsabilidad
        Handler stock = new StockValidationHandler(null);
        Handler payment = new PaymentValidationHandler(null);
        Handler finalize = new FinalizeSaleHandler();

        stock.setNext(payment);
        payment.setNext(finalize);

        // Ejecutar flujo
        assertTrue(stock.handle(sale));
    }
}