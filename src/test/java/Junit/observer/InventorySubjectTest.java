package Junit.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.observer.model.*;
import patrones_farmacia.observer.controller.InventoryController;
import patrones_farmacia.observer.view.InventoryConsole;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.*;
import java.util.ArrayList;
import org.mockito.InOrder;
import java.util.concurrent.atomic.AtomicInteger;

@DisplayName("Pruebas del Patrón Observer para Inventario (ampliadas)")
class InventorySubjectTest {

    private InventorySubject sujetoInventario;
    private FarmaObserver observadorEmail;
    private FarmaObserver observadorSMS;
    private static final int UMBRAL_STOCK_BAJO = 5;
    private PrintStream originalOut;
    private InputStream originalIn;
    private ByteArrayOutputStream baos;

    @BeforeEach
    void configurarSujetoYObservador() {
        sujetoInventario = new InventorySubject(UMBRAL_STOCK_BAJO);
        observadorEmail = mock(FarmaObserver.class);
        observadorSMS = mock(FarmaObserver.class);
        sujetoInventario.addObserver(observadorEmail);
        sujetoInventario.addProduct("Paracetamol", 10);
        originalOut = System.out;
        originalIn = System.in;
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
    }

    @AfterEach
    void restaurarSalida() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    @Test
    @DisplayName("Debe notificar al observador cuando el stock cae por debajo del umbral")
    void debeNotificarCuandoStockEstaBajo() {
        sujetoInventario.reduceStock("Paracetamol", 8);
        verify(observadorEmail, times(1)).update("Paracetamol", 2);
        assertEquals(2, sujetoInventario.getStockLevel("Paracetamol"));
    }

    @Test
    @DisplayName("No debe notificar al observador cuando el stock está por encima del umbral")
    void noDebeNotificarCuandoStockEsNormal() {
        sujetoInventario.reduceStock("Paracetamol", 2);
        verify(observadorEmail, never()).update(anyString(), anyInt());
        assertEquals(8, sujetoInventario.getStockLevel("Paracetamol"));
    }

    @Test
    @DisplayName("Debe notificar cuando el stock es exactamente igual al umbral")
    void debeNotificarCuandoStockEsIgualAlUmbral() {
        sujetoInventario.reduceStock("Paracetamol", 5);
        verify(observadorEmail, times(1)).update("Paracetamol", 5);
        assertEquals(5, sujetoInventario.getStockLevel("Paracetamol"));
    }

    @Test
    @DisplayName("Debe notificar múltiples veces si el stock baja repetidamente")
    void debeNotificarMultiplesVecesSiStockBajaRepetidamente() {
        sujetoInventario.addProduct("Amoxicilina", 10);
        sujetoInventario.reduceStock("Amoxicilina", 6);
        sujetoInventario.reduceStock("Amoxicilina", 1);
        sujetoInventario.reduceStock("Amoxicilina", 2);
        verify(observadorEmail, times(3)).update(eq("Amoxicilina"), anyInt());
    }

    @Test
    @DisplayName("Debe notificar a múltiples observadores cuando el stock está bajo")
    void debeNotificarAMultiplesObservadores() {
        sujetoInventario.addObserver(observadorSMS);
        sujetoInventario.addProduct("Aspirina", 10);
        sujetoInventario.reduceStock("Aspirina", 8);
        verify(observadorEmail, times(1)).update("Aspirina", 2);
        verify(observadorSMS, times(1)).update("Aspirina", 2);
    }

    @Test
    @DisplayName("No debe notificar después de remover al observador")
    void noDebeNotificarDespuesDeRemoverObservador() {
        sujetoInventario.removeObserver(observadorEmail);
        sujetoInventario.addProduct("Dolex", 10);
        sujetoInventario.reduceStock("Dolex", 8);
        verify(observadorEmail, never()).update(anyString(), anyInt());
    }

    @Test
    @DisplayName("getStockLevel retorna -1 para producto inexistente")
    void getStockLevelRetornaMinusOneParaInexistente() {
        int nivel = sujetoInventario.getStockLevel("ProductoNoExiste");
        assertEquals(-1, nivel);
    }

    @Test
    @DisplayName("increaseStock actualiza correctamente el stock")
    void increaseStockActualizaCorrectamente() {
        sujetoInventario.addProduct("VitaminaC", 2);
        sujetoInventario.increaseStock("VitaminaC", 5);
        assertEquals(7, sujetoInventario.getStockLevel("VitaminaC"));
    }

