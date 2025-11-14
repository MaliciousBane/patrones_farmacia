package e2e.chainOfResponsability;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.chainOfResponsability.controller.FinalizeSaleHandler;
import patrones_farmacia.chainOfResponsability.controller.PaymentValidationHandler;
import patrones_farmacia.chainOfResponsability.controller.StockValidationHandler;
import patrones_farmacia.chainOfResponsability.model.Handler;
import patrones_farmacia.chainOfResponsability.model.BaseHandler;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;
import patrones_farmacia.adapter.model.PayMethodInterface;
import patrones_farmacia.adapter.model.CashMethod;
import patrones_farmacia.adapter.model.CreditCardMethod;
import patrones_farmacia.adapter.model.EWalletMethod;
import patrones_farmacia.adapter.model.AdapterPayMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@DisplayName("Chain of Responsibility - Validación de Ventas E2E ampliado")
class ValidationFlowE2ETest {

    private FCreator creator;
    private Handler validationChain;
    private List<Medicine> inventory;

    @BeforeEach
    void setUp() {
        Locale.setDefault(Locale.US);
        creator = new FCreator();
        inventory = new ArrayList<>();

        Medicine med1 = creator.createMedicine(FCreator.Type.GENERIC, "Amoxicilina", "GenFarma", 4500);
        Medicine med2 = creator.createMedicine(FCreator.Type.BRAND, "Ibupirac", "Farmalab", 8900);
        Medicine med3 = creator.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "GenFarma", 2500);
        Medicine med4 = creator.createMedicine(FCreator.Type.GENERIC, "Aspirina", "GenFarma", 1500);
        Medicine med5 = creator.createMedicine(FCreator.Type.BRAND, "Medicamento Premium", "LabPremium", 50000);
        Medicine med6 = creator.createMedicine(FCreator.Type.GENERIC, "Vitamina C", "GenFarma", 3500);

        inventory.add(med1);
        inventory.add(med2);
        inventory.add(med3);
        inventory.add(med4);
        inventory.add(med5);
        inventory.add(med6);

        PayMethodInterface adapterPayment = new PayMethodInterface() {
            @Override
            public boolean pay(double amount) {
                return amount > 0;
            }

            @Override
            public String getName() {
                return "MockAdapterPayment";
            }
        };

