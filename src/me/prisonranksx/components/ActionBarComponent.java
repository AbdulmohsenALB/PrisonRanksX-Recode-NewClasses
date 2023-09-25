package me.prisonranksx.components;

import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import me.prisonranksx.managers.ActionBarManager;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.settings.GlobalSettings;

public class ActionBarComponent extends Component {

	private IActionBarSender actionBarSender;

	public ActionBarComponent(List<String> actionBarMessages) {
		this(actionBarMessages, 20);
	}

	/**
	 * Initializes an animated action bar component
	 * 
	 * @param actionBarMessages action bar messages to be sent
	 * @param interval          speed of animations
	 */
	public ActionBarComponent(List<String> actionBarMessages, int interval) {
		this.actionBarSender = new AnimatedActionBarSender(actionBarMessages, interval);
	}

	/**
	 * Initializes a static action bar component
	 * 
	 * @param message action bar message to be sent
	 */
	public ActionBarComponent(String message) {
		this.actionBarSender = new StaticActionBarSender(message);
	}

	@Nullable
	public static ActionBarComponent parseActionBar(@Nullable List<String> actionBarMessages, int interval) {
		if (actionBarMessages == null || actionBarMessages.isEmpty() || !GlobalSettings.SUPPORTS_ACTION_BAR)
			return null;
		if (actionBarMessages.size() == 1)
			return new ActionBarComponent(actionBarMessages.get(0));
		else
			return new ActionBarComponent(actionBarMessages, interval < 0 ? 20 : interval);
	}

	@Nullable
	public static ActionBarComponent parseActionBar(@Nullable ConfigurationSection actionBarSection) {
		if (actionBarSection == null || actionBarSection.getKeys(false).isEmpty()
				|| !GlobalSettings.SUPPORTS_ACTION_BAR)
			return null;
		List<String> coloredText = StringManager.parseColorsAndSymbols(actionBarSection.getStringList("text"));
		if (coloredText.size() > 1)
			return new ActionBarComponent(coloredText, actionBarSection.getInt("interval", 20));
		else
			return new ActionBarComponent(coloredText.get(0));
	}

	public void sendActionBar(Player player) {
		actionBarSender.send(player);
	}

	public void sendActionBar(Player player, Function<String, String> function) {
		actionBarSender.send(player);
	}

	public IActionBarSender getActionBarSender() {
		return actionBarSender;
	}

	private static interface IActionBarSender {

		void send(Player player);

		void send(Player player, Function<String, String> function);

		int getInterval();

	}

	public static class AnimatedActionBarSender implements IActionBarSender {

		private List<String> messages;
		private int interval;

		public AnimatedActionBarSender(List<String> messages, int interval) {
			this.messages = messages;
			this.interval = interval;
		}

		@Override
		public void send(Player player) {
			ActionBarManager.sendAnimated(player, messages, interval);
		}

		@Override
		public void send(Player player, Function<String, String> function) {
			ActionBarManager.sendAnimated(player, messages, interval, function);
		}

		@Override
		public int getInterval() {
			return interval;
		}

		public List<String> getMessages() {
			return messages;
		}

	}

	public static class StaticActionBarSender implements IActionBarSender {

		private String message;

		public StaticActionBarSender(String message) {
			this.message = message;
		}

		@Override
		public void send(Player player) {
			ActionBarManager.send(player, message);
		}

		@Override
		public void send(Player player, Function<String, String> function) {
			ActionBarManager.send(player, function.apply(message));
		}

		@Override
		public int getInterval() {
			return -1;
		}

		public String getMessage() {
			return message;
		}

	}

	@Override
	public boolean use(Player player) {
		actionBarSender.send(player);
		return true;
	}

	@Override
	public ComponentType getType() {
		return ComponentType.ACTION_BAR;
	}

}
