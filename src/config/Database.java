package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;
import util.LoggerProvider;


public class Database {

	private static final Logger LOGGER = LoggerProvider.getLogger(Database.class.getName());
	private static Database INSTANCE;

	private final String url;
	private final String user;
	private final String password;
	private volatile boolean initialized;

	private Database() {
		this.url = ConfigManager.getDatabaseUrl();
		this.user = ConfigManager.getDatabaseUser();
		this.password = ConfigManager.getDatabasePassword();
		
		// Détecte automatiquement le type de base de données et charge le driver approprié
		loadDatabaseDriver();
	}
	
	
	private void loadDatabaseDriver() {
		try {
			if (url.contains("postgresql")) {
				Class.forName("org.postgresql.Driver");
				LOGGER.info("Driver PostgreSQL chargé");
			} else if (url.contains("h2")) {
				Class.forName("org.h2.Driver");
				LOGGER.info("Driver H2 Database chargé");
			} else if (url.contains("mysql")) {
				Class.forName("com.mysql.cj.jdbc.Driver");
				LOGGER.info("Driver MySQL chargé");
			} else {
				LOGGER.info("Type de base de données non reconnu, utilisation du driver par défaut");
			}
		} catch (ClassNotFoundException e) {
			LOGGER.warning("Driver de base de données non trouvé: " + e.getMessage() + 
			              " (peut être normal avec JDBC 4+)");
		}
	}

	public static synchronized Database getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Database();
		}
		return INSTANCE;
	}

	public Connection getConnection() throws SQLException {
		try {
			return DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			LOGGER.severe("Erreur connexion DB: " + e.getMessage());
			throw e;
		}
	}

	
	public synchronized void init() {
		if (initialized) {
			return;
		}
		final String createWallets =
			"CREATE TABLE IF NOT EXISTS wallets (" +
			"pk BIGSERIAL PRIMARY KEY, " +
			"id VARCHAR(64) UNIQUE NOT NULL, " +
			"type VARCHAR(32) NOT NULL, " +
			"address VARCHAR(128) NOT NULL, " +
			"balance NUMERIC(38, 18) NOT NULL DEFAULT 0, " +
			"created_at TIMESTAMP NOT NULL" +
			")";

		final String createTransactions =
			"CREATE TABLE IF NOT EXISTS transactions (" +
			"pk BIGSERIAL PRIMARY KEY, " +
			"id VARCHAR(64) UNIQUE NOT NULL, " +
			"from_address VARCHAR(128) NOT NULL, " +
			"to_address VARCHAR(128) NOT NULL, " +
			"amount NUMERIC(38, 18) NOT NULL, " +
			"fee_priority VARCHAR(32) NOT NULL, " +
			"fee_amount NUMERIC(38, 18) NOT NULL, " +
			"status VARCHAR(32) NOT NULL, " +
			"created_at TIMESTAMP NOT NULL, " +
			"wallet_id VARCHAR(64) NOT NULL REFERENCES wallets(id)" +
			")";

		try (Connection c = getConnection()) {
			try (PreparedStatement ps = c.prepareStatement(createWallets)) {
				ps.execute();
			}
			try (PreparedStatement ps = c.prepareStatement(createTransactions)) {
				ps.execute();
			}
			initialized = true;
			LOGGER.info("Schéma DB initialisé");
		} catch (SQLException e) {
			LOGGER.severe("Echec d'initialisation du schéma: " + e.getMessage());
		}
	}

	/**
	 * Vérifie rapidement l'accès à la base.
	 */
	public boolean isHealthy() {
		try (Connection c = getConnection()) {
			return c != null && !c.isClosed();
		} catch (SQLException e) {
			return false;
		}
	}
}