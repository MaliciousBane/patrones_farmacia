package Junit.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.command.controller.*;
import patrones_farmacia.command.model.*;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

@DisplayName("Pruebas del Patrón Command para Gestión de Ventas")
class CommandPatternTest {

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

    @Test
    @DisplayName("Debe registrar una venta correctamente al ejecutar el comando")
    void debeRegistrarVentaCorrectamente() {
        Sale venta = new Sale("CMD-001", "Ana Torres");
        Medicine medicamento = creadorMedicamentos.createMedicine(
            FCreator.Type.GENERIC, 
            "Ibuprofeno", 
            "GenFarma", 
            3000
        );
        venta.addMedicine(medicamento);

        RegisterSaleCommand comandoRegistrar = new RegisterSaleCommand(receptorVentas, venta);
        invocadorCajero.executeCommand(comandoRegistrar);

        String salida = salidaConsola.toString();
        assertTrue(
            salida.contains("Venta registrada: CMD-001"),
            "La salida debe confirmar que la venta CMD-001 fue registrada"
        );
    }

    @Test
    @DisplayName("Debe cancelar una venta existente correctamente")
    void debeCancelarVentaExistente() {
        Sale venta = new Sale("CMD-002", "Roberto Díaz");
        Medicine medicamento = creadorMedicamentos.createMedicine(
            FCreator.Type.BRAND, 
            "Dolex Forte", 
            "GSK", 
            6500
        );
        venta.addMedicine(medicamento);

        RegisterSaleCommand comandoRegistrar = new RegisterSaleCommand(receptorVentas, venta);
        invocadorCajero.executeCommand(comandoRegistrar);

        CancelSaleCommand comandoCancelar = new CancelSaleCommand(receptorVentas, "CMD-002");
        invocadorCajero.executeCommand(comandoCancelar);

        String salida = salidaConsola.toString();
        assertTrue(
            salida.contains("Venta cancelada: CMD-002"),
            "La venta CMD-002 debe ser cancelada correctamente"
        );
    }

    @Test
    @DisplayName("Debe deshacer el último comando ejecutado correctamente")
    void debeDeshacerUltimoComando() {
        Sale venta = new Sale("CMD-003", "Laura Méndez");
        Medicine medicamento = creadorMedicamentos.createMedicine(
            FCreator.Type.GENERIC, 
            "Paracetamol", 
            "TecnoFarma", 
            2800
        );
        venta.addMedicine(medicamento);

        RegisterSaleCommand comando = new RegisterSaleCommand(receptorVentas, venta);
        invocadorCajero.executeCommand(comando);
        invocadorCajero.undoLast();

        String salida = salidaConsola.toString();
        assertTrue(
            salida.contains("Deshaciendo: Registrar Venta #CMD-003"),
            "Debe mostrar mensaje de deshacer la venta CMD-003"
        );
        assertTrue(
            salida.contains("Venta cancelada: CMD-003"),
            "La venta debe ser cancelada al deshacer el comando"
        );
    }

    @Test
    @DisplayName("Debe almacenar el historial de comandos ejecutados")
    void debeAlmacenarHistorialDeComandos() {
        Sale venta1 = new Sale("CMD-004", "Pedro Sánchez");
        Sale venta2 = new Sale("CMD-005", "Sofía Ruiz");

        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, venta1));
        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, venta2));

        invocadorCajero.showHistory();

        String salida = salidaConsola.toString();
        assertTrue(
            salida.contains("Registrar Venta #CMD-004"),
            "El historial debe contener la venta CMD-004"
        );
        assertTrue(
            salida.contains("Registrar Venta #CMD-005"),
            "El historial debe contener la venta CMD-005"
        );
    }

    @Test
    @DisplayName("No debe fallar al intentar deshacer cuando no hay comandos en el historial")
    void noDebeFallarAlDeshacerSinHistorial() {
        invocadorCajero.undoLast();
        
        String salida = salidaConsola.toString();
        assertTrue(
            salida.contains("No hay comandos para deshacer."),
            "Debe mostrar mensaje indicando que no hay comandos para deshacer"
        );
    }

    @Test
    @DisplayName("Debe mostrar error al cancelar una venta que no existe")
    void debeMostrarErrorAlCancelarVentaInexistente() {
        CancelSaleCommand comandoCancelar = new CancelSaleCommand(receptorVentas, "CMD-999");
        invocadorCajero.executeCommand(comandoCancelar);

        String salida = salidaConsola.toString();
        assertTrue(
            salida.contains("No se encontró la venta CMD-999"),
            "Debe indicar que la venta CMD-999 no fue encontrada"
        );
    }

    @Test
    @DisplayName("Debe ejecutar múltiples comandos en secuencia correctamente")
    void debeEjecutarMultiplesComandosEnSecuencia() {
        Sale venta1 = new Sale("CMD-006", "Miguel Ángel");
        Sale venta2 = new Sale("CMD-007", "Valentina López");
        Sale venta3 = new Sale("CMD-008", "Diego Castro");

        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, venta1));
        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, venta2));
        invocadorCajero.executeCommand(new CancelSaleCommand(receptorVentas, "CMD-006"));
        invocadorCajero.executeCommand(new RegisterSaleCommand(receptorVentas, venta3));

        String salida = salidaConsola.toString();
        assertTrue(salida.contains("Venta registrada: CMD-006"));
        assertTrue(salida.contains("Venta registrada: CMD-007"));
        assertTrue(salida.contains("Venta cancelada: CMD-006"));
        assertTrue(salida.contains("Venta registrada: CMD-008"));
    }
}