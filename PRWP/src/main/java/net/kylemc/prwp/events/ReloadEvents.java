package net.kylemc.prwp.events;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.kylemc.prwp.PRWP;
import net.kylemc.prwp.utils.PermThread;
import net.kylemc.prwp.utils.Utils;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;

public class ReloadEvents implements Listener
{
	private final PRWP plugin;
	ExecutorService service = Executors.newFixedThreadPool(30);

	public ReloadEvents(PRWP instance){
		plugin = instance;
	}

	@EventHandler
	private void onPlayerJoin(PlayerJoinEvent event){
		Player p = event.getPlayer();
		UUID pu = p.getUniqueId();
		String world = p.getWorld().getName();
		Utils.uuids.put(p.getName(), pu);

		String name = Utils.getNames().getString(pu.toString());

		//First Join
		if(name == null){
			Utils.setNameValue(pu, p.getName());
			Utils.setPlayerRankValue(pu, "");
			name = p.getName();
		}

		//Name Change
		if (!name.equals(p.getName())) {
			Utils.setNameValue(pu, p.getName());
		}

		setPermissions(p, pu, world);
	}

	@EventHandler
	private void onPlayerQuit(PlayerQuitEvent event){
		removePlayer(event.getPlayer());
	}

	@EventHandler
	private void onPlayerKick(PlayerKickEvent event){
		removePlayer(event.getPlayer());
	}

	@EventHandler
	private void onSwitchWorlds(PlayerChangedWorldEvent event){
		Player p = event.getPlayer();
		UUID pu = p.getUniqueId();

		PermissionAttachment attachment1 = Utils.players.get(pu);
		p.removeAttachment(attachment1);
		Utils.players.remove(pu);
		Utils.permissions.remove(pu);

		String world = p.getWorld().getName();

		setPermissions(p, pu, world);
	}

	private final void removePlayer(Player p){
		UUID pu = Utils.uuids.get(p.getName());
		PermissionAttachment attachment = Utils.players.get(pu);

		Utils.uuids.remove(p.getName());
		Utils.players.remove(pu);
		Utils.prefixes.remove(pu);
		Utils.permissions.remove(pu);

		if (attachment != null) {
			p.removeAttachment(attachment);
		}
	}

	private void setPermissions(Player p, UUID pu, String world) {
		PermissionAttachment attachment = p.addAttachment(plugin);

		PermThread permissions = new PermThread(pu, world);
		Future<Set<String>> future = service.submit(permissions);
		try
		{
			Set<String> threadPerms = future.get();
			for (String permission : threadPerms) {
				attachment.setPermission(permission, true);
			}

			Utils.players.put(pu, attachment);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
}