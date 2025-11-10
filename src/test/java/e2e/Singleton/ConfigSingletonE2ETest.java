package e2e.Singleton;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.Singleton.model.ConfigManager;

class ConfigSingletonE2ETest {

    @Test
    void testSharedConfigurationInSingleton() {
        ConfigManager c1 = ConfigManager.getInstance();
        ConfigManager c2 = ConfigManager.getInstance();

        c1.setEnvironment("producción");
        c1.setPharmacyName("Farmacia VidaPlus");

        assertSame(c1, c2);
        assertEquals("producción", c2.getEnvironment());
        assertEquals("Farmacia VidaPlus", c2.getPharmacyName());
    }
}