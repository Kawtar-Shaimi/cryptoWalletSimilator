package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;
import util.LoggerProvider;

/**
 * Gestionnaire de configuration qui lit le fichier config.properties
 * et gère les valeurs par défaut de manière sécurisée.
 */
public class ConfigManager {
    
    private static final Logger LOGGER = LoggerProvider.getLogger(ConfigManager.class.getName());
    private static final String CONFIG_FILE = "/config/config.properties";
    private static Properties properties;
    private static boolean initialized = false;
    
    // Constructeur privé pour empêcher l'instanciation
    private ConfigManager() {
        // Classe utilitaire, ne doit pas être instanciée
    }
    
    /**
     * Initialise la configuration en chargeant le fichier properties
     */
    private static synchronized void init() {
        if (initialized) {
            return;
        }
        
        properties = new Properties();
        
        try (InputStream input = ConfigManager.class.getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
                LOGGER.info("Fichier de configuration chargé: " + CONFIG_FILE);
            } else {
                LOGGER.warning("Fichier de configuration non trouvé: " + CONFIG_FILE + 
                             " - Utilisation des valeurs par défaut");
                loadDefaultProperties();
            }
        } catch (IOException e) {
            LOGGER.severe("Erreur lors du chargement de la configuration: " + e.getMessage());
            loadDefaultProperties();
        }
        
        initialized = true;
    }
    
    /**
     * Charge les valeurs par défaut si le fichier properties n'est pas disponible
     */
    private static void loadDefaultProperties() {
        // Utilisation de PostgreSQL par défaut
        properties.setProperty("db.url", "jdbc:postgresql://localhost:5432/cryptowallet");
        properties.setProperty("db.user", "postgres");
        properties.setProperty("db.password", "password");
        properties.setProperty("db.maxConnections", "10");
        properties.setProperty("db.connectionTimeout", "30000");
    }
    
    /**
     * Récupère une propriété avec valeur par défaut
     */
    private static String getProperty(String key, String defaultValue) {
        if (!initialized) {
            init();
        }
        
        // Priorité: 1. Propriété système, 2. Fichier properties, 3. Valeur par défaut
        String systemProperty = System.getProperty(key);
        if (systemProperty != null) {
            return systemProperty;
        }
        
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Retourne l'URL de la base de données
     */
    public static String getDatabaseUrl() {
        return getProperty("db.url", "jdbc:postgresql://localhost:5432/cryptowallet");
    }
    
    /**
     * Retourne le nom d'utilisateur de la base de données
     */
    public static String getDatabaseUser() {
        return getProperty("db.user", "postgres");
    }
    
    /**
     * Retourne le mot de passe de la base de données
     */
    public static String getDatabasePassword() {
        return getProperty("db.password", "password");
    }
    
    /**
     * Retourne le nombre maximum de connexions
     */
    public static int getMaxConnections() {
        String value = getProperty("db.maxConnections", "10");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            LOGGER.warning("Valeur invalide pour db.maxConnections: " + value + " - Utilisation de la valeur par défaut: 10");
            return 10;
        }
    }
    
    /**
     * Retourne le timeout de connexion en millisecondes
     */
    public static long getConnectionTimeout() {
        String value = getProperty("db.connectionTimeout", "30000");
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            LOGGER.warning("Valeur invalide pour db.connectionTimeout: " + value + " - Utilisation de la valeur par défaut: 30000");
            return 30000L;
        }
    }
}