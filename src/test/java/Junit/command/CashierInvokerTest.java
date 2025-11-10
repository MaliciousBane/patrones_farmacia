package Junit.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.command.controller.*;
import patrones_farmacia.command.model.*;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

class CommandPatternTest {

    private SaleReceiver receiver;
    private CashierInvoker invoker;
    private FCreator creator;
    private ByteArrayOutputStream consoleOutput;

    @BeforeEach
    void setUp() {
        receiver = new SaleReceiver();
        invoker = new CashierInvoker();
        creator = new FCreator();

        consoleOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(consoleOutput));
    }

    @Test
    void testRegisterSaleCommandExecutesCorrectly() {
        Sale sale = new Sale("CMD-001", "Cliente A");
        sale.addMedicine(creator.createMedicine(FCreator.Type.GENERIC, "Ibuprofeno", "GenFarma", 3000));

        RegisterSaleCommand cmd = new RegisterSaleCommand(receiver, sale);
        invoker.executeCommand(cmd);

        String output = consoleOutput.toString();
        assertTrue(output.contains("Venta registrada: CMD-001"),
                   "Debe registrarse la venta correctamente.");
    }

    @Test
    void testCancelSaleCommandRemovesSale() {
        Sale sale = new Sale("CMD-002", "Cliente B");
        sale.addMedicine(creator.createMedicine(FCreator.Type.BRAND, "Dolex Forte", "GSK", 6500));

        RegisterSaleCommand register = new RegisterSaleCommand(receiver, sale);
        invoker.executeCommand(register);

        CancelSaleCommand cancel = new CancelSaleCommand(receiver, "CMD-002");
        invoker.executeCommand(cancel);

        String output = consoleOutput.toString();
        assertTrue(output.contains("Venta cancelada: CMD-002"),
                   "Debe cancelarse la venta con el ID correcto.");
    }

    @Test
    void testUndoRevertsLastCommand() {
        Sale sale = new Sale("CMD-003", "Cliente C");
        sale.addMedicine(creator.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "TecnoFarma", 2800));

        RegisterSaleCommand cmd = new RegisterSaleCommand(receiver, sale);
        invoker.executeCommand(cmd);
        invoker.undoLast();

        String output = consoleOutput.toString();
        assertTrue(output.contains("Deshaciendo: Registrar Venta #CMD-003"));
        assertTrue(output.contains("Venta cancelada: CMD-003"));
    }

    @Test
    void testHistoryStoresExecutedCommands() {
        Sale s1 = new Sale("CMD-004", "Cliente D");
        Sale s2 = new Sale("CMD-005", "Cliente E");

        invoker.executeCommand(new RegisterSaleCommand(receiver, s1));
        invoker.executeCommand(new RegisterSaleCommand(receiver, s2));

        invoker.showHistory();

        String output = consoleOutput.toString();
        assertTrue(output.contains("Registrar Venta #CMD-004"));
        assertTrue(output.contains("Registrar Venta #CMD-005"));
    }

    @Test
    void testUndoWhenHistoryIsEmptyDoesNotFail() {
        invoker.undoLast();
        String output = consoleOutput.toString();
        assertTrue(output.contains("No hay comandos para deshacer."));
    }

    @Test
    void testCancelNonExistingSaleShowsError() {
        CancelSaleCommand cancel = new CancelSaleCommand(receiver, "CMD-999");
        invoker.executeCommand(cancel);

        String output = consoleOutput.toString();
        assertTrue(output.contains("No se encontró la venta CMD-999"),
                   "Debe indicar error al cancelar venta inexistente.");
    }
}