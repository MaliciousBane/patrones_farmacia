package Junit.factoryMethod;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;
import patrones_farmacia.factoryMethod.model.GenericMedicine;
import patrones_farmacia.factoryMethod.model.BrandMedicine;
import patrones_farmacia.factoryMethod.model.ControlledMedicine;
import patrones_farmacia.factoryMethod.view.FactoryConsole;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

@DisplayName("Factory Method - Creación de Medicamentos (mejoradas)")
class FactoryMethodTest {

    private FCreator creator;
    private PrintStream originalOut;
    private InputStream originalIn;
    private ByteArrayOutputStream baos;

    @BeforeEach
    void setUp() {
        creator = new FCreator();
        originalOut = System.out;
        originalIn = System.in;
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    @Test
    @DisplayName("createGenericMedicine retorna instancia con propiedades esperadas")
    void createGenericMedicine_hasExpectedProperties() {
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "GenFarma", 3500);
        assertNotNull(med);
        assertEquals("Paracetamol", med.getName());
        assertEquals(3500, med.getPrice(), 0.0001);
        assertFalse(med.isControlled());
        assertEquals("GenFarma", med.getLaboratory());
        assertEquals("Genérico", med.getType());
        assertTrue(med.toString().contains("Medicamento Genérico") || med.toString().contains("Precio"));
        assertInstanceOf(GenericMedicine.class, med);
    }

    @Test
    @DisplayName("createBrandMedicine retorna instancia con propiedades esperadas")
    void createBrandMedicine_hasExpectedProperties() {
        Medicine med = creator.createMedicine(FCreator.Type.BRAND, "Dolex Forte", "GSK", 6000);
        assertNotNull(med);
        assertEquals("Dolex Forte", med.getName());
        assertEquals(6000, med.getPrice(), 0.0001);
        assertFalse(med.isControlled());
        assertEquals("GSK", med.getLaboratory());
        assertEquals("De Marca", med.getType());
        assertTrue(med instanceof BrandMedicine);
        assertTrue(med.toString().contains("Medicamento de Marca") || med.toString().contains("Marca"));
    }

    @Test
    @DisplayName("createControlledMedicine retorna instancia con propiedades esperadas")
    void createControlledMedicine_hasExpectedProperties() {
        Medicine med = creator.createMedicine(FCreator.Type.CONTROLLED, "Ritalina", "INV-2024-001", 8500);
        assertNotNull(med);
        assertEquals("Ritalina", med.getName());
        assertEquals(8500, med.getPrice(), 0.0001);
        assertTrue(med.isControlled());
        assertEquals("INV-2024-001", med.getLaboratory());
        assertEquals("Controlado", med.getType());
        assertTrue(med instanceof ControlledMedicine);
        assertTrue(med.toString().contains("Medicamento Controlado") || med.toString().contains("INVIMA"));
    }

    @Test
    @DisplayName("toStringContiene indicadores de tipo para todos los tipos")
    void toStringContainsTypeIndicatorsForAllTypes() {
        Medicine g = creator.createMedicine(FCreator.Type.GENERIC, "G", "L", 1);
        Medicine b = creator.createMedicine(FCreator.Type.BRAND, "B", "L", 2);
        Medicine c = creator.createMedicine(FCreator.Type.CONTROLLED, "C", "CODE", 3);
        assertTrue(g.toString().toLowerCase().contains("genérico"));
        assertTrue(b.toString().toLowerCase().contains("marca"));
        assertTrue(c.toString().toLowerCase().contains("controlado"));
    }

