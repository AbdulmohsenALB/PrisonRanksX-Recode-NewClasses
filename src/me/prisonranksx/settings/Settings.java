package me.prisonranksx.settings;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import me.prisonranksx.managers.ConfigManager;
import me.prisonranksx.managers.StringManager;

public abstract class Settings {

	private String parentSectionName;
	private ConfigurationSection parentSection;
	private String configName;

	public Settings(String parentSectionName) {
		this.parentSectionName = parentSectionName;
		this.parentSection = ConfigManager.getConfig().getConfigurationSection(parentSectionName);
	}

	public Settings(String parentSectionName, String configName) {
		this.parentSectionName = parentSectionName;
		this.parentSection = ConfigManager.getConfig(configName).getConfigurationSection(parentSectionName);
		this.configName = configName;
	}

	protected boolean getBoolean(String configNode) {
		return parentSection.getBoolean(configNode);
	}

	protected String getString(String configNode) {
		String string = !parentSection.isList(configNode) ? parentSection.getString(configNode)
				: String.join("\n", parentSection.getStringList(configNode));
		return configNode == null || string == null ? null : string;
	}

	protected String getString(String configNode, boolean parseColors) {
		return parseColors ? parseLines(StringManager.parseColorsAndSymbols(getString(configNode)))
				: parseLines(getString(configNode));
	}

	private String parseLines(String string) {
		return string == null ? null : string.replace("\\n", "\n");
	}

	protected int getInt(String configNode) {
		return parentSection.getInt(configNode);
	}

	protected double getDouble(String configNode) {
		return parentSection.getDouble(configNode);
	}

	protected float getFloat(String configNode) {
		return (float) getDouble(configNode);
	}

	@Nullable
	protected List<String> getStringList(String configNode) {
		return !parentSection.contains(configNode) ? null : parentSection.getStringList(configNode);
	}

	@Nullable
	protected List<String> getStringList(String configNode, boolean parseColors) {
		return !parentSection.contains(configNode) || parentSection.getStringList(configNode).isEmpty() ? null
				: parseColors ? StringManager.parseColorsAndSymbols(parentSection.getStringList(configNode))
				: parentSection.getStringList(configNode);
	}

	public void refreshParentSection() {
		this.parentSection = configName == null ? ConfigManager.getConfig().getConfigurationSection(parentSectionName)
				: ConfigManager.getConfig(configName).getConfigurationSection(parentSectionName);
	}

	public ConfigurationSection getParentSection() {
		return parentSection;
	}

	public String getParentSectionName() {
		return parentSectionName;
	}

	/**
	 * Used for initialization and reloading settings values
	 */
	public abstract void setup();

}
