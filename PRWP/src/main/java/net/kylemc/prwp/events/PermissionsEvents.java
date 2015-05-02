package main.java.net.kylemc.prwp.events;

import java.util.UUID;

import main.java.net.kylemc.prwp.utils.Utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public final class PermissionsEvents implements Listener{

  private final String[] creatures = {"MAGMA_CUBE","GHAST"};

  //Handles prefix additions when chatting
  @EventHandler
  private void onChat(AsyncPlayerChatEvent event){
    if(event.isCancelled()){
      return;
    }
    UUID pu = event.getPlayer().getUniqueId();
    String prefix = Utils.prefixes.get(pu);
    String alteredPrefix = ChatColor.translateAlternateColorCodes('&',prefix);
    String eventFormat = "<" + alteredPrefix + "%s" + "> " + "%s";
    event.setFormat(eventFormat);
  }

  //Handles breaking of blocks
  @EventHandler
  private void blockBreaker(BlockBreakEvent event){
    if(event.getPlayer().hasPermission("permissions.restrict.nobuild") && !(event.getPlayer().hasPermission("permissions.override"))){
      event.setCancelled(true);
    }
  }

  //Handles placing of blocks
  @EventHandler
  private void blockPlacer(BlockPlaceEvent event){
    if(event.getPlayer().hasPermission("permissions.restrict.nobuild") && !(event.getPlayer().hasPermission("permissions.override"))){
      event.setCancelled(true);
    }
  }

  //Handles bucket fill/empty events
  @EventHandler
  private void bucketEvents(PlayerBucketEmptyEvent event){
    if(event.getPlayer().hasPermission("permissions.restrict.nobuild") && !(event.getPlayer().hasPermission("permissions.override"))){
      event.setCancelled(true);
    }
  }
  @EventHandler
  private void bucketEvents(PlayerBucketFillEvent event){
    if(event.getPlayer().hasPermission("permissions.restrict.nobuild") && !(event.getPlayer().hasPermission("permissions.override"))){
      event.setCancelled(true);
    }
  }

  //Handles right and left clicks of blocks
  @SuppressWarnings("deprecation")
  @EventHandler
  private void blockInteraction(PlayerInteractEvent event){
    //Event for block interaction, placement, and removal
    if(event.getClickedBlock() == null){ return; } //Air, Water, or Lava block

    Player p = event.getPlayer();

    //Permission to allow interaction with a block even if black-listed.
    String perm = "interact." + event.getClickedBlock().getTypeId();

    if(!(p.hasPermission("permissions.restrict.nointeract")) || p.hasPermission("permissions.override")){ return; } //Player is free to interact with any block.

    //Player has the permission 'permissions.nointeract'
    if(p.hasPermission(perm)){ return; } //Player has the permissions to interact with this specific block

    //Player has the permission 'permissions.nointeract'
    //Player doesn't have the block_id permission
    if(event.getAction() != Action.RIGHT_CLICK_BLOCK){ return; } //This interaction isn't right-clicking a block, we don't care

    //Player has the permission 'permissions.nointeract'
    //Player doesn't have the block_id permission
    //Player is right-clicking a block
    if(Utils.contains(Integer.toString(event.getClickedBlock().getTypeId()), Utils.bbl)){
      //Block is blacklisted and player cannot interact with the block, cancel the interaction.
      event.setCancelled(true);
      return;
    }

    //We should only reach this point if they are right-clicking a 'white-listed' block
    return;
  }

  //Stop players from picking up items
  @EventHandler
  private void itemPickup(PlayerPickupItemEvent event){
    if(event.getPlayer().hasPermission("permissions.restrict.nopickup") && !(event.getPlayer().hasPermission("permissions.override"))){
      event.setCancelled(true);
    }
  }

  //Stop players from dropping their items
  @EventHandler
  private void itemDrop(PlayerDropItemEvent event){
    if(event.getPlayer().hasPermission("permissions.restrict.nodrop") && !(event.getPlayer().hasPermission("permissions.override"))){
      event.setCancelled(true);
    }
  }

  //Stop players from getting hungry
  @EventHandler
  private void noHunger(FoodLevelChangeEvent event){
    if(event.getEntity().hasPermission("permissions.restrict.nohunger") && !(event.getEntity().hasPermission("permissions.override"))){
      event.setCancelled(true);
    }
  }

  //Stop all damage to players
  @EventHandler
  private void noDamage(EntityDamageEvent event){
    if(!(event.getEntity() instanceof Player)){
      return;
    }

    if(((Player) event.getEntity()).hasPermission("permissions.restrict.nodamage") && !((Player)(event.getEntity())).hasPermission("permissions.override")){
      event.setCancelled(true);
    }
  }

  //Stops players from damaging anything
  @EventHandler
  private void noAttack(EntityDamageByEntityEvent event){
    if(event.getDamager() instanceof Player && ((Player)(event.getDamager())).hasPermission("permissions.restrict.noattack")){
      event.setCancelled(true);
    }
  }

  //Stop mobs from targeting players
  @EventHandler
  private void noFollow(EntityTargetEvent event){
    if(!(event.getTarget() instanceof Player)){
      return;
    }
    Player p = (Player) event.getTarget();
    if(((event.getEntity() instanceof Creature) || Utils.contains(event.getEntity().getType().toString().toUpperCase(), creatures)) && p.hasPermission("permissions.restrict.nofollow") && !(p.hasPermission("permissions.override"))){
      event.setTarget(null);
    }
  }

  //Stop players from inserting/removing items into/from chests
  @EventHandler
  private void noChestRemoval(InventoryClickEvent event){
    if(event.getWhoClicked().hasPermission("permissions.restrict.noremove") && !(event.getWhoClicked().hasPermission("permissions.override")) && event.getInventory().getType() == InventoryType.CHEST){
      event.setCancelled(true);
    }
  }
}