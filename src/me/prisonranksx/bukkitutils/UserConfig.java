package me.prisonranksx.bukkitutils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class UserConfig {

	private JavaPlugin plugin;
	private String directory;

	public UserConfig(JavaPlugin plugin, String directory) {
		this.plugin = plugin;
		this.directory = plugin.getDataFolder() + File.separator + directory;
	}

	public static UserConfig create(JavaPlugin plugin, String directory) {
		return new UserConfig(plugin, directory);
	}

	public String getDirectory() {
		return directory;
	}

	public String getUserDirectory(UUID uniqueId) {
		return directory + File.separator + uniqueId.toString() + ".yml";
	}

	public void save(UUID uniqueId) {
		FileConfiguration userConfiguration = loadOrCreate(uniqueId);
		try {
			userConfiguration.save(directory + File.separator + uniqueId.toString() + ".yml");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public FileConfiguration loadOrCreate(UUID uniqueId) {
		Path userFilePath = Paths.get(directory + File.separator + uniqueId.toString() + ".yml");
		File userFile = userFilePath.toFile();
		FileConfiguration userConfiguration = null;
		if (!Files.exists(userFilePath)) {
			try {
				userFile.getParentFile().mkdirs();
				userFile.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		userConfiguration = YamlConfiguration.loadConfiguration(userFile);
		return userConfiguration;
	}

	public FileConfiguration loadOrCreate(UUID uniqueId, String resourcePath, String resourceName) {
		Path userFilePath = Paths.get(directory + File.separator + uniqueId.toString() + ".yml");
		File userFile = userFilePath.toFile();
		FileConfiguration userConfiguration = null;
		if (!Files.exists(userFilePath)) {
			userFile.getParentFile().mkdirs();
			plugin.saveResource(resourcePath, false);
			File resourceFile = new File(resourcePath);
			resourceFile.renameTo(new File(resourcePath.replace(resourceName, userFile.getName())));
		}
		userConfiguration = YamlConfiguration.loadConfiguration(userFile);
		return userConfiguration;
	}

}
