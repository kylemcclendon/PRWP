package main.java.net.kylemc.prwp;

import java.io.File;
import java.util.Set;
import java.util.UUID;

import main.java.net.kylemc.prwp.commands.Commands;
import main.java.net.kylemc.prwp.events.PermissionsEvents;
import main.java.net.kylemc.prwp.events.ReloadEvents;
import main.java.net.kylemc.prwp.files.PermFileHandler;
import main.java.net.kylemc.prwp.utils.GetPermissions;
import main.java.net.kylemc.prwp.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.wepif.PermissionsProvider;
//import java.util.Set;


//@Plugin(id = "PRWP", name = "PRWP", version = "1.10.1", dependencies = "WorldEdit")
public final class PRWP extends JavaPlugin implements PermissionsProvider{

  // Game game;
  //Logger logger;

  //Global data folder variable
  public static File dFolder;

  //Called when GeneralPermissions loads
  @Override
  public void onEnable(){
    dFolder = getDataFolder();

    //Creates Permissions Folder
    if(!dFolder.exists()){ dFolder.mkdirs(); }

    //Handler for all files
    PermFileHandler pfh = new PermFileHandler(dFolder);

    //Creates settings and names files
    pfh.initFiles();

    //checks to see if groups are enabled. If not, warn the console.
    if(Utils.checkGroups()){
      getLogger().warning("Permissions not enabled, no groups/mods specified!");
      return;
    }
    //Groups have been specified
    Utils.initRanks(); //Initialize ranks string in Utils (prevents computation of in-game ranks when '/permissions ranks' is done)

    //Set up PluginManager for events, and register events
    PluginManager pm = getServer().getPluginManager();
    pm.registerEvents(new PermissionsEvents(), this);
    pm.registerEvents(new ReloadEvents(this), this);

    //Loads the base Players, Groups, and Worlds folders
    pfh.createBaseFolders();

    //Loads the worlds into a list
    Utils.loadWorlds();

    //If World folders were created and Group files created, enable
    if(!pfh.createSubFolders()){
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

    //reloads all permissions after 2 seconds
    reloadHandler();
  }

  //Called when GeneralPermissions is disabled
  @Override
  public void onDisable(){ Utils.disablePlugin(); } //Removes all permissions from online players and removes prefixes from HashMap prefixes

  //Handles a /reload command. Delays re-assigning permissions until reload is complete.
  private final void reloadHandler(){
    getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
      @Override
      public void run(){
        for(Player p : getServer().getOnlinePlayers()){
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
    }, 2*20);
  }

  //vvv Permission Stuff For WEPIF vvv

  //All sk89q hasPermission methods default here
  @Override
  public final boolean hasPermission(String player, String permission){
    UUID pu = Utils.uuids.get(player);
    boolean has = false;
    String[] parts = permission.split("\\.");  // {commandbook, home, set, *} for commandbook.home.set.*
    String combine = parts[0];
    String star = "*";

    if(Utils.permissions.get(pu) == null){ return false; }

    Set<String> playerPerms = Utils.permissions.get(pu);

    if(playerPerms.contains("-"+star) || playerPerms.contains("-"+combine+".*") || playerPerms.contains("-"+permission)){ return false; }

    if(playerPerms.contains("+*") || playerPerms.contains("+"+combine+".*")){ has = true; }

    for(int i = 1; i < parts.length; i++){
      combine = combine + "." + parts[i];
      if(parts[i].equals("*") || i == parts.length-1){
        if(playerPerms.contains("+"+combine)){
          has = true;
        }
        if(playerPerms.contains("-"+combine)){ return false; }
      }
      else{
        if(playerPerms.contains("+"+combine+".*")){
          has = true;
        }
        if(playerPerms.contains("-"+combine+".*")){ return false; }
      }
    }
    return has;
  }

  //sk89q method to check permission in a world
  @Override
  public final boolean hasPermission(String worldName, String player, String permission){
    return hasPermission(player, permission);
  }

  //sk89q method to check if a player is in a group
  @Override
  @SuppressWarnings("deprecation")
  public final boolean inGroup(String player, String group){
    UUID pu = Bukkit.getOfflinePlayer(player).getUniqueId();
    String rank = Commands.getRank(pu).toLowerCase();
    if(rank.equals(group.toLowerCase())){
      return true;
    }
    return false;
  }

  //sk89q method to get the groups a player is part of
  @Override
  public final String[] getGroups(String player){
    //String rank = GeneralPermissionsCommands.getRank(pu).toLowerCase();
    return new String[] {"None"};
  }

  //sk89q method to check if a player has permission
  @Override
  public final boolean hasPermission(OfflinePlayer player, String permission){
    //Offline player, we don't care
    return hasPermission(player.getPlayer().getName(), permission);
  }

  //sk89q method to check if a player has permission
  @Override
  public final boolean hasPermission(String worldName, OfflinePlayer player, String permission){
    //Offline player, we don't care
    return hasPermission(player.getPlayer().getName(), permission);
  }

  //sk89q method to check if a player is in a group
  @Override
  public final boolean inGroup(OfflinePlayer player, String group){
    //Offline player, we don't care
    return false;
  }

  //sk89q method to get the groups a player is a part of
  @Override
  public final String[] getGroups(OfflinePlayer player){
    //Offline player, we don't care
    return null;
  }
}