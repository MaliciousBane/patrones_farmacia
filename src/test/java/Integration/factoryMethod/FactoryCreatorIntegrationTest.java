package Integration.factoryMethod;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;

class FactoryCreatorIntegrationTest {

    private FCreator creator;

    @BeforeEach
    void init() {
        creator = new FCreator();
    }

    @Test
    void createGenericProducesCorrectProperties() {
        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "Ibuprofeno", "GenLab", 1500);
        assertNotNull(m);
        assertEquals("Ibuprofeno", m.getName());
        assertEquals(1500.0, m.getPrice(), 0.001);
        assertFalse(m.isControlled());
        assertEquals("GenLab", m.getLaboratory());
        assertNotNull(m.getType());
        String s = m.toString();
        assertTrue(s.contains("Ibuprofeno"));
        assertTrue(s.contains("GenLab"));
        assertTrue(s.contains("1500"));
    }

    @Test
    void createBrandProducesCorrectProperties() {
        Medicine m = creator.createMedicine(FCreator.Type.BRAND, "Dolex", "GSK", 6200);
        assertNotNull(m);
        assertEquals("Dolex", m.getName());
        assertEquals(6200.0, m.getPrice(), 0.001);
        assertFalse(m.isControlled());
        assertEquals("GSK", m.getLaboratory());
        assertNotNull(m.getType());
        String s = m.toString();
        assertTrue(s.contains("Dolex"));
        assertTrue(s.contains("GSK"));
        assertTrue(s.contains("6200"));
    }

    @Test
    void createControlledProducesCorrectProperties() {
        Medicine m = creator.createMedicine(FCreator.Type.CONTROLLED, "MedicControl", "INV-999", 9800);
        assertNotNull(m);
        assertEquals("MedicControl", m.getName());
        assertEquals(9800.0, m.getPrice(), 0.001);
        assertTrue(m.isControlled());
        assertEquals("INV-999", m.getLaboratory());
        assertNotNull(m.getType());
        String s = m.toString();
        assertTrue(s.contains("MedicControl"));
        assertTrue(s.contains("INV-999"));
        assertTrue(s.contains("9800"));
    }

    @Test
    void multipleCreatesReturnDistinctInstancesWithSameValues() {
        Medicine a = creator.createMedicine(FCreator.Type.GENERIC, "Multi", "LabX", 1200);
        Medicine b = creator.createMedicine(FCreator.Type.GENERIC, "Multi", "LabX", 1200);
        assertNotNull(a);
        assertNotNull(b);
        assertNotSame(a, b);
        assertEquals(a.getName(), b.getName());
        assertEquals(a.getPrice(), b.getPrice(), 0.001);
        assertEquals(a.getLaboratory(), b.getLaboratory());
    }

    @Test
    void invalidEnumTypeThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> creator.createMedicine(FCreator.Type.BRANDED, "X", "Y", 1));
    }

    @Test
    void acceptsZeroAndNegativePricesAsCreated() {
        Medicine free = creator.createMedicine(FCreator.Type.BRAND, "Gratis", "Lab", 0);
        assertNotNull(free);
        assertEquals(0.0, free.getPrice(), 0.001);
        Medicine negative = creator.createMedicine(FCreator.Type.GENERIC, "Neg", "Lab", -50);
        assertNotNull(negative);
        assertEquals(-50.0, negative.getPrice(), 0.001);
    }

    @Test
    void toStringFormatsContainTypeHintsAndPrice() {
        Medicine g = creator.createMedicine(FCreator.Type.GENERIC, "G1", "L1", 111);
        Medicine b = creator.createMedicine(FCreator.Type.BRAND, "B1", "L2", 222);
        Medicine c = creator.createMedicine(FCreator.Type.CONTROLLED, "C1", "CODE123", 333);
        assertNotNull(g.toString());
        assertNotNull(b.toString());
        assertNotNull(c.toString());
        assertTrue(g.toString().toLowerCase().contains("gen") || g.toString().toLowerCase().contains("genérico"));
        assertTrue(b.toString().toLowerCase().contains("mar") || b.toString().toLowerCase().contains("marca"));
        assertTrue(c.toString().toLowerCase().contains("control") || c.toString().toLowerCase().contains("controlado"));
        assertTrue(g.toString().contains("111"));
        assertTrue(b.toString().contains("222"));
        assertTrue(c.toString().contains("333"));
    }

    @Test
    void nullTypeThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> creator.createMedicine(null, "N", "L", 1));
    }

    @Test
    void namesPreservedCaseAndLaboratoryReturnedExactly() {
        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "MiXeDCaSe", "LaB-Name", 123);
        assertNotNull(m);
        assertEquals("MiXeDCaSe", m.getName());
        assertEquals("LaB-Name", m.getLaboratory());
    }

    @Test
    void differentTypesProduceDifferentTypeStrings() {
        Medicine g = creator.createMedicine(FCreator.Type.GENERIC, "X", "L1", 1);
        Medicine b = creator.createMedicine(FCreator.Type.BRAND, "X", "L1", 1);
        Medicine c = creator.createMedicine(FCreator.Type.CONTROLLED, "X", "L1", 1);
        assertNotNull(g.getType());
        assertNotNull(b.getType());
        assertNotNull(c.getType());
        assertNotEquals(g.getType(), b.getType());
        assertNotEquals(b.getType(), c.getType());
        assertNotEquals(g.getType(), c.getType());
    }

    @Test
    void longNameAndLaboratoryDoNotCrashFactory() {
        String longName = "N".repeat(1000);
        String longLab = "L".repeat(1000);
        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, longName, longLab, 42);
        assertNotNull(m);
        assertTrue(m.getName().length() >= 1000);
        assertTrue(m.getLaboratory().length() >= 1000);
        assertTrue(m.toString().contains("42"));
    }

    @Test
    void specialCharactersAreHandled() {
        Medicine m = creator.createMedicine(FCreator.Type.BRAND, "Áccêntø😊", "L@b/Δ", 777);
        assertNotNull(m);
        assertTrue(m.getName().contains("Á") || m.getName().contains("😊"));
        assertTrue(m.getLaboratory().contains("@") || m.getLaboratory().contains("Δ"));
        assertTrue(m.toString().contains("777"));
    }

    @Test
    void bulkCreationProducesUniqueInstances() {
        Set<Medicine> set = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "Bulk" + i, "Lab" + i, i);
            assertNotNull(m);
            set.add(m);
        }
        assertEquals(200, set.size());
    }

    @Test
    void repeatedCreatesWithSameInputsAreDistinctObjects() {
        Medicine a = creator.createMedicine(FCreator.Type.CONTROLLED, "Same", "LabS", 999);
        Medicine b = creator.createMedicine(FCreator.Type.CONTROLLED, "Same", "LabS", 999);
        assertNotSame(a, b);
        assertEquals(a.getName(), b.getName());
        assertEquals(a.getLaboratory(), b.getLaboratory());
        assertEquals(a.getPrice(), b.getPrice(), 0.001);
    }

    @Test
    void pricePrecisionIsPreservedForLargeValues() {
        double large = 9_000_000_123.45;
        Medicine m = creator.createMedicine(FCreator.Type.BRAND, "Big", "BigLab", large);
        assertNotNull(m);
        assertEquals(large, m.getPrice(), 0.001);
        assertTrue(m.toString().contains("9000000") || m.toString().contains("9000000123"));
    }

    @Test
    void typeStringsAreNonEmptyAndDescriptive() {
        for (FCreator.Type t : FCreator.Type.values()) {
            Medicine m = creator.createMedicine(t, "T", "L", 1);
            assertNotNull(m.getType());
            assertFalse(m.getType().trim().isEmpty());
            assertTrue(m.getType().length() >= 1);
        }
    }

    @Test
    void toStringAlwaysContainsNameAndPrice() {
        Medicine m1 = creator.createMedicine(FCreator.Type.GENERIC, "NameX", "LabX", 10);
        Medicine m2 = creator.createMedicine(FCreator.Type.BRAND, "NameY", "LabY", 20);
        assertTrue(m1.toString().contains("NameX"));
        assertTrue(m1.toString().contains("10"));
        assertTrue(m2.toString().contains("NameY"));
        assertTrue(m2.toString().contains("20"));
    }

    @Test
    void creatingWithWhitespaceOnlyNameAndLabStillReturnsMedicine() {
        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "   ", "   ", 5);
        assertNotNull(m);
        assertTrue(m.getName() != null);
        assertTrue(m.getLaboratory() != null);
    }

    @Test
    void creatingManyDifferentCombinations() {
        for (int p = -3; p < 8; p++) {
            for (FCreator.Type t : FCreator.Type.values()) {
                Medicine m = creator.createMedicine(t, "N" + p + t.name(), "L" + p, p * 10);
                assertNotNull(m);
                assertEquals("L" + p, m.getLaboratory());
            }
        }
    }

}