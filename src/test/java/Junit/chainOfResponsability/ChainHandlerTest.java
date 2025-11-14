package Junit.chainOfResponsability;

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
import patrones_farmacia.adapter.model.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@DisplayName("Pruebas ampliadas - Chain of Responsibility para Ventas")
class ChainHandlerTest {

    private FCreator creadorMedicamentos;
    private Handler cadenaValidacion;
    private List<Medicine> inventory;
    private CashMethod cash;
    private CreditCardMethod card;
    private EWalletMethod wallet;
    private AdapterPayMethod adapter;
    private PrintStream originalOut;
    private ByteArrayOutputStream baos;

    @BeforeEach
    void configurarCadenaDeManejadores() {
        creadorMedicamentos = new FCreator();
        inventory = new ArrayList<>();
        cash = new CashMethod(20000);
        card = new CreditCardMethod("1111-2222-3333-4444", "Owner", "999", 50000);
        wallet = new EWalletMethod("WAL-01", 15000);
        adapter = new AdapterPayMethod(cash, card, wallet);
        Handler validacionStock = new StockValidationHandler(inventory);
        Handler validacionPago = new PaymentValidationHandler(adapter);
        Handler finalizacion = new FinalizeSaleHandler();
        validacionStock.setNext(validacionPago);
        validacionPago.setNext(finalizacion);
        cadenaValidacion = validacionStock;
        originalOut = System.out;
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
    }

