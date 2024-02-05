package me.prisonranksx.managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.bukkit.configuration.file.FileConfiguration;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.common.StaticCache;

public class MySQLManager extends StaticCache {

	// This performs the setup as well.
	private static final MySQLHolder HOLDER = new MySQLHolder();

	public static void reload() {
		HOLDER.setup();
	}

	public static Connection getConnection() {
		return HOLDER.connection;
	}

	public static void openConnection() {
		try {
			HOLDER.openConnection();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	public static void closeConnection() {
		HOLDER.closeConnection();
	}

	public static boolean isOpen() {
		return HOLDER.isOpen();
	}

	public static String getDatabase() {
		return HOLDER.getDatabase();
	}

	public static String getTable() {
		return HOLDER.getTable();
	}

	public static Statement newStatement() {
		try {
			return HOLDER.connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static class MySQLHolder {

		private boolean useSSL, autoReconnect, useCursorFetch;
		private int port;
		private Connection connection;
		private String host, database, username, password, table;

		public MySQLHolder() {
			setup();
		}

		public void setup() {
			FileConfiguration config = ConfigManager.getConfig();
			if (!config.getBoolean("MySQL.enable")) {
				closeConnection();
				return;
			}
			useSSL = config.getBoolean("MySQL.use-ssl");
			autoReconnect = config.getBoolean("MySQL.auto-reconnect");
			useCursorFetch = config.getBoolean("MySQL.use-cursor-fetch");
			port = config.getInt("MySQL.port");
			host = config.getString("MySQL.host");
			database = config.getString("MySQL.database");
			username = config.getString("MySQL.username");
			password = config.getString("MySQL.password");
			table = config.getString("MySQL.table");
			Statement statement;
			try {
				openConnection();
				statement = getConnection().createStatement();
				statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + database + "." + table
						+ " (`uuid` varchar(255), `name` varchar(255), `rank` varchar(255), `path` varchar(255), `prestige` varchar(255), `rebirth` varchar(255), `score` int(10));");
				statement.close();;
				PrisonRanksX.log("Successfully connected to MySQL database.");
			} catch (SQLException | ClassNotFoundException e) {
				e.printStackTrace();
			}

		}

		public Connection getConnection() {
			return connection;
		}

		public String getDatabase() {
			return database;
		}

		public String getTable() {
			return table;
		}

		public boolean isOpen() {
			try {
				return getConnection() != null && !getConnection().isClosed();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return false;
		}

		/**
		 * Opens a new MySQL connection.
		 * 
		 * @throws SQLException
		 * @throws ClassNotFoundException
		 */
		public void openConnection() throws SQLException, ClassNotFoundException {
			synchronized (this) {
				if (getConnection() != null && !getConnection().isClosed()) return;
				Class.forName("com.mysql.jdbc.Driver");
				Properties prop = new Properties();
				prop.setProperty("user", username);
				prop.setProperty("password", password);
				prop.setProperty("useSSL", String.valueOf(useSSL));
				prop.setProperty("autoReconnect", String.valueOf(autoReconnect));
				if (useCursorFetch) prop.setProperty("useCursorFetch", String.valueOf(useCursorFetch));
				connection = DriverManager.getConnection(
						"jdbc:mysql://" + host + ":" + port + "/" + database + "?characterEncoding=utf8", prop);
			}
		}

		/**
		 * Closes the MySQL connection.
		 */
		public void closeConnection() {
			try {
				if (getConnection() != null && !getConnection().isClosed()) getConnection().close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

}