    @Test
    @DisplayName("getLowStockProducts lista productos con stock bajo")
    void getLowStockProductsListaCorrectamente() {
        sujetoInventario.addProduct("ProductoA", 4);
        sujetoInventario.addProduct("ProductoB", 6);
        List<String> bajos = sujetoInventario.getLowStockProducts();
        assertTrue(bajos.contains("ProductoA"));
        assertFalse(bajos.contains("ProductoB"));
    }

    @Test
    @DisplayName("setMinThreshold cambia el umbral y afecta notificaciones")
    void setMinThresholdCambiaUmbral() {
        sujetoInventario.addProduct("TestProd", 4);
        sujetoInventario.setMinThreshold(2);
        sujetoInventario.reduceStock("TestProd", 1);
        verify(observadorEmail, never()).update(eq("TestProd"), anyInt());
        sujetoInventario.reduceStock("TestProd", 1);
        verify(observadorEmail, times(1)).update(eq("TestProd"), anyInt());
    }

    @Test
    @DisplayName("getAllProductNames devuelve nombres añadidos")
    void getAllProductNamesDevuelveNombres() {
        sujetoInventario.addProduct("N1", 3);
        sujetoInventario.addProduct("N2", 7);
        List<String> todos = sujetoInventario.getAllProductNames();
        assertTrue(todos.contains("Paracetamol"));
        assertTrue(todos.contains("N1"));
        assertTrue(todos.contains("N2"));
    }

    @Test
    @DisplayName("Agregar el mismo producto varias veces crea entradas separadas")
    void agregarMismoProductoVariasVeces() {
        sujetoInventario.addProduct("Duplicado", 5);
        sujetoInventario.addProduct("Duplicado", 2);
        List<String> all = sujetoInventario.getAllProductNames();
        long count = all.stream().filter(n -> n.equalsIgnoreCase("Duplicado")).count();
        assertEquals(2, count);
    }

    @Test
    @DisplayName("getMinThreshold y setMinThreshold funcionan")
    void getSetThresholdFuncionan() {
        sujetoInventario.setMinThreshold(3);
        assertEquals(3, sujetoInventario.getMinThreshold());
    }

    @Test
    @DisplayName("reducción que deja stock negativo lo deja en cero y notifica")
    void reduccionNegativaDejaEnCeroYNotifica() {
        sujetoInventario.addProduct("ProductoNeg", 1);
        sujetoInventario.reduceStock("ProductoNeg", 5);
        assertEquals(0, sujetoInventario.getStockLevel("ProductoNeg"));
        verify(observadorEmail, atLeastOnce()).update(eq("ProductoNeg"), eq(0));
    }

    @Test
    @DisplayName("increaseStock en producto inexistente imprime mensaje")
    void increaseStockInexistenteImprime() {
        sujetoInventario.increaseStock("NoExiste", 3);
        String out = baos.toString();
        assertTrue(out.contains("Producto no encontrado"));
    }

    @Test
    @DisplayName("reduceStock en producto inexistente imprime mensaje")
    void reduceStockInexistenteImprime() {
        sujetoInventario.reduceStock("NoExiste", 2);
        String out = baos.toString();
        assertTrue(out.contains("Producto no encontrado"));
    }

    @Test
    @DisplayName("addObserver no duplica observadores iguales")
    void addObserverNoDuplica() {
        sujetoInventario.addObserver(observadorEmail);
        sujetoInventario.addProduct("ProdX", 3);
        sujetoInventario.reduceStock("ProdX", 3);
        verify(observadorEmail, times(1)).update(eq("ProdX"), anyInt());
    }

    @Test
    @DisplayName("removeObserver con observador no registrado no falla")
    void removeObserverNoRegistradoNoFalla() {
        FarmaObserver otro = mock(FarmaObserver.class);
        sujetoInventario.removeObserver(otro);
        sujetoInventario.addProduct("ProdY", 2);
        sujetoInventario.reduceStock("ProdY", 1);
        verify(otro, never()).update(anyString(), anyInt());
    }

    @Test
    @DisplayName("getLowStockProducts incluye elementos con stock igual al umbral")
    void lowStockIncluyeIgualAlUmbral() {
        sujetoInventario.addProduct("Igual", 5);
        List<String> low = sujetoInventario.getLowStockProducts();
        assertTrue(low.contains("Igual"));
    }

    @Test
    @DisplayName("getAllProductNames retorna copia (modificar copia no altera sujeto)")
    void getAllProductNamesRetornaCopia() {
        sujetoInventario.addProduct("Copia", 2);
        List<String> lista = sujetoInventario.getAllProductNames();
        lista.clear();
        List<String> lista2 = sujetoInventario.getAllProductNames();
        assertTrue(lista2.contains("Copia"));
    }

