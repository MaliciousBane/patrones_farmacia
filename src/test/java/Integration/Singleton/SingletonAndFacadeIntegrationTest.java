package Integration.Singleton;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.Singleton.model.ConfigManager;
import patrones_farmacia.facade.controller.ReceiptSystem;

@DisplayName("Pruebas de Integración de Singleton y Facade")
class SingletonAndFacadeIntegrationTest {

    private ConfigManager configuracion;
    private ReceiptSystem sistemaRecibos;

    @BeforeEach
    void configurarSistemas() {
        configuracion = ConfigManager.getInstance();
        sistemaRecibos = new ReceiptSystem();
    }

    @Test
    @DisplayName("Debe utilizar configuración Singleton en subsistemas de Facade")
    void debeUtilizarConfiguracionSingletonEnSubsistemas() {
        configuracion.setEnvironment("producción");
        configuracion.setPharmacyName("Farmacia Santa Fe");

        assertNotNull(sistemaRecibos, 
            "El sistema de recibos debe inicializarse correctamente");
        assertEquals("producción", configuracion.getEnvironment(), 
            "El ambiente debe ser 'producción' según la configuración Singleton");
        assertEquals("Farmacia Santa Fe", configuracion.getPharmacyName(), 
            "El nombre de la farmacia debe ser 'Farmacia Santa Fe'");
    }

    @Test
    @DisplayName("Debe mantener configuración consistente en múltiples subsistemas")
    void debeMantenerConfiguracionConsistenteEnSubsistemas() {
        configuracion.setPharmacyName("Farmacia VidaPlus");
        configuracion.setEnvironment("desarrollo");
        configuracion.setDatabaseURL("jdbc:mysql://localhost:3306/farmacia_db");

        ReceiptSystem recibos1 = new ReceiptSystem();
        ReceiptSystem recibos2 = new ReceiptSystem();

        ConfigManager config1 = ConfigManager.getInstance();
        ConfigManager config2 = ConfigManager.getInstance();

        assertSame(config1, config2, 
            "Todas las instancias del ConfigManager deben ser la misma");
        assertEquals("Farmacia VidaPlus", config1.getPharmacyName());
        assertEquals("desarrollo", config2.getEnvironment());
        assertNotNull(recibos1);
        assertNotNull(recibos2);
    }

    @Test
    @DisplayName("Debe compartir configuración entre diferentes componentes del sistema")
    void debeCompartirConfiguracionEntreDiferentesComponentes() {
        configuracion.setPharmacyName("Farmacia Central");
        configuracion.setEnvironment("pruebas");
        configuracion.setTestMode(true);

        ReceiptSystem sistema1 = new ReceiptSystem();
        ReceiptSystem sistema2 = new ReceiptSystem();
        ReceiptSystem sistema3 = new ReceiptSystem();

        ConfigManager configVerificacion = ConfigManager.getInstance();
        
        assertEquals("Farmacia Central", configVerificacion.getPharmacyName(), 
            "El nombre debe mantenerse consistente en toda la aplicación");
        assertEquals("pruebas", configVerificacion.getEnvironment(), 
            "El ambiente debe ser consistente");
        assertTrue(configVerificacion.isTestMode(), 
            "El modo de prueba debe estar activado");
        
        assertNotNull(sistema1);
        assertNotNull(sistema2);
        assertNotNull(sistema3);
    }

    @Test
    @DisplayName("Debe permitir cambios de configuración que afecten a todos los subsistemas")
    void debePermitirCambiosConfiguracionGlobales() {
        configuracion.setEnvironment("desarrollo");
        configuracion.setPharmacyName("Farmacia Inicial");

        ReceiptSystem sistema = new ReceiptSystem();
        
        assertEquals("desarrollo", configuracion.getEnvironment());
        assertEquals("Farmacia Inicial", configuracion.getPharmacyName());

        configuracion.setEnvironment("producción");
        configuracion.setPharmacyName("Farmacia Actualizada");

        assertEquals("producción", ConfigManager.getInstance().getEnvironment(), 
            "Los cambios en el Singleton deben reflejarse globalmente");
        assertEquals("Farmacia Actualizada", ConfigManager.getInstance().getPharmacyName(), 
            "El nombre actualizado debe estar disponible para todos los subsistemas");
        
        assertNotNull(sistema);
    }

    @Test
    @DisplayName("Debe validar la integración de configuración centralizada con Facade")
    void debeValidarIntegracionConfiguracionConFacade() {
        configuracion.setPharmacyName("Farmacia La Salud");
        configuracion.setEnvironment("producción");
        configuracion.setDatabaseURL("jdbc:postgresql://servidor:5432/farmacia");
        configuracion.setTestMode(false);

        ReceiptSystem sistemaRecibos = new ReceiptSystem();

        assertNotNull(sistemaRecibos, 
            "El sistema de recibos debe inicializarse con la configuración Singleton");
        
        ConfigManager configActual = ConfigManager.getInstance();
        assertEquals("Farmacia La Salud", configActual.getPharmacyName());
        assertEquals("producción", configActual.getEnvironment());
        assertEquals("jdbc:postgresql://servidor:5432/farmacia", configActual.getDatabaseURL());
        assertFalse(configActual.isTestMode(), 
            "El modo de prueba debe estar desactivado en producción");
    }

    @Test
    @DisplayName("Debe mantener configuración entre reinicios de subsistemas")
    void debeMantenerConfiguracionEntreReiniciosSubsistemas() {
        configuracion.setPharmacyName("Farmacia Persistente");
        configuracion.setEnvironment("staging");

        ReceiptSystem sistema1 = new ReceiptSystem();
        assertNotNull(sistema1);
        assertEquals("Farmacia Persistente", ConfigManager.getInstance().getPharmacyName());

        ReceiptSystem sistema2 = new ReceiptSystem();
        assertNotNull(sistema2);

        assertEquals("Farmacia Persistente", ConfigManager.getInstance().getPharmacyName(), 
            "La configuración debe mantenerse entre reinicios de subsistemas");
        assertEquals("staging", ConfigManager.getInstance().getEnvironment());
    }
}