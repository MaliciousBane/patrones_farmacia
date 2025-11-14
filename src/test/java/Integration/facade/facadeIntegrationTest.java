package Integration.facade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import patrones_farmacia.facade.controller.FacadeSale;
import patrones_farmacia.facade.controller.InventSystem;
import patrones_farmacia.facade.controller.PaySystem;
import patrones_farmacia.facade.controller.ReceiptSystem;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;
import patrones_farmacia.adapter.model.CashMethod;
import patrones_farmacia.adapter.model.CreditCardMethod;
import patrones_farmacia.adapter.model.EWalletMethod;
import patrones_farmacia.adapter.model.AdapterPayMethod;

class FacadeIntegrationTest {

    private FCreator creator;

    @BeforeEach
    void init() {
        creator = new FCreator();
    }

    @Test
    void saleSucceedsWithCashAndRemovesFromInventory() {
        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "GenFarma", 3500);
        InventSystem invent = new InventSystem();
        invent.addToStock(m);
        Sale sale = new Sale("F-001", "Cliente");
        sale.addMedicine(m);
        CashMethod cash = new CashMethod(5000);
        AdapterPayMethod adapter = new AdapterPayMethod(cash, new CreditCardMethod("0","x","0",0), new EWalletMethod("w",0));
        PaySystem pay = new PaySystem(adapter);
        FacadeSale facade = new FacadeSale(invent, pay, new ReceiptSystem());
        boolean result = facade.doSale(sale, "CASH");
        assertTrue(result);
        assertFalse(invent.verifyStock(m));
    }

    @Test
    void saleFailsWhenStockMissing() {
        Medicine m = creator.createMedicine(FCreator.Type.BRAND, "Dolex", "GSK", 6200);
        InventSystem invent = new InventSystem();
        Sale sale = new Sale("F-002", "Cliente");
        sale.addMedicine(m);
        CashMethod cash = new CashMethod(20000);
        AdapterPayMethod adapter = new AdapterPayMethod(cash, new CreditCardMethod("0","x","0",0), new EWalletMethod("w",0));
        PaySystem pay = new PaySystem(adapter);
        FacadeSale facade = new FacadeSale(invent, pay, new ReceiptSystem());
        boolean result = facade.doSale(sale, "CASH");
        assertFalse(result);
    }

    @Test
    void paymentFailureKeepsInventoryIntact() {
        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "Ibuprofeno", "GenFarma", 2500);
        InventSystem invent = new InventSystem();
        invent.addToStock(m);
        Sale sale = new Sale("F-003", "Cliente");
        sale.addMedicine(m);
        CashMethod cash = new CashMethod(1000);
        AdapterPayMethod adapter = new AdapterPayMethod(cash, new CreditCardMethod("0","x","0",0), new EWalletMethod("w",0));
        PaySystem pay = new PaySystem(adapter);
        FacadeSale facade = new FacadeSale(invent, pay, new ReceiptSystem());
        boolean result = facade.doSale(sale, "CASH");
        assertFalse(result);
        assertTrue(invent.verifyStock(m));
    }

    @Test
    void creditCardExactLimitProcessesAndReducesLimitToZero() {
        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "Antibiotico", "LabX", 4500);
        InventSystem invent = new InventSystem();
        invent.addToStock(m);
        Sale sale = new Sale("F-004", "Cliente");
        sale.addMedicine(m);
        CreditCardMethod card = new CreditCardMethod("1111-2222","Cliente","123",4500);
        AdapterPayMethod adapter = new AdapterPayMethod(new CashMethod(0), card, new EWalletMethod("w",0));
        PaySystem pay = new PaySystem(adapter);
        FacadeSale facade = new FacadeSale(invent, pay, new ReceiptSystem());
        boolean result = facade.doSale(sale, "CREDIT");
        assertTrue(result);
        assertEquals(0.0, card.getLimit(), 0.001);
        assertFalse(invent.verifyStock(m));
    }

    @Test
    void ewalletSequentialPaymentsDepleteBalanceAndFailAfter() {
        List<Medicine> inventory = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            inventory.add(creator.createMedicine(FCreator.Type.GENERIC, "Vitamina", "LabV", 500));
        }
        InventSystem invent = new InventSystem();
        for (Medicine med : inventory) invent.addToStock(med);
        EWalletMethod wallet = new EWalletMethod("W-01", 2000);
        AdapterPayMethod adapter = new AdapterPayMethod(new CashMethod(0), new CreditCardMethod("0","x","0",0), wallet);
        PaySystem pay = new PaySystem(adapter);
        FacadeSale facade = new FacadeSale(invent, pay, new ReceiptSystem());
        for (int i = 0; i < 4; i++) {
            Sale s = new Sale("F-EW-" + i, "C");
            s.addMedicine(inventory.get(i));
            assertTrue(facade.doSale(s, "EWALLET"));
        }
        Sale sFail = new Sale("F-EW-4", "C");
        sFail.addMedicine(inventory.get(4));
        assertFalse(facade.doSale(sFail, "EWALLET"));
    }

    @Test
    void multipleItemsSaleRemovesAllItemsAndTotalIsCorrect() {
        Medicine m1 = creator.createMedicine(FCreator.Type.GENERIC, "MedA", "LabA", 1200);
        Medicine m2 = creator.createMedicine(FCreator.Type.GENERIC, "MedB", "LabB", 800);
        InventSystem invent = new InventSystem();
        invent.addToStock(m1);
        invent.addToStock(m2);
        Sale sale = new Sale("F-006", "Cliente");
        sale.addMedicine(m1);
        sale.addMedicine(m2);
        AdapterPayMethod adapter = new AdapterPayMethod(new CashMethod(5000), new CreditCardMethod("0","x","0",0), new EWalletMethod("w",0));
        PaySystem pay = new PaySystem(adapter);
        FacadeSale facade = new FacadeSale(invent, pay, new ReceiptSystem());
        assertEquals(2000.0, sale.getTotal(), 0.001);
        assertTrue(facade.doSale(sale, "CASH"));
        assertFalse(invent.verifyStock(m1));
        assertFalse(invent.verifyStock(m2));
    }

    @Test
    void invalidPaymentModeCausesFailureAndKeepsStock() {
        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "TestMed", "LabT", 1000);
        InventSystem invent = new InventSystem();
        invent.addToStock(m);
        Sale sale = new Sale("F-007", "Cliente");
        sale.addMedicine(m);
        AdapterPayMethod adapter = new AdapterPayMethod(new CashMethod(0), new CreditCardMethod("0","x","0",0), new EWalletMethod("w",0));
        PaySystem pay = new PaySystem(adapter);
        FacadeSale facade = new FacadeSale(invent, pay, new ReceiptSystem());
        assertFalse(facade.doSale(sale, "UNKNOWN"));
        assertTrue(invent.verifyStock(m));
    }
}
