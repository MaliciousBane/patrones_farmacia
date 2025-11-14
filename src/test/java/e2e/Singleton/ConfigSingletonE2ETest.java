package e2e.Singleton;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.Singleton.model.ConfigManager;

class ConfigSingletonE2ETest {

    @Test
    void shouldMaintainSingleSharedInstanceAcrossReferences() {
        ConfigManager c1 = ConfigManager.getInstance();
        ConfigManager c2 = ConfigManager.getInstance();

        c1.setEnvironment("producción");
        c1.setPharmacyName("Farmacia VidaPlus");

        assertSame(c1, c2, 
                   "Ambas referencias deben apuntar a la misma instancia singleton");
        
        assertEquals("producción", c2.getEnvironment(), 
                     "El entorno debe ser accesible desde la segunda referencia");
        assertEquals("Farmacia VidaPlus", c2.getPharmacyName(), 
                     "El nombre de la farmacia debe ser accesible desde la segunda referencia");
    }
}