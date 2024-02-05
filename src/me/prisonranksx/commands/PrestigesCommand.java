package me.prisonranksx.commands;

import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.data.PrestigeStorage;
import me.prisonranksx.holders.Prestige;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.utils.IntParser;

public class PrestigesCommand extends PluginCommand {

	private PrisonRanksX plugin;

	public static boolean isEnabled() {
		return CommandSetting.getSetting("prestiges", "enable");
	}

	public PrestigesCommand(PrisonRanksX plugin) {
		super(CommandSetting.getStringSetting("prestiges", "name", "prestiges"));
		this.plugin = plugin;
		setLabel(getCommandSection().getString("label", "prestiges"));
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

			Set<Prestige> prestiges = Sets.newLinkedHashSet(PrestigeStorage.getPrestiges());
			prestiges.forEach(prestige -> {
				sender.sendMessage(StringManager.parseColors("&cPrestige: &f" + prestige.getName()));
			});

			return true;
		}
		if (plugin.getGlobalSettings().isGuiPrestigeList()) {
			if (args.length == 0)
				plugin.getPrestigesGUIList().openGUI((Player) sender);
			else
				plugin.getPrestigesGUIList().openGUI((Player) sender, IntParser.asInt(args[0], 1));
			return true;
		}
		plugin.getPrestigesTextList().send(sender, args.length == 0 ? "1" : args[0]);
		return true;
	}
}
