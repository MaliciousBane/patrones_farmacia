package e2e.Singleton;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.Singleton.model.ConfigManager;
import patrones_farmacia.Singleton.model.GlobalInvent;
import patrones_farmacia.Singleton.controller.InventoryController;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;

@DisplayName("Singleton Pattern - Configuración Global E2E")
class ConfigSingletonE2ETest {

    private FCreator creator;

    @BeforeEach
    void setUp() {
        creator = new FCreator();
    }

    @Test
    @DisplayName("Debe mantener instancia única de ConfigManager")
    void testSingleInstanceConfigManager() {
        ConfigManager c1 = ConfigManager.getInstance();
        ConfigManager c2 = ConfigManager.getInstance();
        assertSame(c1, c2, "Ambas referencias deben ser la misma instancia");
    }

    @Test
    @DisplayName("Debe compartir datos entre referencias del Singleton")
    void testSharedDataBetweenReferences() {
        ConfigManager c1 = ConfigManager.getInstance();
        ConfigManager c2 = ConfigManager.getInstance();
        c1.setPharmacyName("Farmacia Nueva");
        assertEquals("Farmacia Nueva", c2.getPharmacyName());
    }

    @Test
    @DisplayName("Debe establecer entorno en producción")
    void testSetEnvironmentProduction() {
        ConfigManager config = ConfigManager.getInstance();
        config.setEnvironment("producción");
        assertEquals("producción", config.getEnvironment());
    }

    @Test
    @DisplayName("Debe establecer entorno en desarrollo")
    void testSetEnvironmentDevelopment() {
        ConfigManager config = ConfigManager.getInstance();
        config.setEnvironment("desarrollo");
        assertEquals("desarrollo", config.getEnvironment());
    }

    @Test
    @DisplayName("Debe rechazar entorno inválido")
    void testRejectInvalidEnvironment() {
        ConfigManager config = ConfigManager.getInstance();
        config.setEnvironment("invalido");
        assertNotEquals("invalido", config.getEnvironment());
    }

    @Test
    @DisplayName("Debe establecer nombre de farmacia válido")
    void testSetPharmacyName() {
        ConfigManager config = ConfigManager.getInstance();
        config.setPharmacyName("Farmacia Central");
        assertEquals("Farmacia Central", config.getPharmacyName());
    }

    @Test
    @DisplayName("Debe rechazar nombre de farmacia vacío")
    void testRejectEmptyPharmacyName() {
        ConfigManager config = ConfigManager.getInstance();
        String original = config.getPharmacyName();
        config.setPharmacyName("");
        assertEquals(original, config.getPharmacyName());
    }

    @Test
    @DisplayName("Debe rechazar nombre de farmacia nulo")
    void testRejectNullPharmacyName() {
        ConfigManager config = ConfigManager.getInstance();
        String original = config.getPharmacyName();
        config.setPharmacyName(null);
        assertEquals(original, config.getPharmacyName());
    }

    @Test
    @DisplayName("Debe establecer URL de base de datos válida")
    void testSetDatabaseURL() {
        ConfigManager config = ConfigManager.getInstance();
        config.setDatabaseURL("jdbc:mysql://server:3306/db");
        assertEquals("jdbc:mysql://server:3306/db", config.getDatabaseURL());
    }

    @Test
    @DisplayName("Debe rechazar URL sin prefijo JDBC")
    void testRejectInvalidDatabaseURL() {
        ConfigManager config = ConfigManager.getInstance();
        String original = config.getDatabaseURL();
        config.setDatabaseURL("mysql://server:3306/db");
        assertEquals(original, config.getDatabaseURL());
    }

    @Test
    @DisplayName("Debe establecer modo prueba verdadero")
    void testSetTestModeTrue() {
        ConfigManager config = ConfigManager.getInstance();
        config.setTestMode(true);
        assertTrue(config.isTestMode());
    }

    @Test
    @DisplayName("Debe establecer modo prueba falso")
    void testSetTestModeFalse() {
        ConfigManager config = ConfigManager.getInstance();
        config.setTestMode(false);
        assertFalse(config.isTestMode());
    }

    @Test
    @DisplayName("Debe generar toString con información correcta")
    void testToStringOutput() {
        ConfigManager config = ConfigManager.getInstance();
        String output = config.toString();
        assertTrue(output.contains("CONFIGURACIÓN"));
        assertTrue(output.contains(config.getPharmacyName()));
    }

    @Test
    @DisplayName("Debe mantener instancia única de GlobalInvent")
    void testSingleInstanceGlobalInvent() {
        GlobalInvent i1 = GlobalInvent.getInstance();
        GlobalInvent i2 = GlobalInvent.getInstance();
        assertSame(i1, i2);
    }

