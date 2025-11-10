package Junit.facade;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.facade.controller.*;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.adapter.model.*;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;

class FacadeSaleTest {

    @Test
    void testSuccessfulSaleProcess() {
        InventSystem invent = new InventSystem();
        CashMethod cash = new CashMethod(100000);
        CreditCardMethod card = new CreditCardMethod("1234", "Farmacia", "999", 200000);
        EWalletMethod wallet = new EWalletMethod("WALLET", 50000);
        AdapterPayMethod adapter = new AdapterPayMethod(cash, card, wallet);
        PaySystem pay = new PaySystem(adapter);
        ReceiptSystem receipt = new ReceiptSystem();

        FacadeSale facade = new FacadeSale(invent, pay, receipt);

        FCreator creator = new FCreator();
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "Amoxicilina", "GenFarma", 8000);
        invent.addToStock(med);

        Sale sale = new Sale("FAC-001", "Cliente Test");
        sale.addMedicine(med);

        boolean result = facade.doSale(sale, "CASH");
        assertTrue(result);
    }
}