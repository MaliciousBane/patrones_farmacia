package patrones_farmacia.Singleton.model;

public class ConfigManager {

    private static ConfigManager instance;

    private String pharmacyName;
    private String environment;
    private String databaseURL;
    private boolean testMode;

    private ConfigManager() {
        this.pharmacyName = "Farmacia SaludTotal";
        this.environment = "desarrollo";
        this.databaseURL = "jdbc:mysql://localhost:3306/farmacia_db";
        this.testMode = true;
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public String getPharmacyName() {
        return pharmacyName;
    }

    public void setPharmacyName(String pharmacyName) {
        if (pharmacyName != null && !pharmacyName.isEmpty()) {
            this.pharmacyName = pharmacyName;
        }
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        if (environment.equalsIgnoreCase("producción") || 
            environment.equalsIgnoreCase("desarrollo")) {
            this.environment = environment.toLowerCase();
        } else {
            System.out.println("Ambiente no válido. Use 'producción' o 'desarrollo'.");
        }
    }

    public String getDatabaseURL() {
        return databaseURL;
    }

    public void setDatabaseURL(String databaseURL) {
        if (databaseURL != null && databaseURL.startsWith("jdbc:")) {
            this.databaseURL = databaseURL;
        }
    }

    public boolean isTestMode() {
        return testMode;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    @Override
    public String toString() {
        return """
               === CONFIGURACIÓN SISTEMA FARMACIA ===
               Farmacia: %s
               Ambiente: %s
               Base de datos: %s
               Modo prueba: %s
               """.formatted(pharmacyName, environment, databaseURL, testMode);
    }
}