    @Test
    @DisplayName("nullType lanza NullPointerException")
    void nullType_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
            creator.createMedicine(null, "X", "L", 1000)
        );
    }

    @Test
    @DisplayName("unsupportedBrandedType lanza IllegalArgumentException")
    void unsupportedBrandedType_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
            creator.createMedicine(FCreator.Type.BRANDED, "Br", "L", 100)
        );
    }

    @Test
    @DisplayName("negativeAndZeroPrices se aceptan y se preservan")
    void negativeAndZeroPrices_areAcceptedAndPreserved() {
        Medicine neg = creator.createMedicine(FCreator.Type.GENERIC, "Neg", "Lab", -50);
        Medicine zero = creator.createMedicine(FCreator.Type.BRAND, "Zero", "Lab", 0);
        assertEquals(-50, neg.getPrice(), 0.0001);
        assertEquals(0, zero.getPrice(), 0.0001);
    }

    @Test
    @DisplayName("decimalPrices se preservan")
    void decimalPrices_preserved() {
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "Dec", "Lab", 3500.50);
        assertEquals(3500.50, med.getPrice(), 0.0001);
    }

    @Test
    @DisplayName("multipleInstances son independientes")
    void multipleInstances_areIndependent() {
        Medicine m1 = creator.createMedicine(FCreator.Type.GENERIC, "A", "LabA", 1000);
        Medicine m2 = creator.createMedicine(FCreator.Type.GENERIC, "B", "LabB", 2000);
        assertNotSame(m1, m2);
        assertNotEquals(m1.getName(), m2.getName());
        assertNotEquals(m1.getPrice(), m2.getPrice());
    }

    @Test
    @DisplayName("differentMedicines pueden tener el mismo precio")
    void differentMedicines_canHaveSamePrice() {
        Medicine p1 = creator.createMedicine(FCreator.Type.BRAND, "P1", "M1", 5000);
        Medicine p2 = creator.createMedicine(FCreator.Type.BRAND, "P2", "M2", 5000);
        assertNotSame(p1, p2);
        assertEquals(p1.getPrice(), p2.getPrice(), 0.0001);
    }

    @Test
    @DisplayName("longNamesAndLaboratories se soportan")
    void longNamesAndLaboratories_areSupported() {
        String longName = "Medicamento con nombre muy largo que describe características específicas";
        String longLab = "Laboratorio Internacional de Investigación y Desarrollo Avanzado";
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, longName, longLab, 7500);
        assertEquals(longName, med.getName());
        assertEquals(longLab, med.getLaboratory());
    }

    @Test
    @DisplayName("controlledLaboratory retorna Health Code")
    void controlledLaboratory_returnsCodeHealth() {
        Medicine med = creator.createMedicine(FCreator.Type.CONTROLLED, "C", "INV-XYZ", 999);
        assertEquals("INV-XYZ", med.getLaboratory());
    }

    @Test
    @DisplayName("typeStrings son consistentes")
    void typeStrings_areConsistent() {
        Medicine g = creator.createMedicine(FCreator.Type.GENERIC, "G", "L", 1);
        Medicine b = creator.createMedicine(FCreator.Type.BRAND, "B", "L", 2);
        Medicine c = creator.createMedicine(FCreator.Type.CONTROLLED, "C", "CODE", 3);
        assertEquals("Genérico", g.getType());
        assertEquals("De Marca", b.getType());
        assertEquals("Controlado", c.getType());
    }

    @Test
    @DisplayName("brandMedicineLaboratory es brand field")
    void brandMedicineLaboratoryIsBrandField() {
        BrandMedicine b = (BrandMedicine) creator.createMedicine(FCreator.Type.BRAND, "BM", "MyBrand", 123);
        assertEquals("MyBrand", b.getLaboratory());
        assertEquals("De Marca", b.getType());
    }

    @Test
    @DisplayName("genericMedicineToString contiene precio y nombre")
    void genericMedicineToString_containsPriceAndName() {
        GenericMedicine g = (GenericMedicine) creator.createMedicine(FCreator.Type.GENERIC, "Gen", "Lab", 77.5);
        String s = g.toString();
        assertTrue(s.contains("Gen") && s.contains("77.5"));
    }

    @Test
    @DisplayName("controlledMedicineToString contiene código y precio")
    void controlledMedicineToString_containsCodeAndPrice() {
        ControlledMedicine c = (ControlledMedicine) creator.createMedicine(FCreator.Type.CONTROLLED, "Ctrl", "CODE-1", 999.9);
        String s = c.toString();
        assertTrue(s.contains("CODE-1") && s.contains("999.9"));
    }

    @Test
    @DisplayName("GenericMedicine implements Medicine correctamente")
    void genericMedicineImplementsMedicine() {
        GenericMedicine gm = new GenericMedicine("Test", "Lab", 500);
        assertEquals("Test", gm.getName());
        assertEquals(500, gm.getPrice(), 0.001);
        assertEquals("Lab", gm.getLaboratory());
        assertEquals("Genérico", gm.getType());
        assertFalse(gm.isControlled());
    }

    @Test
    @DisplayName("BrandMedicine implements Medicine correctamente")
    void brandMedicineImplementsMedicine() {
        BrandMedicine bm = new BrandMedicine("BrandTest", "BrandX", 1500);
        assertEquals("BrandTest", bm.getName());
        assertEquals(1500, bm.getPrice(), 0.001);
        assertEquals("BrandX", bm.getLaboratory());
        assertEquals("De Marca", bm.getType());
        assertFalse(bm.isControlled());
    }

    @Test
    @DisplayName("ControlledMedicine implements Medicine correctamente")
    void controlledMedicineImplementsMedicine() {
        ControlledMedicine cm = new ControlledMedicine("CtrlTest", "INVIMA-001", 2500);
        assertEquals("CtrlTest", cm.getName());
        assertEquals(2500, cm.getPrice(), 0.001);
        assertEquals("INVIMA-001", cm.getLaboratory());
        assertEquals("Controlado", cm.getType());
        assertTrue(cm.isControlled());
    }

    @Test
    @DisplayName("Crear múltiples medicinas genéricas funciona")
    void createMultipleGenericMedicines() {
        for (int i = 0; i < 10; i++) {
            Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "Gen" + i, "Lab" + i, 1000 + i);
            assertNotNull(m);
            assertEquals("Genérico", m.getType());
        }
    }

    @Test
    @DisplayName("Crear múltiples medicinas de marca funciona")
    void createMultipleBrandMedicines() {
        for (int i = 0; i < 10; i++) {
            Medicine m = creator.createMedicine(FCreator.Type.BRAND, "Brand" + i, "Marca" + i, 2000 + i);
            assertNotNull(m);
            assertEquals("De Marca", m.getType());
        }
    }

    @Test
    @DisplayName("Crear múltiples medicinas controladas funciona")
    void createMultipleControlledMedicines() {
        for (int i = 0; i < 10; i++) {
            Medicine m = creator.createMedicine(FCreator.Type.CONTROLLED, "Ctrl" + i, "INV-" + i, 3000 + i);
            assertNotNull(m);
            assertEquals("Controlado", m.getType());
            assertTrue(m.isControlled());
        }
    }

    @Test
    @DisplayName("Precios muy grandes se preservan")
    void veryLargePricesPreserved() {
        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "Expensive", "Lab", 1e10);
        assertEquals(1e10, m.getPrice(), 1e5);
    }

    @Test
    @DisplayName("Precios muy pequeños se preservan")
    void verySmallPricesPreserved() {
        Medicine m = creator.createMedicine(FCreator.Type.BRAND, "Cheap", "Brand", 0.001);
        assertEquals(0.001, m.getPrice(), 0.00001);
    }

    @Test
    @DisplayName("Nombres con caracteres especiales funcionan")
    void namesWithSpecialCharacters() {
        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "Med-123_ñ@#$", "Lab+", 555.55);
        assertEquals("Med-123_ñ@#$", m.getName());
    }

    @Test
    @DisplayName("toString de GenericMedicine incluye 'Lab:'")
    void genericMedicineToStringIncludesLab() {
        GenericMedicine gm = new GenericMedicine("TestG", "LabG", 123.45);
        assertTrue(gm.toString().contains("Lab:"));
    }

    @Test
    @DisplayName("toString de BrandMedicine incluye 'Marca:'")
    void brandMedicineToStringIncludesBrand() {
        BrandMedicine bm = new BrandMedicine("TestB", "BrandB", 234.56);
        assertTrue(bm.toString().contains("Marca:"));
    }

    @Test
    @DisplayName("toString de ControlledMedicine incluye 'Invima:'")
    void controlledMedicineToStringIncludesInvima() {
        ControlledMedicine cm = new ControlledMedicine("TestC", "INVIMA-123", 345.67);
        assertTrue(cm.toString().contains("INVIMA") || cm.toString().contains("Invima"));
    }

    @Test
    @DisplayName("toString contiene el símbolo $ para precio")
    void toStringContainsDollarSignForPrice() {
        Medicine g = creator.createMedicine(FCreator.Type.GENERIC, "G", "L", 100);
        Medicine b = creator.createMedicine(FCreator.Type.BRAND, "B", "Br", 200);
        Medicine c = creator.createMedicine(FCreator.Type.CONTROLLED, "C", "CODE", 300);
        assertTrue(g.toString().contains("$"));
        assertTrue(b.toString().contains("$"));
        assertTrue(c.toString().contains("$"));
    }

    @Test
    @DisplayName("Mismo nombre en diferentes tipos crea instancias diferentes")
    void sameNameDifferentTypesMakeDifferentInstances() {
        Medicine g = creator.createMedicine(FCreator.Type.GENERIC, "Same", "L", 100);
        Medicine b = creator.createMedicine(FCreator.Type.BRAND, "Same", "Br", 100);
        Medicine c = creator.createMedicine(FCreator.Type.CONTROLLED, "Same", "CODE", 100);
        assertNotSame(g, b);
        assertNotSame(b, c);
        assertNotSame(g, c);
    }

    @Test
    @DisplayName("Medicinas con precio flotante preciso")
    void medicinesWithPreciseFloatingPointPrice() {
        double price = 1234.56789;
        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "Float", "Lab", price);
        assertEquals(price, m.getPrice(), 0.00001);
    }

    @Test
    @DisplayName("FCreator.Type enum tiene 4 valores")
    void fcreatorTypeEnumHas4Values() {
        FCreator.Type[] values = FCreator.Type.values();
        assertEquals(4, values.length);
        assertTrue(java.util.Arrays.asList(values).contains(FCreator.Type.GENERIC));
        assertTrue(java.util.Arrays.asList(values).contains(FCreator.Type.BRAND));
        assertTrue(java.util.Arrays.asList(values).contains(FCreator.Type.CONTROLLED));
        assertTrue(java.util.Arrays.asList(values).contains(FCreator.Type.BRANDED));
    }

    @Test
    @DisplayName("Concurrent creation de medicinas funciona")
    void concurrentCreationWorks() throws Exception {
        ExecutorService es = Executors.newFixedThreadPool(4);
        List<Callable<Medicine>> tasks = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            final int idx = i;
            tasks.add(() -> creator.createMedicine(FCreator.Type.GENERIC, "Con-" + idx, "Lab", 100 + idx));
            tasks.add(() -> creator.createMedicine(FCreator.Type.BRAND, "Brand-" + idx, "Br", 200 + idx));
            tasks.add(() -> creator.createMedicine(FCreator.Type.CONTROLLED, "Ctrl-" + idx, "INV-" + idx, 300 + idx));
        }
        List<Future<Medicine>> results = es.invokeAll(tasks);
        for (Future<Medicine> f : results) assertNotNull(f.get());
        es.shutdownNow();
    }

    @Test
    @DisplayName("GenericMedicine nombre nulo preserva")
    void genericMedicineNullNamePreserves() {
        GenericMedicine gm = new GenericMedicine(null, "Lab", 100);
        assertNull(gm.getName());
    }

    @Test
    @DisplayName("BrandMedicine nombre nulo preserva")
    void brandMedicineNullNamePreserves() {
        BrandMedicine bm = new BrandMedicine(null, "Brand", 200);
        assertNull(bm.getName());
    }

    @Test
    @DisplayName("ControlledMedicine nombre nulo preserva")
    void controlledMedicineNullNamePreserves() {
        ControlledMedicine cm = new ControlledMedicine(null, "CODE", 300);
        assertNull(cm.getName());
    }

    @Test
    @DisplayName("Factory crea instancias sin estado compartido")
    void factoryCreatesIndependentInstances() {
        Medicine m1 = creator.createMedicine(FCreator.Type.GENERIC, "M1", "L1", 100);
        Medicine m2 = creator.createMedicine(FCreator.Type.GENERIC, "M2", "L2", 200);
        Medicine m3 = creator.createMedicine(FCreator.Type.GENERIC, "M3", "L3", 300);
        assertEquals("M1", m1.getName());
        assertEquals("M2", m2.getName());
        assertEquals("M3", m3.getName());
    }

    @Test
    @DisplayName("toString no es nulo para ningún tipo")
    void toStringNeverNull() {
        Medicine g = creator.createMedicine(FCreator.Type.GENERIC, "G", "L", 1);
        Medicine b = creator.createMedicine(FCreator.Type.BRAND, "B", "Br", 2);
        Medicine c = creator.createMedicine(FCreator.Type.CONTROLLED, "C", "CODE", 3);
        assertNotNull(g.toString());
        assertNotNull(b.toString());
        assertNotNull(c.toString());
    }

    @Test
    @DisplayName("FactoryConsole.run con input simulado funciona")
    void factoryConsoleRunWorks() throws Exception {
        String input = "1\nTestGen\nLabTest\n500.0\n4\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        FactoryConsole console = new FactoryConsole();
        assertDoesNotThrow(() -> console.run());
        String out = baos.toString();
        assertTrue(out.contains("FACTORY METHOD") || out.contains("Saleccionar"));
    }

    @Test
    @DisplayName("Crear genérico con laboratorio vacío")
    void createGenericWithEmptyLaboratory() {
        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "Med", "", 100);
        assertEquals("", m.getLaboratory());
    }

    @Test
    @DisplayName("Crear marca con marca vacía")
    void createBrandWithEmptyBrand() {
        Medicine m = creator.createMedicine(FCreator.Type.BRAND, "Med", "", 200);
        assertEquals("", m.getLaboratory());
    }

    @Test
    @DisplayName("Crear controlado con código vacío")
    void createControlledWithEmptyCode() {
        Medicine m = creator.createMedicine(FCreator.Type.CONTROLLED, "Med", "", 300);
        assertEquals("", m.getLaboratory());
    }

    @Test
    @DisplayName("Nombre y laboratorio iguales para genérica")
    void sameNameAndLaboratoryGeneric() {
        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "Same", "Same", 100);
        assertEquals("Same", m.getName());
        assertEquals("Same", m.getLaboratory());
    }

    @Test
    @DisplayName("Precio exactamente 0.0")
    void exactlyZeroPrice() {
        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "Zero", "Lab", 0.0);
        assertEquals(0.0, m.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Precio exactamente 1e-10")
    void tinyPrice() {
        Medicine m = creator.createMedicine(FCreator.Type.BRAND, "Tiny", "Brand", 1e-10);
        assertEquals(1e-10, m.getPrice(), 1e-15);
    }

    @Test
    @DisplayName("getType no es nulo para ningún tipo")
    void getTypeNeverNull() {
        Medicine g = creator.createMedicine(FCreator.Type.GENERIC, "G", "L", 1);
        Medicine b = creator.createMedicine(FCreator.Type.BRAND, "B", "Br", 2);
        Medicine c = creator.createMedicine(FCreator.Type.CONTROLLED, "C", "CODE", 3);
        assertNotNull(g.getType());
        assertNotNull(b.getType());
        assertNotNull(c.getType());
    }

    @Test
    @DisplayName("getLaboratory no es nulo para ningún tipo")
    void getLaboratoryNeverNull() {
        Medicine g = creator.createMedicine(FCreator.Type.GENERIC, "G", "L", 1);
        Medicine b = creator.createMedicine(FCreator.Type.BRAND, "B", "Br", 2);
        Medicine c = creator.createMedicine(FCreator.Type.CONTROLLED, "C", "CODE", 3);
        assertNotNull(g.getLaboratory());
        assertNotNull(b.getLaboratory());
        assertNotNull(c.getLaboratory());
    }

    @Test
    @DisplayName("isControlled solo es true para ControlledMedicine")
    void isControlledOnlyTrueForControlled() {
        Medicine g = creator.createMedicine(FCreator.Type.GENERIC, "G", "L", 1);
        Medicine b = creator.createMedicine(FCreator.Type.BRAND, "B", "Br", 2);
        Medicine c = creator.createMedicine(FCreator.Type.CONTROLLED, "C", "CODE", 3);
        assertFalse(g.isControlled());
        assertFalse(b.isControlled());
        assertTrue(c.isControlled());
    }

    @Test
    @DisplayName("Medicinas de cada tipo muestran información completa en toString")
    void toStringShowsCompleteInfo() {
        GenericMedicine gm = (GenericMedicine) creator.createMedicine(FCreator.Type.GENERIC, "GenF", "LabF", 111);
        BrandMedicine bm = (BrandMedicine) creator.createMedicine(FCreator.Type.BRAND, "BrandF", "BrF", 222);
        ControlledMedicine cm = (ControlledMedicine) creator.createMedicine(FCreator.Type.CONTROLLED, "CtrlF", "CODEF", 333);
        
        String gStr = gm.toString();
        String bStr = bm.toString();
        String cStr = cm.toString();
        
        assertTrue(gStr.contains("GenF") && gStr.contains("LabF") && gStr.contains("111"));
        assertTrue(bStr.contains("BrandF") && bStr.contains("BrF") && bStr.contains("222"));
        assertTrue(cStr.contains("CtrlF") && cStr.contains("CODEF") && cStr.contains("333"));
    }

    @Test
    @DisplayName("Crear 100 medicinas no falla")
    void create100Medicines() {
        for (int i = 0; i < 100; i++) {
            Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "M" + i, "L" + i, 100 + i);
            assertNotNull(m);
            assertEquals("M" + i, m.getName());
        }
    }

    @Test
    @DisplayName("Precios negativos grandes se preservan")
    void largNegativePricesPreserved() {
        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "Neg", "Lab", -1e10);
        assertEquals(-1e10, m.getPrice(), 1e5);
    }

    @Test
    @DisplayName("Laboratorio y marca son distintos nombres para los mismos campos")
    void laboratoryAndBrandAreFieldNames() {
        GenericMedicine gm = (GenericMedicine) creator.createMedicine(FCreator.Type.GENERIC, "Gen", "LaboratoryX", 100);
        BrandMedicine bm = (BrandMedicine) creator.createMedicine(FCreator.Type.BRAND, "Brand", "BrandY", 200);
        assertEquals("LaboratoryX", gm.getLaboratory());
        assertEquals("BrandY", bm.getLaboratory());
    }

    @Test
    @DisplayName("Nombres con números y símbolos se preservan exactos")
    void complexNamesPreservedExactly() {
        String complexName = "Med123-XYZ_@#$%^&*()";
        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, complexName, "Lab", 500);
        assertEquals(complexName, m.getName());
    }

    @Test
    @DisplayName("Laboratorios con números y símbolos se preservan exactos")
    void complexLaboratoryPreservedExactly() {
        String complexLab = "Lab_999-ABC@#$XYZ.COM";
        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "Med", complexLab, 500);
        assertEquals(complexLab, m.getLaboratory());
    }

    @Test
    @DisplayName("Códigos INVIMA con caracteres especiales se preservan")
    void complexInvimaCodePreserved() {
        String complexCode = "INV-2024-ABC-123_XYZ@";
        Medicine m = creator.createMedicine(FCreator.Type.CONTROLLED, "Med", complexCode, 500);
        assertEquals(complexCode, m.getLaboratory());
    }
}