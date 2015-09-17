package net.kylemc.prwp.events;

import net.kylemc.prwp.utils.Utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

public final class PermissionsEvents implements Listener
{
	private final String[] creatures = { "MAGMA_CUBE", "GHAST" };

	@EventHandler
	private void onChat(AsyncPlayerChatEvent event)
	{
		if (event.isCancelled()) {
			return;
		}
		java.util.UUID pu = event.getPlayer().getUniqueId();
		String prefix = Utils.prefixes.get(pu);
		String alteredPrefix = ChatColor.translateAlternateColorCodes('&', prefix);
		String eventFormat = "<" + alteredPrefix + "%s" + "> " + "%s";
		event.setFormat(eventFormat);
	}

	@EventHandler
	private void blockBreaker(BlockBreakEvent event)
	{
		if ((event.getPlayer().hasPermission("permissions.restrict.nobuild")) && (!event.getPlayer().hasPermission("permissions.override"))) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	private void blockPlacer(BlockPlaceEvent event)
	{
		if ((event.getPlayer().hasPermission("permissions.restrict.nobuild")) && (!event.getPlayer().hasPermission("permissions.override"))) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	private void bucketEvents(PlayerBucketEmptyEvent event)
	{
		if ((event.getPlayer().hasPermission("permissions.restrict.nobuild")) && (!event.getPlayer().hasPermission("permissions.override"))) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	private void bucketEvents(PlayerBucketFillEvent event) {
		if ((event.getPlayer().hasPermission("permissions.restrict.nobuild")) && (!event.getPlayer().hasPermission("permissions.override"))) {
			event.setCancelled(true);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	private void blockInteraction(PlayerInteractEvent event)
	{
		if (event.getClickedBlock() == null) { return;
		}
		Player p = event.getPlayer();

		String perm = "interact." + event.getClickedBlock().getTypeId();

		if ((!p.hasPermission("permissions.restrict.nointeract")) || (p.hasPermission("permissions.override"))) { return;
		}

		if (p.hasPermission(perm)) { return;
		}


		if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) { return;
		}



		if (Utils.contains(Integer.toString(event.getClickedBlock().getTypeId()), Utils.bbl))
		{
			event.setCancelled(true);
			return;
		}
	}

	@EventHandler
	private void itemPickup(PlayerPickupItemEvent event)
	{
		if ((event.getPlayer().hasPermission("permissions.restrict.nopickup")) && (!event.getPlayer().hasPermission("permissions.override"))) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	private void itemDrop(PlayerDropItemEvent event)
	{
		if ((event.getPlayer().hasPermission("permissions.restrict.nodrop")) && (!event.getPlayer().hasPermission("permissions.override"))) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	private void noHunger(FoodLevelChangeEvent event)
	{
		if ((event.getEntity().hasPermission("permissions.restrict.nohunger")) && (!event.getEntity().hasPermission("permissions.override"))) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	private void noDamage(EntityDamageEvent event)
	{
		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		if ((((Player)event.getEntity()).hasPermission("permissions.restrict.nodamage")) && (!((Player)event.getEntity()).hasPermission("permissions.override"))) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	private void noAttack(EntityDamageByEntityEvent event)
	{
		if (((event.getDamager() instanceof Player)) && (((Player)event.getDamager()).hasPermission("permissions.restrict.noattack"))) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	private void noFollow(EntityTargetEvent event)
	{
		if (!(event.getTarget() instanceof Player)) {
			return;
		}
		Player p = (Player)event.getTarget();
		if ((((event.getEntity() instanceof Creature)) || (Utils.contains(event.getEntity().getType().toString().toUpperCase(), creatures))) && (p.hasPermission("permissions.restrict.nofollow")) && (!p.hasPermission("permissions.override"))) {
			event.setTarget(null);
		}
	}

	@EventHandler
	private void noChestRemoval(InventoryClickEvent event)
	{
		if ((event.getWhoClicked().hasPermission("permissions.restrict.noremove")) && (!event.getWhoClicked().hasPermission("permissions.override")) && (event.getInventory().getType() == InventoryType.CHEST)) {
			event.setCancelled(true);
		}
	}
}