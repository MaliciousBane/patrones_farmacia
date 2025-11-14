package e2e.factoryMethod;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.Set;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;

@DisplayName("Factory Method Pattern - Creación de Medicamentos E2E")
class MedicineCreationE2ETest {

    private FCreator creator;

    @BeforeEach
    void setUp() {
        creator = new FCreator();
    }

    @Test
    @DisplayName("Crear genérico básico")
    void testCreateGenericMedicine() {
        Medicine generic = creator.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "GenFarma", 3000);
        assertNotNull(generic);
        assertEquals("Paracetamol", generic.getName());
        assertEquals(3000.0, generic.getPrice(), 0.001);
        assertEquals("GenFarma", generic.getLaboratory());
        assertEquals("Genérico", generic.getType());
        assertFalse(generic.isControlled());
        String s = generic.toString();
        assertTrue(s.contains("Medicamento Genérico") || s.toLowerCase().contains("genérico"));
        assertTrue(s.contains("Paracetamol"));
    }

    @Test
    @DisplayName("Crear de marca básico")
    void testCreateBrandMedicine() {
        Medicine brand = creator.createMedicine(FCreator.Type.BRAND, "Dolex Forte", "GSK", 6000);
        assertNotNull(brand);
        assertEquals("Dolex Forte", brand.getName());
        assertEquals(6000.0, brand.getPrice(), 0.001);
        assertEquals("GSK", brand.getLaboratory());
        assertEquals("De Marca", brand.getType());
        assertFalse(brand.isControlled());
        String s = brand.toString();
        assertTrue(s.contains("Medicamento de Marca") || s.toLowerCase().contains("marca"));
    }

    @Test
    @DisplayName("Crear controlado básico")
    void testCreateControlledMedicine() {
        Medicine controlled = creator.createMedicine(FCreator.Type.CONTROLLED, "Ritalina", "INV-2024-001", 8500);
        assertNotNull(controlled);
        assertEquals("Ritalina", controlled.getName());
        assertEquals(8500.0, controlled.getPrice(), 0.001);
        assertEquals("INV-2024-001", controlled.getLaboratory());
        assertEquals("Controlado", controlled.getType());
        assertTrue(controlled.isControlled());
    }

    @Test
    @DisplayName("Tipo BRANDED lanza excepción por implementación actual")
    void testBrandedTypeThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            creator.createMedicine(FCreator.Type.BRANDED, "Ibupirac", "Farmalab", 5500));
    }

    @Test
    @DisplayName("Null tipo lanza NullPointer al hacer switch")
    void testNullTypeThrows() {
        assertThrows(NullPointerException.class, () ->
            creator.createMedicine(null, "Med", "Lab", 5000));
    }

    @Test
    @DisplayName("Acepta nombre vacío y lo preserva")
    void testEmptyNameAccepted() {
        Medicine m = assertDoesNotThrow(() ->
            creator.createMedicine(FCreator.Type.GENERIC, "", "Lab", 5000));
        assertNotNull(m);
        assertEquals("", m.getName());
    }

    @Test
    @DisplayName("Acepta nombre nulo y lo preserva como null")
    void testNullNameAccepted() {
        Medicine m = assertDoesNotThrow(() ->
            creator.createMedicine(FCreator.Type.GENERIC, null, "Lab", 5000));
        assertNotNull(m);
        assertNull(m.getName());
    }

    @Test
    @DisplayName("Acepta info vacía y la preserva")
    void testEmptyInfoAccepted() {
        Medicine m = assertDoesNotThrow(() ->
            creator.createMedicine(FCreator.Type.BRAND, "Med", "", 5000));
        assertNotNull(m);
        assertEquals("", m.getLaboratory());
    }

    @Test
    @DisplayName("Acepta info nula y la preserva como null")
    void testNullInfoAccepted() {
        Medicine m = assertDoesNotThrow(() ->
            creator.createMedicine(FCreator.Type.CONTROLLED, "Med", null, 5000));
        assertNotNull(m);
        assertNull(m.getLaboratory());
    }

    @Test
    @DisplayName("Acepta precio negativo y lo preserva")
    void testNegativePriceAccepted() {
        Medicine m = assertDoesNotThrow(() ->
            creator.createMedicine(FCreator.Type.GENERIC, "Med", "Lab", -5000));
        assertNotNull(m);
        assertEquals(-5000.0, m.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Acepta precio cero y lo preserva")
    void testZeroPriceAccepted() {
        Medicine m = assertDoesNotThrow(() ->
            creator.createMedicine(FCreator.Type.BRAND, "Gratis", "Lab", 0));
        assertNotNull(m);
        assertEquals(0.0, m.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Acepta precio decimal")
    void testAcceptDecimalPrice() {
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "Med", "Lab", 3500.50);
        assertEquals(3500.50, med.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Crear múltiples instancias y verificar independencia")
    void testMultipleInstancesIndependence() {
        Medicine m1 = creator.createMedicine(FCreator.Type.GENERIC, "A", "L1", 1000);
        Medicine m2 = creator.createMedicine(FCreator.Type.GENERIC, "B", "L2", 2000);
        assertNotSame(m1, m2);
        assertNotEquals(m1.getName(), m2.getName());
        assertNotEquals(m1.getPrice(), m2.getPrice());
    }

    @Test
    @DisplayName("ToString contiene nombre y precio")
    void testToStringContainsNameAndPrice() {
        Medicine g = creator.createMedicine(FCreator.Type.GENERIC, "G1", "L1", 111);
        Medicine b = creator.createMedicine(FCreator.Type.BRAND, "B1", "L2", 222);
        Medicine c = creator.createMedicine(FCreator.Type.CONTROLLED, "C1", "CODE123", 333);
        assertTrue(g.toString().contains("G1"));
        assertTrue(g.toString().contains("111"));
        assertTrue(b.toString().contains("B1"));
        assertTrue(b.toString().contains("222"));
        assertTrue(c.toString().contains("C1"));
        assertTrue(c.toString().contains("333"));
    }

    @Test
    @DisplayName("Tipos devueltos son no nulos y descriptivos")
    void testTypesAreDescriptive() {
        for (FCreator.Type t : new FCreator.Type[]{FCreator.Type.GENERIC, FCreator.Type.BRAND, FCreator.Type.CONTROLLED}) {
            Medicine m = creator.createMedicine(t, "X", "L", 1);
            assertNotNull(m.getType());
            assertFalse(m.getType().trim().isEmpty());
        }
    }

    @Test
    @DisplayName("Bulk creation produces distinct instances")
    void testBulkCreationDistinct() {
        Set<Medicine> set = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "Bulk" + i, "Lab" + i, i);
            set.add(m);
        }
        assertEquals(100, set.size());
    }

    @Test
    @DisplayName("Precios muy grandes y precisión")
    void testLargePricePrecision() {
        double large = 9_000_000_123.45;
        Medicine m = creator.createMedicine(FCreator.Type.BRAND, "Big", "BigLab", large);
        assertEquals(large, m.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Nombres y laboratorios largos se preservan")
    void testLongNameAndLabPreserved() {
        String longName = "N".repeat(1000);
        String longLab = "L".repeat(1000);
        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, longName, longLab, 42);
        assertEquals(1000, m.getName().length());
        assertEquals(1000, m.getLaboratory().length());
    }

    @Test
    @DisplayName("Caracteres especiales en nombre y lab se preservan")
    void testSpecialCharsPreserved() {
        Medicine m = creator.createMedicine(FCreator.Type.BRAND, "Áccêntø😊", "L@b/Δ", 777);
        assertTrue(m.getName().contains("Á") || m.getName().contains("😊"));
        assertTrue(m.getLaboratory().contains("@") || m.getLaboratory().contains("Δ"));
    }
}