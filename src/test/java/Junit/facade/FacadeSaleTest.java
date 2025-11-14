package Junit.facade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.facade.controller.*;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.adapter.model.*;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

@DisplayName("Pruebas del Patrón Facade para Ventas (ampliadas)")
class FacadeSaleTest {

    private FacadeSale fachada;
    private InventSystem sistemaInventario;
    private FCreator creadorMedicamentos;
    private CashMethod efectivo;
    private CreditCardMethod tarjeta;
    private EWalletMethod billetera;
    private AdapterPayMethod adaptadorPagos;
    private PaySystem sistemaPagos;
    private ReceiptSystem sistemaRecibos;

    @BeforeEach
    void configurarSistemas() {
        sistemaInventario = new InventSystem();
        efectivo = new CashMethod(100000);
        tarjeta = new CreditCardMethod("1234", "Farmacia Central", "999", 200000);
        billetera = new EWalletMethod("WALLET-01", 50000);
        adaptadorPagos = new AdapterPayMethod(efectivo, tarjeta, billetera);
        sistemaPagos = new PaySystem(adaptadorPagos);
        sistemaRecibos = new ReceiptSystem();
        fachada = new FacadeSale(sistemaInventario, sistemaPagos, sistemaRecibos);
        creadorMedicamentos = new FCreator();
    }

    @Test
    @DisplayName("Debe procesar exitosamente una venta completa con pago en efectivo")
    void debeProcesarVentaCompletaConEfectivo() {
        Medicine medicamento = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Amoxicilina", "GenFarma", 8000);
        sistemaInventario.addToStock(medicamento);
        Sale venta = new Sale("FAC-001", "Carmen Rodríguez");
        venta.addMedicine(medicamento);
        boolean resultado = fachada.doSale(venta, "CASH");
        assertTrue(resultado);
    }

    @Test
    @DisplayName("Debe procesar venta con tarjeta de crédito correctamente")
    void debeProcesarVentaConTarjetaCredito() {
        Medicine medicamento = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Aspirina Protect", "Bayer", 15000);
        sistemaInventario.addToStock(medicamento);
        Sale venta = new Sale("FAC-002", "Jorge Morales");
        venta.addMedicine(medicamento);
        boolean resultado = fachada.doSale(venta, "CREDIT");
        assertTrue(resultado);
    }

    @Test
    @DisplayName("Debe procesar venta con billetera electrónica")
    void debeProcesarVentaConBilleteraElectronica() {
        Medicine medicamento = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Dolex Gripa", "GSK", 12000);
        sistemaInventario.addToStock(medicamento);
        Sale venta = new Sale("FAC-004", "Ricardo Herrera");
        venta.addMedicine(medicamento);
        boolean resultado = fachada.doSale(venta, "EWALLET");
        assertTrue(resultado);
    }

    @Test
    @DisplayName("Venta con producto no disponible falla")
    void ventaConProductoNoDisponibleFalla() {
        Medicine medicamento = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Inexistente", "LabX", 5000);
        Sale venta = new Sale("FAC-005", "Cliente X");
        venta.addMedicine(medicamento);
        boolean resultado = fachada.doSale(venta, "CASH");
        assertFalse(resultado);
    }

    @Test
    @DisplayName("Pago insuficiente con efectivo provoca fallo")
    void pagoInsuficienteConEfectivoFalla() {
        Medicine medicamento = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Caris", "LabY", 90000);
        sistemaInventario.addToStock(medicamento);
        CashMethod efectivoPequeno = new CashMethod(1000);
        AdapterPayMethod adaptadorLimitado = new AdapterPayMethod(efectivoPequeno, tarjeta, billetera);
        FacadeSale fachadaLimitada = new FacadeSale(sistemaInventario, new PaySystem(adaptadorLimitado), new ReceiptSystem());
        Sale venta = new Sale("FAC-006", "Cliente Y");
        venta.addMedicine(medicamento);
        boolean resultado = fachadaLimitada.doSale(venta, "CASH");
        assertFalse(resultado);
    }

