package me.prisonranksx.bukkitutils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.TextComponent;

/**
 * Stupid class for sending messages with only one variable to replace using
 * TextComponents for crazy performance gain.
 */
public class SingleReplacementMessage {

	private String message;
	private String[] messages;
	private TextComponentSetter textComponentSetter;

	/**
	 * @param fullString whole string with the color codes
	 * @param stringPart a word/part of a string to get color codes of
	 * @return all color codes that come before this string part or empty string if
	 *         no colors were found
	 */
	private static String getPartColors(String fullString, String stringPart) {
		StringBuilder colorStringBuilder = new StringBuilder();
		boolean firstColorCodeFound = false;
		int stringPartIndex = fullString.indexOf(stringPart);
		if (stringPartIndex == -1) return colorStringBuilder.toString();
		if (fullString.indexOf("&") == -1) return colorStringBuilder.toString();
		for (int i = stringPartIndex; i > -1; i--) {
			char indexChar = fullString.charAt(i);
			if (indexChar == '&' || fullString.charAt(i - 1) == '&') {
				if (indexChar == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(fullString.charAt(i + 1)) != -1) {
					colorStringBuilder.insert(0, String.valueOf(fullString.charAt(i)) + fullString.charAt(i + 1));
					firstColorCodeFound = true;
				}
			} else {
				if (firstColorCodeFound) break;
			}
		}
		return colorStringBuilder.toString();
	}

	public SingleReplacementMessage(String message, String replacementVariable) {
		this.message = message;
		String[] split = message.split(replacementVariable);
		messages = split;
		messages[0] = messages[0] + getPartColors(message, replacementVariable);
		textComponentSetter = messages.length == 2 ? new TwoTextComponentSetter(messages)
				: new SingleTextComponentSetter(messages);
	}

	public void send(Player player, String replacement) {
		player.spigot().sendMessage(textComponentSetter.setAndGet(replacement));
	}

	public void send(CommandSender playerSender, String replacement) {
		send((Player) playerSender, replacement);
	}

	public String getOriginalMessage() {
		return message;
	}

	private static abstract class TextComponentSetter {

		String[] messages;

		public TextComponentSetter(String[] messages) {
			this.messages = messages;
		}

		public abstract TextComponent setAndGet(String replacement);

	}

	private static class SingleTextComponentSetter extends TextComponentSetter {

		private TextComponent textComponent;

		public SingleTextComponentSetter(String[] messages) {
			super(messages);
			textComponent = new TextComponent();
		}

		public TextComponent setAndGet(String replacement) {
			textComponent.setText(messages[0] + replacement);
			return textComponent;
		}

	}

	private static class TwoTextComponentSetter extends TextComponentSetter {

		private TextComponent textComponent;

		public TwoTextComponentSetter(String[] messages) {
			super(messages);
			textComponent = new TextComponent();
		}

		public TextComponent setAndGet(String replacement) {
			textComponent.setText(messages[0] + replacement + messages[1]);
			return textComponent;
		}

	}

}
