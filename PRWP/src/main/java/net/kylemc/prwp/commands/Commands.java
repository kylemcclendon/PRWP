package net.kylemc.prwp.commands;

import net.kylemc.prwp.PRWP;
import net.kylemc.prwp.utils.GetPermissions;
import net.kylemc.prwp.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public final class Commands implements CommandExecutor
{
	private final PRWP plugin;
	private static HashMap<UUID, Long> spamDelay = new HashMap<UUID, Long>();

	public Commands(PRWP instance)
	{
		plugin = instance;
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase("permissions")){
			if ((args.length < 1) || (args.length > 2)) {
				sender.sendMessage(ChatColor.RED + "Usage: /permissions [help,ranks,reload]");
				return true;
			}
			if (args[0].equalsIgnoreCase("help")) {
				sender.sendMessage(ChatColor.RED + "/permissions [help,ranks,reload]");
				return true;
			}
			if (args[0].equalsIgnoreCase("ranks")) {
				sender.sendMessage(ChatColor.GOLD + Utils.getRanks());
				return true;
			}
			if ((Utils.equals(args[0].toLowerCase(), new String[] { "reloadall", "ra" })) && (sender.hasPermission("permissions.moderator.reloadothers"))) {
				for (Player p : sender.getServer().getOnlinePlayers()) {
					reloadPlayer(p, p.getWorld().getName());
				}
				sender.sendMessage(ChatColor.GOLD + "All permissions successfully reloaded!");
				return true;
			}
			if (Utils.equals(args[0].toLowerCase(), new String[] { "reload", "r" })) {
				if (args.length == 1) {
					if (!(sender instanceof Player)) {
						sender.sendMessage(ChatColor.RED + "Can only be used by players");
						return true;
					}

					Player self = (Player)sender;
					final UUID pu = self.getUniqueId();

					if(spamDelay.containsKey(pu)){
						if (System.currentTimeMillis() < spamDelay.get(pu)) {
							self.sendMessage(ChatColor.RED + "You can only reload permissions every 5 minutes!");
							return true;
						}
					}

					spamDelay.put(pu, System.currentTimeMillis() + 300000);
					reloadPlayer(self, self.getWorld().getName());
					sender.sendMessage(ChatColor.GOLD + "Permissions successfully reloaded!");
					return true;
				}
				if (!sender.hasPermission("permissions.moderator.reloadothers")){
					sender.sendMessage("You do not have pemissions to reload another player's permissions!");
					return true;
				}

				Player p = Bukkit.getPlayer(args[1]);
				if (p == null){
					sender.sendMessage(ChatColor.RED + "Player: " + args[1] + " is not online");
					return true;
				}
				reloadPlayer(p, p.getWorld().getName());
				sender.sendMessage(ChatColor.GOLD + args[1] + "'s permissions successfully reloaded!");
				p.sendMessage(ChatColor.GOLD + "Your permissions were reloaded");
				return true;
			}
		}

		if (cmd.getName().equalsIgnoreCase("promote")) {
			String[] ranks = Utils.getRanks().split(",");

			if (((sender instanceof Player)) && (!sender.hasPermission("permissions.moderator.promote")))
			{
				sender.sendMessage(ChatColor.RED + "You do not have permission to do this!");
				return true;
			}
			if (args.length != 1) {
				sender.sendMessage(ChatColor.RED + "Usage: /promote <player>");
				return true;
			}

			UUID pu = Bukkit.getOfflinePlayer(args[0]).getUniqueId();
			String oldRank = Utils.getRank(pu);

			if(oldRank == null){
				sender.sendMessage(ChatColor.RED + "Player does not exist");
				return true;
			}
			if (Utils.contains(oldRank, Utils.modNames) && sender instanceof Player) {
				sender.sendMessage(ChatColor.RED + "Cannot promote mod team in game. Contact the server owner!");
				return true;
			}
			if (Utils.groupNames[0].equals("")) {
				sender.sendMessage(ChatColor.RED + "No groups have been specified");
				return true;
			}
			if ((oldRank.equals("")) || (!Utils.contains(oldRank, ranks))) {
				Utils.setRank(pu, Utils.groupNames[0].toLowerCase());
				sender.sendMessage(args[0] + "set to lowest rank");

				Player p = Bukkit.getPlayer(pu);
				if (p != null) {
					String worldName = p.getWorld().getName();
					reloadPlayer(p, worldName);
				}
				return true;
			}

			int pos = ranks.length - 1;
			for(int i = 0; i < ranks.length; i++){
				if(ranks[i].equals(oldRank)){
					pos = i;
					break;
				}
			}

			if ((pos == Utils.groupNames.length - 1 && sender instanceof Player) ||
					(pos == ranks.length - 1 && !(sender instanceof Player))) {
				sender.sendMessage(ChatColor.RED + args[0] + " is fully promoted.");
				return true;
			}

			Player p = Bukkit.getPlayer(pu);
			Utils.setRank(pu, ranks[(pos + 1)].toLowerCase());
			sender.sendMessage(ChatColor.AQUA + args[0] + " promoted to: " + ranks[(pos + 1)]);
			if (p != null) {
				String worldName = p.getWorld().getName();
				reloadPlayer(p, worldName);
				p.sendMessage(ChatColor.AQUA + "You have been promoted to: " + ranks[(pos + 1)]);
			}
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("demote")) {
			String[] ranks = Utils.getRanks().split(",");

			if (((sender instanceof Player)) && (!sender.hasPermission("permissions.moderator.demote"))) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to do this!");
				return true;
			}
			if (args.length != 1) {
				sender.sendMessage(ChatColor.RED + "Usage: /demote <player>");
			}

			String playerName = args[0].toLowerCase();
			UUID pu = Bukkit.getOfflinePlayer(playerName).getUniqueId();

			String orank = Utils.getRank(pu).toLowerCase();

			if (orank == null) {
				sender.sendMessage(ChatColor.RED + "Player does not exist");
				return true;
			}

			if (Utils.contains(orank, Utils.modNames) && sender instanceof Player) {
				sender.sendMessage(ChatColor.RED + "Cannot demote mod team in game. Contact server owner!");
				return true;
			}
			if (Utils.groupNames[0].equals("")) {
				sender.sendMessage(ChatColor.RED + "No groups have been specified)");
				return true;
			}

			int position = 0;
			for (int i = 0; i < ranks.length; i++) {
				if (ranks[i].equals(orank)) {
					position = i;
					break;
				}
			}

			if (position == 0) {
				sender.sendMessage(ChatColor.RED + args[0] + " is fully demoted.");
				return true;
			}

			Utils.setRank(pu, ranks[(position - 1)].toLowerCase());
			sender.sendMessage(ChatColor.AQUA + args[0] + " demoted to: " + ranks[(position - 1)]);
			Player p = sender.getServer().getPlayer(pu);
			if (p != null) {
				String worldName = p.getWorld().getName();
				reloadPlayer(p, worldName);
				p.sendMessage(ChatColor.AQUA + "You have been demoted to: " + ranks[(position - 1)]);
			}
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("getID")) {
			if (!sender.hasPermission("permissions.moderator.getid")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to use this");
				return true;
			}

			if (args.length != 1) {
				sender.sendMessage(ChatColor.RED + "Usage: /getid <player>");
				return true;
			}

			OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
			sender.sendMessage(ChatColor.AQUA + p.getName() + "'s UUID: " + p.getUniqueId());
			return true;
		}
		return false;
	}

	private final void reloadPlayer(Player player, String world)
	{
		UUID pu = player.getUniqueId();
		player.removeAttachment(Utils.players.get(pu));
		Utils.players.remove(pu);
		Utils.prefixes.remove(pu);
		PermissionAttachment attachment2 = player.addAttachment(plugin);
		Set<String> perms = GetPermissions.collectPermissions(pu, world);

		for (String perm : perms) {
			attachment2.setPermission(perm, true);
		}
		Utils.players.put(pu, attachment2);
	}
}