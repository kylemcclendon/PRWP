package main.java.net.kylemc.prwp.events;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import main.java.net.kylemc.prwp.PRWP;
import main.java.net.kylemc.prwp.commands.Commands;
import main.java.net.kylemc.prwp.files.PermFileHandler;
import main.java.net.kylemc.prwp.utils.PermThread;
import main.java.net.kylemc.prwp.utils.Utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;

public class ReloadEvents implements Listener{

  private final PRWP plugin;
  ExecutorService service = Executors.newFixedThreadPool(30);

  //GeneralPermissionsReloadEvents Constructor
  public ReloadEvents(PRWP instance){
    plugin = instance;
  }

  //Handles when a player joins
  @EventHandler
  private void onPlayerJoin(PlayerJoinEvent event){
    //Assign permissions to player when they join
    final Player p = event.getPlayer();
    final UUID pu = p.getUniqueId();
    final String world = p.getWorld().getName();

    Utils.uuids.put(p.getName(), pu);

    if(p.isOp()){
      p.setAllowFlight(true);
    }

    p.setWalkSpeed(setSpeed(world));

    YamlConfiguration YC = YamlConfiguration.loadConfiguration(PermFileHandler.namesFile);
    String name = YC.getString(pu.toString());

    if(name == null || !(name.equals(p.getName()))){
      YC.set(pu.toString(), p.getName());
      try {
        YC.save(PermFileHandler.namesFile);
      } catch (IOException e) {
        e.printStackTrace();
      }

      try{
        File playerFile = new File(Commands.playersFolder, pu + ".txt");
        String text = "";

        if(playerFile.exists()){
          BufferedReader reader = new BufferedReader(new FileReader(playerFile));
          String line = "";

          text += reader.readLine() + Utils.newLine(); //Rank
          text += "Name: " + p.getName() + Utils.newLine();
          reader.readLine(); //Name, which is replaced
          text += reader.readLine() + Utils.newLine(); //blank line
          text += reader.readLine() + Utils.newLine(); //"Permissions:"
          text += reader.readLine() + Utils.newLine(); //dotted line

          while((line = reader.readLine()) != null){
            //Each permission
            text += line + Utils.newLine();
          }
          reader.close();
        }
        else{
          text += "Rank: " + Utils.groupNames[0] + Utils.newLine(); //Rank
          text += "Name: " + p.getName() + Utils.newLine(); //Name Line
          text += Utils.newLine(); //Blank line
          text += "Permissions:" + Utils.newLine(); //Permissions:
          text += "------------" + Utils.newLine(); //dotted line
        }

        FileWriter writer = new FileWriter(playerFile);
        writer.write(text);
        writer.close();
      }
      catch (IOException ioe){
        ioe.printStackTrace();
      }
    }

    setPermissions(p, pu, world);
  }

  //Handles when a player quits
  @EventHandler
  private void onPlayerQuit(PlayerQuitEvent event){
    removePlayer(event.getPlayer());
  }

  //Handles when a player is kicked
  @EventHandler
  private void onPlayerKick(PlayerKickEvent event){
    removePlayer(event.getPlayer());
  }

  //Handles when a player changes worlds
  @EventHandler
  private void onSwitchWorlds(PlayerChangedWorldEvent event){
    Player p = event.getPlayer();
    final UUID pu = p.getUniqueId();

    //Removes permissions from player
    PermissionAttachment attachment1 = Utils.players.get(pu);
    p.removeAttachment(attachment1);
    Utils.players.remove(pu);
    Utils.permissions.remove(pu);

    final String world = p.getWorld().getName();

    p.setWalkSpeed(setSpeed(world));

    //Adds permissions to player
    setPermissions(p, pu, world);
  }

  //Remove permissions and prefixes from player
  private final void removePlayer(Player p){
    UUID pu = Utils.uuids.get(p.getName());
    PermissionAttachment attachment = Utils.players.get(pu);

    Utils.uuids.remove(p.getName());
    Utils.players.remove(pu);
    Utils.prefixes.remove(pu);
    Utils.permissions.remove(pu);

    if(attachment != null){
      p.removeAttachment(attachment);
    }
  }

  //Sets the player's walk speed
  private final float setSpeed(String worldName){
    if(worldName.equals("Hyrule")){
      return (float) 0.3;
    }
    return (float) 0.2;
  }

  private void setPermissions(Player p, final UUID pu, final String world){
    final PermissionAttachment attachment = p.addAttachment(plugin);

    PermThread permissions = new PermThread(pu,world);
    Future<Set<String>> future = service.submit(permissions);
    Set<String> threadPerms;
    try {
      threadPerms = future.get();
      for(String permission: threadPerms){
        attachment.setPermission(permission, true);
      }

      Utils.players.put(pu, attachment);
    }
    catch (InterruptedException e) {e.printStackTrace();}
    catch (ExecutionException e) {e.printStackTrace();}

    /* Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new BukkitRunnable()
    {
      @Override
      public void run(){
        Set<String> perms = GetPermissions.collectPermissions(pu, world);

        for(String permission : perms)
        {
          attachment.setPermission(permission, true);
        }

        Utils.players.put(pu, attachment);
      }
    });*/
  }
}