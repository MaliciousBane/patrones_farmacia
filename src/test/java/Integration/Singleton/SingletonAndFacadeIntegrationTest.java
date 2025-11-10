package Integration.Singleton;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.Singleton.model.ConfigManager;
import patrones_farmacia.facade.controller.ReceiptSystem;

class SingletonAndFacadeIntegrationTest {

    @Test
    void testSingletonConfigUsedBySubsystem() {
        ConfigManager config = ConfigManager.getInstance();
        config.setEnvironment("producción");
        config.setPharmacyName("Farmacia Santa Fe");

        ReceiptSystem receipt = new ReceiptSystem();
        assertNotNull(receipt);
        assertEquals("producción", config.getEnvironment());
    }
}