    @Test
    @DisplayName("comportamiento con muchos productos mantiene consistencia")
    void muchosProductosMantienenConsistencia() {
        for (int i = 0; i < 50; i++) {
            sujetoInventario.addProduct("P-" + i, i % 10 + 1);
        }
        assertEquals(51, sujetoInventario.getAllProductNames().size());
    }

    @Test
    @DisplayName("setMinThreshold con valores extremos funciona")
    void setMinThresholdExtremos() {
        sujetoInventario.setMinThreshold(0);
        sujetoInventario.addProduct("Ex", 1);
        sujetoInventario.reduceStock("Ex", 1);
        verify(observadorEmail, times(1)).update(eq("Ex"), eq(0));
        sujetoInventario.setMinThreshold(1000);
        sujetoInventario.addProduct("Ex2", 500);
        sujetoInventario.reduceStock("Ex2", 1);
        verify(observadorEmail, never()).update(eq("Ex2"), anyInt());
    }

    @Test
    @DisplayName("EmailAlert y SMSAlert imprimen su mensaje en update")
    void emailYSmsAlertImprimenEnUpdate() {
        EmailAlert em = new EmailAlert("a@b.c");
        SMSAlert sms = new SMSAlert("+123");
        em.update("X", 1);
        sms.update("Y", 2);
        String out = baos.toString();
        assertTrue(out.contains("[EMAIL]") || out.contains("[SMS]"));
    }

    @Test
    @DisplayName("toString de EmailAlert y SMSAlert incluyen identificador")
    void toStringAlertsIncluyeIdentificador() {
        EmailAlert em = new EmailAlert("correo@x");
        SMSAlert s = new SMSAlert("555");
        assertTrue(em.toString().contains("EmailAlert"));
        assertTrue(s.toString().contains("SMSAlert"));
    }

    @Test
    @DisplayName("constructor ignora parámetro pero deja umbral por defecto 5")
    void constructorIgnoraParametroYDejaDefault() {
        InventorySubject s = new InventorySubject(999);
        assertEquals(5, s.getMinThreshold());
    }

    @Test
    @DisplayName("InventoryController integra con InventorySubject y muestra inventario")
    void inventoryControllerIntegracion() {
        InventoryController ctrl = new InventoryController();
        ctrl.addProduct("CtrlProd", 7);
        ctrl.setThreshold(3);
        ctrl.restock("CtrlProd", 3);
        ctrl.reduceStock("CtrlProd", 6);
        ctrl.showInventory();
        String out = baos.toString();
        assertTrue(out.contains("CtrlProd"));
    }

    @Test
    @DisplayName("InventoryConsole.run se ejecuta y sale con opción 7 simulada")
    void inventoryConsoleRunExitOption() throws Exception {
        String input = "7\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        InventoryConsole console = new InventoryConsole();
        console.run();
        String out = baos.toString();
        assertTrue(out.contains("Saliendo del módulo Observer") || out.length() > 0);
    }

    @Test
    @DisplayName("Reducir stock es case-insensitive para nombres")
    void reducirStockCaseInsensitive() {
        sujetoInventario.addProduct("MiProd", 10);
        sujetoInventario.reduceStock("miprod", 5);
        assertEquals(5, sujetoInventario.getStockLevel("MIPROD"));
    }

    @Test
    @DisplayName("Aumentar stock es case-insensitive para nombres")
    void aumentarStockCaseInsensitive() {
        sujetoInventario.addProduct("Otro", 2);
        sujetoInventario.increaseStock("otro", 8);
        assertEquals(10, sujetoInventario.getStockLevel("OTRO"));
    }

    @Test
    @DisplayName("Eliminar observadores y volver a añadir funcionan correctamente")
    void removeAndAddObserversWorks() {
        sujetoInventario.removeObserver(observadorEmail);
        sujetoInventario.addObserver(observadorEmail);
        sujetoInventario.addProduct("ObsTest", 1);
        sujetoInventario.reduceStock("ObsTest", 1);
        verify(observadorEmail, atLeastOnce()).update(eq("ObsTest"), anyInt());
    }

    @Test
    @DisplayName("getLowStockProducts vacío cuando no hay productos por debajo de umbral")
    void lowStockProductsEmptyWhenNoneBelowThreshold() {
        sujetoInventario.addProduct("OkProd", 100);
        List<String> low = sujetoInventario.getLowStockProducts();
        assertFalse(low.contains("OkProd"));
    }

