package net.kylemc.prwp.files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;

import net.kylemc.prwp.utils.Utils;

import org.bukkit.configuration.file.YamlConfiguration;


public final class PermFileHandler
{
	static File dFolder;
	private File settingsFile;
	private YamlConfiguration settings;
	private static YamlConfiguration namesSettings;
	public static File namesFile;

	public PermFileHandler(File main)
	{
		dFolder = main;
	}

	public final void initFiles()
	{
		settingsFile = new File(dFolder, "config.yml");
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


		if (!settingsFile.exists()) {
			settings = new YamlConfiguration();
			settings.set("groups", "#EXAMPLE: guest,member,veteran");
			settings.set("mods", "#EXAMPLE: op,admin");
			settings.set("block-blacklist", "#EXAMPLE: 1,2,3,4");
			settings.set("block-whitelist", "#EXAMPLE: 1,2,3,4");
			saveSettings();
		}

		settings = YamlConfiguration.loadConfiguration(settingsFile);
		saveSettings();


		checkSettings();
	}

	private final boolean saveSettings()
	{
		if (!settingsFile.exists()) {
			settingsFile.getParentFile().mkdirs();
		}
		try {
			settings.save(settingsFile);
			return true;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private final void checkSettings()
	{
		if ((settings.getString("groups") == null) || (settings.getString("groups").contains("#"))) {
			Utils.groupNames = new String[] { "" };
		}
		else {
			Utils.groupNames = settings.getString("groups").split("\\s*,\\s*");
		}

		if ((settings.getString("mods") == null) || (settings.getString("mods").contains("#"))) {
			Utils.modNames = new String[] { "" };
		}
		else {
			Utils.modNames = settings.getString("mods").split("\\s*,\\s*");
		}

		if ((settings.getString("block-blacklist") == null) || (settings.getString("block-blacklist").contains("#"))) {
			Utils.bbl = new String[] { "" };
		}
		else {
			Utils.bbl = settings.getString("block-blacklist").split("\\s*,\\s*");
			Arrays.sort(Utils.bbl);
		}

		if ((settings.getString("block-whitelist") == null) || (settings.getString("block-whitelist").contains("#"))) {
			Utils.bwl = new String[] { "" };
		}
		else {
			Utils.bwl = settings.getString("block-whitelist").split("\\s*,\\s*");
			Arrays.sort(Utils.bwl);
		}
	}

	public final void createBaseFolders()
	{
		File worldsFolder = new File(dFolder, "Worlds");
		File groupsFolder = new File(dFolder, "Groups");
		File playersFolder = new File(dFolder, "Players");

		if (!worldsFolder.exists()) {
			worldsFolder.mkdir();
		}

		if (!groupsFolder.exists()) {
			groupsFolder.mkdir();
		}

		if (!playersFolder.exists()) {
			playersFolder.mkdir();
		}
	}

	public final boolean createSubFolders()
	{
		if ((createWorldFolders(Utils.worldNames)) && (createGroupFiles())) {
			return true;
		}
		return false;
	}

	private final boolean createWorldFolders(List<String> worldnames)
	{
		File worldsFolder = new File(dFolder, "Worlds");

		for (String world : worldnames) {
			File newWorld = new File(worldsFolder, world);
			if (!newWorld.exists()) {
				newWorld.mkdir();
				if (newWorld.exists())
				{
					writeGroups(newWorld);
				}
			}
			else
			{
				writeGroups(newWorld);
			}
		}
		return true;
	}

	private final boolean createGroupFiles()
	{
		File groupsFolder = new File(dFolder, "Groups");
		File newGroup = new File(groupsFolder, "_all.txt");
		if (!newGroup.exists()) {
			try {
				newGroup.createNewFile();
			} catch (IOException e) {
				e.printStackTrace(); }
			writeDefaultGroup(newGroup);
		}

		for (int i = 0; i < Utils.groupNames.length; i++) {
			newGroup = new File(groupsFolder, Utils.groupNames[i] + ".txt");
			if (Utils.groupNames[i].equals("")) {
				break;
			}

			if (!newGroup.exists()) {
				try {
					newGroup.createNewFile();
				} catch (IOException e) {
					e.printStackTrace(); }
				writeDefaultGroup(newGroup);
			}
		}
		for (int i = 0; i < Utils.modNames.length; i++) {
			newGroup = new File(groupsFolder, Utils.modNames[i] + ".txt");
			if (Utils.modNames[i].equals("")) {
				break;
			}
			if (!newGroup.exists()) {
				try {
					newGroup.createNewFile();
				} catch (IOException e) {
					e.printStackTrace(); }
				writeDefaultGroup(newGroup);
			}
		}
		return true;
	}

	private final void writeDefaultGroup(File file)
	{
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));

			writer.write("Prefix: #[&4G&f]");
			writer.newLine();
			writer.newLine();
			writer.write("Permissions:");
			writer.newLine();
			writer.write("--------------");
			writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}


	private final void writeGroups(File file)
	{
		try
		{
			File group = new File(file, "_all.txt");
			if (!group.exists()) {
				BufferedWriter output = new BufferedWriter(new FileWriter(group));
				output.write("#Add all permissions after the 'Permissions:' line. Do not Modify any other lines");
				output.newLine();
				output.newLine();
				output.write("Permissions:");
				output.newLine();
				output.write("--------------");
				output.close();
			}
		} catch (IOException e) {
			e.printStackTrace(); }
		String[] arrayOfString;
		int j = (arrayOfString = Utils.groupNames).length; for (int i = 0; i < j; i++) { String newGroup = arrayOfString[i];
		if (newGroup.equals("")) {
			break;
		}
		try {
			File group = new File(file, newGroup + ".txt");
			if (!group.exists()) {
				BufferedWriter output = new BufferedWriter(new FileWriter(group));
				output.write("#Add all permissions after the 'Permissions:' line. Do not Modify any other lines");
				output.newLine();
				output.newLine();
				output.write("Permissions:");
				output.newLine();
				output.write("--------------");
				output.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} }
		j = (arrayOfString = Utils.modNames).length; for (int i = 0; i < j; i++) { String modGroup = arrayOfString[i];
		if (modGroup.equals("")) {
			break;
		}
		try {
			File group = new File(file, modGroup + ".txt");
			if (!group.exists()) {
				BufferedWriter output = new BufferedWriter(new FileWriter(group));
				output.write("#Add all permissions after the 'Permissions:' line. Do not Modify any other lines");
				output.newLine();
				output.newLine();
				output.write("Permissions:");
				output.newLine();
				output.write("--------------");
				output.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		}
	}
}