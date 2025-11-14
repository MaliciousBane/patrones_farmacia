package e2e.facade;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.facade.controller.*;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;
import patrones_farmacia.adapter.model.*;

class SaleProcessE2ETest {

    @Test
    void shouldCompleteEntireSaleProcessThroughFacade() {
        InventSystem invent = new InventSystem();
        FCreator creator = new FCreator();
        Medicine med = creator.createMedicine(FCreator.Type.BRAND, 
                                               "Dolex", "GSK", 6000);
        invent.addToStock(med);

        CashMethod cash = new CashMethod(100000);
        AdapterPayMethod adapter = new AdapterPayMethod(cash, null, null);
        PaySystem pay = new PaySystem(adapter);
        
        ReceiptSystem receipt = new ReceiptSystem();
        FacadeSale facade = new FacadeSale(invent, pay, receipt);

        Sale sale = new Sale("FAC-E2E", "Cliente E2E");
        sale.addMedicine(med);

        assertTrue(facade.doSale(sale, "CASH"), 
                   "La fachada debe completar la venta con inventario, pago y recibo");
    }
}