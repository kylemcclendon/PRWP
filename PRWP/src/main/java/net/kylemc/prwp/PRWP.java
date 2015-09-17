package net.kylemc.prwp;

import java.io.File;
import java.util.Set;
import java.util.UUID;

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

import com.sk89q.wepif.PermissionsProvider;

public final class PRWP
extends JavaPlugin
implements PermissionsProvider
{
	public static File dFolder;

	@Override
	public void onEnable()
	{
		dFolder = getDataFolder();

		if (!dFolder.exists()) {
			dFolder.mkdirs();
		}

		PermFileHandler pfh = new PermFileHandler(dFolder);
		pfh.initFiles();

		if (Utils.checkGroups()) {
			getLogger().warning("Permissions not enabled, no groups/mods specified!");
			return;
		}

		Utils.initRanks();


		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PermissionsEvents(), this);
		pm.registerEvents(new ReloadEvents(this), this);

		pfh.createBaseFolders();

		Utils.loadWorlds();

		if (!pfh.createSubFolders()) {
			getServer().getLogger().warning("Permissions Not Enabled!");
			return;
		}

		Commands pc = new Commands(this, dFolder);
		getCommand("permissions").setExecutor(pc);
		getCommand("promote").setExecutor(pc);
		getCommand("demote").setExecutor(pc);
		getCommand("setrank").setExecutor(pc);
		getCommand("getid").setExecutor(pc);
		getServer().getLogger().info("Permissions Enabled!");

		reloadHandler();
	}


	@Override
	public void onDisable() {}


	private final void reloadHandler()
	{
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers())
				{
					UUID pu = p.getUniqueId();
					Utils.uuids.put(p.getName(), pu);
					String world = p.getWorld().getName();
					PermissionAttachment attachment = p.addAttachment(PRWP.this);
					Utils.players.put(pu, attachment);
					Set<String> perms = GetPermissions.collectPermissions(pu, world);

					for(String permission : perms){
						attachment.setPermission(permission, true);
					}
				}
				System.out.println("All permissions reloaded");
			}
		}, 40L);
	}




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

		if ((playerPerms.contains("-" + star)) || (playerPerms.contains("-" + combine + ".*")) || (playerPerms.contains("-" + permission))) {
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
		String rank = Commands.getRank(pu).toLowerCase();
		return rank.equals(group.toLowerCase());
	}

	@Override
	public final String[] getGroups(String player)
	{
		return new String[] { "None" };
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
		return false;
	}

	@Override
	public final String[] getGroups(OfflinePlayer player)
	{
		return null;
	}
}