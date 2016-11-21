package net.kylemc.prwp.utils;

import net.kylemc.prwp.files.PermFileHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class Utils {
	private static File dFolder;
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
	private static YamlConfiguration sessionNamesConfiguration;
	private static YamlConfiguration sessionRanksConfiguration;
	private static YamlConfiguration sessionPlayerRanksConfiguration;

	private Utils() {
		throw new AssertionError();
	}

	public static final void setYamlConfigurations(PermFileHandler pfh){
		sessionNamesConfiguration = YamlConfiguration.loadConfiguration(pfh.namesFile);
		sessionRanksConfiguration = YamlConfiguration.loadConfiguration(PermFileHandler.ranksFile);
		sessionPlayerRanksConfiguration = YamlConfiguration.loadConfiguration(pfh.playerRanksFile);
	}

	public static final void initRanks() {
		if (groupNames.length < 1) {
			ranks = "No ranks";
			return;
		}

		ranks += groupNames[0];

		for (int i = 1; i < groupNames.length; i++) {
			ranks = ranks + "," + groupNames[i];
		}

		if(modNames.length > 0){
			for(int i = 0; i < modNames.length; i++){
				ranks = ranks + "," + modNames[i];
			}
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

	public static final void disablePlugin(PermFileHandler pfh, Boolean isEnabled) {
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
		if(isEnabled) {
			saveConfigurations(pfh);
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

	public static File getdFolder() {
		return dFolder;
	}

	public static void setdFolder(File dFolder) {
		if (!dFolder.exists()) {
			dFolder.mkdirs();
		}
		Utils.dFolder = dFolder;
	}

	private static void saveConfigurations(PermFileHandler pfh){
		try {
            sessionNamesConfiguration.save(pfh.namesFile);
			sessionPlayerRanksConfiguration.save(pfh.playerRanksFile);
		} catch (IOException e) {
			Bukkit.getLogger().severe("Could not save yaml file(s)");
			e.printStackTrace();
		}
	}

	public static YamlConfiguration getNames(){
		return sessionNamesConfiguration;
	}

	public static YamlConfiguration getRankValues(){
		return sessionRanksConfiguration;
	}

	public static YamlConfiguration getPlayerRanks(){
		return sessionPlayerRanksConfiguration;
	}

	public static void setNameValue(UUID pu, String name){
		sessionNamesConfiguration.set(pu.toString(), name);
	}

	public static void setPlayerRankValue(UUID pu, String rank){
		sessionPlayerRanksConfiguration.set(pu.toString(), rank);
	}

	public final static String getRank(UUID pu){
		return sessionPlayerRanksConfiguration.getString(pu.toString());
	}

	public final static void setRank(UUID pu, String newRank)
	{
		sessionPlayerRanksConfiguration.set(pu.toString(), newRank);
	}
}