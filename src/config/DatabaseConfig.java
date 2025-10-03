package config;

/**
 * Configuration de la base de données isolée pour la sécurité.
 * Les valeurs peuvent être surchargées par les propriétés système.
 */
public class DatabaseConfig {
    
    // Valeurs par défaut pour le développement
    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/cryptowallet";
    private static final String DEFAULT_USER = "postgres";
    private static final String DEFAULT_PASSWORD = "password";
    
    // Constructeur privé pour empêcher l'instanciation
    private DatabaseConfig() {
        // Classe utilitaire, ne doit pas être instanciée
    }
    
    /**
     * Retourne l'URL de la base de données.
     * Peut être surchargée avec -Ddb.url=...
     */
    public static String getUrl() {
        return System.getProperty("db.url", DEFAULT_URL);
    }
    
    /**
     * Retourne le nom d'utilisateur de la base de données.
     * Peut être surchargé avec -Ddb.user=...
     */
    public static String getUser() {
        return System.getProperty("db.user", DEFAULT_USER);
    }
    
    /**
     * Retourne le mot de passe de la base de données.
     * Peut être surchargé avec -Ddb.password=...
     */
    public static String getPassword() {
        return System.getProperty("db.password", DEFAULT_PASSWORD);
    }
}