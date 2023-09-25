package me.prisonranksx.components;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import me.prisonranksx.bukkitutils.FireworkColor;
import me.prisonranksx.managers.ConfigManager;

public class FireworkComponent extends Component {

	private int power;
	private List<FireworkEffect> fireworkEffects;

	public FireworkComponent() {
		this(1, null);
	}

	public FireworkComponent(int power) {
		this(power, null);
	}

	public FireworkComponent(List<FireworkEffect> fireworkEffects) {
		this(1, fireworkEffects);
	}

	public FireworkComponent(int power, List<FireworkEffect> fireworkEffects) {
		this.power = power;
		this.fireworkEffects = fireworkEffects;
	}

	@Nullable
	public static FireworkComponent parseFirework(@Nullable ConfigurationSection fireworkSection) {
		if (fireworkSection == null) return null;
		boolean flicker = fireworkSection.getBoolean("flicker");
		boolean trail = fireworkSection.getBoolean("trail");
		List<Color> color = new ArrayList<>();
		ConfigManager.getListOrElse(fireworkSection, String.class, "colors", "color")
				.forEach(colorLine -> color.add(FireworkColor.parseColor(colorLine)));
		List<Color> fade = new ArrayList<>();
		ConfigManager.getListOrElse(fireworkSection, String.class, "fade", "fades")
				.forEach(fadeLine -> fade.add(FireworkColor.parseColor(fadeLine)));
		int power = fireworkSection.getInt("power");
		List<FireworkEffect> fireworkEffects = new ArrayList<>();
		ConfigManager.getListOrElse(fireworkSection, String.class, "effects", "effect")
				.forEach(effect -> fireworkEffects.add(FireworkEffect.builder()
						.with(Type.valueOf(effect.replace("SPARKLE", "BURST").replace("STARS", "STAR")))
						.flicker(flicker)
						.trail(trail)
						.withColor(color)
						.withFade(fade)
						.build()));
		return new FireworkComponent(power, fireworkEffects);
	}

	public void setPower(int power) {
		this.power = power;
	}

	public int getPower() {
		return power;
	}

	public void setFireworkEffects(List<FireworkEffect> fireworkEffects) {
		this.fireworkEffects = fireworkEffects;
	}

	public List<FireworkEffect> getFireworkEffects() {
		return fireworkEffects;
	}

	public void spawnFirework(Player player) {
		Firework firework = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
		FireworkMeta meta = firework.getFireworkMeta();
		meta.addEffects(fireworkEffects);
		meta.setPower(power);
		firework.setFireworkMeta(meta);
	}

	public void spawnFirework(Location location) {
		Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
		FireworkMeta meta = firework.getFireworkMeta();
		meta.addEffects(fireworkEffects);
		meta.setPower(power);
		firework.setFireworkMeta(meta);
	}

	@Override
	public boolean use(Player player) {
		spawnFirework(player);
		return true;
	}

	@Override
	public ComponentType getType() {
		return ComponentType.FIREWORK;
	}

}
