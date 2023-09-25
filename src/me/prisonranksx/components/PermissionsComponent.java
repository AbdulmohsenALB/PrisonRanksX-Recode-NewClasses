package me.prisonranksx.components;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import org.bukkit.entity.Player;

import me.prisonranksx.hooks.EZLuckPerms;
import me.prisonranksx.managers.PermissionsManager;

public class PermissionsComponent extends Component {

	private Set<String> permissionsAdditionList, permissionsDeletionList;
	private Map<String, Set<String>> worldPermissionsAdditionMap, worldPermissionsDeletionMap;

	public PermissionsComponent(Set<String> permissionsAdditionList, Set<String> permissionsDeletionList,
			Map<String, Set<String>> worldPermissionsAdditionMap,
			Map<String, Set<String>> worldPermissionsDeletionMap) {
		this.permissionsAdditionList = permissionsAdditionList;
		this.permissionsDeletionList = permissionsDeletionList;
		this.worldPermissionsAdditionMap = worldPermissionsAdditionMap;
		this.worldPermissionsDeletionMap = worldPermissionsDeletionMap;
	}

	@Nullable
	public static PermissionsComponent parsePermissions(@Nullable List<String> addPermissionsList,
			@Nullable List<String> delPermissionsList) {
		if ((addPermissionsList == null || addPermissionsList.isEmpty())
				&& (delPermissionsList == null || delPermissionsList.isEmpty()))
			return null;
		Set<String> permissionsAdditionList = new HashSet<>(), permissionsDeletionList = new HashSet<>();
		Map<String, Set<String>> worldPermissionsAdditionMap = new HashMap<>(),
				worldPermissionsDeletionMap = new HashMap<>(), contextualPermissionsAdditionMap = new HashMap<>(),
				contextualPermissionsDeletionMap = new HashMap<>();
		addPermissionsList.forEach(unfilteredPermissionLine -> {
			if (unfilteredPermissionLine.startsWith("[")) {
				String[] bracketsSplit = unfilteredPermissionLine.split("]");
				String betweenBrackets = bracketsSplit[0].replace("[", "").trim();
				if (betweenBrackets.startsWith("server=")) {
					String serverName = betweenBrackets.replace("server=", "");
					String permissionLine = bracketsSplit[1].trim();
					Set<String> contextualPermissionsAdditionList = contextualPermissionsAdditionMap.containsKey(
							serverName) ? contextualPermissionsAdditionMap.get(serverName) : new HashSet<>();
					contextualPermissionsAdditionList.add(permissionLine);
					contextualPermissionsAdditionMap.put(serverName, contextualPermissionsAdditionList);
				} else if (betweenBrackets.startsWith("world=")) {
					String worldName = betweenBrackets.replace("world=", "");
					String permissionLine = bracketsSplit[1].trim();
					Set<String> worldPermissionsAdditionList = worldPermissionsAdditionMap.containsKey(worldName)
							? worldPermissionsAdditionMap.get(worldName) : new HashSet<>();
					worldPermissionsAdditionList.add(permissionLine);
					worldPermissionsAdditionMap.put(worldName, worldPermissionsAdditionList);
				} else {
					String worldName = betweenBrackets;
					String permissionLine = bracketsSplit[1].trim();
					Set<String> worldPermissionsAdditionList = worldPermissionsAdditionMap.containsKey(worldName)
							? worldPermissionsAdditionMap.get(worldName) : new HashSet<>();
					worldPermissionsAdditionList.add(permissionLine);
					worldPermissionsAdditionMap.put(worldName, worldPermissionsAdditionList);
				}
			} else {
				permissionsAdditionList.add(unfilteredPermissionLine);
			}
		});
		delPermissionsList.forEach(unfilteredPermissionLine -> {
			if (unfilteredPermissionLine.startsWith("[")) {
				String[] bracketsSplit = unfilteredPermissionLine.split("]");
				String betweenBrackets = bracketsSplit[0].replace("[", "");
				if (betweenBrackets.startsWith("server=")) {
					String serverName = betweenBrackets.replace("server=", "");
					String permissionLine = bracketsSplit[1].replace(" ", "");
					Set<String> contextualPermissionsDeletionList = contextualPermissionsDeletionMap.containsKey(
							serverName) ? contextualPermissionsDeletionMap.get(serverName) : new HashSet<>();
					contextualPermissionsDeletionList.add(permissionLine);
					contextualPermissionsDeletionMap.put(serverName, contextualPermissionsDeletionList);
				} else if (betweenBrackets.startsWith("world=")) {
					String worldName = betweenBrackets.replace("world=", "");
					String permissionLine = bracketsSplit[1].replace(" ", "");
					Set<String> worldPermissionsDeletionList = worldPermissionsDeletionMap.containsKey(worldName)
							? worldPermissionsDeletionMap.get(worldName) : new HashSet<>();
					worldPermissionsDeletionList.add(permissionLine);
					worldPermissionsDeletionMap.put(worldName, worldPermissionsDeletionList);
				} else {
					String worldName = betweenBrackets;
					String permissionLine = bracketsSplit[1].replace(" ", "");
					Set<String> worldPermissionsDeletionList = worldPermissionsDeletionMap.containsKey(worldName)
							? worldPermissionsDeletionMap.get(worldName) : new HashSet<>();
					worldPermissionsDeletionList.add(permissionLine);
					worldPermissionsDeletionMap.put(worldName, worldPermissionsDeletionList);
				}
			} else {
				permissionsDeletionList.add(unfilteredPermissionLine);
			}
		});
		return contextualPermissionsAdditionMap.isEmpty() && contextualPermissionsDeletionMap.isEmpty()
				? worldPermissionsAdditionMap.isEmpty() && worldPermissionsDeletionMap.isEmpty()
						&& permissionsAdditionList.isEmpty() && permissionsDeletionList.isEmpty()
								? null
								: new PermissionsComponent(permissionsAdditionList, permissionsDeletionList,
										worldPermissionsAdditionMap, worldPermissionsDeletionMap)
				: worldPermissionsAdditionMap.isEmpty() && worldPermissionsDeletionMap.isEmpty()
						&& permissionsAdditionList.isEmpty() && permissionsDeletionList.isEmpty() ? null
				: new LuckPermsPermissionsComponent(permissionsAdditionList, permissionsDeletionList,
						worldPermissionsAdditionMap, worldPermissionsDeletionMap, contextualPermissionsAdditionMap,
						contextualPermissionsDeletionMap);
	}

