package main.java.net.kylemc.prwp.utils;

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

import main.java.net.kylemc.prwp.PRWP;
import main.java.net.kylemc.prwp.commands.Commands;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

public final class GetPermissions{

  private static HashMap<String, Set<String>> Wildcards = new HashMap<String, Set<String>>(); //Hashmap to hold wildcards for pseudo-caching. Keys: .*, values: list of all sub-permissions of the .*
  private static Set<Permission> serverPermissions = Bukkit.getPluginManager().getPermissions(); //Set of all registered server permissions.
  private static File dfolder = PRWP.dFolder;
  private static File playersFolder = new File(dfolder, "Players");
  private static File groupsFolder = new File(dfolder, "Groups");
  private static File worldsFolder = new File(dfolder, "Worlds");

  //Collects permission nodes and returns the Set of "positive" permissions
  public final static HashSet<String> collectPermissions(UUID pu, String world){
    HashSet<String> addPermissions = new HashSet<String>();
    HashSet<String> removePermissions = new HashSet<String>();
    HashSet<String> storedPermissions = new HashSet<String>();

    //Look in player's file
    try{
      File playerFile = new File(playersFolder, pu + ".txt");

      if(!playerFile.exists()){
        try{
          File p = new File(playersFolder, pu + ".txt");
          BufferedWriter out = new BufferedWriter(new FileWriter(p));
          out.write("Rank: " + Utils.groupNames[0] + Utils.newLine() + "Name: " + Bukkit.getPlayer(pu).getName() + Utils.newLine() +Utils.newLine() + "Permissions:");
          out.close();
        }
        catch(IOException e){ e.printStackTrace(); }
      }
      parseFiles(playerFile, addPermissions, removePermissions, storedPermissions);
    }
    catch(FileNotFoundException e){ e.printStackTrace(); }


    //Get player's rank
    String rank = Commands.getRank(pu);

    if(Utils.contains(rank, Utils.groupNames)){
      File globalFile = new File(groupsFolder, "_all.txt");
      try {
        parseFiles(globalFile, addPermissions, removePermissions, storedPermissions);
      }
      catch (FileNotFoundException e1) { e1.printStackTrace(); }
    }

    //If rank is not in groupNames or modNames, set rank to lowest rank (if available), and warn console
    if(rank.equals("") || (!Utils.contains(rank, Utils.groupNames) && !Utils.contains(rank, Utils.modNames))){
      if(!Utils.groupNames[0].equals("")){
        System.out.println(pu + " has an invalid rank! Being set to lowest rank or null if not set!");
        Commands.setRank(pu, rank, Utils.groupNames[0]);
        rank = Utils.groupNames[0];
      }
    }

    //Look in rank file
    if(rank.equals("")){
      //Assign no permissions for rank
      Utils.prefixes.put(pu, "");
    }
    else{
      File groupFile = new File(groupsFolder, rank + ".txt");
      try{
        Scanner scan = new Scanner(groupFile);
        scan.next(); //Skip "Prefix:"
        String prefix = scan.next().trim();
        scan.close();

        if(prefix.contains("#")){
          Utils.prefixes.put(pu, "");
        }
        else{
          Utils.prefixes.put(pu, prefix);
        }
        parseFiles(groupFile, addPermissions, removePermissions, storedPermissions);
      }
      catch(FileNotFoundException e){ e.printStackTrace(); }
    }

    //Look in world's rank file
    if(!rank.equals("")){
      try{
        File worldFile = new File(worldsFolder, world);

        if(Utils.contains(rank, Utils.groupNames)){
          File globalGroup = new File(worldFile, "_all.txt");
          parseFiles(globalGroup, addPermissions, removePermissions, storedPermissions);
        }

        File groupFile = new File(worldFile, rank + ".txt");
        parseFiles(groupFile, addPermissions, removePermissions, storedPermissions);
      }
      catch(FileNotFoundException e){ e.printStackTrace(); }
    }

    //Remove any permissions in addPermissions that appear in removePermissions
    addPermissions.removeAll(removePermissions);

    for(String remainingAdd : addPermissions){
      storedPermissions.add("+"+remainingAdd);
    }
    for(String removal : removePermissions){
      storedPermissions.add("-"+removal);
    }

    Utils.permissions.put(pu, storedPermissions);

    return addPermissions;
  }

  //Method to parse through the files for permission nodes
  private final static void parseFiles(File f, Set<String> addPermissions, Set<String> removePermissions, Set<String> storedPermissions) throws FileNotFoundException{
    BufferedReader br = new BufferedReader(new FileReader(f));
    String line = null;
    String text = "";
    String newLine = Utils.newLine();

    try{
      //skip 4 lines to get to first permission node (if any)
      br.readLine();
      br.readLine();
      br.readLine();
      br.readLine();

      //Read in line, if not null, add to 'text'; repeat until EOF
      line = br.readLine();

      while(line != null){
        text += line + newLine;
        line = br.readLine();
      }
      br.close();
    }
    catch(IOException e){ e.printStackTrace(); }

    //No permissions found, immediately end
    if(text.equals("")){
      return;
    }

    //Array holding each +/- permission
    String[] parsedPerms = text.split(newLine);
    String identifier = ""; //+ or -
    String node = ""; //worldedit.*

    //Loop through each permission, adding to sets
    for(String perm : parsedPerms){
      identifier = perm.substring(0,1); //+ or -
      node = perm.substring(2); //permission node

      if(node.contains("*")){
        //Permission has a wildcard
        Set<String> wP = wildcardPermissions(node);

        if(identifier.equals("+")){
          addPermissions.addAll(wP);
        }
        else{
          removePermissions.addAll(wP);
        }
      }
      else{
        //'Pure' permission
        if(identifier.equals("+")){
          addPermissions.add(node);
        }
        else{
          removePermissions.add(node);
        }
      }
    }
    return;
  }

  //Method to handle Wildcard Permissions (.* permissions)
  private final static Set<String> wildcardPermissions(String wild){
    if(Wildcards.containsKey(wild)){
      //This wildcard has been cached already
      return Wildcards.get(wild);
    }
    else{
      //Go through server permissions and create new Set of permissions
      Set<String> resolveWild = new HashSet<String>();
      int starIndex = wild.indexOf("*");
      String baseNode = wild.substring(0,starIndex); //'worldedit.biome.' part of worldedit.biome.*
      String permName = "";

      for(Permission p : serverPermissions){
        permName = p.getName();

        if(permName.contains(baseNode)){
          //If permission has the node part
          if(permName.contains("*")  && !(permName.equals(wild))){
            //Another wildcard found while iterating
            Set<String> internalWild = wildcardPermissions(permName); //Collect all permissions related to the wildcard
            resolveWild.addAll(internalWild); //Add all permissions found from the internal to the main
          }
          else{
            //Add the permission
            if(!(permName.contains("*"))){
              resolveWild.add(permName);
            }
          }
        }
      }

      //Add in wildcard permission to set if the set is empty
      if(resolveWild.isEmpty()){
        resolveWild.add(wild);
      }

      //Add new wildcard to HashMap for faster access
      Wildcards.put(wild, resolveWild);
      return resolveWild;
    }
  }
}