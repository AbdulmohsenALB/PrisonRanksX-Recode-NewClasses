package me.prisonranksx.commands;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.api.PRXAPI;
import me.prisonranksx.bukkitutils.Confirmation;
import me.prisonranksx.data.PrestigeStorage;
import me.prisonranksx.holders.Prestige;
import me.prisonranksx.holders.User;
import me.prisonranksx.reflections.UniqueId;
import me.prisonranksx.settings.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PrestigeCommand extends PluginCommand {

	private PrisonRanksX plugin;
	private Confirmation.ConfirmationProcessor prestigeConfirmation;

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
		prestigeConfirmation = Confirmation.setupConfirmationProcessor("prestige",
				plugin.getGlobalSettings().getPrestigeConfirmTimeOut());
	}

	public void clearConfirmation(Player p) {
		prestigeConfirmation.clearConfirmation(p.getName());
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
			prestige(p, true);
		}
		return true;
	}

	public void prestige(Player p, boolean checkConfirmation) {
		if (plugin.getGlobalSettings().isWorldIncluded(p.getWorld())) return;
		if (checkConfirmation && plugin.getGlobalSettings().isPrestigeConfirm()) {
			User user = plugin.getUserController().getUser(UniqueId.getUUID(p));
			Prestige nextPrestige = user.hasPrestige()
					? PrestigeStorage.getPrestige(user.getPrestige().getNextPrestigeName())
					: PrestigeStorage.getPrestige(1);
			prestigeConfirmation.getState(p.getName())
					.ifConfirmed(() -> plugin.getPrestigeExecutor().prestige(p))
					.orElse(() -> {
						if (PRXAPI.isLastPrestige(p)) {
							// If the player is already at the last prestige, just prestige for last prestige message to appear.
							plugin.getPrestigeExecutor().prestige(p);
						} else {
							Messages.sendMessage(p, Messages.getPrestigeConfirm(),
									s -> s.replace("%nextprestige%", nextPrestige.getName())
											.replace("%nextprestige_display%", nextPrestige.getDisplayName()));
						}
					});
		} else {
			plugin.getPrestigeExecutor().prestige(p);
		}
	}

}