        Handler stock = new StockValidationHandler(inventory);
        Handler pay = new PaymentValidationHandler(adapterPayment);
        Handler finalizer = new FinalizeSaleHandler();
        stock.setNext(pay);
        pay.setNext(finalizer);
        validationChain = stock;
    }

    @Test
    @DisplayName("Valida venta con un medicamento disponible y pago OK")
    void testSuccessfulSingleItemSale() {
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "Amoxicilina", "GenFarma", 4500);
        Sale sale = new Sale("E2E-001", "Cliente A");
        sale.addMedicine(med);
        assertTrue(validationChain.handle(sale));
        assertEquals(4500, sale.getTotal());
    }

    @Test
    @DisplayName("Rechaza venta vacía")
    void testRejectEmptySale() {
        Sale sale = new Sale("E2E-002", "Cliente B");
        assertFalse(validationChain.handle(sale));
    }

    @Test
    @DisplayName("Valida venta con múltiples medicamentos")
    void testMultipleItemsSale() {
        Medicine a = creator.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "GenFarma", 2500);
        Medicine b = creator.createMedicine(FCreator.Type.BRAND, "Ibupirac", "Farmalab", 8900);
        Sale sale = new Sale("E2E-003", "Cliente C");
        sale.addMedicine(a);
        sale.addMedicine(b);
        assertTrue(validationChain.handle(sale));
        assertEquals(11400, sale.getTotal());
    }

    @Test
    @DisplayName("Rechaza medicamento no encontrado en inventario")
    void testRejectWhenMedicineNotInInventory() {
        Medicine missing = creator.createMedicine(FCreator.Type.GENERIC, "NoExiste", "LabX", 3000);
        Sale sale = new Sale("E2E-004", "Cliente D");
        sale.addMedicine(missing);
        assertFalse(validationChain.handle(sale));
    }

    @Test
    @DisplayName("Case-insensitive match en inventario")
    void testCaseInsensitiveInventoryMatch() {
        Sale sale = new Sale("E2E-005", "Cliente E");
        Medicine saleMed = creator.createMedicine(FCreator.Type.GENERIC, "aspIrina", "GenFarma", 1500);
        sale.addMedicine(saleMed);
        assertTrue(validationChain.handle(sale));
    }

    @Test
    @DisplayName("Acepta venta con medicamento duplicado varias veces")
    void testDuplicateMedicineInSale() {
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "Vitamina C", "GenFarma", 3500);
        Sale sale = new Sale("E2E-006", "Cliente F");
        sale.addMedicine(med);
        sale.addMedicine(med);
        sale.addMedicine(med);
        assertTrue(validationChain.handle(sale));
        assertEquals(10500, sale.getTotal());
    }

    @Test
    @DisplayName("Pago falla si monto es cero según implementación de pago")
    void testPaymentFailsOnZeroAmount() {
        Medicine freeMed = creator.createMedicine(FCreator.Type.GENERIC, "Muestra", "Lab", 0);
        inventory.add(freeMed);
        Sale sale = new Sale("E2E-007", "Cliente G");
        sale.addMedicine(freeMed);
        assertFalse(validationChain.handle(sale));
    }

    @Test
    @DisplayName("Payment handler rechaza cuando método de pago devuelve false por límite")
    void testPaymentHandlerRejectsOnPaymentLimit() {
        PayMethodInterface limited = new PayMethodInterface() {
            @Override
            public boolean pay(double amount) {
                return amount <= 10000;
            }

            @Override
            public String getName() {
                return "Limited";
            }
        };
        Handler stock = new StockValidationHandler(inventory);
        Handler pay = new PaymentValidationHandler(limited);
        stock.setNext(pay);
        pay.setNext(new FinalizeSaleHandler());
        Sale sale = new Sale("E2E-008", "Cliente H");
        Medicine pricey = creator.createMedicine(FCreator.Type.BRAND, "Medicamento Premium", "LabPremium", 50000);
        inventory.add(pricey);
        sale.addMedicine(pricey);
        assertFalse(stock.handle(sale));
    }

    @Test
    @DisplayName("Cadena completa se ejecuta en orden y preserva datos de venta")
    void testChainPreservesSaleData() {
        Medicine m = creator.createMedicine(FCreator.Type.BRAND, "Ibupirac", "Farmalab", 8900);
        Sale sale = new Sale("E2E-009", "Cliente I");
        sale.addMedicine(m);
        double beforeTotal = sale.getTotal();
        String beforeClient = sale.getClient();
        String beforeId = sale.getId();
        assertTrue(validationChain.handle(sale));
        assertEquals(beforeId, sale.getId());
        assertEquals(beforeClient, sale.getClient());
        assertEquals(beforeTotal, sale.getTotal());
    }

    @Test
    @DisplayName("Múltiples ejecuciones consecutivas mantienen integridad de la cadena")
    void testChainMultipleConsecutiveExecutions() {
        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "GenFarma", 2500);
        inventory.add(m);
        for (int i = 0; i < 6; i++) {
            Sale sale = new Sale("E2E-010-" + i, "Cliente " + i);
            sale.addMedicine(m);
            assertTrue(validationChain.handle(sale));
        }
    }

    @Test
    @DisplayName("Venta con mezcla de disponibles e indisponibles es rechazada")
    void testMixedAvailabilityRejected() {
        Medicine available = creator.createMedicine(FCreator.Type.GENERIC, "Amoxicilina", "GenFarma", 4500);
        Medicine missing = creator.createMedicine(FCreator.Type.GENERIC, "NoStock", "LabX", 1000);
        Sale sale = new Sale("E2E-011", "Cliente J");
        sale.addMedicine(available);
        sale.addMedicine(missing);
        assertFalse(validationChain.handle(sale));
    }

    @Test
    @DisplayName("Handler intermedio nulo (no next) procesa correctamente")
    void testHandlerWhenNextIsNull() {
        Handler stockOnly = new StockValidationHandler(inventory);
        Sale sale = new Sale("E2E-012", "Cliente K");
        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "GenFarma", 2500);
        inventory.add(m);
        sale.addMedicine(m);
        assertTrue(stockOnly.handle(sale));
    }

    @Test
    @DisplayName("Venta con muchos items calcula total correctamente")
    void testLargeSaleTotalCalculation() {
        Medicine a = creator.createMedicine(FCreator.Type.GENERIC, "A", "L", 100);
        Medicine b = creator.createMedicine(FCreator.Type.GENERIC, "B", "L", 200);
        inventory.add(a);
        inventory.add(b);
        Sale sale = new Sale("E2E-013", "Cliente L");
        for (int i = 0; i < 10; i++)
            sale.addMedicine(a);
        for (int i = 0; i < 5; i++)
            sale.addMedicine(b);
        assertTrue(validationChain.handle(sale));
        assertEquals(10 * 100 + 5 * 200, sale.getTotal());
    }

    @Test
    @DisplayName("AdapterPayMethod con CASH procesa y finaliza")
    void testAdapterCashProcesses() {
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "TestCash", "Lab", 2000);
        inventory.add(med);
        Sale sale = new Sale("E2E-014", "Cliente M");
        sale.addMedicine(med);
        CashMethod cash = new CashMethod(2500);
        AdapterPayMethod adapter = new AdapterPayMethod(cash, new CreditCardMethod("0", "x", "0", 0),
                new EWalletMethod("W", 0));
        adapter.setMode("CASH");
        Handler stock = new StockValidationHandler(inventory);
        Handler pay = new PaymentValidationHandler(adapter);
        Handler fin = new FinalizeSaleHandler();
        stock.setNext(pay);
        pay.setNext(fin);
        assertTrue(stock.handle(sale));
    }

    @Test
    @DisplayName("AdapterPayMethod con CASH insuficiente falla")
    void testAdapterCashInsufficientFails() {
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "TestCash2", "Lab", 3000);
        inventory.add(med);
        Sale sale = new Sale("E2E-015", "Cliente N");
        sale.addMedicine(med);
        CashMethod cash = new CashMethod(2999);
        AdapterPayMethod adapter = new AdapterPayMethod(cash, new CreditCardMethod("0", "x", "0", 0),
                new EWalletMethod("W", 0));
        adapter.setMode("CASH");
        Handler stock = new StockValidationHandler(inventory);
        Handler pay = new PaymentValidationHandler(adapter);
        stock.setNext(pay);
        assertFalse(stock.handle(sale));
    }

    @Test
    @DisplayName("AdapterPayMethod con CREDIT procesa consumiendo límite")
    void testAdapterCreditConsumesLimit() {
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "TestCard", "Lab", 1500);
        inventory.add(med);
        Sale sale = new Sale("E2E-016", "Cliente O");
        sale.addMedicine(med);
        CreditCardMethod card = new CreditCardMethod("1111", "Cliente O", "321", 1500);
        AdapterPayMethod adapter = new AdapterPayMethod(new CashMethod(0), card, new EWalletMethod("W", 0));
        adapter.setMode("CREDIT");
        Handler stock = new StockValidationHandler(inventory);
        Handler pay = new PaymentValidationHandler(adapter);
        stock.setNext(pay);
        assertTrue(stock.handle(sale));
        assertEquals(0.0, card.getLimit(), 0.001);
    }

    @Test
    @DisplayName("AdapterPayMethod con EWALLET procesa decrementando saldo")
    void testAdapterEwalletDecrementsBalance() {
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "EWalletMed", "Lab", 800);
        inventory.add(med);
        Sale sale = new Sale("E2E-017", "Cliente P");
        sale.addMedicine(med);
        EWalletMethod wallet = new EWalletMethod("W-42", 1000);
        AdapterPayMethod adapter = new AdapterPayMethod(new CashMethod(0), new CreditCardMethod("0", "x", "0", 0),
                wallet);
        adapter.setMode("EWALLET");
        Handler stock = new StockValidationHandler(inventory);
        Handler pay = new PaymentValidationHandler(adapter);
        stock.setNext(pay);
        assertTrue(stock.handle(sale));
        assertEquals(200.0, wallet.getBalance(), 0.001);
    }

    @Test
    @DisplayName("FinalizeSaleHandler es llamado cuando todo es correcto (spy)")
    void testFinalizeCalledWhenSuccess() {
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "FinalizeMe", "Lab", 1200);
        inventory.add(med);
        Sale sale = new Sale("E2E-018", "Cliente Q");
        sale.addMedicine(med);
        CashMethod cash = new CashMethod(2000);
        AdapterPayMethod adapter = new AdapterPayMethod(cash, new CreditCardMethod("0", "x", "0", 0),
                new EWalletMethod("0", 0));
        adapter.setMode("CASH");
        Handler stock = new StockValidationHandler(inventory);
        Handler pay = new PaymentValidationHandler(adapter);
        class SpyFinalize extends BaseHandler {
            boolean called = false;

            @Override
            public boolean handle(Sale s) {
                called = true;
                return true;
            }
        }
        SpyFinalize spy = new SpyFinalize();
        stock.setNext(pay);
        pay.setNext(spy);
        assertTrue(stock.handle(sale));
        assertTrue(spy.called);
    }

    @Test
    @DisplayName("Stock handler no altera venta salvo verificar nombres")
    void testStockDoesNotMutateSaleItems() {
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "Immutable", "Lab", 500);
        inventory.add(med);
        Sale sale = new Sale("E2E-019", "Cliente R");
        Medicine saleCopy = creator.createMedicine(FCreator.Type.GENERIC, "Immutable", "OtherLab", 500);
        sale.addMedicine(saleCopy);
        int beforeSize = sale.getItems().size();
        Handler stock = new StockValidationHandler(inventory);
        assertTrue(stock.handle(sale));
        assertEquals(beforeSize, sale.getItems().size());
    }

}