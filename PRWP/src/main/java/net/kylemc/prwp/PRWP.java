package net.kylemc.prwp;

import com.sk89q.wepif.PermissionsProvider;
import net.kylemc.prwp.commands.Commands;
import net.kylemc.prwp.events.PermissionsEvents;
import net.kylemc.prwp.events.ReloadEvents;
import net.kylemc.prwp.files.PermFileHandler;
import net.kylemc.prwp.utils.GetPermissions;
import net.kylemc.prwp.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Set;
import java.util.UUID;

public final class PRWP extends JavaPlugin implements PermissionsProvider
{
	private PermFileHandler pfh;
	private boolean isEnabled = false;
	Commands pc;

	@Override
	public void onEnable()
	{
		Utils.setdFolder(getDataFolder());
		Utils.loadWorlds();

		File dFolder = Utils.getdFolder();
		pfh = new PermFileHandler(dFolder);

		if (Utils.checkGroups()) {
			getLogger().warning("Permissions not enabled, no groups/mods specified!");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		isEnabled = true;

		Utils.setYamlConfigurations(pfh);
		Utils.initRanks();

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PermissionsEvents(), this);
		pm.registerEvents(new ReloadEvents(this), this);

		pc = new Commands(this);
		getCommand("permissions").setExecutor(pc);
		getCommand("promote").setExecutor(pc);
		getCommand("demote").setExecutor(pc);
		getCommand("getid").setExecutor(pc);
		getServer().getLogger().info("Permissions Enabled!");

		reloadHandler();
	}

	@Override
	public void onDisable() {
		Utils.disablePlugin(pfh, isEnabled);
	}

	public PermFileHandler getPermFileHandler(){
		return pfh;
	}

	private final void reloadHandler()
	{
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				Utils.uuids.clear();
				Utils.players.clear();

				for (Player p : Bukkit.getOnlinePlayers())
				{
					UUID pu = p.getUniqueId();
					Utils.uuids.put(p.getName(), pu);
					PermissionAttachment attachment = p.addAttachment(PRWP.this);
					Set<String> perms = GetPermissions.collectPermissions(pu, p.getWorld().getName());

					for(String permission : perms){
						attachment.setPermission(permission, true);
					}

					Utils.players.put(pu, attachment);
				}
				Bukkit.getLogger().info("All permissions reloaded");
			}
		}, 40L);
	}

	//Refactor vvvvv
	@Override
	public final boolean hasPermission(String player, String permission)
	{
		UUID pu = Utils.uuids.get(player);
		boolean has = false;
		String[] parts = permission.split("\\.");
		String combine = parts[0];
		String star = "*";

		if (Utils.permissions.get(pu) == null) {
			return false;
		}
		Set<String> playerPerms = Utils.permissions.get(pu);
		if ((playerPerms.contains("-" + star)) || (playerPerms.contains("-" + combine + ".*")) || (playerPerms.contains("-" + permission)) || (playerPerms.contains("-"+permission+".*"))) {
			return false;
		}
		if ((playerPerms.contains("+*")) || (playerPerms.contains("+" + combine + ".*"))) {
			has = true;
		}
		for (int i = 1; i < parts.length; i++) {
			combine = combine + "." + parts[i];
			if ((parts[i].equals("*")) || (i == parts.length - 1)) {
				if (playerPerms.contains("+" + combine)) {
					has = true;
				}
				if (playerPerms.contains("-" + combine)){
					return false;
				}
			}
			else {
				if (playerPerms.contains("+" + combine + ".*")) {
					has = true;
				}
				if (playerPerms.contains("-" + combine + ".*")) {
					return false;
				}
			}
		}
		return has;
	}

	@Override
	public final boolean hasPermission(String worldName, String player, String permission)
	{
		return hasPermission(player, permission);
	}

	@Override
	@SuppressWarnings("deprecation")
	public final boolean inGroup(String player, String group)
	{
		UUID pu = Bukkit.getOfflinePlayer(player).getUniqueId();
		String rank = Utils.getRank(pu);
		if(rank == null){
			return false;
		}
		return rank.equals(group.toLowerCase());
	}

	@SuppressWarnings("deprecation")
	@Override
	public final String[] getGroups(String player)
	{
		UUID pu = Bukkit.getOfflinePlayer(player).getUniqueId();
		String rank = Utils.getRank(pu);
		if(rank == null){
			return new String[0];
		}
		return new String[] { rank };
	}

	@Override
	public final boolean hasPermission(OfflinePlayer player, String permission)
	{
		return hasPermission(player.getPlayer().getName(), permission);
	}

	@Override
	public final boolean hasPermission(String worldName, OfflinePlayer player, String permission)
	{
		return hasPermission(player.getPlayer().getName(), permission);
	}

	@Override
	public final boolean inGroup(OfflinePlayer player, String group)
	{
		UUID pu = player.getUniqueId();
		String rank = Utils.getRank(pu);
		return rank == group;
	}

	@Override
	public final String[] getGroups(OfflinePlayer player)
	{
		UUID pu = player.getUniqueId();
		String rank = Utils.getRank(pu);
		return new String[] { rank };
	}
}