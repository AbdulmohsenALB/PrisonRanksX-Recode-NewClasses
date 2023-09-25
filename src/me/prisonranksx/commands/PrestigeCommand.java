package me.prisonranksx.commands;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.executors.PrestigeExecutor;
import me.prisonranksx.reflections.UniqueId;
import me.prisonranksx.settings.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PrestigeCommand extends PluginCommand {

    private PrisonRanksX plugin;

    public static boolean isEnabled() {
        return CommandSetting.getSetting("prestige", "enable");
    }

    public PrestigeCommand(PrisonRanksX plugin) {
        super(CommandSetting.getStringSetting("prestige", "name", "prestige"));
        this.plugin = plugin;
        setLabel(getCommandSection().getString("label", "prestige"));
        setDescription(getCommandSection().getString("description"));
        setUsage(getCommandSection().getString("usage"));
        setPermission(getCommandSection().getString("permission"));
        setPermissionMessage(getCommandSection().getString("permission-message"));
        setAliases(getCommandSection().getStringList("aliases"));
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!testPermission(sender)) return true;
        if (!(sender instanceof Player)) {
            Messages.sendMessage(sender, Messages.getPlayerOnlyCommand());
            return true;
        }
        Player p = (Player) sender;
        if (args.length == 0) {
            if (plugin.getGlobalSettings().isWorldIncluded(p.getWorld())) return true;
            plugin.getPrestigeExecutor().prestige(p);
        } else if (args.length == 1) {
            if (args[0].equals("gui")) {
                // GUI
                return true;
            } else if (args[0].equals("max")) {
                if (PrestigeExecutor.isMaxPrestiging(p)) {
                    plugin.getPrestigeExecutor().breakMaxPrestige(UniqueId.getUUID(p));
                } else {
                    plugin.getPrestigeExecutor().maxPrestige(p);
                }
                return true;
            } else if (args[0].equals("maxall")) {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (PrestigeExecutor.isMaxPrestiging(player)) {
                        plugin.getPrestigeExecutor().breakMaxPrestige(UniqueId.getUUID(player));
                    } else {
                        plugin.getPrestigeExecutor().maxPrestige(player);
                    }
                });
            } else if (args[0].startsWith("list")) {
                // LIST
                return true;
            }
        }
        return true;
    }

}
