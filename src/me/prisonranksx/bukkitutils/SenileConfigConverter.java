package me.prisonranksx.bukkitutils;

import com.google.common.collect.Lists;
import me.prisonranksx.utils.FileBackup;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

public class SenileConfigConverter {

	private Map<Entry<String, String>, String> conversions;

	private FileConfiguration oldConfig;
	private File oldFile;
	private boolean old;

	public SenileConfigConverter(String filePath) {
		this(new File(filePath));
	}

	public SenileConfigConverter(File file) {
		oldFile = file;
		this.oldConfig = file.exists() ? YamlConfiguration.loadConfiguration(file) : null;
		this.conversions = new LinkedHashMap<>();
	}

	public boolean checkSenility(Predicate<FileConfiguration> oldConfigPredicate) {
		return old = oldConfig != null && oldConfigPredicate.test(oldConfig);
	}

	public FileConfiguration getSenileConfig() {
		return oldConfig;
	}

	public boolean isSenile() {
		return old;
	}

	public void addFieldRename(String oldFieldName, String newFieldName) {
		if (oldFieldName != null && newFieldName != null && oldConfig.contains(oldFieldName))
			conversions.put(new AbstractMap.SimpleEntry<>(oldFieldName, newFieldName), "field-rename");
	}

	public void addConfigurationSectionRename(String oldSectionName, String newSectionName) {
		if (oldSectionName != null && newSectionName != null
				&& oldConfig.getConfigurationSection(oldSectionName) != null)
			conversions.put(new AbstractMap.SimpleEntry<>(oldSectionName, newSectionName), "section-rename");
	}

	public void addStringUpdate(String fieldName, String newValue) {
		conversions.put(new AbstractMap.SimpleEntry<>(fieldName, newValue), "string-update");
	}

	public SenileConfigConverter addAnyStringReplaceUpdate(String sectionName, String contains, String replaceWith) {
		conversions.put(new AbstractMap.SimpleEntry<>(sectionName, contains + "$$" + replaceWith),
				"string-replace-update");
		return this;
	}

	public void addStringListUpdate(String fieldName, List<String> newValue) {
		conversions.put(new AbstractMap.SimpleEntry<String, String>(fieldName, String.join("$$", newValue)),
				"list-update");
	}

	public <T> void addConditionToBoolean(T t, Predicate<T> condition, String booleanField) {
		if (condition != null && t != null && booleanField != null) {
			boolean check = condition.test(t);
			if (check) conversions.put(new AbstractMap.SimpleEntry<>(booleanField, Boolean.toString(check)),
					"condition-boolean");
		}
	}

	/**
	 * @param fields (oldField1, newField1, oldField2, newField2, etc...)
	 */
	public void addFieldRename(String... fields) {
		for (int i = 1; i < fields.length; i += 2) addFieldRename(fields[i - 1], fields[i]);
	}

	/**
	 * @param fields (oldField1, newField1, oldField2, newField2, etc...)
	 */
	public void addFieldRename(boolean useNewSectionName, String sectionName, @Nullable String newSectionName,
							   String... fields) {
		String updatedSection = useNewSectionName ? newSectionName : sectionName;
		for (int i = 1; i < fields.length; i += 2)
			addFieldRename(sectionName + "." + fields[i - 1], updatedSection + "." + fields[i]);
	}

	private List<String> emptify(List<String> stringList) {
		return stringList == null || stringList.isEmpty() || stringList.get(0).isEmpty() ? Lists.newArrayList()
				: stringList;
	}

	public void process(FileConfiguration newConfig) {
		if (old) {
			FileBackup.fromFile(oldFile);
			conversions.forEach((nodes, conversionName) -> {
				switch (conversionName) {
					case "field-rename":
						newConfig.set(nodes.getValue(), oldConfig.get(nodes.getKey()));
						System.out.println("[FIELD] " + nodes.getKey() + " -> " + nodes.getValue());
						break;
					case "section-rename":
						ConfigurationSection oldSection = oldConfig.getConfigurationSection(nodes.getKey());
						oldSection.getKeys(false)
								.forEach(field -> newConfig.getConfigurationSection(nodes.getValue())
										.set(field, oldSection.get(field)));
						System.out.println("[SECTION] " + nodes.getKey() + " -> " + nodes.getValue());
						break;
					case "condition-boolean":
						newConfig.set(nodes.getKey(), Boolean.valueOf(nodes.getValue()));
						System.out.println("[CONDITION] " + nodes.getKey() + " -> " + nodes.getValue());
						break;
					case "string-update":
						newConfig.set(nodes.getKey(), nodes.getValue());
						break;
					case "list-update":
						newConfig.set(nodes.getKey(), emptify(Lists.newArrayList(nodes.getValue().split("$$"))));
						break;
					case "string-replace-update":
						ConfigurationSection section = nodes.getKey() == null ? newConfig
								: newConfig.getConfigurationSection(nodes.getKey());
						String contains = nodes.getValue().split("$$")[0];
						String replaceWith = nodes.getValue().split("$$")[1];
						section.getKeys(false).forEach(field -> {
							if (section.isString(field)) if (section.getString(field).contains(contains))
								section.set(field, field.replace(contains, replaceWith));
						});
					default:
						System.out.println("Unknown conversion: " + conversionName);
						break;
				}
			});
		}
	}

}