    @Test
    @DisplayName("Después de venta exitosa, producto se remueve del inventario")
    void productoRemovidoDelInventarioTrasVenta() {
        Medicine medicamento = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "GenFarma", 3500);
        sistemaInventario.addToStock(medicamento);
        Sale venta = new Sale("FAC-007", "Cliente Z");
        venta.addMedicine(medicamento);
        boolean resultado = fachada.doSale(venta, "CASH");
        assertTrue(resultado);
        assertFalse(sistemaInventario.verifyStock(medicamento));
    }

    @Test
    @DisplayName("Venta fallida no remueve producto del inventario")
    void ventaFallidaNoRemueveProducto() {
        Medicine medicamento = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Ibupirac", "Farmalab", 45000);
        sistemaInventario.addToStock(medicamento);
        CashMethod efectivoMuyBajo = new CashMethod(100);
        AdapterPayMethod adaptadorPobre = new AdapterPayMethod(efectivoMuyBajo, tarjeta, billetera);
        FacadeSale fachadaPobre = new FacadeSale(sistemaInventario, new PaySystem(adaptadorPobre), new ReceiptSystem());
        Sale venta = new Sale("FAC-008", "Cliente W");
        venta.addMedicine(medicamento);
        boolean resultado = fachadaPobre.doSale(venta, "CASH");
        assertFalse(resultado);
        assertTrue(sistemaInventario.verifyStock(medicamento));
    }

    @Test
    @DisplayName("Modo de pago case-insensitive funciona")
    void modoPagoCaseInsensitive() {
        Medicine medicamento = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Clorfenamina", "LabC", 3000);
        sistemaInventario.addToStock(medicamento);
        Sale venta = new Sale("FAC-009", "Cliente V");
        venta.addMedicine(medicamento);
        boolean r1 = fachada.doSale(venta, "cash");
        sistemaInventario.addToStock(medicamento);
        boolean r2 = fachada.doSale(venta, "CrEdIt");
        assertTrue(r1);
        assertTrue(r2);
    }

    @Test
    @DisplayName("Procesar múltiples ventas secuenciales")
    void procesarMultiplesVentasSecuenciales() {
        Medicine m1 = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "MedA", "LabA", 1000);
        Medicine m2 = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "MedB", "LabB", 2000);
        sistemaInventario.addToStock(m1);
        sistemaInventario.addToStock(m2);
        Sale s1 = new Sale("FAC-010", "C1");
        Sale s2 = new Sale("FAC-011", "C2");
        s1.addMedicine(m1);
        s2.addMedicine(m2);
        assertTrue(fachada.doSale(s1, "CASH"));
        assertTrue(fachada.doSale(s2, "CASH"));
    }

    @Test
    @DisplayName("Venta con medicamento repetido procesa si hay stock suficiente")
    void ventaConMedicamentoRepetido() {
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Duplicado", "LabD", 500);
        sistemaInventario.addToStock(m);
        sistemaInventario.addToStock(m);
        Sale venta = new Sale("FAC-012", "Cliente D");
        venta.addMedicine(m);
        venta.addMedicine(m);
        boolean resultado = fachada.doSale(venta, "CASH");
        assertTrue(resultado);
        assertFalse(sistemaInventario.verifyStock(m));
    }

    @Test
    @DisplayName("Método de pago inválido provoca fallo")
    void metodoPagoInvalidoFalla() {
        Medicine medicamento = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Simple", "LabS", 1000);
        sistemaInventario.addToStock(medicamento);
        Sale venta = new Sale("FAC-013", "Cliente S");
        venta.addMedicine(medicamento);
        boolean resultado = fachada.doSale(venta, "INVALID");
        assertFalse(resultado);
    }

    @Test
    @DisplayName("ReceiptSystem imprime recibo con datos correctos")
    void receiptSystemPrintsReceipt() {
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "TestR", "LabR", 1234);
        Sale s = new Sale("R-001", "Cliente R");
        s.addMedicine(m);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream old = System.out;
        try {
            System.setOut(new PrintStream(baos));
            sistemaRecibos.generateReceipt(s);
            String out = baos.toString();
            assertTrue(out.contains("RECIBO DE VENTA"));
            assertTrue(out.contains("Cliente: " + s.getClient()));
            assertTrue(out.contains(m.getName()));
            assertTrue(out.contains(String.valueOf((int)s.getTotal())));
        } finally {
            System.setOut(old);
        }
    }

    @Test
    @DisplayName("InventSystem add, verify y remove funcionan correctamente")
    void inventSystemAddVerifyRemove() {
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "StockTest", "LabS", 700);
        assertFalse(sistemaInventario.verifyStock(m));
        sistemaInventario.addToStock(m);
        assertTrue(sistemaInventario.verifyStock(m));
        sistemaInventario.removeFromStock(m);
        assertFalse(sistemaInventario.verifyStock(m));
    }

    @Test
    @DisplayName("PaySystem delega correctamente al adaptador y respeta modos inválidos")
    void paySystemDelegationAndInvalidMode() {
        AdapterPayMethod localAdapter = new AdapterPayMethod(new CashMethod(500), tarjeta, billetera);
        PaySystem ps = new PaySystem(localAdapter);
        assertTrue(ps.processPayment(100, "CASH"));
        assertFalse(ps.processPayment(1000, "UNKNOWN_MODE"));
    }

    @Test
    @DisplayName("doSale imprime mensajes de éxito y recibo en venta exitosa")
    void doSalePrintsReceiptAndSuccess() {
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "MsgTest", "LabM", 1500);
        sistemaInventario.addToStock(m);
        Sale s = new Sale("MSG-1", "Cliente MSG");
        s.addMedicine(m);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream old = System.out;
        try {
            System.setOut(new PrintStream(baos));
            boolean ok = fachada.doSale(s, "CASH");
            String out = baos.toString();
            assertTrue(ok);
            assertTrue(out.contains("Procesando venta"));
            assertTrue(out.contains("RECIBO DE VENTA"));
            assertTrue(out.contains("Venta completada con éxito."));
        } finally {
            System.setOut(old);
        }
    }

    @Test
    @DisplayName("doSale con lista vacía de items procesa sin remover stock y devuelve true")
    void doSaleWithEmptyItems() {
        Sale s = new Sale("EMPTY-1", "Cliente Empty");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream old = System.out;
        try {
            System.setOut(new PrintStream(baos));
            boolean ok = fachada.doSale(s, "CASH");
            assertTrue(ok);
        } finally {
            System.setOut(old);
        }
    }

    @Test
    @DisplayName("Sale total se calcula correctamente al agregar varios medicamentos")
    void saleTotalCalculatesCorrectly() {
        Medicine a = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "A", "L", 100);
        Medicine b = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "B", "L", 200);
        Sale s = new Sale("T-1", "Cliente T");
        s.addMedicine(a);
        s.addMedicine(b);
        assertEquals(300, s.getTotal(), 0.001);
    }

    @Test
    @DisplayName("doSale no modifica la lista de items original más allá de remover stock")
    void doSaleDoesNotMutateSaleItemsList() {
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Keep", "L", 800);
        sistemaInventario.addToStock(m);
        Sale s = new Sale("MUT-1", "Cliente M");
        s.addMedicine(m);
        int sizeBefore = s.getItems().size();
        fachada.doSale(s, "CASH");
        assertEquals(sizeBefore, s.getItems().size());
    }

    @Test
    @DisplayName("InventSystem verifica existencia ignorando mayúsculas")
    void inventSystemCaseInsensitiveVerify() {
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "CaseName", "L", 100);
        sistemaInventario.addToStock(m);
        Medicine query = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "casename", "L", 100);
        assertTrue(sistemaInventario.verifyStock(query));
    }

    @Test
    @DisplayName("doSale remueve correctamente multiples items presentes")
    void doSaleRemovesMultipleItems() {
        Medicine m1 = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Rm1", "L", 100);
        Medicine m2 = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Rm2", "L", 200);
        sistemaInventario.addToStock(m1);
        sistemaInventario.addToStock(m2);
        Sale s = new Sale("RM-1", "Cliente RM");
        s.addMedicine(m1);
        s.addMedicine(m2);
        boolean ok = fachada.doSale(s, "CASH");
        assertTrue(ok);
        assertFalse(sistemaInventario.verifyStock(m1));
        assertFalse(sistemaInventario.verifyStock(m2));
    }

    @Test
    @DisplayName("doSale con adaptador que tiene solo efectivo insuficiente falla y no remueve stock")
    void doSaleWithLimitedCashAdapterFailsAndKeepsStock() {
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Lim", "L", 50000);
        sistemaInventario.addToStock(m);
        CashMethod tinyCash = new CashMethod(10);
        AdapterPayMethod limited = new AdapterPayMethod(tinyCash, tarjeta, billetera);
        FacadeSale fLimited = new FacadeSale(sistemaInventario, new PaySystem(limited), sistemaRecibos);
        Sale s = new Sale("LIM-1", "Cliente L");
        s.addMedicine(m);
        boolean ok = fLimited.doSale(s, "CASH");
        assertFalse(ok);
        assertTrue(sistemaInventario.verifyStock(m));
    }

    @Test
    @DisplayName("ReceiptSystem genera recibo con múltiples items y total correcto")
    void receiptSystemMultipleItemsReceipt() {
        Medicine m1 = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "R1", "L", 111);
        Medicine m2 = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "R2", "L", 222);
        Sale s = new Sale("REC-2", "Cliente Multi");
        s.addMedicine(m1);
        s.addMedicine(m2);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream old = System.out;
        try {
            System.setOut(new PrintStream(baos));
            sistemaRecibos.generateReceipt(s);
            String out = baos.toString();
            assertTrue(out.contains("RECIBO DE VENTA"));
            assertTrue(out.contains("R1"));
            assertTrue(out.contains("R2"));
            assertTrue(out.contains(String.valueOf((int)s.getTotal())));
        } finally {
            System.setOut(old);
        }
    }

    @Test
    @DisplayName("PaySystem con adaptador nulo no lanza y devuelve false")
    void paySystemWithNullAdapterReturnsFalse() {
        PaySystem ps = new PaySystem(new AdapterPayMethod(null, null, null));
        boolean r = ps.processPayment(100, "CASH");
        assertFalse(r);
    }

    @Test
    @DisplayName("doSale con venta vacía no remueve stock existente")
    void doSaleWithEmptySaleDoesNotAffectStock() {
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Stable", "L", 50);
        sistemaInventario.addToStock(m);
        Sale s = new Sale("EMPTY-2", "Cliente Stable");
        boolean ok = fachada.doSale(s, "CASH");
        assertTrue(ok);
        assertTrue(sistemaInventario.verifyStock(m));
    }

    @Test
    @DisplayName("Múltiples doSale consecutivos consumen stock adecuadamente")
    void multipleDoSaleConsumeStock() {
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Seq", "L", 10);
        sistemaInventario.addToStock(m);
        sistemaInventario.addToStock(m);
        Sale s1 = new Sale("SEQ-1", "C1"); s1.addMedicine(m);
        Sale s2 = new Sale("SEQ-2", "C2"); s2.addMedicine(m);
        assertTrue(fachada.doSale(s1, "CASH"));
        assertTrue(fachada.doSale(s2, "CASH"));
        assertFalse(sistemaInventario.verifyStock(m));
    }

    @Test
    @DisplayName("doSale con modo en mayúsculas y minúsculas funciona consistentemente")
    void doSaleModeCaseVariants() {
        Medicine m = creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "CaseX", "L", 500);
        sistemaInventario.addToStock(m);
        Sale s = new Sale("CASE-1", "Client");
        s.addMedicine(m);
        assertTrue(fachada.doSale(s, "cAsH"));
    }

    @Test
    @DisplayName("ReceiptSystem imprime recuadro completo incluso con total 0")
    void receiptSystemPrintsEvenWhenTotalZero() {
        Sale s = new Sale("ZERO-REC", "Cliente Zero");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream old = System.out;
        try {
            System.setOut(new PrintStream(baos));
            sistemaRecibos.generateReceipt(s);
            String out = baos.toString();
            assertTrue(out.contains("RECIBO DE VENTA"));
            assertTrue(out.contains("TOTAL: $0.0") || out.contains("TOTAL: $0"));
        } finally {
            System.setOut(old);
        }
    }

    @Test
    @DisplayName("FacadeSale no altera objeto Sale más allá de lecturas y no lanza en entradas nulas")
    void facadeHandlesNullAndDoesNotMutate() {
        Sale s = new Sale("NUL-1", "Nulo");
        boolean ok = fachada.doSale(s, null);
        assertFalse(ok);
    }
}