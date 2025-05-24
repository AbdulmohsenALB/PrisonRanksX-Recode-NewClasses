package me.prisonranksx.holders;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.data.PrestigeStorage;
import me.prisonranksx.data.RankStorage;
import me.prisonranksx.data.RebirthStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Holds a player's levels (rank, prestige, rebirth) information in memory.
 */
public class User {

	private UUID uniqueId;
	private String name, rankName, pathName, prestigeName, rebirthName;

	public User(UUID uniqueId, String name) {
		this.uniqueId = uniqueId;
		this.name = name;
	}

	@NotNull
	public UUID getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(@NotNull UUID uniqueId) {
		this.uniqueId = uniqueId;
	}

	@NotNull
	public String getName() {
		return name;
	}

	public void setName(@NotNull String name) {
		this.name = name;
	}

	@Nullable
	public String getPrestigeName() {
		return prestigeName;
	}

	/**
	 * Retrieves prestige object of player current prestige name from prestige
	 * storage.
	 *
	 * @return player prestige or null if they don't have a prestige.
	 */
	@Nullable
	public Prestige getPrestige() {
		return PrestigeStorage.getPrestige(prestigeName);
	}

	/**
	 * Changes player current prestige name in memory, setting it to null will
	 * remove
	 * player's current prestige.
	 *
	 * @param prestigeName to change to. For infinite prestige it will be just
	 *                     numbers "1", "43", etc...
	 */
	public void setPrestigeName(@Nullable String prestigeName) {
		this.prestigeName = prestigeName;
		forceSave();
	}

	public void forceSave() {
		if (PrisonRanksX.getInstance().isForceSave())
			PrisonRanksX.getInstance().forceSave(this);
	}

	@Nullable
	public String getRebirthName() {
		return rebirthName;
	}

	/**
	 * Retrieves rebirth object of player current rebirth name from rebirth
	 * storage.
	 *
	 * @return player rebirth or null if they don't have a rebirth.
	 */
	@Nullable
	public Rebirth getRebirth() {
		return RebirthStorage.getRebirth(rebirthName);
	}

	/**
	 * Changes player current rebirth name in memory, setting it to null will remove
	 * player's current rebirth.
	 *
	 * @param rebirthName to change to.
	 */
	public void setRebirthName(@Nullable String rebirthName) {
		this.rebirthName = rebirthName;
		forceSave();
	}

	@Nullable
	public String getRankName() {
		return rankName;
	}

	/**
	 * Retrieves rank object of player current rank name from rank storage.
	 *
	 * @return player rank or null if they don't have a rank.
	 */
	@Nullable
	public Rank getRank() {
		return RankStorage.getRank(rankName, pathName);
	}

	/**
	 * Changes player current rank name in memory, setting it to null will remove
	 * player's current rank.
	 *
	 * @param rankName to change to.
	 */
	public void setRankName(@Nullable String rankName) {
		this.rankName = rankName;
		forceSave();
	}

	@Nullable
	public String getPathName() {
		return pathName;
	}

	public void setPathName(@Nullable String pathName) {
		this.pathName = pathName;
	}

	public void setRankAndPathName(@Nullable String rankName, @Nullable String pathName) {
		this.rankName = rankName;
		this.pathName = pathName;
		forceSave();
	}

	public boolean hasRank() {
		return rankName != null;
	}

	public boolean hasPath() {
		return pathName != null;
	}

	public boolean hasPrestige() {
		return prestigeName != null;
	}

	public boolean hasRebirth() {
		return rebirthName != null;
	}

	/**
	 * Gets player from this user's name
	 *
	 * @return user as bukkit player, null if player is not online.
	 */
	@Nullable
	public Player getPlayer() {
		return Bukkit.getPlayer(name);
	}

}
