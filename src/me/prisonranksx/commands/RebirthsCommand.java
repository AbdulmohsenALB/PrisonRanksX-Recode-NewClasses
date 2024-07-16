package me.prisonranksx.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.data.RebirthStorage;
import me.prisonranksx.managers.EconomyManager;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.utils.NumParser;

public class RebirthsCommand extends PluginCommand {

	private PrisonRanksX plugin;

	public static boolean isEnabled() {
		return CommandSetting.getSetting("rebirths", "enable");
	}

	public RebirthsCommand(PrisonRanksX plugin) {
		super(CommandSetting.getStringSetting("rebirths", "name", "rebirths"));
		this.plugin = plugin;
		setLabel(getCommandSection().getString("label", "rebirths"));
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
			RebirthStorage.getRebirths()
					.forEach(rebirth -> sender.sendMessage(StringManager.parseColors("&cRebirth: &f" + rebirth.getName()
							+ " &ccost: " + EconomyManager.shortcutFormat(rebirth.getCost()))));
			return true;
		}
		if (plugin.getGlobalSettings().isGuiRebirthList()) {
			if (args.length == 0)
				plugin.getRebirthsGUIList().openGUI((Player) sender);
			else
				plugin.getRebirthsGUIList().openGUI((Player) sender, NumParser.asInt(args[0], 1));
			return true;
		}
		plugin.getRebirthsTextList().send(sender, args.length == 0 ? "1" : args[0]);
		return true;
	}
}
