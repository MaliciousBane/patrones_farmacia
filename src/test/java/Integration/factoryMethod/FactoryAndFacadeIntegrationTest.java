package Integration.factoryMethod;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.facade.controller.*;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;
import patrones_farmacia.adapter.model.*;

class FactoryAndFacadeIntegrationTest {

    @Test
    void testCreateMedicineAndProcessSale() {
        FCreator creator = new FCreator();
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "Ibuprofeno", "GenFarma", 2500);

        InventSystem invent = new InventSystem();
        invent.addToStock(med);
        CashMethod cash = new CashMethod(50000);
        CreditCardMethod card = new CreditCardMethod("123", "Cliente", "999", 100000);
        EWalletMethod wallet = new EWalletMethod("WAL-001", 20000);
        AdapterPayMethod adapter = new AdapterPayMethod(cash, card, wallet);
        PaySystem pay = new PaySystem(adapter);
        ReceiptSystem receipt = new ReceiptSystem();
        FacadeSale facade = new FacadeSale(invent, pay, receipt);

        Sale sale = new Sale("INT-001", "Cliente");
        sale.addMedicine(med);

        assertTrue(facade.doSale(sale, "CASH"));
    }
}