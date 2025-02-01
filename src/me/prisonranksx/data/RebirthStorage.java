package me.prisonranksx.data;

import me.prisonranksx.components.*;
import me.prisonranksx.holders.Rebirth;
import me.prisonranksx.managers.ConfigManager;
import me.prisonranksx.managers.StringManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class RebirthStorage {

    private static final Map<String, Rebirth> REBIRTHS = new HashMap<>();
    private static final Map<String, String> ALTERNATIVE_NAMES = new HashMap<>();
    private static final List<String> REBIRTH_NAMES = new ArrayList<>();
    private static String firstRebirthName;
    private static String lastRebirthName;
    private static int lastRebirthNumber;

    @SuppressWarnings("unchecked")
    public static void loadRebirths() {
        REBIRTHS.clear();
        ALTERNATIVE_NAMES.clear();
        REBIRTH_NAMES.clear();
        firstRebirthName = null;
        lastRebirthName = null;
        lastRebirthNumber = 0;
        FileConfiguration rebirthsConfig = ConfigManager.getRebirthsConfig();
        ConfigurationSection rebirthSection = rebirthsConfig.getConfigurationSection("Rebirths");
        for (String rebirthName : rebirthSection.getKeys(false)) {
            ConfigurationSection current = rebirthSection.getConfigurationSection(rebirthName);
            Rebirth rebirth = new Rebirth(rebirthName,
                    StringManager.parseColorsAndSymbols(
                            ConfigManager.getPossible(current, String.class, "display-name", "display", "prefix")),
                    ConfigManager.getPossible(current, "next-rebirth", "nextrebirth"),
                    ConfigManager.getPossibleDouble(current, "cost", "price"),
                    StringManager.parseColorsAndSymbols(rebirthSection.getStringList("broadcast")),
                    StringManager.parseColorsAndSymbols(
                            ConfigManager.getPossible(current, List.class, "message", "msg", "messages")),
                    CommandsComponent.parseCommands(
                            ConfigManager.getPossible(current, "commands", "executecmds", "command", "cmd")),
                    RequirementsComponent.parseRequirements(
                            ConfigManager.getPossible(current, "requirements", "requirement", "require", "requires")),
                    ActionBarComponent.parseActionBar(ConfigManager.getPossible(current, "action-bar", "actionbar")),
                    PermissionsComponent.parsePermissions(
                            ConfigManager.getPossible(current, "add-permissions", "addpermission", "add-permission",
                                    "addperm", "add-perm", "add-perms"),
                            ConfigManager.getPossible(current, "delete-permissions", "delpermission", "del-permission",
                                    "delete-permission", "remove-permissions", "remove-permission", "del-perms")),
                    FireworkComponent.parseFirework(
                            ConfigManager.getPossible(current, "firework", "firework-builder", "fireworks", "fire-work")),
                    RandomCommandsComponent.parseRandomCommands(ConfigManager.getPossible(current, "random-commands",
                            "randomcmds", "random-command", "randomcmd", "random-cmds", "random-cmd")),
                    StringManager.parseColorsAndSymbols(ConfigManager.getPossible(current, List.class,
                            "requirements-fail-message", "custom-requirement-message", "custom-requirements-message",
                            "requirement-fail-message", "requirements-fail-messages", "requirements-message",
                            "requirement-message")),
                    ConfigManager.getPossibleDouble(current, "cost-increase", "prestige_cost_increase_percentage",
                            "cost-increase-percentage", "cost_increase", "prestige-cost-increase-percentage"),
                    ConfigManager.getPossibleLong(current, "required-prestiges", "prestiges", "required-prestige"));
            lastRebirthNumber += 1;
            REBIRTHS.put(rebirthName, rebirth);
            ALTERNATIVE_NAMES.put(rebirthName.toLowerCase(), rebirthName);
            REBIRTH_NAMES.add(rebirthName);
            if (firstRebirthName == null) firstRebirthName = rebirthName;
        }
        lastRebirthName = REBIRTH_NAMES.get(lastRebirthNumber - 1);
    }

    public static boolean rebirthExists(String name) {
        return getRebirth(name) != null;
    }

    public static boolean rebirthExists(int number) {
        return number > 0 && number <= lastRebirthNumber;
    }

    public static Rebirth getRebirth(String name) {
        return REBIRTHS.get(name);
    }

    public static Rebirth getRebirth(int number) {
        return getRebirth(REBIRTH_NAMES.get(number - 1));
    }

    public static String getFirstRebirthName() {
        return firstRebirthName;
    }

    public static int getFirstRebirthAsInt() {
        return REBIRTH_NAMES.indexOf(firstRebirthName) + 1;
    }

    public static String getLastRebirthName() {
        return lastRebirthName;
    }

    public static int getLastRebirthAsInt() {
        return lastRebirthNumber;
    }

    public static Set<String> getRebirthNames() {
        return REBIRTHS.keySet();
    }

    public static Collection<Rebirth> getRebirths() {
        return REBIRTHS.values();
    }

    public static String matchRebirthName(String name) {
        return ALTERNATIVE_NAMES.get(name.toLowerCase());
    }

    public static Rebirth matchRebirth(String name) {
        return REBIRTHS.get(matchRebirthName(name));
    }

}
