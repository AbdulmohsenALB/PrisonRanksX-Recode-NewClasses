package me.prisonranksx.commands;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.api.PRXAPI;
import me.prisonranksx.bukkitutils.Confirmation;
import me.prisonranksx.data.RebirthStorage;
import me.prisonranksx.holders.Rebirth;
import me.prisonranksx.holders.User;
import me.prisonranksx.reflections.UniqueId;
import me.prisonranksx.settings.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RebirthCommand extends PluginCommand {

	private PrisonRanksX plugin;
	private Confirmation.ConfirmationProcessor rebirthConfirmation;

	public static boolean isEnabled() {
		return CommandSetting.getSetting("rebirth", "enable");
	}

	public RebirthCommand(PrisonRanksX plugin) {
		super(CommandSetting.getStringSetting("rebirth", "name", "rebirth"));
		this.plugin = plugin;
		setLabel(getCommandSection().getString("label", "rebirth"));
		setDescription(getCommandSection().getString("description"));
		setUsage(getCommandSection().getString("usage"));
		setPermission(getCommandSection().getString("permission"));
		setPermissionMessage(getCommandSection().getString("permission-message"));
		setAliases(getCommandSection().getStringList("aliases"));
		this.rebirthConfirmation = Confirmation.setupConfirmationProcessor("rebirth",
				plugin.getGlobalSettings().getRebirthConfirmTimeOut());
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
			rebirth(p, true);
		}
		return true;
	}

	public void clearConfirmation(Player p) {
		rebirthConfirmation.clearConfirmation(p.getName());
	}

	public void rebirth(Player p, boolean checkConfirmation) {
		if (plugin.getGlobalSettings().isWorldIncluded(p.getWorld())) return;
		if (checkConfirmation && plugin.getGlobalSettings().isRebirthConfirm()) {
			User user = plugin.getUserController().getUser(UniqueId.getUUID(p));
			Rebirth nextRebirth = user.hasRebirth() ? RebirthStorage.getRebirth(user.getRebirth().getNextRebirthName())
					: RebirthStorage.getRebirth(1);
			rebirthConfirmation.getState(p.getName())
					.ifConfirmed(() -> plugin.getRebirthExecutor().rebirth(p))
					.orElse(() -> {
						if (PRXAPI.isLastRebirth(p)) {
							// If player is at the last rebirth, execute rebirth just to send the last rebirth message.
							plugin.getRebirthExecutor().rebirth(p);
						} else {
							Messages.sendMessage(p, Messages.getRebirthConfirm(),
									s -> s.replace("%nextrebirth%", nextRebirth.getName())
											.replace("%nextrebirth_display%", nextRebirth.getDisplayName()));
						}
					});
		} else {
			plugin.getRebirthExecutor().rebirth(p);
		}
	}

}