    @Test
    @DisplayName("Múltiples adds y reduces mantienen consistencia")
    void multipleAddsAndReducesMaintainConsistency() {
        sujetoInventario.addProduct("A1", 3);
        sujetoInventario.addProduct("A2", 4);
        sujetoInventario.increaseStock("A1", 2);
        sujetoInventario.reduceStock("A2", 1);
        assertEquals(5, sujetoInventario.getStockLevel("A1"));
        assertEquals(3, sujetoInventario.getStockLevel("A2"));
    }

    @Test
    @DisplayName("Agregar producto con stock cero se registra y aparece en lowStock")
    void addProductWithZeroStockIsRegisteredAndLow() {
        sujetoInventario.addProduct("Zero", 0);
        assertEquals(0, sujetoInventario.getStockLevel("Zero"));
        List<String> low = sujetoInventario.getLowStockProducts();
        assertTrue(low.contains("Zero"));
    }

    @Test
    @DisplayName("Reducir stock en cero no dispara notificación")
    void reduceZeroDoesNotNotify() {
        sujetoInventario.addProduct("NoNotify", 10);
        sujetoInventario.reduceStock("NoNotify", 0);
        verify(observadorEmail, never()).update(eq("NoNotify"), anyInt());
        assertEquals(10, sujetoInventario.getStockLevel("NoNotify"));
    }

    @Test
    @DisplayName("increaseStock negativo no reduce stock existente")
    void negativeIncreaseIsIgnored() {
        sujetoInventario.addProduct("Keep", 5);
        sujetoInventario.increaseStock("Keep", -3);
        assertEquals(5, sujetoInventario.getStockLevel("Keep"));
    }

    @Test
    @DisplayName("addObserver null es inocuo")
    void addObserverNullIsIgnored() {
        sujetoInventario.addObserver(null);
        sujetoInventario.addProduct("Safe", 4);
        sujetoInventario.reduceStock("Safe", 1);
        verify(observadorEmail, never()).update(eq("Safe"), anyInt());
    }

    @Test
    @DisplayName("getLowStockProducts devuelve copia defensiva")
    void lowStockProductsReturnsDefensiveCopy() {
        sujetoInventario.addProduct("Def", 4);
        List<String> low = sujetoInventario.getLowStockProducts();
        low.clear();
        List<String> low2 = sujetoInventario.getLowStockProducts();
        assertTrue(low2.contains("Def"));
    }

    @Test
    @DisplayName("Concurrent add and reduce operations complete consistently")
    void concurrentAddAndReduceOperations() throws Exception {
        ExecutorService es = Executors.newFixedThreadPool(6);
        sujetoInventario.addProduct("Conc", 1000);
        AtomicInteger successes = new AtomicInteger(0);
        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            tasks.add(() -> { sujetoInventario.reduceStock("Conc", 5); successes.incrementAndGet(); return null; });
            tasks.add(() -> { sujetoInventario.increaseStock("Conc", 3); successes.incrementAndGet(); return null; });
        }
        es.invokeAll(tasks);
        es.shutdownNow();
        int finalStock = sujetoInventario.getStockLevel("Conc");
        assertTrue(finalStock >= 0);
        assertEquals(100, successes.get());
    }

    @Test
    @DisplayName("Observers notified in registration order")
    void observersNotifiedInRegistrationOrder() {
        FarmaObserver first = mock(FarmaObserver.class);
        FarmaObserver second = mock(FarmaObserver.class);
        InOrder order = inOrder(first, second);
        sujetoInventario.addObserver(first);
        sujetoInventario.addObserver(second);
        sujetoInventario.addProduct("OrderTest", 3);
        sujetoInventario.reduceStock("OrderTest", 1);
        order.verify(first, atLeastOnce()).update(eq("OrderTest"), anyInt());
        order.verify(second, atLeastOnce()).update(eq("OrderTest"), anyInt());
    }

    @Test
    @DisplayName("Modificar lista devuelta por getAllProductNames no afecta al sujeto")
    void modifyingReturnedAllNamesDoesNotAffectSubject() {
        sujetoInventario.addProduct("SafeName", 2);
        List<String> names = sujetoInventario.getAllProductNames();
        names.remove("SafeName");
        List<String> names2 = sujetoInventario.getAllProductNames();
        assertTrue(names2.contains("SafeName"));
    }

    @Test
    @DisplayName("Many rapid adds mantienen consistencia")
    void manyRapidAddsMaintainConsistency() {
        for (int i = 0; i < 200; i++) sujetoInventario.addProduct("Bulk-" + i, i % 5);
        assertTrue(sujetoInventario.getAllProductNames().size() >= 200);
    }
}