package net.kylemc.prwp.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

public final class Utils {
	private static final String OS = System.getProperty("os.name").contains("Windows") ? "\r\n" : "\n";
	private static String ranks = "";
	public static HashMap<UUID, PermissionAttachment> players = new HashMap<UUID, PermissionAttachment>();
	public static HashMap<UUID, String> prefixes = new HashMap<UUID, String>();
	public static HashMap<UUID, HashSet<String>> permissions = new HashMap<UUID, HashSet<String>>();
	public static HashMap<String, UUID> uuids = new HashMap<String, UUID>();
	public static String[] groupNames;
	public static String[] modNames;
	public static String[] bbl;
	public static String[] bwl;
	public static ArrayList<String> worldNames = new ArrayList<String>();

	private Utils() {
		throw new AssertionError();
	}

	public static final void initRanks() {
		ranks = "";
		if (groupNames.length < 1) {
			return;
		}

		ranks += groupNames[0];

		for (int i = 1; i < groupNames.length; i++) {
			ranks = ranks + ", " + groupNames[i];
		}

		if (ranks.equals("")) {
			ranks = "No ranks";
		}
	}

	public static final void loadWorlds() {
		for (World w : Bukkit.getServer().getWorlds()) {
			worldNames.add(w.getName());
		}
	}

	public static final boolean checkGroups() {
		return (groupNames[0].equals("")) && (modNames[0].equals(""));
	}

	public static final String getRanks() {
		return ranks;
	}

	public static final String newLine() {
		return OS;
	}

	public static final boolean contains(String type, String[] array) {
		if ((array.length > 39) && (!array[0].equals(""))) {
			if (Arrays.binarySearch(array, type) != -1) {
				return true;
			}
			return false;
		}

		for (int i = 0; i < array.length; i++) {
			if (type.equalsIgnoreCase(array[i])) {
				return true;
			}
		}
		return false;
	}

	public static final void disablePlugin() {
		uuids.clear();
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			UUID pu = p.getUniqueId();
			PermissionAttachment attachment = players.get(pu);
			players.remove(pu);
			if (attachment != null) {
				p.removeAttachment(attachment);
			}
			prefixes.remove(pu);
			permissions.remove(pu);
		}
		Bukkit.getServer().getLogger().info("Permissions Disabled!");
	}

	public static final boolean equals(String command, String... args) {
		String[] arrayOfString;
		int j = (arrayOfString = args).length;
		for (int i = 0; i < j; i++) {
			String check = arrayOfString[i];
			if (check.equals(command)) {
				return true;
			}
		}
		return false;
	}
}