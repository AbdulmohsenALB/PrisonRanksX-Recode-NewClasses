package me.prisonranksx.data;

import org.jetbrains.annotations.Nullable;

public enum UserControllerType {
	YAML,
	YAML_PER_USER,
	MYSQL;

	@Nullable
	public static UserControllerType matchType(String name) {
		switch (name.toUpperCase()) {
			case "YAML_PER_USER":
			case "YAMLPERUSER":
				return UserControllerType.YAML_PER_USER;
			case "YAML":
			case "YML":
				return UserControllerType.YAML;
			case "MYSQL":
			case "SQL":
				return UserControllerType.MYSQL;
			default:
				return null;
		}
	}
}
