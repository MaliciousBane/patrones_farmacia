package Junit.Singleton;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.Singleton.model.ConfigManager;

class ConfigManagerTest {

    @Test
    void testSingletonUniqueInstance() {
        ConfigManager c1 = ConfigManager.getInstance();
        ConfigManager c2 = ConfigManager.getInstance();
        assertSame(c1, c2);
    }

    @Test
    void testSetAndGetValues() {
        ConfigManager config = ConfigManager.getInstance();
        config.setPharmacyName("Farmacia VidaPlus");
        config.setEnvironment("producción");
        config.setDatabaseURL("jdbc:mysql://servidor:3306/vida_db");
        config.setTestMode(false);

        assertEquals("Farmacia VidaPlus", config.getPharmacyName());
        assertEquals("producción", config.getEnvironment());
        assertEquals("jdbc:mysql://servidor:3306/vida_db", config.getDatabaseURL());
        assertFalse(config.isTestMode());
    }
}