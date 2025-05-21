package me.prisonranksx.bukkitutils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import javax.sql.rowset.*;


/**
 * Utility to query or update your database with easy to use methods,
 * eliminating the need to write commonly used SQL statements manually.
 * Start with {@linkplain #createTable(StupidColumn...)} method.
 * <br>
 */
public class StupidMySQL {

	public static class StupidColumn {

		private Class<?> valueType;
		private int valueLength;
		private String columnName;
		private String valueSQLType;

		public <T> StupidColumn(String columnName, Class<T> valueType, int valueLength) {
			this.valueType = valueType;
			this.valueLength = valueLength;
			this.columnName = columnName;
			switch (valueType.getName()) {
				case "java.lang.String":
					valueSQLType = "varchar";
					break;
				case "java.lang.Integer":
					valueSQLType = "int";
					break;
				case "java.lang.Character":
					valueSQLType = "char";
					break;
				case "java.lang.Boolean":
					valueSQLType = "bool";
				default:
					throw new IllegalStateException("Unexpected value: " + valueType.getName());
			}
		}

		public <T> StupidColumn(String columnName, Class<T> valueType, int valueLength, String valueSQLType) {
			this.columnName = columnName;
			this.valueType = valueType;
			this.valueLength = valueLength;
			this.valueSQLType = valueSQLType;
		}

		/**
		 * Convert java class type to mysql data type, String.class would be varchar for
		 * example. Example usage: {@code parse("name", String.class, 16);}
		 *
		 * @param columnName
		 * @param valueType
		 * @param valueLength maximum length of data, varchar(<b>valueLength</b>)
		 * @return
		 */
		public static StupidColumn parse(String columnName, Class<?> valueType, int valueLength) {
			return new StupidColumn(columnName, valueType, valueLength);
		}

		public Class<?> getValueType() {
			return valueType;
		}

		public int getValueLength() {
			return valueLength;
		}

		public String getValueSQLType() {
			return valueSQLType;
		}

		public String getColumnName() {
			return columnName;
		}

	}

	private Connection connection;
	private String database, table, insertKeys, insertValues, selectKey, selectValue, setPairs;
	private String insertStatement = "INSERT INTO ";
	private String selectStatement = "SELECT * FROM ";
	private String setStatement = "UPDATE ";
	private String alterStatement = "ALTER TABLE ";
	private PreparedStatement preparedStatement, preparedStatement2, selectPreparedStatement;

	public StupidMySQL(Connection connection, String database, String table) {
		this.connection = connection;
		this.database = database;
		this.table = table;
		this.insertStatement += database + "." + table + " ";
		this.selectStatement += database + "." + table + " WHERE ";
		this.setStatement += database + "." + table + " SET ";
		this.alterStatement += database + "." + table + " ";
	}

