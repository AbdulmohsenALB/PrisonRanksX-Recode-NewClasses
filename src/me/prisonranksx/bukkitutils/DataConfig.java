package me.prisonranksx.bukkitutils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

/**
 * 
 * Manage multi player data with ease.
 *
 */
public class DataConfig {

	private JavaPlugin plugin;
	private Map<String, Object> data;
	private Map<String, Object> dataFirstTime;
	private Map<String, Object> dataOptional;
	private Set<UUID> firstTime;
	private String directory;

	public DataConfig(JavaPlugin plugin) {
		this.plugin = plugin;
		this.data = new LinkedHashMap<>();
		this.dataFirstTime = new LinkedHashMap<>();
		this.dataOptional = new LinkedHashMap<>();
		this.firstTime = new HashSet<>();
	}

	public DataConfig(JavaPlugin plugin, String directory) {
		this.plugin = plugin;
		this.data = new LinkedHashMap<>();
		this.dataFirstTime = new LinkedHashMap<>();
		this.dataOptional = new LinkedHashMap<>();
		this.firstTime = new HashSet<>();
		this.directory = directory;
	}

	public static DataConfig createDataConfig(JavaPlugin plugin) {
		return new DataConfig(plugin, plugin.getDataFolder().getAbsolutePath() + File.pathSeparator + "/data");
	}

	public static DataConfig createDataConfig(JavaPlugin plugin, String directory) {
		return new DataConfig(plugin, directory);
	}

	/**
	 * 
	 * @param directory the directory/folder where player data files will be saved
	 *                  in.
	 */
	public void setDirectory(String directory) {
		this.directory = directory;
	}

	/**
	 * 
	 * @return the directory that contains player data files
	 */
	public String getDirectory() {
		return this.directory;
	}

	/**
	 * 
	 * @param key
	 * @param defaultValue
	 *                     <p>
	 *                     <i>register an entry that will always be added to the
	 *                     config no matter what happens
	 */
	public void registerData(String key, Object defaultValue) {
		data.put(key, defaultValue);
	}

	/**
	 * 
	 * @param key          {@code<}<b>key</b>{@code>:<configValue>}, example ->
	 *                     <i><b>points</b></i>
	 * @param defaultValue {@code<configKey>:<}<b>defaultValue</b>{@code>}, example
	 *                     -> <i><b>1000.0</i></b>
	 *                     <p>
	 *                     combining both -> <i><b>points: 1000.0</i></b>
	 *                     <p>
	 *                     <i>register an entry that will only be added if the user
	 *                     didn't get registered before
	 *                     <p>
	 *                     <i>if the object "<b>defaultValue</b>" is an instance of
	 *                     <b>String</b>, you will be able to use the variables:
	 *                     <p>
	 *                     <b><u>%uuid%</b></u><i> and</i> <u><b>%name%
	 */
	public void registerFirstTimeData(String key, Object defaultValue) {
		dataFirstTime.put(key, defaultValue);
	}

	/**
	 * 
	 * @param key
	 * @param value
	 *              <p>
	 *              <i>register an entry that will be added if it doesn't have a key
	 *              in the config
	 */
	public DataConfig registerOptionalData(String key, Object value) {
		dataOptional.put(key, value);
		return this;
	}

