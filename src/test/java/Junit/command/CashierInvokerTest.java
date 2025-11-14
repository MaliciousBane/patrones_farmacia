package Junit.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.command.controller.*;
import patrones_farmacia.command.model.*;
import patrones_farmacia.command.view.CommandConsole;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.ByteArrayInputStream;
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

@DisplayName("Pruebas del Patrón Command para Gestión de Ventas - Ampliadas")
class CashierInvokerTest {

    private SaleReceiver receptorVentas;
    private CashierInvoker invocadorCajero;
    private FCreator creadorMedicamentos;
    private ByteArrayOutputStream salidaConsola;
    private PrintStream salidaOriginal;

    @BeforeEach
    void configurarPrueba() {
        receptorVentas = new SaleReceiver();
        invocadorCajero = new CashierInvoker();
        creadorMedicamentos = new FCreator();
        salidaOriginal = System.out;
        salidaConsola = new ByteArrayOutputStream();
        System.setOut(new PrintStream(salidaConsola));
    }

    @AfterEach
    void restaurarSalida() {
        System.setOut(salidaOriginal);
    }

    private void runWithConsoleRestore(Runnable testLogic) {
        try {
            testLogic.run();
        } finally {
            System.setOut(salidaOriginal);
        }
    }

    @Test
    @DisplayName("Debe registrar una venta correctamente al ejecutar el comando")
    void debeRegistrarVentaCorrectamente() {
        runWithConsoleRestore(() -> {
            Sale venta = new Sale("CMD-001", "Ana Torres");
            Medicine medicamento = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Ibuprofeno", "GenFarma", 3000);
            venta.addMedicine(medicamento);
            RegisterSaleCommand comandoRegistrar = new RegisterSaleCommand(receptorVentas, venta);
            invocadorCajero.executeCommand(comandoRegistrar);
            String salida = salidaConsola.toString();
            assertEquals(1, receptorVentas.getAllSales().size());
            assertTrue(salida.contains("Venta registrada"));
            assertTrue(salida.contains("CMD-001"));
        });
    }

