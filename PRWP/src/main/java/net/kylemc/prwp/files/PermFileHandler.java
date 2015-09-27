package net.kylemc.prwp.files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import net.kylemc.prwp.utils.Utils;

import org.bukkit.configuration.file.YamlConfiguration;

public final class PermFileHandler
{
	private static File dFolder;
	private static File configFile; //PRWP Settings
	public File namesFile; //UUID: Name
	public static File ranksFile; //Rank Name: Prefix
	public File playerRanksFile; //UUID: Rank
	public static YamlConfiguration configSettings;
	public static YamlConfiguration namesSettings;
	public static YamlConfiguration ranksSettings;
	public static YamlConfiguration playerRanksSettings;

	public PermFileHandler(File main)
	{
		dFolder = main;
		initConfigFile();
		initNamesFile();
		initPlayerRanksFile();
		addRanksToFile();
		createBaseFolders();
		createSubFolders();
	}

	private final void initConfigFile(){
		configFile = new File(dFolder, "config.yml");
		if (!configFile.exists()) {
			configSettings = new YamlConfiguration();
			configSettings.set("groups", "#EXAMPLE: guest,member,veteran");
			configSettings.set("mods", "#EXAMPLE: op,admin");
			configSettings.set("block-blacklist", "#EXAMPLE: 1,2,3,4");
			configSettings.set("block-whitelist", "#EXAMPLE: 1,2,3,4");
			saveSettings();
		}

		configSettings = YamlConfiguration.loadConfiguration(configFile);
		saveSettings();
		checkSettings();
	}

	private final void initNamesFile(){
		namesFile = new File(dFolder, "names.yml");
		if (!namesFile.exists()) {
			namesSettings = new YamlConfiguration();
			namesSettings.set("#", "Example");
			try {
				namesSettings.save(namesFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private final void initPlayerRanksFile(){
		playerRanksFile = new File(dFolder, "playerRanks.yml");
		if(!playerRanksFile.exists()){
			playerRanksSettings = new YamlConfiguration();
			try {
				playerRanksSettings.save(playerRanksFile);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private final boolean saveSettings(){
		if (!configFile.exists()) {
			configFile.getParentFile().mkdirs();
		}
		try {
			configSettings.save(configFile);
			return true;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private final void checkSettings()
	{
		if ((configSettings.getString("groups") == null) || (configSettings.getString("groups").contains("#"))) {
			Utils.groupNames = new String[] { "" };
		}
		else {
			Utils.groupNames = configSettings.getString("groups").split("\\s*,\\s*");
		}

		if ((configSettings.getString("mods") == null) || (configSettings.getString("mods").contains("#"))) {
			Utils.modNames = new String[] { "" };
		}
		else {
			Utils.modNames = configSettings.getString("mods").split("\\s*,\\s*");
		}

		if ((configSettings.getString("block-blacklist") == null) || (configSettings.getString("block-blacklist").contains("#"))) {
			Utils.bbl = new String[] { "" };
		}
		else {
			Utils.bbl = configSettings.getString("block-blacklist").split("\\s*,\\s*");
			Arrays.sort(Utils.bbl);
		}

		if ((configSettings.getString("block-whitelist") == null) || (configSettings.getString("block-whitelist").contains("#"))) {
			Utils.bwl = new String[] { "" };
		}
		else {
			Utils.bwl = configSettings.getString("block-whitelist").split("\\s*,\\s*");
			Arrays.sort(Utils.bwl);
		}
	}

	private final void addRanksToFile(){
		ranksFile = new File(dFolder, "ranks.yml");
		if (!ranksFile.exists()){
			try {
				ranksFile.createNewFile();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			ranksSettings = new YamlConfiguration();
		}

		ranksSettings = YamlConfiguration.loadConfiguration(ranksFile);

		Set<String> existingRanks = ranksSettings.getKeys(true);
		String[] fileGroupRanks = configSettings.getString("groups").split("\\s*,\\s*");
		String[] fileModRanks = configSettings.getString("mods").split("\\s*,\\s*");

		for(String rank: fileGroupRanks){
			if(!existingRanks.contains(rank)){
				ranksSettings.set(rank, "[&4_&F]");
			}
		}

		for(String rank: fileModRanks){
			if(!existingRanks.contains(rank)){
				ranksSettings.set(rank, "[&4_&F]");
			}
		}

		try {
			ranksSettings.save(ranksFile);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final void createBaseFolders()
	{
		File worldsFolder = new File(dFolder, "Worlds");
		if (!worldsFolder.exists()) {
			worldsFolder.mkdir();
		}

		File groupsFolder = new File(dFolder, "Groups");
		if (!groupsFolder.exists()) {
			groupsFolder.mkdir();
		}

		File playersFolder = new File(dFolder, "Players");
		if (!playersFolder.exists()) {
			playersFolder.mkdir();
		}
	}

	public final void createSubFolders()
	{
		createWorldFolders(Utils.worldNames);
		writeGroups(new File(dFolder, "Groups"));
	}

	private final void createWorldFolders(List<String> worldnames)
	{
		File worldsFolder = new File(dFolder, "Worlds");

		for (String world : worldnames) {
			File newWorld = new File(worldsFolder, world);
			if (!newWorld.exists()){
				newWorld.mkdir();
			}
			if (newWorld.exists()){
				writeGroups(newWorld);
			}
		}
	}

	private final void writeGroups(File file)
	{
		File allGroup = new File(file, "_all.txt");
		if (!allGroup.exists()) {
			writeDefaultGroup(allGroup);
		}

		String[] groups = Utils.groupNames;
		for(String group : groups){
			if(group.equals("")){
				break;
			}
			File groupFile = new File(file, group + ".txt");
			if(!groupFile.exists()){
				writeDefaultGroup(groupFile);
			}
		}

		String[] mods = Utils.modNames;
		for(String mod : mods){
			if(mod.equals("")){
				break;
			}
			File modFile = new File(file, mod + ".txt");
			if(!modFile.exists()){
				writeDefaultGroup(modFile);
			}
		}
	}

	private final void writeDefaultGroup(File file)
	{
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
			writer.write("Permissions:");
			writer.newLine();
			writer.write("--------------");
			writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}