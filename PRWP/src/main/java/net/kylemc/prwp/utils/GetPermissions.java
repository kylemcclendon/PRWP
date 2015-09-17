package net.kylemc.prwp.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import net.kylemc.prwp.commands.Commands;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

public final class GetPermissions
{
	private static HashMap<String, Set<String>> Wildcards = new HashMap<String, Set<String>>();
	private static Set<Permission> serverPermissions = Bukkit.getPluginManager().getPermissions();
	private static File dfolder = net.kylemc.prwp.PRWP.dFolder;
	private static File playersFolder = new File(dfolder, "Players");
	private static File groupsFolder = new File(dfolder, "Groups");
	private static File worldsFolder = new File(dfolder, "Worlds");

	public static final HashSet<String> collectPermissions(UUID pu, String world)
	{
		HashSet<String> addPermissions = new HashSet<String>();
		HashSet<String> removePermissions = new HashSet<String>();
		HashSet<String> storedPermissions = new HashSet<String>();

		try
		{
			File playerFile = new File(playersFolder, pu + ".txt");

			if (!playerFile.exists()) {
				try {
					File p = new File(playersFolder, pu + ".txt");
					BufferedWriter out = new BufferedWriter(new FileWriter(p));
					out.write("Rank: " + Utils.groupNames[0] + Utils.newLine() + "Name: " + Bukkit.getPlayer(pu).getName() + Utils.newLine() + Utils.newLine() + "Permissions:");
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			parseFiles(playerFile, addPermissions, removePermissions, storedPermissions);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}


		String rank = Commands.getRank(pu);

		if(Utils.contains(rank, Utils.groupNames)) {
			File globalFile = new File(groupsFolder, "_all.txt");
			try {
				parseFiles(globalFile, addPermissions, removePermissions, storedPermissions);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		}

		if (((rank.equals("")) || ((!Utils.contains(rank, Utils.groupNames)) && (!Utils.contains(rank, Utils.modNames)))) &&
				(!Utils.groupNames[0].equals(""))) {
			System.out.println(pu + " has an invalid rank! Being set to lowest rank or null if not set!");
			Commands.setRank(pu, rank, Utils.groupNames[0]);
			rank = Utils.groupNames[0];
		}



		if (rank.equals(""))
		{
			Utils.prefixes.put(pu, "");
		}
		else {
			File groupFile = new File(groupsFolder, rank + ".txt");
			try {
				Scanner scan = new Scanner(groupFile);
				scan.next();
				String prefix = scan.next().trim();
				scan.close();

				if (prefix.contains("#")) {
					Utils.prefixes.put(pu, "");
				}
				else {
					Utils.prefixes.put(pu, prefix);
				}
				parseFiles(groupFile, addPermissions, removePermissions, storedPermissions);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		File groupFile;
		if (!rank.equals("")) {
			try {
				File worldFile = new File(worldsFolder, world);

				if (Utils.contains(rank, Utils.groupNames)) {
					File globalGroup = new File(worldFile, "_all.txt");
					parseFiles(globalGroup, addPermissions, removePermissions, storedPermissions);
				}

				groupFile = new File(worldFile, rank + ".txt");
				parseFiles(groupFile, addPermissions, removePermissions, storedPermissions);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		addPermissions.removeAll(removePermissions);

		for (String remainingAdd : addPermissions) {
			storedPermissions.add("+" + remainingAdd);
		}
		for (String removal : removePermissions) {
			storedPermissions.add("-" + removal);
		}

		Utils.permissions.put(pu, storedPermissions);

		return addPermissions;
	}

	private static final void parseFiles(File f, Set<String> addPermissions, Set<String> removePermissions, Set<String> storedPermissions) throws FileNotFoundException
	{
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line = null;
		String text = "";
		String newLine = Utils.newLine();

		try
		{
			br.readLine();
			br.readLine();
			br.readLine();
			br.readLine();


			line = br.readLine();

			while (line != null) {
				text = text + line + newLine;
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (text.equals("")) {
			return;
		}


		String[] parsedPerms = text.split(newLine);
		String identifier = "";
		String node = "";

		String[] arrayOfString1;
		int j = (arrayOfString1 = parsedPerms).length; for (int i = 0; i < j; i++) { String perm = arrayOfString1[i];
		identifier = perm.substring(0, 1);
		node = perm.substring(2);

		if (node.contains("*"))
		{
			Set<String> wP = wildcardPermissions(node);

			if (identifier.equals("+")) {
				addPermissions.addAll(wP);
			}
			else {
				removePermissions.addAll(wP);
			}


		}
		else if (identifier.equals("+")) {
			addPermissions.add(node);
		}
		else {
			removePermissions.add(node);
		}
		}
	}

	private static final Set<String> wildcardPermissions(String wild)
	{
		if (Wildcards.containsKey(wild))
		{
			return Wildcards.get(wild);
		}

		Set<String> resolveWild = new HashSet<String>();
		int starIndex = wild.indexOf("*");
		String baseNode = wild.substring(0, starIndex);
		String permName = "";

		for (Permission p : serverPermissions) {
			permName = p.getName();

			if (permName.contains(baseNode))
			{
				if ((permName.contains("*")) && (!permName.equals(wild)))
				{
					Set<String> internalWild = wildcardPermissions(permName);
					resolveWild.addAll(internalWild);
				}
				else if (!permName.contains("*")) {
					resolveWild.add(permName);
				}
			}
		}

		if (resolveWild.isEmpty()) {
			resolveWild.add(wild);
		}

		Wildcards.put(wild, resolveWild);
		return resolveWild;
	}
}