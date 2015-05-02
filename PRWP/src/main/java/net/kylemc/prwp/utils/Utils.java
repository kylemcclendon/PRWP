package main.java.net.kylemc.prwp.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

public final class Utils{
  private final static String OS = System.getProperty("os.name").contains("Windows") ? "\r\n" : "\n";
  private static String ranks = "";
  public static HashMap<UUID, PermissionAttachment> players = new HashMap<UUID, PermissionAttachment>();
  public static HashMap<UUID, String> prefixes = new HashMap<UUID, String>();
  public static HashMap<UUID, HashSet<String>> permissions = new HashMap<UUID, HashSet<String>>();
  public static HashMap<String, UUID> uuids = new HashMap<String, UUID>();
  public static String[] groupNames;
  public static String[] modNames;
  public static String[] bbl;
  public static String[] bwl;
  public static List<String> worldNames = new ArrayList<String>();

  //Private constructor to prevent instantiation
  private Utils(){
    throw new AssertionError();
  }

  //Initializes the ranks string declared above
  public final static void initRanks(){
    ranks = "";
    if(groupNames.length < 1){
      return;
    }
    else{
      ranks += groupNames[0];

      for(int i = 1; i < groupNames.length; i++){
        ranks += ", " + groupNames[i];
      }
    }

    if(ranks.equals("")){
      ranks = "No ranks";
    }
  }

  //Load world names into worldNames list
  public final static void loadWorlds(){
    for(World w : Bukkit.getServer().getWorlds()){
      Utils.worldNames.add(w.getName());
    }
  }

  //Returns if any groups were specified
  public final static boolean checkGroups(){
    return (groupNames[0].equals("") && modNames[0].equals(""));
  }

  //returns the ranks string
  public final static String getRanks(){
    return ranks;
  }

  //returns the OS string for OS-dependent newline format
  public final static String newLine(){
    return OS;
  }

  //Method to check if a string is in an array of strings
  public final static boolean contains(String type, String[] array){
    if(array.length > 39 && !array[0].equals("")){
      if(Arrays.binarySearch(array, type) != -1){
        return true;
      }
      return false;
    }
    else{
      for(int i = 0; i < array.length; i++){
        if(type.equalsIgnoreCase(array[i])){
          return true;
        }
      }
      return false;
    }
  }

  //Called when permissions are disabled (/reload, /restart, or /stop)
  public final static void disablePlugin(){
    uuids.clear();
    for(Player p : Bukkit.getServer().getOnlinePlayers()){
      UUID pu = p.getUniqueId();
      PermissionAttachment attachment = players.get(pu);
      players.remove(pu);
      if(attachment != null){
        p.removeAttachment(attachment);
      }
      prefixes.remove(pu);
      permissions.remove(pu);
    }
    Bukkit.getServer().getLogger().info("Permissions Disabled!");
  }

  //Used for comparing 2 strings cleanly
  public final static boolean equals(String command, String... args){
    for(String check : args){
      if(check.equals(command)){
        return true;
      }
    }
    return false;
  }
}