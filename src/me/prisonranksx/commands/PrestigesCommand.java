package me.prisonranksx.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.data.PrestigeStorage;
import me.prisonranksx.managers.EconomyManager;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.utils.NumParser;

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
			if (PrestigeStorage.getHandler().isInfinite()) {
				PrestigeStorage.InfinitePrestigeStorage ips = (PrestigeStorage.InfinitePrestigeStorage) PrestigeStorage
						.getHandler()
						.getStorage();
				sender.sendMessage(StringManager.parseColors("&cFirst prestige: &f" + ips.getFirstPrestigeName()
						+ " &cCost: &f" + EconomyManager.commaFormatWithDecimals(ips.getPrestige(1).getCost())
						+ "\n&cLast prestige: &f" + ips.getLastPrestigeName() + " &cCost: &f"
						+ EconomyManager.commaFormatWithDecimals(ips.getPrestige(ips.getLastPrestigeName()).getCost())
						+ "\n&cPrestige default display: &f" + ips.getPrestige(1).getDisplayName()
						+ "\n&cPrestige cost expression: &f" + ips.getCostExpression() + "\n&cLong ranges: &f"
						+ ips.getRegisteredLongRanges() + "\n&cDisplays: " + ips.getRangedDisplays()));
				return true;
			}
			PrestigeStorage.getPrestiges()
					.forEach(prestige -> sender.sendMessage(StringManager.parseColors("&cPrestige: &f"
							+ prestige.getName() + " &ccost: " + EconomyManager.shortcutFormat(prestige.getCost()))));
			return true;
		}
		if (plugin.getGlobalSettings().isGuiPrestigeList()) {
			if (args.length == 0)
				plugin.getPrestigesGUIList().openGUI((Player) sender);
			else
				plugin.getPrestigesGUIList().openGUI((Player) sender, NumParser.asInt(args[0], 1));
			return true;
		}
		plugin.getPrestigesTextList().send(sender, args.length == 0 ? "1" : args[0]);
		return true;
	}
}
