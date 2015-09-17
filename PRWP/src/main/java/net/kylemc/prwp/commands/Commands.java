package net.kylemc.prwp.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

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
import org.bukkit.scheduler.BukkitRunnable;

public final class Commands implements CommandExecutor
{
	private final PRWP plugin;
	private static HashMap<UUID, Boolean> spamDelay = new HashMap<UUID, Boolean>();
	public static File playersFolder;

	public Commands(PRWP instance, File dfolder)
	{
		plugin = instance;
		playersFolder = new File(dfolder, "Players");
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase("permissions"))
		{
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

					if (!spamDelay.containsKey(pu)) {
						spamDelay.put(pu, Boolean.valueOf(true));
						Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new BukkitRunnable()
						{
							@Override
							public void run()
							{
								Commands.spamDelay.put(pu, Boolean.valueOf(false));
							}
						}, 6000L);
					}
					else {
						if (spamDelay.get(pu).booleanValue()) {
							self.sendMessage(ChatColor.RED + "You can only reload permissions every 5 minutes!");
							return true;
						}
						spamDelay.put(pu, Boolean.valueOf(true));
					}

					reloadPlayer(self, self.getWorld().getName());
					sender.sendMessage(ChatColor.GOLD + "Permissions successfully reloaded!");
					return true;
				}
				if (!sender.hasPermission("permissions.moderator.reloadothers"))
				{
					sender.sendMessage("You do not have pemissions to reload another player's permissions!");
					return true;
				}

				Player p = Bukkit.getPlayer(args[1]);
				if (p == null)
				{
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
			File playerFile = new File(playersFolder, pu + ".txt");
			if (!playerFile.exists()) {
				sender.sendMessage(ChatColor.RED + "Player does not exist");
				return true;
			}

			String oldRank = getRank(pu).toLowerCase();

			if (Utils.contains(oldRank, Utils.modNames)) {
				sender.sendMessage(ChatColor.RED + "Cannot promote mod team in game. Contact the server owner!");
				return true;
			}
			if (Utils.groupNames[0].equals("")) {
				sender.sendMessage(ChatColor.RED + "No groups have been specified");
				return true;
			}
			if ((oldRank.equals("")) || (!Utils.contains(oldRank, Utils.groupNames))) {
				setRank(pu, oldRank, Utils.groupNames[0].toLowerCase());
				sender.sendMessage(args[0] + "set to lowest rank");

				Player p = Bukkit.getPlayer(pu);
				if (p != null) {
					String worldName = p.getWorld().getName();
					reloadPlayer(p, worldName);
				}
				return true;
			}

			int position = Utils.groupNames.length - 1;
			for (int i = 0; i < Utils.groupNames.length; i++) {
				if (Utils.groupNames[i].equals(oldRank)) {
					position = i;
					break;
				}
			}
			if (position == Utils.groupNames.length - 1) {
				sender.sendMessage(ChatColor.RED + args[0] + " is fully promoted.");
				return true;
			}

			Player p = Bukkit.getPlayer(pu);
			setRank(pu, oldRank, Utils.groupNames[(position + 1)].toLowerCase());
			sender.sendMessage(ChatColor.AQUA + args[0] + " promoted to: " + Utils.groupNames[(position + 1)]);
			if (p != null) {
				String worldName = p.getWorld().getName();
				reloadPlayer(p, worldName);
				p.sendMessage(ChatColor.AQUA + "You have been promoted to: " + Utils.groupNames[(position + 1)]);
			}
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("demote")) {
			if (((sender instanceof Player)) && (!sender.hasPermission("permissions.moderator.demote"))) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to do this!");
				return true;
			}
			if (args.length != 1) {
				sender.sendMessage(ChatColor.RED + "Usage: /demote <player>");
			}

			String playerName = args[0].toLowerCase();
			UUID pu = Bukkit.getOfflinePlayer(playerName).getUniqueId();
			File playerFile = new File(playersFolder, pu + ".txt");
			if (!playerFile.exists()) {
				sender.sendMessage(ChatColor.RED + "Player does not exist");
				return true;
			}

			String orank = getRank(pu).toLowerCase();
			if (Utils.contains(orank, Utils.modNames)) {
				sender.sendMessage(ChatColor.RED + "Cannot demote mod team in game. Contact server owner!");
				return true;
			}
			if (Utils.groupNames[0].equals("")) {
				sender.sendMessage(ChatColor.RED + "No groups have been specified)");
				return true;
			}

			int position = 0;
			for (int i = 0; i < Utils.groupNames.length; i++) {
				if (Utils.groupNames[i].equals(orank)) {
					position = i;
					break;
				}
			}
			if (position == 0) {
				sender.sendMessage(ChatColor.RED + args[0] + " is fully demoted.");
				return true;
			}

			setRank(pu, orank, Utils.groupNames[(position - 1)].toLowerCase());
			sender.sendMessage(ChatColor.AQUA + args[0] + " demoted to: " + Utils.groupNames[(position - 1)]);
			Player p = sender.getServer().getPlayer(pu);
			if (p != null) {
				String worldName = p.getWorld().getName();
				reloadPlayer(p, worldName);
				p.sendMessage(ChatColor.AQUA + "You have been demoted to: " + Utils.groupNames[(position - 1)]);
			}
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("setrank")) {
			if (((sender instanceof Player)) && (!sender.hasPermission("permissions.moderator.setrank"))) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to do this!");
				return true;
			}
			if (args.length != 2) {
				sender.sendMessage(ChatColor.RED + "Usage: /setrank <player> <rank>");
			}

			UUID pu = Bukkit.getOfflinePlayer(args[0]).getUniqueId();
			File playerFile = new File(playersFolder, pu + ".txt");
			if (!playerFile.exists()) {
				sender.sendMessage(ChatColor.RED + args[0] + " does not exist");
				return true;
			}

			String orank = getRank(pu);
			String nrank = args[1].toLowerCase();
			if ((Utils.contains(orank, Utils.modNames)) || (Utils.contains(nrank, Utils.modNames))) {
				sender.sendMessage(ChatColor.RED + "Cannot change mod team ranks in game. Contact server owner!");
				return true;
			}

			if (Utils.contains(nrank, Utils.groupNames)) {
				Player p = Bukkit.getPlayer(pu);
				setRank(pu, orank, nrank);
				sender.sendMessage(ChatColor.AQUA + args[0] + " set to " + nrank);
				if (p != null) {
					reloadPlayer(p, p.getWorld().getName());
					p.sendMessage(ChatColor.AQUA + "Your rank was set to: " + nrank);
				}
				return true;
			}

			sender.sendMessage(ChatColor.RED + "Invalid new rank");
			return true;
		}

		if (cmd.getName().equalsIgnoreCase("getID")) {
			if (!sender.hasPermission("permissions.moderator.getid")) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to use this");
			}

			if (args.length != 1) {
				sender.sendMessage(ChatColor.RED + "Usage: /getid <player>");
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

	public static final String getRank(UUID pu)
	{
		File playerFile = new File(playersFolder, pu + ".txt");
		String rest = "";
		if (!playerFile.exists()) {
			return rest;
		}
		try
		{
			Scanner scan = new Scanner(playerFile);
			String rank = scan.nextLine();
			scan.close();
			int i = rank.indexOf(' ');
			if (i != -1) {
				rest = rank.substring(i + 1);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		rest = rest.toLowerCase();
		return rest;
	}

	public static final void setRank(UUID player, String oldRank, String newRank)
	{
		try {
			File playerFile = new File(playersFolder, player + ".txt");
			BufferedReader reader = new BufferedReader(new FileReader(playerFile));
			String line = "";String oldtext = "";
			while ((line = reader.readLine()) != null) {
				oldtext = oldtext + line + Utils.newLine();
			}
			reader.close();
			String newtext;
			if (oldRank.equals("")) {
				newtext = oldtext.replaceFirst("Rank:" + oldRank, "Rank: " + newRank);
			}
			else {
				newtext = oldtext.replaceFirst("Rank: " + oldRank, "Rank: " + newRank);
			}
			FileWriter writer = new FileWriter(playerFile);
			writer.write(newtext);
			writer.close();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}