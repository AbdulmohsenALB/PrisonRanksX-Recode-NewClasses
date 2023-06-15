package me.prisonranksx.holders;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class User {

	private UUID uniqueId;
	private String name, rankName, pathName, prestigeName, rebirthName;

	public User(UUID uniqueId, String name) {
		this.uniqueId = uniqueId;
		this.name = name;
	}

	public UUID getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(UUID uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPrestigeName() {
		return prestigeName;
	}

	public void setPrestigeName(String prestigeName) {
		this.prestigeName = prestigeName;
	}

	public String getRebirthName() {
		return rebirthName;
	}

	public void setRebirthName(String rebirthName) {
		this.rebirthName = rebirthName;
	}

	public String getRankName() {
		return rankName;
	}

	public void setRankName(String rankName) {
		this.rankName = rankName;
	}

	public String getPathName() {
		return pathName;
	}

	public void setPathName(String pathName) {
		this.pathName = pathName;
	}

	public void setRankAndPathName(String rankName, String pathName) {
		this.rankName = rankName;
		this.pathName = pathName;
	}

	public Player getPlayer() {
		return Bukkit.getPlayer(name);
	}

}