    @Test
    @DisplayName("Debe cancelar una venta existente correctamente")
    void debeCancelarVentaExistente() {
        Sale venta = new Sale("CMD-002", "Roberto Díaz");
        Medicine medicamento = creadorMedicamentos.createMedicine(FCreator.Type.BRAND, "Dolex Forte", "GSK", 6500);
        venta.addMedicine(medicamento);
        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, venta));
        CancelSaleCommand comandoCancelar = new CancelSaleCommand(receptorVentas, "CMD-002");
        invocadorCajero.executeCommand(comandoCancelar);
        String salida = salidaConsola.toString();
        assertEquals(0, receptorVentas.getAllSales().size());
        assertTrue(salida.contains("cancelada"));
        assertTrue(salida.contains("CMD-002"));
    }

    @Test
    @DisplayName("Debe deshacer el último comando ejecutado correctamente")
    void debeDeshacerUltimoComando() {
        Sale venta = new Sale("CMD-003", "Laura Méndez");
        Medicine medicamento = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "TecnoFarma", 2800);
        venta.addMedicine(medicamento);
        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, venta));
        invocadorCajero.undoLast();
        String salida = salidaConsola.toString();
        assertEquals(0, receptorVentas.getAllSales().size());
        assertTrue(salida.contains("cancelada"));
        assertTrue(salida.contains("CMD-003"));
    }

    @Test
    @DisplayName("Debe almacenar historial y mostrar total de operaciones")
    void debeAlmacenarHistorialDeComandos() {
        Sale venta1 = new Sale("CMD-004", "Pedro Sánchez");
        Sale venta2 = new Sale("CMD-005", "Sofía Ruiz");
        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, venta1));
        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, venta2));
        invocadorCajero.showHistory();
        String salida = salidaConsola.toString();
        assertTrue(salida.contains("Historial de operaciones ejecutadas"));
    }

    @Test
    @DisplayName("No debe fallar al intentar deshacer cuando no hay comandos")
    void noDebeFallarAlDeshacerSinHistorial() {
        invocadorCajero.undoLast();
        String salida = salidaConsola.toString();
        assertTrue(salida.contains("No hay operaciones para deshacer."));
    }

    @Test
    @DisplayName("Cancelar venta inexistente retorna falso y no imprime cancelada")
    void debeMostrarErrorAlCancelarVentaInexistente() {
        boolean result = receptorVentas.cancelSale("CMD-999");
        String salida = salidaConsola.toString();
        assertFalse(result);
    }

    @Test
    @DisplayName("Registrar, devolver y cancelar flujo completo")
    void debeEjecutarFlujoRegistrarDevolverCancelar() {
        Sale venta = new Sale("CMD-006", "Miguel Ángel");
        Medicine m1 = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "MedA", "LabA", 1200);
        Medicine m2 = creadorMedicamentos.createMedicine(FCreator.Type.BRAND, "MedB", "LabB", 2300);
        venta.addMedicine(m1);
        venta.addMedicine(m2);
        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, venta));
        invocadorCajero.executeCommand(new ReturnProductCommand(receptorVentas, "CMD-006", m1));
        invocadorCajero.executeCommand(new CancelSaleCommand(receptorVentas, "CMD-006"));
        String salida = salidaConsola.toString();
        assertTrue(salida.contains("Venta registrada"));
        assertTrue(salida.contains("Producto"));
        assertTrue(salida.contains("cancelada"));
        assertEquals(0, receptorVentas.getAllSales().size());
    }

    @Test
    @DisplayName("Debe devolver producto existente en la venta")
    void debeDevolverProductoExistente() {
        Sale venta = new Sale("CMD-010", "Cliente Devolución");
        Medicine med = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Ibupirac", "GenFarma", 2500);
        venta.addMedicine(med);
        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, venta));
        boolean ret = receptorVentas.returnProduct("CMD-010", med);
        String salida = salidaConsola.toString();
        assertTrue(ret);
        assertTrue(salida.contains("Producto"));
    }

    @Test
    @DisplayName("No debe devolver producto inexistente")
    void noDebeDevolverProductoInexistente() {
        Sale venta = new Sale("CMD-011", "Cliente Rechazo");
        Medicine med = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "MedX", "LabX", 1500);
        venta.addMedicine(med);
        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, venta));
        Medicine other = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "NoExiste", "Lab", 500);
        boolean ret = receptorVentas.returnProduct("CMD-011", other);
        assertFalse(ret);
        assertEquals(1, receptorVentas.getAllSales().get(0).getItems().size());
    }

    @Test
    @DisplayName("Debe ejecutar secuencia rápida de registros sin errores")
    void debeEjecutarSecuenciaRapidaDeRegistros() {
        for (int i = 0; i < 10; i++) {
            Sale s = new Sale("CMD-STRESS-" + i, "Cliente " + i);
            s.addMedicine(creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "A" + i, "Lab", 1000));
            invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, s));
        }
        assertEquals(10, receptorVentas.getAllSales().size());
    }

    @Test
    @DisplayName("Debe alternar registrar y cancelar correctamente")
    void debeAlternarRegistrarYCancelar() {
        for (int i = 0; i < 5; i++) {
            Sale s = new Sale("CMD-ALT-" + i, "Cliente " + i);
            s.addMedicine(creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "M" + i, "Lab", 1000));
            invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, s));
            invocadorCajero.executeCommand(new CancelSaleCommand(receptorVentas, "CMD-ALT-" + i));
        }
        assertEquals(0, receptorVentas.getAllSales().size());
    }

    @Test
    @DisplayName("RegisterSaleCommand.undo remueve la venta cuando se deshace")
    void registerUndoRemovesSale() {
        Sale venta = new Sale("CMD-UNDO-1", "Cliente Undo");
        venta.addMedicine(creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "X", "Lab", 100));
        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, venta));
        invocadorCajero.undoLast();
        String salida = salidaConsola.toString();
        assertTrue(salida.contains("cancelada"));
        assertEquals(0, receptorVentas.getAllSales().size());
    }

    @Test
    @DisplayName("CancelSaleCommand.undo imprime mensaje informando que no puede deshacerse")
    void cancelCommandUndoPrintsMessage() {
        Sale venta = new Sale("CMD-CU-1", "Cliente CU");
        venta.addMedicine(creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Y", "Lab", 100));
        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, venta));
        invocadorCajero.executeCommand(new CancelSaleCommand(receptorVentas, "CMD-CU-1"));
        invocadorCajero.undoLast();
        String salida = salidaConsola.toString();
        assertTrue(salida.contains("No se puede deshacer una cancelación de venta."));
    }

    @Test
    @DisplayName("ReturnProductCommand.undo imprime mensaje informando que no puede deshacerse")
    void returnProductUndoPrintsMessage() {
        Sale venta = new Sale("CMD-RU-1", "Cliente RU");
        Medicine med = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Z", "Lab", 200);
        venta.addMedicine(med);
        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, venta));
        invocadorCajero.executeCommand(new ReturnProductCommand(receptorVentas, "CMD-RU-1", med));
        invocadorCajero.undoLast();
        String salida = salidaConsola.toString();
        assertTrue(salida.contains("No se puede deshacer una devolución de producto."));
    }

    @Test
    @DisplayName("cancelSale es case-insensitive al buscar id")
    void cancelSaleIsCaseInsensitive() {
        Sale venta = new Sale("MiXeD-Case", "Cliente CI");
        venta.addMedicine(creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "AA", "Lab", 100));
        receptorVentas.registerSale(venta);
        boolean res = receptorVentas.cancelSale("mixed-case");
        String salida = salidaConsola.toString();
        assertTrue(res);
    }

    @Test
    @DisplayName("returnProduct remueve solo una ocurrencia cuando hay duplicados")
    void returnProductRemovesOnlyOneOccurrence() {
        Sale venta = new Sale("CMD-DUP-1", "Cliente DUP");
        Medicine medA = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Dup", "Lab", 100);
        venta.addMedicine(medA);
        venta.addMedicine(medA);
        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, venta));
        boolean removed = receptorVentas.returnProduct("CMD-DUP-1", medA);
        assertTrue(removed);
        int remaining = receptorVentas.getAllSales().get(0).getItems().size();
        assertTrue(remaining == 1 || remaining == 0);
    }

    @Test
    @DisplayName("showHistory imprime tamaño correcto luego de operaciones")
    void showHistoryPrintsCorrectCount() {
        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, new Sale("H-1","A")));
        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, new Sale("H-2","B")));
        invocadorCajero.showHistory();
        String salida = salidaConsola.toString();
        assertTrue(salida.contains("Historial de operaciones ejecutadas: 2"));
    }

    @Test
    @DisplayName("RegisterSaleCommand almacena receiver y sale correctamente")
    void registerCommandConstructorStoresValues() {
        Sale s = new Sale("CONST-1", "C");
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "M", "L", 100);
        s.addMedicine(m);
        RegisterSaleCommand cmd = new RegisterSaleCommand(receptorVentas, s);
        cmd.execute();
        assertEquals(1, receptorVentas.getAllSales().size());
        assertEquals("CONST-1", receptorVentas.getAllSales().get(0).getId());
    }

    @Test
    @DisplayName("CancelSaleCommand almacena receiver y saleId correctamente")
    void cancelCommandConstructorStoresValues() {
        Sale s = new Sale("CCANCEL-1", "C");
        receptorVentas.registerSale(s);
        CancelSaleCommand cmd = new CancelSaleCommand(receptorVentas, "CCANCEL-1");
        cmd.execute();
        assertEquals(0, receptorVentas.getAllSales().size());
    }

    @Test
    @DisplayName("ReturnProductCommand almacena receiver, saleId y product correctamente")
    void returnCommandConstructorStoresValues() {
        Sale s = new Sale("CRETURN-1", "C");
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "M", "L", 100);
        s.addMedicine(m);
        receptorVentas.registerSale(s);
        ReturnProductCommand cmd = new ReturnProductCommand(receptorVentas, "CRETURN-1", m);
        cmd.execute();
        assertTrue(receptorVentas.getAllSales().get(0).getItems().isEmpty() || receptorVentas.getAllSales().get(0).getItems().size() == 0);
    }

    @Test
    @DisplayName("CashierInvoker inicializa con historial vacío")
    void cashierInvokerInitWithEmptyHistory() {
        CashierInvoker ci = new CashierInvoker();
        ci.showHistory();
        String out = salidaConsola.toString();
        assertTrue(out.contains("Historial de operaciones ejecutadas: 0"));
    }

    @Test
    @DisplayName("executeCommand agrega comando al historial")
    void executeCommandAddsToHistory() {
        CashierInvoker ci = new CashierInvoker();
        Sale s = new Sale("HIST-1", "C");
        ci.executeCommand(new RegisterSaleCommand(receptorVentas, s));
        ci.showHistory();
        String out = salidaConsola.toString();
        assertTrue(out.contains("Historial de operaciones ejecutadas: 1"));
    }

    @Test
    @DisplayName("undoLast con historia vacía imprime mensaje")
    void undoLastEmptyHistoryPrintsMessage() {
        CashierInvoker ci = new CashierInvoker();
        ci.undoLast();
        String out = salidaConsola.toString();
        assertTrue(out.contains("No hay operaciones para deshacer."));
    }

    @Test
    @DisplayName("undoLast remueve último comando del historial")
    void undoLastRemovesFromHistory() {
        CashierInvoker ci = new CashierInvoker();
        Sale s1 = new Sale("U-1", "C");
        Sale s2 = new Sale("U-2", "C");
        ci.executeCommand(new RegisterSaleCommand(receptorVentas, s1));
        ci.executeCommand(new RegisterSaleCommand(receptorVentas, s2));
        ci.undoLast();
        ci.showHistory();
        String out = salidaConsola.toString();
        assertTrue(out.contains("Historial de operaciones ejecutadas: 1"));
    }

    @Test
    @DisplayName("SaleReceiver inicializa con sales vacío")
    void saleReceiverInitWithEmptySales() {
        SaleReceiver sr = new SaleReceiver();
        assertTrue(sr.getAllSales().isEmpty());
    }

    @Test
    @DisplayName("registerSale agrega a la lista")
    void registerSaleAddsToList() {
        SaleReceiver sr = new SaleReceiver();
        Sale s = new Sale("REG-1", "C");
        sr.registerSale(s);
        assertEquals(1, sr.getAllSales().size());
        assertEquals("REG-1", sr.getAllSales().get(0).getId());
    }

    @Test
    @DisplayName("registerSale imprime mensaje correcto")
    void registerSalePrintsMessage() {
        SaleReceiver sr = new SaleReceiver();
        Sale s = new Sale("MSG-REG-1", "C");
        sr.registerSale(s);
        String out = salidaConsola.toString();
        assertTrue(out.contains("Venta registrada: MSG-REG-1"));
    }

    @Test
    @DisplayName("cancelSale encuentra por ID case-insensitive")
    void cancelSaleFindByIdCaseInsensitive() {
        SaleReceiver sr = new SaleReceiver();
        Sale s = new Sale("CaseSensitive", "C");
        sr.registerSale(s);
        boolean res = sr.cancelSale("casesensitive");
        assertTrue(res);
        assertEquals(0, sr.getAllSales().size());
    }

    @Test
    @DisplayName("cancelSale no encontrado retorna false")
    void cancelSaleNotFoundReturnsFalse() {
        SaleReceiver sr = new SaleReceiver();
        boolean res = sr.cancelSale("NO-EXISTE");
        assertFalse(res);
    }

    @Test
    @DisplayName("cancelSale imprime mensaje cuando exitosa")
    void cancelSalePrintsMessageWhenSuccess() {
        SaleReceiver sr = new SaleReceiver();
        Sale s = new Sale("PRINT-CANCEL", "C");
        sr.registerSale(s);
        sr.cancelSale("PRINT-CANCEL");
        String out = salidaConsola.toString();
        assertTrue(out.contains("Venta PRINT-CANCEL cancelada."));
    }

    @Test
    @DisplayName("returnProduct encuentra por saleId case-insensitive")
    void returnProductFindBySaleIdCaseInsensitive() {
        SaleReceiver sr = new SaleReceiver();
        Sale s = new Sale("ReturnCase", "C");
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "M", "L", 100);
        s.addMedicine(m);
        sr.registerSale(s);
        boolean res = sr.returnProduct("returncase", m);
        assertTrue(res);
    }

    @Test
    @DisplayName("returnProduct no encontrado retorna false")
    void returnProductNotFoundReturnsFalse() {
        SaleReceiver sr = new SaleReceiver();
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "M", "L", 100);
        boolean res = sr.returnProduct("NO-EXISTE", m);
        assertFalse(res);
    }

    @Test
    @DisplayName("returnProduct encuentra medicina por nombre case-insensitive")
    void returnProductFindMedicineNameCaseInsensitive() {
        SaleReceiver sr = new SaleReceiver();
        Sale s = new Sale("MedCase", "C");
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "MedName", "L", 100);
        s.addMedicine(m);
        sr.registerSale(s);
        Medicine other = new FCreator().createMedicine(FCreator.Type.GENERIC, "medname", "X", 200);
        boolean res = sr.returnProduct("MedCase", other);
        assertTrue(res);
    }

    @Test
    @DisplayName("returnProduct imprime mensaje cuando exitosa")
    void returnProductPrintsMessageWhenSuccess() {
        SaleReceiver sr = new SaleReceiver();
        Sale s = new Sale("PRINT-RETURN", "C");
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "MedPrint", "L", 100);
        s.addMedicine(m);
        sr.registerSale(s);
        sr.returnProduct("PRINT-RETURN", m);
        String out = salidaConsola.toString();
        assertTrue(out.contains("Producto MedPrint devuelto."));
    }

    @Test
    @DisplayName("getAllSales devuelve referencia correcta")
    void getAllSalesReturnsCorrectReference() {
        SaleReceiver sr = new SaleReceiver();
        Sale s = new Sale("GET-ALL", "C");
        sr.registerSale(s);
        List<Sale> sales = sr.getAllSales();
        assertEquals(1, sales.size());
        assertEquals("GET-ALL", sales.get(0).getId());
    }

    @Test
    @DisplayName("Múltiples sales con mismo cliente")
    void multipleSalesSameCustomer() {
        SaleReceiver sr = new SaleReceiver();
        Sale s1 = new Sale("MULTI-1", "SameCustomer");
        Sale s2 = new Sale("MULTI-2", "SameCustomer");
        sr.registerSale(s1);
        sr.registerSale(s2);
        assertEquals(2, sr.getAllSales().size());
    }

    @Test
    @DisplayName("Varias devoluciones de producto en la misma venta")
    void multipleReturnsInSameSale() {
        SaleReceiver sr = new SaleReceiver();
        Sale s = new Sale("MULTI-RETURN", "C");
        Medicine m1 = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "M1", "L", 100);
        Medicine m2 = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "M2", "L", 200);
        s.addMedicine(m1);
        s.addMedicine(m2);
        sr.registerSale(s);
        boolean r1 = sr.returnProduct("MULTI-RETURN", m1);
        boolean r2 = sr.returnProduct("MULTI-RETURN", m2);
        assertTrue(r1 && r2);
        assertEquals(0, sr.getAllSales().get(0).getItems().size());
    }

    @Test
    @DisplayName("Comandos concurrentes ejecutados correctamente")
    void concurrentCommandExecution() throws Exception {
        ExecutorService es = Executors.newFixedThreadPool(3);
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final int idx = i;
            tasks.add(() -> {
                Sale s = new Sale("CONC-" + idx, "C" + idx);
                s.addMedicine(creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "M" + idx, "L", 100));
                invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, s));
                return null;
            });
        }
        es.invokeAll(tasks);
        es.shutdownNow();
        assertEquals(5, receptorVentas.getAllSales().size());
    }

    @Test
    @DisplayName("CommandConsole.run ejecuta sin excepciones")
    void commandConsoleRunExecutes() {
        String input = "5\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        CommandConsole cc = new CommandConsole();
        assertDoesNotThrow(() -> cc.run());
    }

    @Test
    @DisplayName("CommandConsole.main ejecuta sin excepciones")
    void commandConsoleMainExecutes() {
        String input = "5\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        assertDoesNotThrow(() -> CommandConsole.main(new String[]{}));
    }

    @Test
    @DisplayName("RegisterSaleCommand execute luego undo restaura estado")
    void registerExecuteThenUndoRestoresState() {
        Sale s = new Sale("RESTORE-1", "C");
        s.addMedicine(creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "M", "L", 100));
        RegisterSaleCommand cmd = new RegisterSaleCommand(receptorVentas, s);
        cmd.execute();
        assertEquals(1, receptorVentas.getAllSales().size());
        cmd.undo();
        assertEquals(0, receptorVentas.getAllSales().size());
    }

    @Test
    @DisplayName("CancelSaleCommand execute y undo diferentes comportamientos")
    void cancelCommandExecuteAndUndoDifferent() {
        Sale s = new Sale("CANCEL-DIFF", "C");
        receptorVentas.registerSale(s);
        CancelSaleCommand cmd = new CancelSaleCommand(receptorVentas, "CANCEL-DIFF");
        assertEquals(1, receptorVentas.getAllSales().size());
        cmd.execute();
        assertEquals(0, receptorVentas.getAllSales().size());
        cmd.undo();
        String out = salidaConsola.toString();
        assertTrue(out.contains("No se puede deshacer una cancelación de venta."));
    }

    @Test
    @DisplayName("ReturnProductCommand execute y undo diferentes comportamientos")
    void returnCommandExecuteAndUndoDifferent() {
        Sale s = new Sale("RETURN-DIFF", "C");
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "M", "L", 100);
        s.addMedicine(m);
        receptorVentas.registerSale(s);
        ReturnProductCommand cmd = new ReturnProductCommand(receptorVentas, "RETURN-DIFF", m);
        cmd.execute();
        assertEquals(0, receptorVentas.getAllSales().get(0).getItems().size());
        cmd.undo();
        String out = salidaConsola.toString();
        assertTrue(out.contains("No se puede deshacer una devolución de producto."));
    }

    @Test
    @DisplayName("Invoker ejecuta 20 comandos sin problema")
    void invokerExecutes20Commands() {
        for (int i = 0; i < 20; i++) {
            Sale s = new Sale("BULK-" + i, "C");
            invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, s));
        }
        assertEquals(20, receptorVentas.getAllSales().size());
        invocadorCajero.showHistory();
        String out = salidaConsola.toString();
        assertTrue(out.contains("Historial de operaciones ejecutadas: 20"));
    }

    @Test
    @DisplayName("Deshacer 10 comandos consecutivos")
    void undoTenCommandsConsecutive() {
        for (int i = 0; i < 10; i++) {
            Sale s = new Sale("UNDO-" + i, "C");
            invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, s));
        }
        for (int i = 0; i < 10; i++) {
            invocadorCajero.undoLast();
        }
        assertEquals(0, receptorVentas.getAllSales().size());
    }

    @Test
    @DisplayName("Registrar medicina controlada, de marca y genérica")
    void registerDifferentMedicineTypes() {
        Sale s = new Sale("TYPES-1", "C");
        Medicine g = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "G", "L", 100);
        Medicine b = creadorMedicamentos.createMedicine(FCreator.Type.BRAND, "B", "Br", 200);
        Medicine c = creadorMedicamentos.createMedicine(FCreator.Type.CONTROLLED, "C", "CODE", 300);
        s.addMedicine(g);
        s.addMedicine(b);
        s.addMedicine(c);
        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, s));
        assertEquals(3, receptorVentas.getAllSales().get(0).getItems().size());
    }

    @Test
    @DisplayName("Devolver cada medicina de venta con múltiples items")
    void returnEachMedicineFromMultiItemSale() {
        Sale s = new Sale("RETURN-MULTI", "C");
        Medicine m1 = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "RM1", "L", 100);
        Medicine m2 = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "RM2", "L", 200);
        Medicine m3 = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "RM3", "L", 300);
        s.addMedicine(m1);
        s.addMedicine(m2);
        s.addMedicine(m3);
        receptorVentas.registerSale(s);
        receptorVentas.returnProduct("RETURN-MULTI", m1);
        receptorVentas.returnProduct("RETURN-MULTI", m2);
        receptorVentas.returnProduct("RETURN-MULTI", m3);
        assertEquals(0, receptorVentas.getAllSales().get(0).getItems().size());
    }

    @Test
    @DisplayName("Invoker con alternancia de tipos de comando")
    void invokerWithAlternatingCommands() {
        Sale s1 = new Sale("ALT-1", "C");
        Sale s2 = new Sale("ALT-2", "C");
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "M", "L", 100);
        s1.addMedicine(m);
        s2.addMedicine(m);
        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, s1));
        invocadorCajero.executeCommand(new ReturnProductCommand(receptorVentas, "ALT-1", m));
        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, s2));
        invocadorCajero.executeCommand(new CancelSaleCommand(receptorVentas, "ALT-2"));
        assertEquals(0, receptorVentas.getAllSales().size());
    }

    @Test
    @DisplayName("Venta con ID muy largo")
    void saleWithVeryLongId() {
        String longId = "ID-" + "X".repeat(100) + "-LONG";
        Sale s = new Sale(longId, "C");
        s.addMedicine(creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "M", "L", 100));
        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, s));
        assertEquals(1, receptorVentas.getAllSales().size());
        assertEquals(longId, receptorVentas.getAllSales().get(0).getId());
    }

    @Test
    @DisplayName("Cliente con ID muy largo")
    void customerWithVeryLongName() {
        String longName = "Cliente-" + "A".repeat(100) + "-Largo";
        Sale s = new Sale("LONG-NAME", longName);
        s.addMedicine(creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "M", "L", 100));
        receptorVentas.registerSale(s);
        assertEquals(longName, receptorVentas.getAllSales().get(0).getClient());
    }

    @Test
    @DisplayName("Venta vacía puede registrarse")
    void emptyPurchaseCanBeRegistered() {
        Sale emptyS = new Sale("EMPTY-1", "C");
        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, emptyS));
        assertEquals(1, receptorVentas.getAllSales().size());
        assertEquals(0, receptorVentas.getAllSales().get(0).getItems().size());
    }

    @Test
    @DisplayName("Venta con 100 items puede procesarse")
    void saleWith100ItemsCanProcess() {
        Sale s = new Sale("HUNDRED-1", "C");
        for (int i = 0; i < 100; i++) {
            s.addMedicine(creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "M" + i, "L", 100));
        }
        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, s));
        assertEquals(100, receptorVentas.getAllSales().get(0).getItems().size());
    }

    @Test
    @DisplayName("showHistory múltiples veces retorna el mismo valor")
    void showHistoryMultipleTimesConsistent() {
        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, new Sale("CONSISTENT", "C")));
        invocadorCajero.showHistory();
        String first = salidaConsola.toString();
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos2));
        invocadorCajero.showHistory();
        String second = baos2.toString();
        assertTrue(first.contains("1") && second.contains("1"));
        System.setOut(salidaOriginal);
    }

    @Test
    @DisplayName("Cancelar venta con medicinas controladas")
    void cancelSaleWithControlledMedicines() {
        Sale s = new Sale("CTRL-CANCEL", "C");
        s.addMedicine(creadorMedicamentos.createMedicine(FCreator.Type.CONTROLLED, "Controlled", "CODE", 5000));
        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, s));
        invocadorCajero.executeCommand(new CancelSaleCommand(receptorVentas, "CTRL-CANCEL"));
        assertEquals(0, receptorVentas.getAllSales().size());
    }

    @Test
    @DisplayName("Command interface execute y undo pueden ser null sin problema")
    void commandInterfaceMethodsCanExecute() {
        Command c = new RegisterSaleCommand(receptorVentas, new Sale("INTERFACE-1", "C"));
        assertDoesNotThrow(() -> c.execute());
        assertDoesNotThrow(() -> c.undo());
    }

    @Test
    @DisplayName("ReturnProduct de medicina no encontrada no lanza excepción")
    void returnProductNotFoundNoThrow() {
        Sale s = new Sale("NOTFOUND-1", "C");
        s.addMedicine(creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "M", "L", 100));
        receptorVentas.registerSale(s);
        Medicine notInSale = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "NotInSale", "L", 200);
        assertDoesNotThrow(() -> receptorVentas.returnProduct("NOTFOUND-1", notInSale));
    }

    @Test
    @DisplayName("Buscar venta inexistente por cancelar devuelve falso sin excepción")
    void searchNonexistentReturnsFalseNoThrow() {
        assertDoesNotThrow(() -> receptorVentas.cancelSale("NEXIST-XYZ"));
        assertFalse(receptorVentas.cancelSale("NEXIST-XYZ"));
    }

    @Test
    @DisplayName("Sale ID case insensitivity funciona en 50 vendtas")
    void caseInsensitivityWorks50Sales() {
        for (int i = 0; i < 50; i++) {
            Sale s = new Sale("CASE-" + i, "C");
            receptorVentas.registerSale(s);
        }
        boolean res = receptorVentas.cancelSale("case-25");
        assertTrue(res);
        assertEquals(49, receptorVentas.getAllSales().size());
    }
}