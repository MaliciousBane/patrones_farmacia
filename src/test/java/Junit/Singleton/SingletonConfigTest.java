package Junit.Singleton;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.Singleton.model.ConfigManager;
import patrones_farmacia.Singleton.model.GlobalInvent;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;
import patrones_farmacia.Singleton.controller.InventoryController;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

class ConfigManagerTest {

    private ConfigManager cfg;
    private GlobalInvent global;
    private FCreator creator;
    private PrintStream originalOut;
    private ByteArrayOutputStream out;

    @BeforeEach
    void setup() {
        cfg = ConfigManager.getInstance();
        cfg.setPharmacyName("Farmacia Temp");
        cfg.setEnvironment("desarrollo");
        cfg.setDatabaseURL("jdbc:test://localhost");
        cfg.setTestMode(true);
        global = GlobalInvent.getInstance();
        global.getAllMedicines().clear();
        creator = new FCreator();
        originalOut = System.out;
        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        global.getAllMedicines().clear();
    }

    @Test
    void testSingletonUniqueInstanceConfig() {
        ConfigManager c1 = ConfigManager.getInstance();
        ConfigManager c2 = ConfigManager.getInstance();
        assertSame(c1, c2);
    }

    @Test
    void testSingletonUniqueInstanceGlobalInvent() {
        GlobalInvent g1 = GlobalInvent.getInstance();
        GlobalInvent g2 = GlobalInvent.getInstance();
        assertSame(g1, g2);
    }

    @Test
    void testDefaultsAndMutations() {
        cfg.setPharmacyName("Farmacia VidaPlus");
        cfg.setEnvironment("producción");
        cfg.setDatabaseURL("jdbc:mysql://servidor:3306/vida_db");
        cfg.setTestMode(false);
        assertEquals("Farmacia VidaPlus", cfg.getPharmacyName());
        assertEquals("producción", cfg.getEnvironment());
        assertEquals("jdbc:mysql://servidor:3306/vida_db", cfg.getDatabaseURL());
        assertFalse(cfg.isTestMode());
    }

    @Test
    void testSetPharmacyNameRejectsNullAndEmpty() {
        cfg.setPharmacyName(null);
        assertEquals("Farmacia Temp", cfg.getPharmacyName());
        cfg.setPharmacyName("");
        assertEquals("Farmacia Temp", cfg.getPharmacyName());
        cfg.setPharmacyName("Nueva");
        assertEquals("Nueva", cfg.getPharmacyName());
    }

    @Test
    void testSetEnvironmentValidAndInvalid() {
        cfg.setEnvironment("producción");
        assertEquals("producción", cfg.getEnvironment());
        cfg.setEnvironment("DESARROLLO");
        assertEquals("desarrollo", cfg.getEnvironment());
        String before = cfg.getEnvironment();
        cfg.setEnvironment("staging");
        assertEquals(before, cfg.getEnvironment());
    }

    @Test
    void testSetDatabaseURLValidation() {
        cfg.setDatabaseURL("http://notjdbc");
        assertEquals("jdbc:test://localhost", cfg.getDatabaseURL());
        cfg.setDatabaseURL("jdbc:postgresql://db");
        assertEquals("jdbc:postgresql://db", cfg.getDatabaseURL());
    }

    @Test
    void testSetTestModeToggle() {
        cfg.setTestMode(false);
        assertFalse(cfg.isTestMode());
        cfg.setTestMode(true);
        assertTrue(cfg.isTestMode());
    }

    @Test
    void testToStringContainsValues() {
        cfg.setPharmacyName("MiFarmacia");
        cfg.setEnvironment("producción");
        cfg.setDatabaseURL("jdbc:dummy://x");
        cfg.setTestMode(false);
        String s = cfg.toString();
        assertTrue(s.contains("MiFarmacia"));
        assertTrue(s.contains("producción"));
        assertTrue(s.contains("jdbc:dummy://x"));
        assertTrue(s.contains("false"));
    }