    @Test
    @DisplayName("Venta exitosa con stock y pago en efectivo")
    void debeProcesarVentaCompletaConEfectivo() {
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "GenFarma", 3500);
        inventory.add(m);
        adapter.setMode("CASH");
        Sale venta = new Sale("CHAIN-A1", "Cliente A");
        venta.addMedicine(m);
        assertTrue(cadenaValidacion.handle(venta));
    }

    @Test
    @DisplayName("Falla por falta de stock")
    void debeFallarPorFaltaDeStock() {
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "NoStock", "LabX", 2000);
        adapter.setMode("CASH");
        Sale venta = new Sale("CHAIN-B1", "Cliente B");
        venta.addMedicine(m);
        assertFalse(cadenaValidacion.handle(venta));
    }

    @Test
    @DisplayName("Falla por pago insuficiente con efectivo")
    void debeFallarPagoInsuficienteEfectivo() {
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "GenFarma", 15000);
        inventory.add(m);
        cash = new CashMethod(1000);
        adapter = new AdapterPayMethod(cash, card, wallet);
        adapter.setMode("CASH");
        Handler validacionPago = new PaymentValidationHandler(adapter);
        Handler finalizacion = new FinalizeSaleHandler();
        StockValidationHandler stock = new StockValidationHandler(inventory);
        stock.setNext(validacionPago);
        validacionPago.setNext(finalizacion);
        Sale venta = new Sale("CHAIN-C1", "Cliente C");
        venta.addMedicine(m);
        assertFalse(stock.handle(venta));
    }

    @Test
    @DisplayName("Falla por pago insuficiente con billetera")
    void debeFallarPagoInsuficienteWallett() {
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Ibuprofeno", "LabY", 20000);
        inventory.add(m);
        wallet = new EWalletMethod("WAL-02", 1000);
        adapter = new AdapterPayMethod(cash, card, wallet);
        adapter.setMode("EWALLET");
        StockValidationHandler stock = new StockValidationHandler(inventory);
        PaymentValidationHandler pay = new PaymentValidationHandler(adapter);
        stock.setNext(pay);
        pay.setNext(new FinalizeSaleHandler());
        Sale venta = new Sale("CHAIN-D1", "Cliente D");
        venta.addMedicine(m);
        assertFalse(stock.handle(venta));
    }

    @Test
    @DisplayName("Caso sensible a mayúsculas/minúsculas en inventario")
    void pruebaCaseInsensitiveStock() {
        Medicine mInv = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "AspiRina", "GSK", 3000);
        inventory.add(mInv);
        adapter.setMode("CASH");
        Medicine mSale = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "aspIrina", "GSK", 3000);
        Sale venta = new Sale("CHAIN-E1", "Cliente E");
        venta.addMedicine(mSale);
        assertTrue(cadenaValidacion.handle(venta));
    }

    @Test
    @DisplayName("Venta con varios medicamentos donde uno falta falla")
    void ventaConVariosMedicamentosUnoFalta() {
        Medicine m1 = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "GenFarma", 1000);
        Medicine m2 = creadorMedicamentos.createMedicine(FCreator.Type.BRAND, "Dolex", "GSK", 3000);
        inventory.add(m1);
        adapter.setMode("CASH");
        Sale venta = new Sale("CHAIN-F1", "Cliente F");
        venta.addMedicine(m1);
        venta.addMedicine(m2);
        assertFalse(cadenaValidacion.handle(venta));
    }

    @Test
    @DisplayName("Pago con tarjeta dentro del límite pasa la validación")
    void pagoConTarjetaDentroDelLimite() {
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.BRAND, "Premium", "Lab", 40000);
        inventory.add(m);
        adapter.setMode("CREDIT");
        Sale venta = new Sale("CHAIN-G1", "Cliente G");
        venta.addMedicine(m);
        assertTrue(cadenaValidacion.handle(venta));
    }

    @Test
    @DisplayName("Pago con tarjeta que excede límite falla")
    void pagoConTarjetaExcedeLimite() {
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.BRAND, "Caro", "Lab", 60000);
        inventory.add(m);
        adapter.setMode("CREDIT");
        Sale venta = new Sale("CHAIN-H1", "Cliente H");
        venta.addMedicine(m);
        assertFalse(cadenaValidacion.handle(venta));
    }

    @Test
    @DisplayName("Cadena continúa si next es null en último manejador")
    void cadenaContinuaConUltimoHandler() {
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "GenFarma", 2000);
        inventory.add(m);
        adapter.setMode("CASH");
        StockValidationHandler stock = new StockValidationHandler(inventory);
        Sale s = new Sale("CHAIN-I1", "Cliente I");
        assertTrue(stock.handle(s));
    }

    @Test
    @DisplayName("Varias ventas procesadas secuencialmente")
    void variasVentasProcesadasSecuencialmente() {
        Medicine m1 = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "MedA", "LabA", 1000);
        Medicine m2 = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "MedB", "LabB", 2000);
        inventory.add(m1);
        inventory.add(m2);
        adapter.setMode("CASH");
        Sale v1 = new Sale("CHAIN-J1", "C1");
        Sale v2 = new Sale("CHAIN-J2", "C2");
        v1.addMedicine(m1);
        v2.addMedicine(m2);
        assertTrue(cadenaValidacion.handle(v1));
        assertTrue(cadenaValidacion.handle(v2));
    }

    @Test
    @DisplayName("Venta con varios medicamentos todos disponibles procesa correctamente")
    void ventaConVariosMedicamentosTodosDisponibles() {
        Medicine m1 = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "MedA", "LabA", 1200);
        Medicine m2 = creadorMedicamentos.createMedicine(FCreator.Type.BRAND, "MedB", "LabB", 2300);
        Medicine m3 = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "MedC", "LabC", 3400);
        inventory.add(m1);
        inventory.add(m2);
        inventory.add(m3);
        adapter.setMode("CASH");
        Sale venta = new Sale("CHAIN-K1", "Cliente K");
        venta.addMedicine(m1);
        venta.addMedicine(m2);
        venta.addMedicine(m3);
        assertTrue(cadenaValidacion.handle(venta));
    }

    @Test
    @DisplayName("Modo de pago inválido falla la validación")
    void modoPagoInvalidoFalla() {
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Simple", "Lab", 1000);
        inventory.add(m);
        adapter.setMode("UNKNOWN");
        Sale venta = new Sale("CHAIN-L1", "Cliente L");
        venta.addMedicine(m);
        assertFalse(cadenaValidacion.handle(venta));
    }

    @Test
    @DisplayName("Venta con mismo medicamento repetido procesa si stock suficiente")
    void ventaConMismoMedicamentoRepetido() {
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Duplicado", "LabX", 500);
        inventory.add(m);
        adapter.setMode("CASH");
        Sale venta = new Sale("CHAIN-M1", "Cliente M");
        venta.addMedicine(m);
        venta.addMedicine(m);
        assertTrue(cadenaValidacion.handle(venta));
    }

    @Test
    @DisplayName("StockValidationHandler con venta sin items retorna true")
    void stockHandlerEmptySaleReturnsTrue() {
        adapter.setMode("CASH");
        Sale emptySale = new Sale("CHAIN-EMPTY", "Cliente Z");
        assertTrue(cadenaValidacion.handle(emptySale));
    }

    @Test
    @DisplayName("PaymentValidationHandler detiene la cadena cuando pay devuelve false")
    void paymentHandlerStopsWhenPaymentFails() {
        PayMethodInterface failing = new PayMethodInterface() {
            @Override
            public boolean pay(double amount) { return false; }
            @Override
            public String getName() { return "Failer"; }
        };
        PaymentValidationHandler handler = new PaymentValidationHandler(failing);
        Handler nextThatShouldNotRun = new BaseHandler() {
            @Override public boolean handle(Sale sale) { fail(); return true; }
        };
        handler.setNext(nextThatShouldNotRun);
        Sale s = new Sale("P-FAIL", "Cliente X");
        s.addMedicine(creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "X", "Lab", 100));
        assertFalse(handler.handle(s));
    }

    @Test
    @DisplayName("FinalizeSaleHandler imprime mensaje y retorna true")
    void finalizeHandlerPrintsAndReturnsTrue() {
        FinalizeSaleHandler f = new FinalizeSaleHandler();
        Sale s = new Sale("FINAL-1", "Cliente Final");
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        try {
            System.setOut(new PrintStream(baos2));
            assertTrue(f.handle(s));
            String out = baos2.toString();
            assertTrue(out.contains("Venta " + s.getId() + " validada y completada correctamente."));
        } finally {
            System.setOut(oldOut);
        }
    }

    @Test
    @DisplayName("StockValidationHandler detiene cadena cuando un item falta")
    void stockHandlerStopsWhenItemMissing() {
        inventory.clear();
        Medicine present = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Presente", "Lab", 500);
        inventory.add(present);
        StockValidationHandler stock = new StockValidationHandler(inventory);
        Handler nextShouldNotRun = new BaseHandler() {
            @Override public boolean handle(Sale sale) { fail(); return true; }
        };
        stock.setNext(nextShouldNotRun);
        Sale s = new Sale("S-STOP", "Cliente Stop");
        s.addMedicine(creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Ausente", "Lab", 200));
        assertFalse(stock.handle(s));
    }

    @Test
    @DisplayName("ChainConsole.run ejecuta el flujo principal sin excepciones")
    void chainConsoleRunExecutes() {
        patrones_farmacia.chainOfResponsability.view.ChainConsole console = new patrones_farmacia.chainOfResponsability.view.ChainConsole();
        assertDoesNotThrow(() -> console.run());
    }

    @Test
    @DisplayName("Concurrent handling of independent sales does not throw")
    void concurrentHandlingIndependentSales() throws Exception {
        inventory.clear();
        for (int i = 0; i < 5; i++) {
            inventory.add(creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "M-" + i, "Lab", 1000 + i));
        }
        adapter.setMode("CASH");
        ExecutorService es = Executors.newFixedThreadPool(4);
        List<Callable<Boolean>> tasks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final int idx = i;
            tasks.add(() -> {
                Sale s = new Sale("CONC-" + idx, "C" + idx);
                s.addMedicine(creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "M-" + idx, "Lab", 1000 + idx));
                return cadenaValidacion.handle(s);
            });
        }
        List<Future<Boolean>> results = es.invokeAll(tasks);
        for (Future<Boolean> f : results) {
            assertTrue(f.get());
        }
        es.shutdownNow();
    }

    @Test
    @DisplayName("PaymentValidationHandler with zero total allows chain to continue")
    void paymentHandlerAllowsZeroTotal() {
        PayMethodInterface pi = new PayMethodInterface() {
            @Override public boolean pay(double amount) { return amount >= 0; }
            @Override public String getName() { return "Any"; }
        };
        PaymentValidationHandler handler = new PaymentValidationHandler(pi);
        Handler finalHandler = new FinalizeSaleHandler();
        handler.setNext(finalHandler);
        Sale s = new Sale("ZERO-1", "Cliente Z");
        assertTrue(handler.handle(s));
    }

    @Test
    @DisplayName("StockValidationHandler returns true for sale with duplicate items")
    void stockHandlerHandlesDuplicateItems() {
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Dup", "Lab", 500);
        inventory.add(m);
        inventory.add(m);
        StockValidationHandler stock = new StockValidationHandler(inventory);
        Sale s = new Sale("DUP-1", "Cliente Dup");
        s.addMedicine(m);
        s.addMedicine(m);
        assertTrue(stock.handle(s));
    }

    @Test
    @DisplayName("PaymentValidationHandler prints message on failure")
    void paymentHandlerPrintsOnFailure() {
        PayMethodInterface failing = new PayMethodInterface() {
            @Override public boolean pay(double amount) { return false; }
            @Override public String getName() { return "Fail"; }
        };
        PaymentValidationHandler handler = new PaymentValidationHandler(failing);
        Sale s = new Sale("PPRINT", "Cliente P");
        s.addMedicine(creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "X", "L", 1000));
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        PrintStream old = System.out;
        try {
            System.setOut(new PrintStream(baos2));
            assertFalse(handler.handle(s));
            String out = baos2.toString();
            assertTrue(out.contains("Pago fallido durante validación."));
        } finally {
            System.setOut(old);
        }
    }

    @Test
    @DisplayName("BaseHandler.handleNext retorna true si next es null")
    void baseHandlerHandleNextReturnsTrue() {
        StockValidationHandler h = new StockValidationHandler(new ArrayList<>());
        h.setNext(null);
        Sale s = new Sale("NULL-NEXT", "Client");
        assertTrue(h.handle(s));
    }

    @Test
    @DisplayName("BaseHandler.setNext asigna correctamente")
    void baseHandlerSetNextAssigns() {
        StockValidationHandler h1 = new StockValidationHandler(new ArrayList<>());
        FinalizeSaleHandler h2 = new FinalizeSaleHandler();
        h1.setNext(h2);
        assertDoesNotThrow(() -> h1.handle(new Sale("SET-NEXT", "C")));
    }

    @Test
    @DisplayName("StockValidationHandler con inventario vacío falla para items")
    void stockHandlerEmptyInventoryFails() {
        inventory.clear();
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Any", "Lab", 100);
        Sale s = new Sale("EMPTY-INV", "Client");
        s.addMedicine(m);
        assertFalse(cadenaValidacion.handle(s));
    }

    @Test
    @DisplayName("PaymentValidationHandler imprime en sout al fallar")
    void paymentHandlerPrintsSoutFail() {
        PayMethodInterface pi = new PayMethodInterface() {
            @Override public boolean pay(double amount) { return false; }
            @Override public String getName() { return "TestPay"; }
        };
        PaymentValidationHandler ph = new PaymentValidationHandler(pi);
        Sale s = new Sale("SOUT-FAIL", "C");
        s.addMedicine(creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "M", "L", 100));
        String out = baos.toString();
        assertFalse(ph.handle(s));
    }

    @Test
    @DisplayName("StockValidationHandler imprime mensaje cuando no hay stock")
    void stockHandlerPrintsMensajeSinStock() {
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "NoStock", "Lab", 100);
        Sale s = new Sale("NO-STOCK-MSG", "Client");
        s.addMedicine(m);
        StockValidationHandler sh = new StockValidationHandler(new ArrayList<>());
        assertFalse(sh.handle(s));
    }

    @Test
    @DisplayName("FinalizeSaleHandler imprime sale id correctamente")
    void finalizeHandlerPrintsSaleId() {
        FinalizeSaleHandler fh = new FinalizeSaleHandler();
        Sale s = new Sale("FINAL-PRINT", "Cliente");
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        try {
            System.setOut(new PrintStream(baos2));
            fh.handle(s);
            String out = baos2.toString();
            assertTrue(out.contains("FINAL-PRINT"));
        } finally {
            System.setOut(oldOut);
        }
    }

    @Test
    @DisplayName("Múltiples handlers en cadena con diferentes tipos")
    void multipleHandlersChainWithDifferentTypes() {
        Medicine generic = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "G", "L", 1000);
        Medicine brand = creadorMedicamentos.createMedicine(FCreator.Type.BRAND, "B", "Br", 2000);
        Medicine controlled = creadorMedicamentos.createMedicine(FCreator.Type.CONTROLLED, "C", "CODE", 3000);
        inventory.add(generic);
        inventory.add(brand);
        inventory.add(controlled);
        adapter.setMode("CASH");
        Sale s = new Sale("MULTI-TYPE", "Client");
        s.addMedicine(generic);
        s.addMedicine(brand);
        s.addMedicine(controlled);
        assertTrue(cadenaValidacion.handle(s));
    }

    @Test
    @DisplayName("StockValidationHandler con item cuyo nombre es substring de otro")
    void stockHandlerSubstringNames() {
        Medicine m1 = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "Lab", 100);
        Medicine m2 = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Para", "Lab", 50);
        inventory.add(m1);
        adapter.setMode("CASH");
        Sale s = new Sale("SUBSTRING", "C");
        s.addMedicine(m2);
        assertFalse(cadenaValidacion.handle(s));
    }

    @Test
    @DisplayName("PaymentValidationHandler con monto exacto al límite")
    void paymentHandlerExactLimit() {
        CreditCardMethod cc = new CreditCardMethod("1", "O", "1", 500);
        AdapterPayMethod adp = new AdapterPayMethod(new CashMethod(0), cc, new EWalletMethod("", 0));
        adp.setMode("CREDIT");
        PaymentValidationHandler ph = new PaymentValidationHandler(adp);
        ph.setNext(new FinalizeSaleHandler());
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "M", "L", 500);
        Sale s = new Sale("EXACT-LIMIT", "C");
        s.addMedicine(m);
        assertTrue(ph.handle(s));
    }

    @Test
    @DisplayName("Venta con 10 medicamentos diferentes")
    void ventaConMuchosProductos() {
        adapter.setMode("CASH");
        Sale s = new Sale("MANY-ITEMS", "Client");
        for (int i = 0; i < 10; i++) {
            Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Med" + i, "Lab", 100);
            inventory.add(m);
            s.addMedicine(m);
        }
        assertTrue(cadenaValidacion.handle(s));
    }

    @Test
    @DisplayName("StockValidationHandler verifica cada medicamento en venta")
    void stockHandlerVerifyEachMedicine() {
        Medicine m1 = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "M1", "L", 100);
        Medicine m2 = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "M2", "L", 200);
        inventory.add(m1);
        adapter.setMode("CASH");
        Sale s = new Sale("VERIFY-EACH", "C");
        s.addMedicine(m1);
        s.addMedicine(m2);
        assertFalse(cadenaValidacion.handle(s));
    }

    @Test
    @DisplayName("Handler interface setNext y handle son invocados")
    void handlerInterfaceMethodsInvoked() {
        Handler h = new StockValidationHandler(new ArrayList<>());
        Handler h2 = new FinalizeSaleHandler();
        h.setNext(h2);
        Sale s = new Sale("H-INTERFACE", "C");
        assertDoesNotThrow(() -> h.handle(s));
    }

    @Test
    @DisplayName("Cadena larga con 4 handlers")
    void longChainWithFourHandlers() {
        inventory.clear();
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "M", "L", 1000);
        inventory.add(m);
        adapter.setMode("CASH");
        
        StockValidationHandler h1 = new StockValidationHandler(inventory);
        PaymentValidationHandler h2 = new PaymentValidationHandler(adapter);
        FinalizeSaleHandler h3 = new FinalizeSaleHandler();
        
        h1.setNext(h2);
        h2.setNext(h3);
        
        Sale s = new Sale("LONG-CHAIN", "C");
        s.addMedicine(m);
        assertTrue(h1.handle(s));
    }

    @Test
    @DisplayName("Fallo en primer handler no ejecuta siguiente")
    void firstHandlerFailureStopsChain() {
        inventory.clear();
        StockValidationHandler stock = new StockValidationHandler(inventory);
        boolean executed = false;
        Handler failHandler = new BaseHandler() {
            @Override public boolean handle(Sale sale) { fail("Should not execute"); return true; }
        };
        stock.setNext(failHandler);
        
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Missing", "L", 100);
        Sale s = new Sale("FIRST-FAIL", "C");
        s.addMedicine(m);
        
        assertFalse(stock.handle(s));
    }

    @Test
    @DisplayName("PaymentValidationHandler usa adapter setMode y pay")
    void paymentHandlerUsesAdapterPay() {
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "M", "L", 5000);
        inventory.add(m);
        adapter.setMode("CASH");
        PaymentValidationHandler ph = new PaymentValidationHandler(adapter);
        ph.setNext(new FinalizeSaleHandler());
        Sale s = new Sale("ADAPTER-PAY", "C");
        s.addMedicine(m);
        assertTrue(ph.handle(s));
    }

    @Test
    @DisplayName("StockValidationHandler accede correctamente a inventory")
    void stockHandlerAccessesInventory() {
        List<Medicine> inv = new ArrayList<>();
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Test", "L", 100);
        inv.add(m);
        StockValidationHandler sh = new StockValidationHandler(inv);
        Sale s = new Sale("INV-ACCESS", "C");
        s.addMedicine(m);
        assertTrue(sh.handle(s));
    }

    @Test
    @DisplayName("ChainConsole.main ejecuta sin problemas")
    void chainConsoleMainExecutes() {
        assertDoesNotThrow(() -> patrones_farmacia.chainOfResponsability.view.ChainConsole.main(new String[]{}));
    }
}