	private void saveConfig(final FileConfiguration config, final File file) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try {
				config.save(file);
			} catch (IOException e) {
				System.out.println("Config saving failed.");
				e.printStackTrace();
			}
		});
	}

	/**
	 * 
	 * @param uuid player unique id
	 * @return whether this is the first time the user is getting registered or not
	 */
	public boolean isFirstTime(UUID uuid) {
		return firstTime.contains(uuid);
	}

	/**
	 * 
	 * @param uuid player unique id
	 * @param name player name
	 *             <p>
	 *             <i> creates a config file with the registered entries otherwise
	 *             just load it if it's already there
	 */
	public FileConfiguration initUserConfig(final UUID uuid, final String name) {
		FileConfiguration userConfig = null;
		File usersDirectory = new File(plugin.getDataFolder() + File.separator + this.directory);
		usersDirectory.mkdirs();
		File userFile = new File(
				plugin.getDataFolder() + File.separator + this.directory + File.separator + uuid + ".yml");
		if (!userFile.exists()) {
			try {
				userFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			userConfig = YamlConfiguration.loadConfiguration(userFile);
			for (String key : dataFirstTime.keySet()) {
				Object data = dataFirstTime.get(key);
				if (data instanceof String) {
					data = ((String) data).replace("%uuid%", uuid.toString()).replace("%name%", name);
				}
				userConfig.set(key, data);
			}
			firstTime.add(uuid);
		} else {
			firstTime.remove(uuid);
			userConfig = YamlConfiguration.loadConfiguration(userFile);
			for (String key : data.keySet()) {
				userConfig.set(key, data.get(key));
			}
			for (String key : dataOptional.keySet()) {
				if (!userConfig.isSet(key)) {
					userConfig.set(key, dataOptional.get(key));
				}
			}
		}
		saveConfig(userConfig, userFile);
		return userConfig;
	}

	public UserData initUserData(final UUID uuid, final String name) {
		FileConfiguration userConfig = null;
		UserData userData = new UserData(uuid, name);
		File usersDirectory = new File(plugin.getDataFolder() + File.separator + this.directory);
		usersDirectory.mkdirs();
		File userFile = new File(
				plugin.getDataFolder() + File.separator + this.directory + File.separator + uuid + ".yml");
		if (!userFile.exists()) {
			try {
				userFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			userConfig = YamlConfiguration.loadConfiguration(userFile);
			for (String key : dataFirstTime.keySet()) {
				Object data = dataFirstTime.get(key);
				if (data instanceof String) {
					data = ((String) data).replace("%uuid%", uuid.toString()).replace("%name%", name);
				}
				userData.data.put(key, data);
				userConfig.set(key, data);
			}
			firstTime.add(uuid);
		} else {
			firstTime.remove(uuid);
			userConfig = YamlConfiguration.loadConfiguration(userFile);
			for (String key : data.keySet()) {
				userConfig.set(key, data.get(key));
				userData.data.put(key, data.get(key));
			}
			for (String key : dataOptional.keySet()) {
				if (!userConfig.isSet(key)) {
					userConfig.set(key, dataOptional.get(key));
					userData.data.put(key, dataOptional.get(key));
				}
			}
		}
		saveConfig(userConfig, userFile);
		userData.configFile = userConfig;
		userData.file = userFile;
		return userData;
	}

	public class UserData {

		private UUID uniqueId;
		private String name;
		private FileConfiguration configFile;
		private File file;
		private Map<String, Object> data;

		public UserData(UUID uniqueId, String name) {
			this.uniqueId = uniqueId;
			this.name = name;
			this.data = Collections.synchronizedMap(new LinkedHashMap<>());
		}

		public UserData(UUID uniqueId, String name, FileConfiguration configFile, File file) {
			this.uniqueId = uniqueId;
			this.name = name;
			this.configFile = configFile;
			this.file = file;
			this.data = Collections.synchronizedMap(new LinkedHashMap<>());
		}

		public Set<String> getKeys() {
			return this.data.keySet();
		}

		public Collection<Object> getValues() {
			return this.data.values();
		}

		public void save() {
			this.data.entrySet().forEach(entry -> this.configFile.set(entry.getKey(), entry.getValue()));
			try {
				this.configFile.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void saveFile() {
			try {
				this.configFile.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void save(boolean async) {
			if (!async) save();
			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
				this.data.entrySet().forEach(entry -> this.configFile.set(entry.getKey(), entry.getValue()));
				try {
					this.configFile.save(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}

		public void saveFile(boolean async) {
			if (!async) save();
			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
				try {
					this.configFile.save(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}

		public void reloadFile() {
			this.configFile = YamlConfiguration.loadConfiguration(file);
		}

		public void reload() {
			this.data.clear();
			this.configFile = YamlConfiguration.loadConfiguration(file);
			this.configFile.getKeys(false).forEach(key -> {
				this.data.put(key, this.configFile.get(key));
			});
		}

		public void load() {
			this.configFile = YamlConfiguration.loadConfiguration(file);
			this.configFile.getKeys(false).forEach(key -> {
				this.data.put(key, this.configFile.get(key));
			});
		}

		public void reloadFile(boolean async) {
			if (!async) reloadFile();
			Bukkit.getScheduler()
					.runTaskAsynchronously(plugin, () -> this.configFile = YamlConfiguration.loadConfiguration(file));
		}

		public void reload(boolean async) {
			if (!async) reload();
			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
				this.data.clear();
				this.configFile = YamlConfiguration.loadConfiguration(file);
				this.configFile.getKeys(false).forEach(key -> {
					this.data.put(key, this.configFile.get(key));
				});
			});
		}

		public void load(boolean async) {
			if (!async) reload();
			Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
				this.configFile = YamlConfiguration.loadConfiguration(file);
				this.configFile.getKeys(false).forEach(key -> {
					this.data.put(key, this.configFile.get(key));
				});
			});
		}

		public UUID getUniqueId() {
			return this.uniqueId;
		}

		public String getName() {
			return this.name;
		}

		public Object get(String key) {
			return this.data.get(key);
		}

		public void set(String key, Object object) {
			this.data.put(key, object);
		}

		public void set(String key, Object object, boolean config) {
			this.data.put(key, object);
			this.configFile.set(key, object);
		}

		public boolean containsKey(String key) {
			return this.data.containsKey(key);
		}

		public boolean containsValue(Object value) {
			return this.data.containsValue(value);
		}

		public boolean getBoolean(String key) {
			return (boolean) this.data.get(key);
		}

		public boolean isBoolean(String key) {
			return this.data.get(key) instanceof Boolean;
		}

		public int getInt(String key) {
			return (int) this.data.get(key);
		}

		public boolean isInt(String key) {
			return this.data.get(key) instanceof Integer;
		}

		/**
		 * @deprecated
		 *             another name for getInt(...)
		 * @param key config key
		 */
		@Deprecated
		public int getInteger(String key) {
			return (int) this.data.get(key);
		}

		public String getString(String key) {
			return (String) this.data.get(key);
		}

		public boolean isString(String key) {
			return this.data.get(key) instanceof String;
		}

		public char getChar(String key) {
			return (char) this.data.get(key);
		}

		public boolean isChar(String key) {
			Object object = this.data.get(key);
			if (object instanceof String) {
				if (((String) object).length() == 1) {
					return true;
				}
			}
			return false;
		}

		public double getDouble(String key) {
			return (double) this.data.get(key);
		}

		public boolean isDouble(String key) {
			return this.data.get(key) instanceof Double;
		}

		public BigDecimal getBigDecimal(String key) {
			return (BigDecimal) this.data.get(key);
		}

		public boolean isBigDecimal(String key) {
			return this.data.get(key) instanceof BigDecimal;
		}

		public String getPlainDouble(String key) {
			return BigDecimal.valueOf((double) this.data.get(key)).toPlainString();
		}

		public long getLong(String key) {
			return (long) this.data.get(key);
		}

		public boolean isLong(String key) {
			return this.data.get(key) instanceof Long;
		}

		public float getFloat(String key) {
			return (float) this.data.get(key);
		}

		public boolean isFloat(String key) {
			return this.data.get(key) instanceof Float;
		}

		public short getShort(String key) {
			return (short) this.data.get(key);
		}

		public boolean isShort(String key) {
			return this.data.get(key) instanceof Short;
		}

		public byte getByte(String key) {
			return (byte) this.data.get(key);
		}

		public boolean isByte(String key) {
			return this.data.get(key) instanceof Byte;
		}

		public Number getNumber(String key) {
			return (Number) this.data.get(key);
		}

		public boolean isNumber(String key) {
			return this.data.get(key) instanceof Number;
		}

		@SuppressWarnings("unchecked")
		public List<String> getStringList(String key) {
			return (List<String>) this.data.get(key);
		}

		@SuppressWarnings("unchecked")
		public List<Integer> getIntegerList(String key) {
			return (List<Integer>) this.data.get(key);
		}

		@SuppressWarnings("unchecked")
		public List<Double> getDoubleList(String key) {
			return (List<Double>) this.data.get(key);
		}

		@SuppressWarnings("unchecked")
		public List<Float> getFloatList(String key) {
			return (List<Float>) this.data.get(key);
		}

		@SuppressWarnings("unchecked")
		public List<Short> getShortList(String key) {
			return (List<Short>) this.data.get(key);
		}

		@SuppressWarnings("unchecked")
		public List<Byte> getByteList(String key) {
			return (List<Byte>) this.data.get(key);
		}

		@SuppressWarnings("rawtypes")
		public List getList(String key) {
			return (List) this.data.get(key);
		}

		public boolean isList(String key) {
			return this.data.get(key) instanceof List;
		}

		@SuppressWarnings("unchecked")
		public Set<String> getStringSet(String key) {
			return (Set<String>) this.data.get(key);
		}

		@SuppressWarnings("rawtypes")
		public Set getSet(String key) {
			return (Set) this.data.get(key);
		}

		public boolean isSet(String key) {
			return this.data.get(key) instanceof Set;
		}

		@SuppressWarnings("unchecked")
		public Map<String, Object> getStringMap(String key) {
			return (Map<String, Object>) this.data.get(key);
		}

		@SuppressWarnings("rawtypes")
		public Map getMap(String key) {
			return (Map) this.data.get(key);
		}

		public boolean isMap(String key) {
			return this.data.get(key) instanceof Map;
		}

		public Object[] getArray(String key) {
			return (Object[]) this.data.get(key);
		}

		public boolean isArray(String key) {
			return this.data.get(key) instanceof Object[];
		}

		public String[] getStringArray(String key) {
			return (String[]) this.data.get(key);
		}

		public boolean isStringArray(String key) {
			return this.data.get(key) instanceof String[];
		}

		public ItemStack getItemStack(String key) {
			return (ItemStack) this.data.get(key);
		}

		public boolean isItemStack(String key) {
			return this.data.get(key) instanceof ItemStack;
		}

		public ItemStack[] getItemStackArray(String key) {
			return (ItemStack[]) this.data.get(key);
		}

		public boolean isItemStackArray(String key) {
			return this.data.get(key) instanceof ItemStack[];
		}

		@SuppressWarnings("unchecked")
		public List<ItemStack> getItemStackList(String key) {
			return (List<ItemStack>) this.data.get(key);
		}

		public Location getLocation(String key) {
			return (Location) this.data.get(key);
		}

		public boolean isLocation(String key) {
			return this.data.get(key) instanceof Location;
		}

		public Vector getVector(String key) {
			return (Vector) this.data.get(key);
		}

		public boolean isVector(String key) {
			return this.data.get(key) instanceof Vector;
		}

	}
}