	public Set<String> getAddPermissionCollection() {
		return permissionsAdditionList;
	}

	public Set<String> getDelPermissionCollection() {
		return permissionsDeletionList;
	}

	public Map<String, Set<String>> getAddWorldPermissionMap() {
		return worldPermissionsAdditionMap;
	}

	public Map<String, Set<String>> getDelWorldPermissionMap() {
		return worldPermissionsDeletionMap;
	}

	public boolean hasGlobalAddPerms() {
		return permissionsAdditionList != null;
	}

	public boolean hasGlobalDelPerms() {
		return permissionsDeletionList != null;
	}

	public boolean hasAddWorldPerms() {
		return worldPermissionsAdditionMap != null;
	}

	public boolean hasDelWorldPerms() {
		return worldPermissionsDeletionMap != null;
	}

	public void updatePermissions(Player player) {
		if (hasGlobalAddPerms()) PermissionsManager.givePermissions(player, permissionsAdditionList);
		if (hasGlobalDelPerms()) PermissionsManager.removePermissions(player, permissionsDeletionList);
		if (hasAddWorldPerms()) PermissionsManager.givePermissions(player, worldPermissionsAdditionMap);
		if (hasDelWorldPerms()) PermissionsManager.removePermissions(player, worldPermissionsDeletionMap);
	}

	public static class LuckPermsPermissionsComponent extends PermissionsComponent {

		private Map<String, Set<String>> contextualPermissionsAdditionMap, contextualPermissionsDeletionMap;

		public LuckPermsPermissionsComponent(Set<String> permissionsAdditionList, Set<String> permissionsDeletionList,
				Map<String, Set<String>> worldPermissionsAdditionMap,
				Map<String, Set<String>> worldPermissionsDeletionMap,
				Map<String, Set<String>> contextualPermissionsAdditionMap,
				Map<String, Set<String>> contextualPermissionsDeletionMap) {
			super(permissionsAdditionList, permissionsDeletionList, worldPermissionsAdditionMap,
					worldPermissionsDeletionMap);
			this.contextualPermissionsAdditionMap = contextualPermissionsAdditionMap;
			this.contextualPermissionsDeletionMap = contextualPermissionsDeletionMap;
		}

		@Override
		public Set<String> getAddPermissionCollection() {
			return super.permissionsAdditionList;
		}

		@Override
		public Set<String> getDelPermissionCollection() {
			return super.permissionsDeletionList;
		}

		@Override
		public Map<String, Set<String>> getAddWorldPermissionMap() {
			return super.worldPermissionsAdditionMap;
		}

		@Override
		public Map<String, Set<String>> getDelWorldPermissionMap() {
			return super.worldPermissionsDeletionMap;
		}

		public Map<String, Set<String>> getAddServerPermissionMap() {
			return contextualPermissionsAdditionMap;
		}

		public Map<String, Set<String>> getDelServerPermissionMap() {
			return contextualPermissionsDeletionMap;
		}

		@Override
		public boolean hasGlobalAddPerms() {
			return super.permissionsAdditionList != null;
		}

		@Override
		public boolean hasGlobalDelPerms() {
			return super.permissionsDeletionList != null;
		}

		@Override
		public boolean hasAddWorldPerms() {
			return super.worldPermissionsAdditionMap != null;
		}

		@Override
		public boolean hasDelWorldPerms() {
			return super.worldPermissionsDeletionMap != null;
		}

		public boolean hasAddServerPerms() {
			return contextualPermissionsAdditionMap != null;
		}

		public boolean hasDelServerPerms() {
			return contextualPermissionsDeletionMap != null;
		}

		@Override
		public void updatePermissions(Player player) {
			if (hasGlobalAddPerms()) PermissionsManager.givePermissions(player, super.permissionsAdditionList);
			if (hasGlobalDelPerms()) PermissionsManager.removePermissions(player, super.permissionsDeletionList);
			if (hasAddWorldPerms()) PermissionsManager.givePermissions(player, super.worldPermissionsAdditionMap);
			if (hasDelWorldPerms()) PermissionsManager.removePermissions(player, super.worldPermissionsDeletionMap);
			if (hasAddServerPerms()) contextualPermissionsAdditionMap.keySet()
					.forEach(serverName -> contextualPermissionsAdditionMap.get(serverName)
							.forEach(permission -> EZLuckPerms.givePermission(player.getUniqueId(), permission,
									serverName)));
			if (hasDelServerPerms()) contextualPermissionsDeletionMap.keySet()
					.forEach(serverName -> contextualPermissionsDeletionMap.get(serverName)
							.forEach(permission -> EZLuckPerms.removePermission(player.getUniqueId(), permission,
									serverName)));
		}

		@Override
		public boolean use(Player player) {
			updatePermissions(player);
			return true;
		}

	}

	@Override
	public boolean use(Player player) {
		updatePermissions(player);
		return true;
	}

	@Override
	public ComponentType getType() {
		return ComponentType.PERMISSIONS;
	}

}