	/**
	 * Creates the database table if it doesn't exist. Otherwise, it does nothing.
	 * The following statement is used here: <b>CREATE TABLE IF NOT EXISTS </b>
	 * <br>
	 * Example:
	 * <br>
	 * {@code createTable(StupidColumn.parse("name",  String.class, 16),  StupidColumn.parse("age", int.class, 3));}
	 * <br>
	 * 
	 * @param columns columns the table will have in order.
	 *                <br>
	 *                {@linkplain StupidColumn#parse(String, Class, int)}
	 * @return this instance for further usage.
	 */
	public StupidMySQL createTable(StupidColumn... columns) {
		String createStatement = "CREATE TABLE IF NOT EXISTS " + database + "." + table + " ";
		StringBuilder columnsPart = new StringBuilder();
		for (StupidColumn column : columns) {
			if (columnsPart.length() > 0) {
				if (column.valueLength == -1)
					columnsPart.append("(`").append(column.columnName).append("` ").append(column.valueSQLType);
				else
					columnsPart.append("(`")
							.append(column.columnName)
							.append("` ")
							.append(column.valueSQLType)
							.append("(")
							.append(column.valueLength)
							.append(")");
			} else {
				if (column.valueLength == -1)
					columnsPart.append(", `").append(column.columnName).append("` ").append(column.valueSQLType);
				else
					columnsPart.append(", `")
							.append(column.columnName)
							.append("` ")
							.append(column.valueSQLType)
							.append("(")
							.append(column.valueLength)
							.append(")");
			}
		}
		String finalStatement = createStatement + columnsPart + ");";
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(finalStatement);
			statement.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	/**
	 * Removes / drops a column from the table if it exists, uses the following
	 * statement:
	 * <b>ALTER TABLE table_name DROP COLUMN column1, DROP COLUMN column2,...</b>
	 * An exception is supposed to be thrown if trying to remove a column that
	 * doesn't exist, but it is ignored due to absence of a SQL statement that drops
	 * columns only if they exist.
	 * 
	 * @param columns to remove
	 * @return this instance for further usage.
	 */
	public StupidMySQL removeColumn(String... columns) {
		StringBuilder columnsPart = new StringBuilder();
		for (String column : columns) {
			if (columnsPart.length() == 0)
				columnsPart.append("DROP COLUMN ").append(column);
			else
				columnsPart.append(", DROP COLUMN ").append(column);
		}
		String finalStatement = alterStatement + columnsPart + ";";
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(finalStatement);
			statement.close();
		} catch (SQLException ignored) {}
		return this;
	}

	/**
	 * Creates a new instance of StupidMySQL. This instance should be re-used after
	 * each execution (execute()) method.
	 *
	 * @param connection connection to use for statement creations
	 * @param database   name of database
	 * @param table      name of table to perform statements on
	 * @return new StupidMySQL instance.
	 */
	public static StupidMySQL use(Connection connection, String database, String table) {
		return new StupidMySQL(connection, database, table);
	}

	/**
	 * Creates a new instance of StupidMySQL. This instance should be re-used after
	 * each execution (execute()) method.
	 *
	 * @param user     database user
	 * @param password database password
	 * @param database database
	 * @param table    database table
	 * @param host     database host
	 * @param port     database port
	 * @return new StupidMySQL instance.
	 */
	public static StupidMySQL use(String user, String password, String database, String table, String host,
			String port) {
		StupidMySQL stupidMySQL = new StupidMySQL(null, database, table);
		stupidMySQL.openConnection(user, password, "false", "true", false, host, port);
		return stupidMySQL;
	}

	/**
	 * Creates a new instance of StupidMySQL. This instance should be re-used after
	 * each execution (execute()) method.
	 *
	 * @param user     database user
	 * @param password database password
	 * @param database database
	 * @param table    database table
	 * @param host     database host
	 * @param port     database port
	 * @return new StupidMySQL instance.
	 */
	public static StupidMySQL use(String user, String password, String database, String table, String host, String port,
			boolean useSSL, boolean autoReconnect, boolean useCursorFetch) {
		StupidMySQL stupidMySQL = new StupidMySQL(null, database, table);
		stupidMySQL.openConnection(user, password, String.valueOf(useSSL), String.valueOf(autoReconnect),
				useCursorFetch, host, port);
		return stupidMySQL;
	}

