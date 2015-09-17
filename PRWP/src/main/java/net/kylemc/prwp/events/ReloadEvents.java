package net.kylemc.prwp.events;

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

import net.kylemc.prwp.PRWP;
import net.kylemc.prwp.commands.Commands;
import net.kylemc.prwp.files.PermFileHandler;
import net.kylemc.prwp.utils.PermThread;
import net.kylemc.prwp.utils.Utils;

import org.bukkit.configuration.file.YamlConfiguration;
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

	public ReloadEvents(PRWP instance)
	{
		plugin = instance;
	}

	@EventHandler
	private void onPlayerJoin(PlayerJoinEvent event)
	{
		Player p = event.getPlayer();
		UUID pu = p.getUniqueId();
		String world = p.getWorld().getName();
		Utils.uuids.put(p.getName(), pu);

		if (p.isOp()) {
			p.setAllowFlight(true);
		}

		p.setWalkSpeed(setSpeed(world));

		YamlConfiguration YC = YamlConfiguration.loadConfiguration(PermFileHandler.namesFile);
		String name = YC.getString(pu.toString());

		if ((name == null) || (!name.equals(p.getName()))) {
			YC.set(pu.toString(), p.getName());
			try {
				YC.save(PermFileHandler.namesFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try
			{
				File playerFile = new File(Commands.playersFolder, pu + ".txt");
				String text = "";

				if (playerFile.exists()) {
					BufferedReader reader = new BufferedReader(new FileReader(playerFile));
					String line = "";

					text = text + reader.readLine() + Utils.newLine();
					text = text + "Name: " + p.getName() + Utils.newLine();
					reader.readLine();
					text = text + reader.readLine() + Utils.newLine();
					text = text + reader.readLine() + Utils.newLine();
					text = text + reader.readLine() + Utils.newLine();

					while ((line = reader.readLine()) != null)
					{
						text = text + line + Utils.newLine();
					}
					reader.close();
				}
				else {
					text = text + "Rank: " + Utils.groupNames[0] + Utils.newLine();
					text = text + "Name: " + p.getName() + Utils.newLine();
					text = text + Utils.newLine();
					text = text + "Permissions:" + Utils.newLine();
					text = text + "------------" + Utils.newLine();
				}

				FileWriter writer = new FileWriter(playerFile);
				writer.write(text);
				writer.close();
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		setPermissions(p, pu, world);
	}

	@EventHandler
	private void onPlayerQuit(PlayerQuitEvent event)
	{
		removePlayer(event.getPlayer());
	}

	@EventHandler
	private void onPlayerKick(PlayerKickEvent event)
	{
		removePlayer(event.getPlayer());
	}

	@EventHandler
	private void onSwitchWorlds(PlayerChangedWorldEvent event)
	{
		Player p = event.getPlayer();
		UUID pu = p.getUniqueId();


		PermissionAttachment attachment1 = Utils.players.get(pu);
		p.removeAttachment(attachment1);
		Utils.players.remove(pu);
		Utils.permissions.remove(pu);

		String world = p.getWorld().getName();

		p.setWalkSpeed(setSpeed(world));


		setPermissions(p, pu, world);
	}

	private final void removePlayer(Player p)
	{
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

	private final float setSpeed(String worldName)
	{
		if (worldName.equals("Hyrule")) {
			return 0.3F;
		}
		return 0.2F;
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