    @Test
    @DisplayName("Debe agregar medicina al inventario global")
    void testAddMedicineToGlobalInvent() {
        GlobalInvent invent = GlobalInvent.getInstance();
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "Lab", 5000);
        invent.addMedicine(med);
        assertNotNull(invent.findMedicine("Paracetamol"));
    }

    @Test
    @DisplayName("Debe encontrar medicina case-insensitive")
    void testFindMedicineCaseInsensitive() {
        GlobalInvent invent = GlobalInvent.getInstance();
        invent.getAllMedicines().clear();
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "Ibuprofeno", "Lab", 4000);
        invent.addMedicine(med);
        assertNotNull(invent.findMedicine("ibuprofeno"));
        assertNotNull(invent.findMedicine("IBUPROFENO"));
    }

    @Test
    @DisplayName("Debe remover medicina del inventario")
    void testRemoveMedicineFromInvent() {
        GlobalInvent invent = GlobalInvent.getInstance();
        invent.getAllMedicines().clear();
        Medicine med = creator.createMedicine(FCreator.Type.BRAND, "Dolex", "GSK", 6000);
        invent.addMedicine(med);
        assertTrue(invent.removeMedicine("Dolex"));
        assertNull(invent.findMedicine("Dolex"));
    }

    @Test
    @DisplayName("Debe rechazar remoción de medicina inexistente")
    void testRejectRemoveNonExistentMedicine() {
        GlobalInvent invent = GlobalInvent.getInstance();
        assertFalse(invent.removeMedicine("NoExiste"));
    }

    @Test
    @DisplayName("Debe obtener lista de medicinas")
    void testGetAllMedicines() {
        GlobalInvent invent = GlobalInvent.getInstance();
        invent.getAllMedicines().clear();
        Medicine m1 = creator.createMedicine(FCreator.Type.GENERIC, "Med1", "Lab", 1000);
        Medicine m2 = creator.createMedicine(FCreator.Type.BRAND, "Med2", "Brand", 2000);
        invent.addMedicine(m1);
        invent.addMedicine(m2);
        assertEquals(2, invent.getAllMedicines().size());
    }

    @Test
    @DisplayName("Debe compartir inventario entre referencias")
    void testSharedInventoryBetweenReferences() {
        GlobalInvent inv1 = GlobalInvent.getInstance();
        GlobalInvent inv2 = GlobalInvent.getInstance();
        inv1.getAllMedicines().clear();
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "Aspirin", "Lab", 2000);
        inv1.addMedicine(med);
        assertEquals(1, inv2.getAllMedicines().size());
    }

    @Test
    @DisplayName("Debe procesar múltiples medicinas en inventario global")
    void testMultipleMedicinesInGlobalInvent() {
        GlobalInvent invent = GlobalInvent.getInstance();
        invent.getAllMedicines().clear();
        for(int i = 1; i <= 5; i++) {
            Medicine m = creator.createMedicine(FCreator.Type.GENERIC, "Med" + i, "Lab", i * 1000);
            invent.addMedicine(m);
        }
        assertEquals(5, invent.getAllMedicines().size());
    }

    @Test
    @DisplayName("Debe registrar medicina a través de controlador")
    void testRegisterMedicineViaController() {
        GlobalInvent.getInstance().getAllMedicines().clear();
        InventoryController controller = new InventoryController();
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "Lab", 3000);
        controller.registerMedicine(med);
        assertNotNull(GlobalInvent.getInstance().findMedicine("Paracetamol"));
    }

    @Test
    @DisplayName("Debe remover medicina a través de controlador")
    void testRemoveMedicineViaController() {
        GlobalInvent.getInstance().getAllMedicines().clear();
        InventoryController controller = new InventoryController();
        Medicine med = creator.createMedicine(FCreator.Type.BRAND, "Dolex", "GSK", 5000);
        controller.registerMedicine(med);
        controller.removeMedicine("Dolex");
        assertNull(GlobalInvent.getInstance().findMedicine("Dolex"));
    }

    @Test
    @DisplayName("Debe mantener sincronización entre controladores")
    void testControllerSynchronization() {
        GlobalInvent.getInstance().getAllMedicines().clear();
        InventoryController ctrl1 = new InventoryController();
        InventoryController ctrl2 = new InventoryController();
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "Med", "Lab", 1000);
        ctrl1.registerMedicine(med);
        assertEquals(1, GlobalInvent.getInstance().getAllMedicines().size());
    }

    @Test
    @DisplayName("Debe flujo completo con Singleton")
    void testCompleteSingletonFlow() {
        ConfigManager config = ConfigManager.getInstance();
        GlobalInvent invent = GlobalInvent.getInstance();
        invent.getAllMedicines().clear();
        
        config.setPharmacyName("Farmacia Central");
        config.setEnvironment("producción");
        config.setTestMode(false);
        
        Medicine m1 = creator.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "Lab", 3500);
        Medicine m2 = creator.createMedicine(FCreator.Type.BRAND, "Dolex", "GSK", 6000);
        invent.addMedicine(m1);
        invent.addMedicine(m2);
        
        assertEquals("Farmacia Central", config.getPharmacyName());
        assertEquals("producción", config.getEnvironment());
        assertEquals(2, invent.getAllMedicines().size());
    }
}