	/**
	 * Opens a new MySQL connection.
	 */
	private Connection openConnection(String username, String password, String useSSL, String autoReconnect,
			boolean useCursorFetch, String host, String port) {
		synchronized (this) {
			try {
				if (connection != null && !connection.isClosed()) return connection;
				Class.forName("com.mysql.jdbc.Driver");
				Properties prop = new Properties();
				prop.setProperty("user", username);
				prop.setProperty("password", password);
				prop.setProperty("useSSL", String.valueOf(useSSL));
				prop.setProperty("autoReconnect", String.valueOf(autoReconnect));
				prop.setProperty("useCursorFetch", String.valueOf(useCursorFetch));
				return connection = DriverManager.getConnection(
						"jdbc:mysql://" + host + ":" + port + "/" + database + "?characterEncoding=utf8", prop);
			} catch (SQLException | ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private String fixValue(String value) {
		return value == null ? "null" : "'" + value + "'";
	}

	/**
	 * Gets connection included in this instance.
	 * 
	 * @return Connection that's being used to create statements.
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * Sets up <b>INSERT INTO myTable (`name`) VALUES ('alex');</b> statement
	 *
	 * @param key   being the name
	 * @param value being john
	 * @return this instance for execution of set-up statement using
	 *         {@linkplain #execute()}
	 */
	public synchronized StupidMySQL insert(String key, String value) {
		String fixedValue = fixValue(value);
		if (insertKeys == null || insertValues == null) {
			insertKeys = "(`" + key + "`";
			insertValues = " VALUES (" + fixedValue;
		} else {
			insertKeys += ", `" + key + "`";
			insertValues += ", " + fixedValue;
		}
		return this;
	}

	/**
	 * Sets up <b>INSERT INTO myTable (`name`, `gender`) VALUES ('alex',
	 * `male`);</b> statement
	 * with multiple keys and values.
	 *
	 * @param keysAndValues in the above example would be: <b>"name", "alex",
	 *                      "gender", "male"</b>
	 * @return this instance for execution of set-up statement using
	 *         {@linkplain #execute()}
	 */
	public synchronized StupidMySQL insert(String... keysAndValues) {
		for (int i = 1; i < keysAndValues.length; i += 2) insert(keysAndValues[i - 1], keysAndValues[i]);
		return this;
	}

	private synchronized StupidMySQL insertWithId(String idKey, String idValue, String... keysAndValues) {
		insert(idKey, idValue);
		for (int i = 1; i < keysAndValues.length; i += 2) insert(keysAndValues[i - 1], keysAndValues[i]);
		return this;
	}

	/**
	 * Sets up <b>UPDATE myTable SET `gender`='female' WHERE `name`='alex'</b>
	 * statement
	 * with SELECT statement using identifier (idKey) and its value (idValue) to
	 * check whether column exists or not
	 *
	 * @param idKey   "name"
	 * @param idValue "alex" - <b>WHERE name = 'alex'</b>
	 * @param key     "gender"
	 * @param value   "female"
	 * @return this instance for execution of set-up statement using
	 *         {@linkplain #execute()}
	 */
	public synchronized StupidMySQL set(String idKey, String idValue, String key, String value) {
		selectKey = "`" + idKey + "` ";
		String fixedValue = fixValue(value);
		selectValue = "= " + fixValue(idValue);
		if (setPairs == null)
			setPairs = "`" + key + "` = " + fixedValue;
		else
			setPairs += ", `" + key + "` = " + fixedValue;
		return this;
	}

	/**
	 * Sets up <b>UPDATE myTable SET `gender`='female',`age`='24' WHERE
	 * `name`='alex'</b>
	 * statement with multiple keys and values
	 * with SELECT statement using identifier (idKey) and its value (idValue) to
	 * check whether column exists or not
	 *
	 * @param idKey         "name"
	 * @param idValue       "alex" - <b>WHERE name = 'alex'</b>
	 * @param keysAndValues being {@code "gender", "female", "age", "24"} in this
	 *                      order
	 *                      {@code key1, value1, key2, value2, key3, value3...}
	 * @return this instance for execution of set-up statement using
	 *         {@linkplain #execute()}
	 */
	public synchronized StupidMySQL set(String idKey, String idValue, String... keysAndValues) {
		for (int i = 1; i < keysAndValues.length; i += 2) set(idKey, idValue, keysAndValues[i - 1], keysAndValues[i]);
		return this;
	}

	/**
	 * Sets up both statements for insertion and updating, so if information doesn't
	 * exist, rather than updating, it will get inserted
	 *
	 * @param idKey
	 * @param idValue
	 * @param key
	 * @param value
	 * @return
	 */
	public synchronized StupidMySQL setOrInsert(String idKey, String idValue, String key, String value) {
		insert(idKey, idValue, key, value);
		set(idKey, idValue, key, value);
		return this;
	}

	/**
	 * Sets up both statements for insertion and updating, so if information doesn't
	 * exist, rather than updating, it will get inserted
	 *
	 * @param idKey
	 * @param idValue
	 * @param keysAndValues
	 * @return
	 */
	public synchronized StupidMySQL setOrInsert(String idKey, String idValue, String... keysAndValues) {
		insertWithId(idKey, idValue, keysAndValues);
		set(idKey, idValue, keysAndValues);
		return this;
	}

	/**
	 *
	 * @return all data stored
	 */
	public synchronized ResultSet get() {
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(selectStatement.replace(" WHERE ", ""));
			CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
			crs.populate(resultSet);
			statement.close();
			return crs;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 *
	 * @return all data stored
	 */
	public synchronized void use(Consumer<ResultSet> action) {
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(selectStatement.replace(" WHERE ", ""));
			action.accept(resultSet);
			statement.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 *
	 * @param idKey   such as name
	 * @param idValue such as johnny
	 * @return data for specified identifier
	 */
	public synchronized ResultSet get(String idKey, String idValue) {
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(selectStatement + "`" + idKey + "` = " + fixValue(idValue));
			CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
			crs.populate(resultSet);
			statement.close();
			return crs;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 *
	 * @param idKey   such as name
	 * @param idValue such as johnny
	 * @return data for specified identifier
	 */
	public synchronized void use(String idKey, String idValue, Consumer<ResultSet> action) {
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(selectStatement + "`" + idKey + "` = " + fixValue(idValue));
			action.accept(resultSet);
			statement.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 *
	 * @param wantedKey key to get value from
	 * @param idKey     such as name
	 * @param idValue   such as johnny
	 * @return data from specified key for specified identifier
	 */
	public synchronized ResultSet get(String wantedKey, String idKey, String idValue) {
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement
					.executeQuery(selectStatement.replace("*", wantedKey) + "`" + idKey + "` = " + fixValue(idValue));
			CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
			crs.populate(resultSet);
			statement.close();
			return crs;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Uses retrieved data from specified key for specified identifier
	 * 
	 * @param wantedKey key to get value from
	 * @param idKey     such as name
	 * @param idValue   such as johnny
	 */
	public synchronized void use(String wantedKey, String idKey, String idValue, Consumer<ResultSet> action) {
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement
					.executeQuery(selectStatement.replace("*", wantedKey) + "`" + idKey + "` = " + fixValue(idValue));
			action.accept(resultSet);
			statement.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Prepares a statement which can be re-used to add multiple data all at once.
	 * This is instead of committing each time we want to store data into the
	 * database.
	 *
	 * @param keys to prepare {@code prepareInsert("name", "gender, "age");}
	 *             then we can use {@linkplain #addToPrepared(String...)} multiple
	 *             times to insert values.
	 *             So for example,
	 *             {@code addToPrepared("alex", "male", "20"); addToPrepared("sarah", "female", "19"); // and so on...}
	 *             and end it with {@linkplain #execute()}
	 * @return this instance for execution.
	 */
	public synchronized StupidMySQL prepareInsert(String... keys) {
		StringBuilder statement = new StringBuilder(insertStatement + "(");
		StringBuilder values = new StringBuilder(" VALUES (");
		for (String key : keys) {
			if (!statement.toString().equals(insertStatement + "(")) {
				statement.append(", ").append(key);
				values.append(", ?");
			} else {
				statement.append(key);
				values.append("?");
			}
		}
		statement.append(")");
		values.append(")");
		String finalStatement = statement + values.toString();
		try {
			connection.setAutoCommit(false);
			preparedStatement = connection.prepareStatement(finalStatement);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	/**
	 * Prepares a statement which can be re-used to add multiple data all at once.
	 * This is instead of committing each time we want to store data into the
	 * database.
	 *
	 * @param keys to prepare {@code prepareSet("name", "gender, "age");}
	 *             then we can use {@linkplain #addToPrepared(String...)} multiple
	 *             times to update values. In contrast to prepareInsert, the idKey,
	 *             would be the last parameter when we want to store values.
	 *             So for example,
	 *             {@code addToPrepared("male", "20", "alex"); addToPrepared("female", "19", "sarah"); // and so on...}
	 *             and end it with {@linkplain #execute()}
	 * @return
	 */
	public synchronized StupidMySQL prepareSet(String idKey, String... keys) {
		StringBuilder statement = new StringBuilder(setStatement);
		for (String key : keys) {
			if (!statement.toString().equals(setStatement)) {
				statement.append(", `").append(key).append("`=?");
			} else {
				statement.append("`").append(key).append("`=?");
			}
		}
		statement.append(" WHERE `").append(idKey).append("`=?");
		String finalStatement = statement.toString();
		try {
			connection.setAutoCommit(false);
			preparedStatement = connection.prepareStatement(finalStatement);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	/**
	 * Prepares a statement which can be re-used to add multiple data all at once.
	 * When values get added, first they are checked in a select statement to
	 * determine whether they are going to be inserted or updated using a set
	 * statement.
	 *
	 * @param keys to prepare {@code prepareSetOrInsert("name", "gender, "age");}
	 *             then we can use {@linkplain #addToPrepared(String...)} multiple
	 *             times to insert values.
	 *             So for example,
	 *             {@code addToPrepared("alex", "male", "20"); addToPrepared("sarah", "female", "19"); // and so on...}
	 *             and end it with {@linkplain #execute()}
	 * @return
	 */
	public synchronized StupidMySQL prepareSetOrInsert(String idKey, String... keys) {
		List<String> keyArray = new ArrayList<>();
		keyArray.add(idKey);
		keyArray.addAll(Arrays.asList(keys));
		prepareInsert(keyArray.toArray(new String[0]));
		preparedStatement2 = preparedStatement;
		prepareSet(idKey, keys);
		try {
			selectPreparedStatement = connection.prepareStatement(selectStatement + "`" + idKey + "`=?");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	/**
	 * This method can be used multiple times, efficient for saving large chunk of
	 * data.
	 *
	 * @param values to add to prepared statement, values should be in order of
	 *               prepared keys,
	 *               so if we used {@code prepareInsert("name", "gender", "age");},
	 *               this method would be used as follows:
	 *               {@code addToPrepared("alex", "male", "19");}
	 *               <br>
	 *               <br>
	 *               As for prepareSet, the idKey value would be the last one, so if
	 *               we used {@code prepareSet("name", "gender", "age");},
	 *               name being the idKey, this method would be used as follows:
	 *               {@code addToPrepared("male", "0", "alex");} this is assuming
	 *               someone named alex is already in the database.
	 *               <br>
	 *               <br>
	 *               prepareSetOrInsert follows prepareInsert order.
	 * @return
	 */
	public synchronized StupidMySQL addToPrepared(String... values) {
		if (preparedStatement2 != null) {
			try {
				preparedStatement.setString(values.length, values[0]);
				for (int i = 0; i < values.length; i++) {
					int startIndex = i + 1;
					preparedStatement2.setString(startIndex, values[i]);
					// don't touch the last index, we already did. Also, start from values[1].
					if (startIndex < values.length) preparedStatement.setString(startIndex, values[startIndex]);
				}
				selectPreparedStatement.setString(1, values[0]);
				selectPreparedStatement.addBatch();
				ResultSet resultSet = selectPreparedStatement.executeQuery();
				if (resultSet.next()) {
					preparedStatement.addBatch();
					preparedStatement2.clearParameters();
					selectPreparedStatement.clearBatch();
				} else {
					preparedStatement2.addBatch();
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			return this;
		}
		try {
			for (int i = 0; i < values.length; i++) {
				preparedStatement.setString(i + 1, values[i]);
			}
			preparedStatement.addBatch();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	/**
	 * This method can be used multiple times, efficient for saving large chunk of
	 * data.
	 *
	 * @param values to add to prepared statement, values should be in order of
	 *               prepared keys,
	 *               so if we used {@code prepareInsert("name", "gender", "age");},
	 *               this method would be used as follows:
	 *               {@code addToPrepared("alex", "male", 19);}
	 *               <br>
	 *               <br>
	 *               As for prepareSet, the idKey value would be the last one, so if
	 *               we used {@code prepareSet("name", "gender", "age");},
	 *               name being the idKey, this method would be used as follows:
	 *               {@code addToPrepared("male", 0, "alex");} this is assuming
	 *               someone named alex is already in the database.
	 *               <br>
	 *               <br>
	 *               prepareSetOrInsert follows prepareInsert order.
	 * @return
	 */
	public synchronized StupidMySQL addToPrepared(Object... values) {
		if (preparedStatement2 != null) {
			try {
				preparedStatement.setObject(values.length, values[0]);
				for (int i = 0; i < values.length; i++) {
					int startIndex = i + 1;
					preparedStatement2.setObject(startIndex, values[i]);
					// don't touch the last index, we already did. Also, start from values[1].
					if (startIndex < values.length) {
						preparedStatement.setObject(startIndex, values[startIndex]);
					}
				}
				selectPreparedStatement.setObject(1, values[0]);
				selectPreparedStatement.addBatch();
				ResultSet resultSet = selectPreparedStatement.executeQuery();
				if (resultSet.next()) {
					preparedStatement.addBatch();
					preparedStatement2.clearParameters();
					selectPreparedStatement.clearBatch();
				} else {
					preparedStatement2.addBatch();
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			return this;
		}
		try {
			for (int i = 0; i < values.length; i++) {
				preparedStatement.setObject(i + 1, values[i]);
			}
			preparedStatement.addBatch();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	public void clean() {
		insertKeys = null;
		insertValues = null;
		setPairs = null;
		selectKey = null;
		selectValue = null;
	}

	/**
	 * Executes last created statement, whether it's prepared one with multiple
	 * data,
	 * or a basic one with single data. Then clears the statements, and prepared
	 * values for reuse.
	 *
	 * @return
	 */
	public synchronized StupidMySQL execute() {
		// If using prepareSetOrInsert
		if (preparedStatement2 != null) {
			try {
				preparedStatement2.executeBatch();
				preparedStatement.executeBatch();
				connection.commit();
				connection.setAutoCommit(true);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			clean();
			preparedStatement = null;
			preparedStatement2 = null;
			selectPreparedStatement = null;
			return this;
		}
		// If using prepareSet or prepareInsert
		if (preparedStatement != null) {
			try {
				preparedStatement.executeBatch();
				connection.commit();
				connection.setAutoCommit(true);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			clean();
			preparedStatement = null;
			return this;
		}
		// If using insert only
		if (insertKeys != null && setPairs == null) {
			insertKeys += ")";
			insertValues += ")";
			String lastStatement = insertStatement + insertKeys + insertValues + ";";
			try {
				Statement statement = connection.createStatement();
				statement.executeUpdate(lastStatement);
				statement.close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		// If using setOrInsert
		if (setPairs != null) {
			String lastSelectStatement = selectStatement + selectKey + selectValue;
			ResultSet resultSet = null;
			try {
				resultSet = connection.createStatement().executeQuery(lastSelectStatement);
				if (resultSet.next()) {
					String lastStatement = setStatement + setPairs + " WHERE " + selectKey + selectValue + ";";
					Statement statement = connection.createStatement();
					statement.executeUpdate(lastStatement);
					statement.close();
				} else if (insertKeys != null) {
					insertKeys += ")";
					insertValues += ")";
					String lastStatement = insertStatement + insertKeys + insertValues + ";";
					try {
						Statement statement = connection.createStatement();
						statement.executeUpdate(lastStatement);
						statement.close();
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		clean();
		return this;
	}

	public String getDatabase() {
		return database;
	}

	public String getTable() {
		return table;
	}

}