    @Test
    void globalInventAddFindRemove() {
        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "GenFarma", 3500);
        global.addMedicine(m);
        Medicine found = global.findMedicine("Paracetamol");
        assertNotNull(found);
        assertEquals("Paracetamol", found.getName());
        boolean removed = global.removeMedicine("paracetamol");
        assertTrue(removed);
        assertNull(global.findMedicine("Paracetamol"));
    }

    @Test
    void globalInventRemoveNonExistingReturnsFalse() {
        boolean removed = global.removeMedicine("NoExiste");
        assertFalse(removed);
    }

    @Test
    void globalInventGetAllMedicinesWorks() {
        Medicine a = creator.createMedicine(FCreator.Type.GENERIC, "A", "L", 100);
        Medicine b = creator.createMedicine(FCreator.Type.BRAND, "B", "L", 200);
        global.addMedicine(a);
        global.addMedicine(b);
        List<Medicine> all = global.getAllMedicines();
        assertTrue(all.stream().anyMatch(x -> x.getName().equalsIgnoreCase("A")));
        assertTrue(all.stream().anyMatch(x -> x.getName().equalsIgnoreCase("B")));
    }

    @Test
    void globalInventShowInventoryEmptyAndNonEmpty() {
        global.getAllMedicines().clear();
        global.showInventory();
        String empty = out.toString();
        assertTrue(empty.contains("Inventario vacío"));
        out.reset();
        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "X", "L", 1);
        global.addMedicine(m);
        global.showInventory();
        String nonEmpty = out.toString();
        assertTrue(nonEmpty.contains("X"));
    }

    @Test
    void multipleAddsAndRemovalsMaintainConsistency() {
        Medicine m1 = creator.createMedicine(FCreator.Type.GENERIC, "M1", "L", 10);
        Medicine m2 = creator.createMedicine(FCreator.Type.GENERIC, "M2", "L", 20);
        global.addMedicine(m1);
        global.addMedicine(m2);
        assertEquals(2, global.getAllMedicines().stream().filter(x -> x.getName().startsWith("M")).count());
        global.removeMedicine("M1");
        assertNull(global.findMedicine("M1"));
        assertNotNull(global.findMedicine("M2"));
    }

    @Test
    void inventoryControllerRegisterAndRemoveOutputs() {
        InventoryController controller = new InventoryController();
        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "VitC", "Lab", 120);
        controller.registerMedicine(m);
        controller.removeMedicine("VitC");
        String outStr = out.toString();
        assertTrue(outStr.contains("Medicamento agregado"));
        assertTrue(outStr.contains("Medicamento eliminado"));
    }

    @Test
    void inventoryControllerRemoveNonExistingPrintsNotFound() {
        InventoryController controller = new InventoryController();
        controller.removeMedicine("Inexistente");
        String outStr = out.toString();
        assertTrue(outStr.contains("No se encontró el medicamento"));
    }

    @Test
    void inventoryControllerShowInventoryDelegatesToGlobalInvent() {
        InventoryController controller = new InventoryController();
        global.getAllMedicines().clear();
        controller.showInventory();
        assertTrue(out.toString().contains("Inventario vacío"));
        out.reset();
        Medicine m = creator.createMedicine(FCreator.Type.BRAND, "BRM", "LB", 500);
        controller.registerMedicine(m);
        controller.showInventory();
        assertTrue(out.toString().contains("BRM"));
    }

    @Test
    void addManyMedicinesPerformanceLike() {
        for (int i = 0; i < 30; i++) {
            Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "Bulk-" + i, "Lab", i);
            global.addMedicine(m);
        }
        assertEquals(30, global.getAllMedicines().stream().filter(x -> x.getName().startsWith("Bulk-")).count());
    }

    @Test
    void removeCaseInsensitiveBehavior() {
        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "CaSe", "Lab", 10);
        global.addMedicine(m);
        boolean removed = global.removeMedicine("case");
        assertTrue(removed);
    }

    @Test
    void findMedicineReturnsNullWhenEmpty() {
        global.getAllMedicines().clear();
        assertNull(global.findMedicine("Nada"));
    }

    @Test
    void toStringReflectsStateAfterChanges() {
        cfg.setPharmacyName("Reflex");
        cfg.setEnvironment("producción");
        cfg.setDatabaseURL("jdbc:reflex://x");
        cfg.setTestMode(false);
        String s = cfg.toString();
        assertTrue(s.contains("Reflex"));
        assertTrue(s.contains("producción"));
        assertTrue(s.contains("jdbc:reflex://x"));
    }
}