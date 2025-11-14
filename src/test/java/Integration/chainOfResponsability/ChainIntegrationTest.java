package Integration.chainOfResponsability;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import patrones_farmacia.chainOfResponsability.controller.FinalizeSaleHandler;
import patrones_farmacia.chainOfResponsability.controller.PaymentValidationHandler;
import patrones_farmacia.chainOfResponsability.controller.StockValidationHandler;
import patrones_farmacia.chainOfResponsability.model.BaseHandler;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.adapter.model.CashMethod;
import patrones_farmacia.adapter.model.CreditCardMethod;
import patrones_farmacia.adapter.model.EWalletMethod;
import patrones_farmacia.adapter.model.AdapterPayMethod;

class ChainIntegrationTest {

    private FCreator creator;

    @BeforeEach
    void setup() {
        creator = new FCreator();
    }

    @Test
    void fullValidationChainWithCashSucceeds() {
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "Ibuprofeno", "GenFarma", 2500);
        List<Medicine> inventory = new ArrayList<>();
        inventory.add(med);
        Sale sale = new Sale("CHAIN-001", "Cliente A");
        sale.addMedicine(med);
        CashMethod cash = new CashMethod(3000);
        AdapterPayMethod adapter = new AdapterPayMethod(cash, new CreditCardMethod("0000","x","000",0), new EWalletMethod("ACC",0));
        adapter.setMode("CASH");
        StockValidationHandler stock = new StockValidationHandler(inventory);
        PaymentValidationHandler payment = new PaymentValidationHandler(adapter);
        FinalizeSaleHandler finalize = new FinalizeSaleHandler();
        stock.setNext(payment);
        payment.setNext(finalize);
        assertTrue(stock.handle(sale));
    }

    @Test
    void chainFailsWhenStockMissing() {
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "GenFarma", 1500);
        List<Medicine> inventory = new ArrayList<>();
        Sale sale = new Sale("CHAIN-002", "Cliente B");
        sale.addMedicine(med);
        CashMethod cash = new CashMethod(10000);
        AdapterPayMethod adapter = new AdapterPayMethod(cash, new CreditCardMethod("0000","x","000",0), new EWalletMethod("ACC",0));
        adapter.setMode("CASH");
        StockValidationHandler stock = new StockValidationHandler(inventory);
        PaymentValidationHandler payment = new PaymentValidationHandler(adapter);
        FinalizeSaleHandler finalize = new FinalizeSaleHandler();
        stock.setNext(payment);
        payment.setNext(finalize);
        assertFalse(stock.handle(sale));
    }

    @Test
    void paymentFailurePreventsFinalizeBeingCalled() {
        Medicine med = creator.createMedicine(FCreator.Type.BRAND, "Aspirina", "Bayer", 3000);
        List<Medicine> inventory = new ArrayList<>();
        inventory.add(med);
        Sale sale = new Sale("CHAIN-003", "Cliente C");
        sale.addMedicine(med);
        CashMethod cash = new CashMethod(1000);
        AdapterPayMethod adapter = new AdapterPayMethod(cash, new CreditCardMethod("0000","x","000",0), new EWalletMethod("ACC",0));
        adapter.setMode("CASH");
        StockValidationHandler stock = new StockValidationHandler(inventory);
        PaymentValidationHandler payment = new PaymentValidationHandler(adapter);
        class TestFinalize extends BaseHandler {
            boolean called = false;
            @Override
            public boolean handle(Sale sale) {
                called = true;
                return true;
            }
        }
        TestFinalize testFinalize = new TestFinalize();
        stock.setNext(payment);
        payment.setNext(testFinalize);
        assertFalse(stock.handle(sale));
        assertFalse(testFinalize.called);
    }

    @Test
    void multipleItemsAndTotalsAreValidated() {
        Medicine m1 = creator.createMedicine(FCreator.Type.GENERIC, "MedA", "LabA", 1200);
        Medicine m2 = creator.createMedicine(FCreator.Type.GENERIC, "MedB", "LabB", 800);
        List<Medicine> inventory = new ArrayList<>();
        inventory.add(m1);
        inventory.add(m2);
        Sale sale = new Sale("CHAIN-004", "Cliente D");
        sale.addMedicine(m1);
        sale.addMedicine(m2);
        CashMethod cash = new CashMethod(5000);
        AdapterPayMethod adapter = new AdapterPayMethod(cash, new CreditCardMethod("0000","x","000",0), new EWalletMethod("ACC",0));
        adapter.setMode("CASH");
        StockValidationHandler stock = new StockValidationHandler(inventory);
        PaymentValidationHandler payment = new PaymentValidationHandler(adapter);
        FinalizeSaleHandler finalize = new FinalizeSaleHandler();
        stock.setNext(payment);
        payment.setNext(finalize);
        assertEquals(2000.0, sale.getTotal(), 0.001);
        assertTrue(stock.handle(sale));
    }

    @Test
    void creditCardModeProcessesPaymentCorrectly() {
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "Antibiotico", "LabX", 4500);
        List<Medicine> inventory = new ArrayList<>();
        inventory.add(med);
        Sale sale = new Sale("CHAIN-005", "Cliente E");
        sale.addMedicine(med);
        CreditCardMethod card = new CreditCardMethod("1111-2222","Cliente E","123",4500);
        AdapterPayMethod adapter = new AdapterPayMethod(new CashMethod(0), card, new EWalletMethod("ACC",0));
        adapter.setMode("CREDIT");
        StockValidationHandler stock = new StockValidationHandler(inventory);
        PaymentValidationHandler payment = new PaymentValidationHandler(adapter);
        FinalizeSaleHandler finalize = new FinalizeSaleHandler();
        stock.setNext(payment);
        payment.setNext(finalize);
        assertTrue(stock.handle(sale));
        assertEquals(0.0, card.getLimit(), 0.001);
    }

    @Test
    void ewalletMultipleSmallPaymentsCanBeHandledSequentially() {
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "Vitamina", "LabV", 1000);
        List<Medicine> inventory = new ArrayList<>();
        inventory.add(med);
        Sale sale = new Sale("CHAIN-006", "Cliente F");
        sale.addMedicine(med);
        EWalletMethod wallet = new EWalletMethod("W-01", 3000);
        AdapterPayMethod adapter = new AdapterPayMethod(new CashMethod(0), new CreditCardMethod("0","x","0",0), wallet);
        adapter.setMode("EWALLET");
        StockValidationHandler stock = new StockValidationHandler(inventory);
        PaymentValidationHandler payment = new PaymentValidationHandler(adapter);
        FinalizeSaleHandler finalize = new FinalizeSaleHandler();
        stock.setNext(payment);
        payment.setNext(finalize);
        assertTrue(stock.handle(sale));
        assertTrue(stock.handle(sale));
        assertTrue(stock.handle(sale));
        assertEquals(0.0, wallet.getBalance(), 0.001);
        assertFalse(stock.handle(sale));
    }

    @Test
    void cashExactAmountSucceeds() {
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "Exacto", "LabX", 2000);
        List<Medicine> inventory = new ArrayList<>();
        inventory.add(med);
        Sale sale = new Sale("CHAIN-007", "Cliente G");
        sale.addMedicine(med);
        CashMethod cash = new CashMethod(2000);
        AdapterPayMethod adapter = new AdapterPayMethod(cash, new CreditCardMethod("0","x","0",0), new EWalletMethod("0",0));
        adapter.setMode("CASH");
        StockValidationHandler stock = new StockValidationHandler(inventory);
        PaymentValidationHandler payment = new PaymentValidationHandler(adapter);
        stock.setNext(payment);
        assertTrue(stock.handle(sale));
    }

    @Test
    void cashInsufficientFails() {
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "Insuf", "LabY", 2000);
        List<Medicine> inventory = new ArrayList<>();
        inventory.add(med);
        Sale sale = new Sale("CHAIN-008", "Cliente H");
        sale.addMedicine(med);
        CashMethod cash = new CashMethod(1999);
        AdapterPayMethod adapter = new AdapterPayMethod(cash, new CreditCardMethod("0","x","0",0), new EWalletMethod("0",0));
        adapter.setMode("CASH");
        StockValidationHandler stock = new StockValidationHandler(inventory);
        PaymentValidationHandler payment = new PaymentValidationHandler(adapter);
        stock.setNext(payment);
        assertFalse(stock.handle(sale));
    }

    @Test
    void switchingModesBetweenRequestsWorks() {
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "Switch", "LabS", 1500);
        List<Medicine> inventory = new ArrayList<>();
        inventory.add(med);
        Sale sale = new Sale("CHAIN-009", "Cliente I");
        sale.addMedicine(med);
        CashMethod cash = new CashMethod(0);
        CreditCardMethod card = new CreditCardMethod("9999","Cliente I","321",2000);
        EWalletMethod wallet = new EWalletMethod("W-99", 500);
        AdapterPayMethod adapter = new AdapterPayMethod(cash, card, wallet);
        StockValidationHandler stock = new StockValidationHandler(inventory);
        PaymentValidationHandler payment = new PaymentValidationHandler(adapter);
        FinalizeSaleHandler finalize = new FinalizeSaleHandler();
        stock.setNext(payment);
        payment.setNext(finalize);
        adapter.setMode("CREDIT");
        assertTrue(stock.handle(sale));
        assertEquals(500.0, card.getLimit(), 0.001);
        sale = new Sale("CHAIN-009-B", "Cliente I");
        sale.addMedicine(med);
        adapter.setMode("EWALLET");
        assertFalse(stock.handle(sale));
        wallet = new EWalletMethod("W-99", 1500);
        adapter = new AdapterPayMethod(new CashMethod(0), card, wallet);
        adapter.setMode("EWALLET");
        payment = new PaymentValidationHandler(adapter);
        stock.setNext(payment);
        assertTrue(stock.handle(sale));
    }

    @Test
    void finalizeHandlerReceivesCallAfterSuccessfulPayment() {
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "FinalTest", "LabF", 800);
        List<Medicine> inventory = new ArrayList<>();
        inventory.add(med);
        Sale sale = new Sale("CHAIN-010", "Cliente J");
        sale.addMedicine(med);
        CashMethod cash = new CashMethod(1000);
        AdapterPayMethod adapter = new AdapterPayMethod(cash, new CreditCardMethod("0","x","0",0), new EWalletMethod("0",0));
        adapter.setMode("CASH");
        StockValidationHandler stock = new StockValidationHandler(inventory);
        PaymentValidationHandler payment = new PaymentValidationHandler(adapter);
        class SpyFinalize extends BaseHandler {
            boolean called = false;
            @Override
            public boolean handle(Sale sale) {
                called = true;
                return true;
            }
        }
        SpyFinalize spy = new SpyFinalize();
        stock.setNext(payment);
        payment.setNext(spy);
        assertTrue(stock.handle(sale));
        assertTrue(spy.called);
    }

    @Test
    void largeBasketWithMixedPricesProcessesWithCredit() {
        Medicine a = creator.createMedicine(FCreator.Type.GENERIC, "A", "L1", 1000);
        Medicine b = creator.createMedicine(FCreator.Type.GENERIC, "B", "L2", 2000);
        Medicine c = creator.createMedicine(FCreator.Type.GENERIC, "C", "L3", 3000);
        List<Medicine> inventory = new ArrayList<>();
        inventory.add(a);
        inventory.add(b);
        inventory.add(c);
        Sale sale = new Sale("CHAIN-011", "Cliente K");
        sale.addMedicine(a);
        sale.addMedicine(b);
        sale.addMedicine(c);
        CreditCardMethod card = new CreditCardMethod("4444","Cliente K","999",7000);
        AdapterPayMethod adapter = new AdapterPayMethod(new CashMethod(0), card, new EWalletMethod("0",0));
        adapter.setMode("CREDIT");
        StockValidationHandler stock = new StockValidationHandler(inventory);
        PaymentValidationHandler payment = new PaymentValidationHandler(adapter);
        stock.setNext(payment);
        assertTrue(stock.handle(sale));
        assertEquals(0.0, card.getLimit(), 0.001);
    }

}