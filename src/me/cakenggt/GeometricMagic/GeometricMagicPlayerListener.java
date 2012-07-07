/**
 * GeometricMagic allows players to draw redstone circles on the ground to do things such as teleport and transmute blocks.
 * Copyright (C) 2012  Alec Cox (cakenggt), Andrew Stevanus (Hoot215) <hoot893@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.cakenggt.GeometricMagic;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import net.h31ix.anticheat.api.AnticheatAPI;
import net.h31ix.anticheat.manage.CheckType;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginManager;

public class GeometricMagicPlayerListener implements Listener {
	static GeometricMagic plugin = new GeometricMagic();

	public GeometricMagicPlayerListener(GeometricMagic instance) {
		plugin = instance;
	}

	public static Economy economy = null;
	private static HashMap<String, Long> mapCoolDowns = new HashMap<String, Long>();

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {

		// System.out.println("is playerinteractevent");
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
			// System.out.println("doesn't equal click block or click air");
			return;
		}

		boolean sacrifices = false;
		boolean sacrificed = false;
		try {
			sacrifices = GeometricMagic.checkSacrifices(event.getPlayer());
			sacrificed = GeometricMagic.checkSacrificed(event.getPlayer());
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		if (sacrificed) {
			event.getPlayer().sendMessage("You have sacrificed your alchemy abilities forever.");
			return;
		}

		Block actBlock = event.getPlayer().getLocation().getBlock();

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {

			if (event.getClickedBlock().getType() == Material.WORKBENCH && sacrifices) {
				// cancel event instead of turning block into air
				event.setCancelled(true);
			}
			actBlock = event.getClickedBlock();
		}

		if (event.getAction() == Action.RIGHT_CLICK_AIR && event.getPlayer().getItemInHand().getType() == Material.FLINT) {
			actBlock = event.getPlayer().getTargetBlock(null, 120);
		}

		Player player = event.getPlayer();
		World world = player.getWorld();
		try {
			isCircle(player, world, actBlock);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void isCircle(Player player, World world, Block actBlock) throws IOException {
		// System.out.println("isCircle?");
		if (actBlock.getType() == Material.REDSTONE_WIRE && player.getItemInHand().getAmount() == 0 && !GeometricMagic.checkSacrificed(player)) {
			// System.out.println("isCircle");
			circleChooser(player, world, actBlock);
		}
		boolean sacrifices = GeometricMagic.checkSacrifices(player);
		if (player.getItemInHand().getType() == Material.FLINT && sacrifices && !GeometricMagic.checkSacrificed(player)) {

			// set circle cool down
			String coolDownConfig = plugin.getConfig().getString("setcircles.cooldown").toString();
			int coolDown = Integer.parseInt(coolDownConfig);
			if (mapCoolDowns.containsKey(player.getName() + " set circle")) {
				long diff = (System.currentTimeMillis() - mapCoolDowns.get(player.getName() + " set circle")) / 1000;
				if (diff < coolDown) {
					// still cooling down
					player.sendMessage("You have to wait before you can do that again.");
					return;
				}
			}
			mapCoolDowns.put(player.getName() + " set circle", System.currentTimeMillis());

			File myFile = new File("plugins/GeometricMagic/sacrifices.txt");
			Scanner inputFile = new Scanner(myFile);
			String circle = "[0, 0, 0, 0]";
			while (inputFile.hasNextLine()) {
				String name = inputFile.nextLine();
				if (name.equals(player.getName())) {
					circle = inputFile.nextLine();
				} else
					inputFile.nextLine();
			}
			inputFile.close();

			try {
				// exempt player from AntiCheat check
				if (Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null) {
					AnticheatAPI.exemptPlayer(player, CheckType.FAST_PLACE);
					AnticheatAPI.exemptPlayer(player, CheckType.FAST_BREAK);
					AnticheatAPI.exemptPlayer(player, CheckType.LONG_REACH);
					AnticheatAPI.exemptPlayer(player, CheckType.NO_SWING);
				}

				setCircleEffects(player, player.getWorld(), player.getLocation().getBlock(), actBlock, circle);

				// unexempt player from AntiCheat check
				if (Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null) {
					AnticheatAPI.unexemptPlayer(player, CheckType.FAST_PLACE);
					AnticheatAPI.unexemptPlayer(player, CheckType.FAST_BREAK);
					AnticheatAPI.unexemptPlayer(player, CheckType.LONG_REACH);
					AnticheatAPI.unexemptPlayer(player, CheckType.NO_SWING);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		} else
			return;
	}

	public static void circleChooser(Player player, World world, Block actBlock) {
		// System.out.println("circleChooser");
		Block northBlock = actBlock.getRelative(0, 0, -1);
		Block southBlock = actBlock.getRelative(0, 0, 1);
		Block eastBlock = actBlock.getRelative(1, 0, 0);
		Block westBlock = actBlock.getRelative(-1, 0, 0);

		// teleportation circle
		if (northBlock.getType() == Material.REDSTONE_WIRE && southBlock.getType() == Material.REDSTONE_WIRE && eastBlock.getType() == Material.REDSTONE_WIRE
				&& westBlock.getType() == Material.REDSTONE_WIRE) {
			if (player.hasPermission("geometricmagic.teleportation")) {
				// System.out.println("teleportation");
				teleportationCircle(player, world, actBlock);
			} else
				player.sendMessage("You do not have permission to use this circle");

			// micro circle
		} else if (northBlock.getType() != Material.REDSTONE_WIRE && southBlock.getType() != Material.REDSTONE_WIRE && eastBlock.getType() != Material.REDSTONE_WIRE
				&& westBlock.getType() != Material.REDSTONE_WIRE && actBlock.getRelative(-3, 0, 0).getType() != Material.REDSTONE_WIRE
				&& actBlock.getRelative(3, 0, 0).getType() != Material.REDSTONE_WIRE && actBlock.getRelative(0, 0, -3).getType() != Material.REDSTONE_WIRE
				&& actBlock.getRelative(0, 0, 3).getType() != Material.REDSTONE_WIRE) {
			if (player.hasPermission("geometricmagic.micro")) {
				// System.out.println("micro");
				microCircle(player, world, actBlock);
			} else
				player.sendMessage("You do not have permission to use this circle");

			// transmutation circle
		} else if ((northBlock.getType() == Material.REDSTONE_WIRE && southBlock.getType() == Material.REDSTONE_WIRE && eastBlock.getType() != Material.REDSTONE_WIRE && westBlock.getType() != Material.REDSTONE_WIRE)
				|| (northBlock.getType() != Material.REDSTONE_WIRE && southBlock.getType() != Material.REDSTONE_WIRE && eastBlock.getType() == Material.REDSTONE_WIRE && westBlock.getType() == Material.REDSTONE_WIRE)) {

			// transmutation circle size permissions
			// - allows use of all circles smaller than then the max
			// size permission node they have
			int circleSize = 1;
			if (player.hasPermission("geometricmagic.transmutation.*") || player.hasPermission("geometricmagic.transmutation.9")) {
				circleSize = 9;
			} else if (player.hasPermission("geometricmagic.transmutation.7")) {
				circleSize = 7;
			} else if (player.hasPermission("geometricmagic.transmutation.5")) {
				circleSize = 5;
			} else if (player.hasPermission("geometricmagic.transmutation.3")) {
				circleSize = 3;
			} else if (player.hasPermission("geometricmagic.transmutation.1")) {
				circleSize = 1;
			} else {
				circleSize = 0;
				player.sendMessage("You do not have permission to use this circle");
			}

			// System.out.println("circleSize:" + circleSize);

			// transmute cool down
			String coolDownConfig = plugin.getConfig().getString("transmutation.cooldown").toString();
			int coolDown = Integer.parseInt(coolDownConfig);
			if (mapCoolDowns.containsKey(player.getName() + " transmute circle")) {
				long diff = (System.currentTimeMillis() - mapCoolDowns.get(player.getName() + " transmute circle")) / 1000;
				if (diff < coolDown) {
					// still cooling down
					player.sendMessage("You have to wait before you can do that again.");
					return;
				}
			}
			mapCoolDowns.put(player.getName() + " transmute circle", System.currentTimeMillis());

			if (circleSize > 0) {
				transmutationCircle(player, world, actBlock, circleSize);
			}

			// set circle
		} else if (northBlock.getType() != Material.REDSTONE_WIRE && southBlock.getType() != Material.REDSTONE_WIRE && eastBlock.getType() != Material.REDSTONE_WIRE
				&& westBlock.getType() != Material.REDSTONE_WIRE && actBlock.getRelative(-3, 0, 0).getType() == Material.REDSTONE_WIRE
				&& actBlock.getRelative(3, 0, 0).getType() == Material.REDSTONE_WIRE && actBlock.getRelative(0, 0, -3).getType() == Material.REDSTONE_WIRE
				&& actBlock.getRelative(0, 0, 3).getType() == Material.REDSTONE_WIRE) {

			if (player.hasPermission("geometricmagic.set")) {
				// set circle cool down
				String coolDownConfig = plugin.getConfig().getString("setcircles.cooldown").toString();
				int coolDown = Integer.parseInt(coolDownConfig);
				if (mapCoolDowns.containsKey(player.getName() + " set circle")) {
					long diff = (System.currentTimeMillis() - mapCoolDowns.get(player.getName() + " set circle")) / 1000;
					if (diff < coolDown) {
						// still cooling down
						player.sendMessage("You have to wait before you can do that again.");
						return;
					}
				}
				mapCoolDowns.put(player.getName() + " set circle", System.currentTimeMillis());

				setCircleRemote(player, world, actBlock);
			} else
				player.sendMessage("You do not have permission to use this circle");

			// no circle
		} else {
			return;
		}
	}

	public static void teleportationCircle(Player player, World world, Block actBlock) {

		// activation block in center
		Location actPoint = actBlock.getLocation();

		// init some variables
		int na = 0, nb = 0, ea = 0, eb = 0, sa = 0, sb = 0, wa = 0, wb = 0, nc = 0, ec = 0, sc = 0, wc = 0;

		// set core blocks to air
		actBlock.setType(Material.AIR);
		actBlock.getRelative(1, 0, 0).setType(Material.AIR);
		actBlock.getRelative(-1, 0, 0).setType(Material.AIR);
		actBlock.getRelative(0, 0, 1).setType(Material.AIR);
		actBlock.getRelative(0, 0, -1).setType(Material.AIR);

		// count and track all redstone blocks
		Block curBlock = actBlock.getRelative(0, 0, -1);
		while (curBlock.getRelative(0, 0, -1).getType() == Material.REDSTONE_WIRE) {
			na++;
			curBlock = curBlock.getRelative(0, 0, -1);
		}
		Block fineBlock = curBlock;
		while (fineBlock.getRelative(-1, 0, 0).getType() == Material.REDSTONE_WIRE) {
			nb++;
			fineBlock = fineBlock.getRelative(-1, 0, 0);
		}
		fineBlock = curBlock;
		while (fineBlock.getRelative(1, 0, 0).getType() == Material.REDSTONE_WIRE) {
			nc++;
			fineBlock = fineBlock.getRelative(1, 0, 0);
		}

		curBlock = actBlock.getRelative(1, 0, 0);
		while (curBlock.getRelative(1, 0, 0).getType() == Material.REDSTONE_WIRE) {
			ea++;
			curBlock = curBlock.getRelative(1, 0, 0);
		}
		fineBlock = curBlock;
		while (fineBlock.getRelative(0, 0, -1).getType() == Material.REDSTONE_WIRE) {
			eb++;
			fineBlock = fineBlock.getRelative(0, 0, -1);
		}
		fineBlock = curBlock;
		while (fineBlock.getRelative(0, 0, 1).getType() == Material.REDSTONE_WIRE) {
			ec++;
			fineBlock = fineBlock.getRelative(0, 0, 1);
		}

		curBlock = actBlock.getRelative(0, 0, 1);
		while (curBlock.getRelative(0, 0, 1).getType() == Material.REDSTONE_WIRE) {
			sa++;
			curBlock = curBlock.getRelative(0, 0, 1);
		}
		fineBlock = curBlock;
		while (fineBlock.getRelative(1, 0, 0).getType() == Material.REDSTONE_WIRE) {
			sb++;
			fineBlock = fineBlock.getRelative(1, 0, 0);
		}
		fineBlock = curBlock;
		while (fineBlock.getRelative(-1, 0, 0).getType() == Material.REDSTONE_WIRE) {
			sc++;
			fineBlock = fineBlock.getRelative(-1, 0, 0);
		}

		curBlock = actBlock.getRelative(-1, 0, 0);
		while (curBlock.getRelative(-1, 0, 0).getType() == Material.REDSTONE_WIRE) {
			wa++;
			curBlock = curBlock.getRelative(-1, 0, 0);
		}
		fineBlock = curBlock;
		while (fineBlock.getRelative(0, 0, 1).getType() == Material.REDSTONE_WIRE) {
			wb++;
			fineBlock = fineBlock.getRelative(0, 0, 1);
		}
		fineBlock = curBlock;
		while (fineBlock.getRelative(0, 0, -1).getType() == Material.REDSTONE_WIRE) {
			wc++;
			fineBlock = fineBlock.getRelative(0, 0, -1);
		}

		// set all redstone to air
		curBlock = actBlock.getRelative(0, 0, -1);
		for (int c = 0; c < na; c++) {
			curBlock = curBlock.getRelative(0, 0, -1);
			curBlock.setType(Material.AIR);
		}
		fineBlock = curBlock;
		for (int c = 0; c < nb; c++) {
			fineBlock = fineBlock.getRelative(-1, 0, 0);
			fineBlock.setType(Material.AIR);
		}
		fineBlock = curBlock;
		for (int c = 0; c < nc; c++) {
			fineBlock = fineBlock.getRelative(1, 0, 0);
			fineBlock.setType(Material.AIR);
		}

		curBlock = actBlock.getRelative(1, 0, 0);
		for (int c = 0; c < ea; c++) {
			curBlock = curBlock.getRelative(1, 0, 0);
			curBlock.setType(Material.AIR);
		}
		fineBlock = curBlock;
		for (int c = 0; c < eb; c++) {
			fineBlock = fineBlock.getRelative(0, 0, -1);
			fineBlock.setType(Material.AIR);
		}
		fineBlock = curBlock;
		for (int c = 0; c < ec; c++) {
			fineBlock = fineBlock.getRelative(0, 0, 1);
			fineBlock.setType(Material.AIR);
		}

		curBlock = actBlock.getRelative(0, 0, 1);
		for (int c = 0; c < sa; c++) {
			curBlock = curBlock.getRelative(0, 0, 1);
			curBlock.setType(Material.AIR);
		}
		fineBlock = curBlock;
		for (int c = 0; c < sb; c++) {
			fineBlock = fineBlock.getRelative(1, 0, 0);
			fineBlock.setType(Material.AIR);
		}
		fineBlock = curBlock;
		for (int c = 0; c < sc; c++) {
			fineBlock = fineBlock.getRelative(-1, 0, 0);
			fineBlock.setType(Material.AIR);
		}

		curBlock = actBlock.getRelative(-1, 0, 0);
		for (int c = 0; c < wa; c++) {
			curBlock = curBlock.getRelative(-1, 0, 0);
			curBlock.setType(Material.AIR);
		}
		fineBlock = curBlock;
		for (int c = 0; c < wb; c++) {
			fineBlock = fineBlock.getRelative(0, 0, 1);
			fineBlock.setType(Material.AIR);
		}
		fineBlock = curBlock;
		for (int c = 0; c < wc; c++) {
			fineBlock = fineBlock.getRelative(0, 0, -1);
			fineBlock.setType(Material.AIR);
		}

		// find out teleport location and modify it

		// north negative z
		// south positive z
		// east positive x
		// west negative x
		int z = ((sa * 100 + sb) - (na * 100 + nb));
		int x = ((ea * 100 + eb) - (wa * 100 + wb));
		int y = nc + ec + sc + wc;

		double actPointX = actPoint.getX();
		double actPointZ = actPoint.getZ();

		Location teleLoc = actPoint.add(x, y, z);

		float yaw = player.getLocation().getYaw();
		float pitch = player.getLocation().getPitch();

		teleLoc.setYaw(yaw);
		teleLoc.setPitch(pitch);

		double distance = Math.sqrt(Math.pow(teleLoc.getX() - actPointX, 2) + Math.pow(teleLoc.getZ() - actPointZ, 2));

		double mathRandX = philosopherStoneModifier(player) * distance / 10 * Math.random();
		double mathRandZ = philosopherStoneModifier(player) * distance / 10 * Math.random();

		double randX = (teleLoc.getX() - (0.5 * mathRandX)) + (mathRandX);
		double randZ = (teleLoc.getZ() - (0.5 * mathRandZ)) + (mathRandZ);

		teleLoc.setX(randX);
		teleLoc.setZ(randZ);

		// wait for chunk to be loaded before teleporting player
		while (teleLoc.getWorld().getChunkAt(teleLoc).isLoaded() == false) {
			teleLoc.getWorld().getChunkAt(teleLoc).load(true);
		}

		// teleport player
		player.teleport(teleLoc);

		ItemStack redstonePile = new ItemStack(331, 5 + na + nb + nc + sa + sb + sc + ea + eb + ec + wa + wb + wc);

		teleLoc.getWorld().dropItem(teleLoc, redstonePile);

		actBlock.getWorld().strikeLightningEffect(actBlock.getLocation());
		actBlock.getWorld().strikeLightningEffect(teleLoc);

		return;
	}

	public static void microCircle(Player player, World world, Block actBlock) {
		if (getTransmutationCostSystem(plugin).equalsIgnoreCase("vault")) {
			Economy econ = GeometricMagic.getEconomy();

			// Tell the player how much money they have
			player.sendMessage("You have " + econ.format(getBalance(player)));
		} else if (getTransmutationCostSystem(plugin).equalsIgnoreCase("xp")) {
			// Tell the player how many levels they have
			player.sendMessage("Your experience level is " + player.getLevel());
		}

		List<Entity> entitiesList = player.getNearbyEntities(100, 10, 100);
		for (int i = 0; i < entitiesList.size(); i++) {
			if (entitiesList.get(i) instanceof Arrow) {
				Arrow shotArrow = (Arrow) entitiesList.get(i);
				if (shotArrow.getLocation().getBlock().getType() == Material.REDSTONE_WIRE) {
					Block newActPoint = shotArrow.getLocation().getBlock();
					Player newPlayer = (Player) shotArrow.getShooter();
					circleChooser(newPlayer, world, newActPoint);
				}
			}
		}
	}

	public static void transmutationCircle(Player player, World world, Block actBlock, int circleSize) {
		int halfWidth = 0;
		int fullWidth = 0;
		Location startLoc = actBlock.getLocation();
		Location endLoc = actBlock.getLocation();
		Location circleStart = actBlock.getLocation();
		Location circleEnd = actBlock.getLocation();
		Material fromType = actBlock.getType();
		Material toType = actBlock.getType();
		boolean lightning = false;
		if (actBlock.getRelative(0, 0, -1).getType() == Material.REDSTONE_WIRE && actBlock.getRelative(0, 0, 1).getType() == Material.REDSTONE_WIRE) {
			halfWidth = 0;
			while (actBlock.getRelative(0, 0, -1 * halfWidth).getType() == Material.REDSTONE_WIRE) {
				if (halfWidth > circleSize) {
					break;
				}
				halfWidth++;
			}
			fullWidth = (halfWidth * 2) - 1;
			int dimensionOfEffect = (fullWidth - 2) * (fullWidth - 2);
			if (actBlock.getRelative((fullWidth - 1), 0, 0).getType() == Material.REDSTONE_WIRE) {
				// east
				fromType = actBlock.getLocation().add(halfWidth - 1, 0, -1 * (halfWidth + 1)).getBlock().getType();
				Block toBlock = actBlock.getLocation().add(halfWidth - 1, 0, halfWidth + 1).getBlock();
				toType = toBlock.getType();
				byte toData = toBlock.getData();
				startLoc = actBlock.getLocation().add(fullWidth, 0, -1 * dimensionOfEffect / 2);
				// System.out.println(startLoc);
				endLoc = actBlock.getLocation().add(fullWidth + dimensionOfEffect - 1, dimensionOfEffect - 1, dimensionOfEffect / 2 - 1);
				// System.out.println(endLoc);
				circleStart = actBlock.getLocation().add(1, 0, -1 * (halfWidth - 2));
				// System.out.println(circleStart);
				circleEnd = actBlock.getLocation().add(fullWidth - 2, fullWidth - 3, halfWidth - 2);
				// System.out.println(circleEnd);
				alchemyCheck(fromType, toType, toData, circleStart, circleEnd, startLoc, endLoc, player, fullWidth - 2);
				lightning = true;
			} else if (actBlock.getRelative(-1 * (fullWidth - 1), 0, 0).getType() == Material.REDSTONE_WIRE) {
				// west
				// System.out.println("transmutationCircle west");
				fromType = actBlock.getLocation().add(-1 * (halfWidth - 1), 0, halfWidth + 1).getBlock().getType();
				Block toBlock = actBlock.getLocation().add((-1) * (halfWidth - 1), 0, (-1) * (halfWidth + 1)).getBlock();
				toType = toBlock.getType();
				byte toData = toBlock.getData();
				startLoc = actBlock.getLocation().add(-1 * fullWidth, 0, dimensionOfEffect / 2);
				endLoc = actBlock.getLocation().add(-1 * (fullWidth + dimensionOfEffect) + 1, dimensionOfEffect - 1, -1 * dimensionOfEffect / 2 + 1);
				circleStart = actBlock.getLocation().add(-1, 0, (halfWidth - 2));
				circleEnd = actBlock.getLocation().add(-1 * (fullWidth - 2), fullWidth - 3, -1 * (halfWidth - 2));
				alchemyCheck(fromType, toType, toData, circleStart, circleEnd, startLoc, endLoc, player, fullWidth - 2);
				lightning = true;
			}
		} else if (actBlock.getRelative(1, 0, 0).getType() == Material.REDSTONE_WIRE && actBlock.getRelative(-1, 0, 0).getType() == Material.REDSTONE_WIRE) {
			halfWidth = 0;
			while (actBlock.getRelative(halfWidth, 0, 0).getType() == Material.REDSTONE_WIRE) {
				if (halfWidth > circleSize) {
					break;
				}
				halfWidth++;
			}
			fullWidth = (halfWidth * 2) - 1;
			// System.out
			// .println("half is " + halfWidth + " full is " + fullWidth);
			int dimensionOfEffect = (fullWidth - 2) * (fullWidth - 2);
			if (actBlock.getRelative(0, 0, -1 * (fullWidth - 1)).getType() == Material.REDSTONE_WIRE) {
				// north
				// System.out.println("transmutationCircle north");
				fromType = actBlock.getLocation().add(-1 * (halfWidth + 1), 0, -1 * (halfWidth - 1)).getBlock().getType();
				Block toBlock = actBlock.getLocation().add(halfWidth + 1, 0, -1 * (halfWidth - 1)).getBlock();
				toType = toBlock.getType();
				byte toData = toBlock.getData();
				startLoc = actBlock.getLocation().add(-1 * dimensionOfEffect / 2, 0, -1 * fullWidth);
				endLoc = actBlock.getLocation().add(dimensionOfEffect / 2 - 1, dimensionOfEffect - 1, -1 * (dimensionOfEffect + fullWidth) + 1);
				circleStart = actBlock.getLocation().add(-1 * (halfWidth - 2), 0, -1);
				circleEnd = actBlock.getLocation().add((halfWidth - 2), fullWidth - 3, -1 * (fullWidth - 2));
				alchemyCheck(fromType, toType, toData, circleStart, circleEnd, startLoc, endLoc, player, fullWidth - 2);
				lightning = true;
			} else if (actBlock.getRelative(0, 0, (fullWidth - 1)).getType() == Material.REDSTONE_WIRE) {
				// south
				// System.out.println("transmutationCircle south");
				fromType = actBlock.getLocation().add(halfWidth + 1, 0, halfWidth - 1).getBlock().getType();
				Block toBlock = actBlock.getLocation().add(-1 * (halfWidth + 1), 0, halfWidth - 1).getBlock();
				toType = toBlock.getType();
				byte toData = toBlock.getData();
				startLoc = actBlock.getLocation().add(dimensionOfEffect / 2, 0, fullWidth);
				endLoc = actBlock.getLocation().add(-1 * dimensionOfEffect / 2 + 1, dimensionOfEffect - 1, fullWidth + dimensionOfEffect - 1);
				circleStart = actBlock.getLocation().add(halfWidth - 2, 0, 1);
				circleEnd = actBlock.getLocation().add(-1 * (halfWidth - 2), fullWidth - 3, (fullWidth - 2));
				alchemyCheck(fromType, toType, toData, circleStart, circleEnd, startLoc, endLoc, player, fullWidth - 2);
				lightning = true;
			}
		}
		if (lightning)
			actBlock.getWorld().strikeLightningEffect(actBlock.getLocation());
	}

	public static void setCircleRemote(Player player, World world, Block actBlock) {
		Boolean remote = false;
		Block effectBlock = actBlock;
		List<Entity> entitiesList = player.getNearbyEntities(242, 20, 242);
		for (int i = 0; i < entitiesList.size(); i++) {
			if (entitiesList.get(i) instanceof Arrow) {
				Arrow shotArrow = (Arrow) entitiesList.get(i);
				if (shotArrow.getLocation().getBlock().getX() == actBlock.getLocation().getBlock().getX() && shotArrow.getLocation().getBlock().getZ() == actBlock.getLocation().getBlock().getZ()) {
					remote = true;
					entitiesList.remove(i);
				}
			}
		}
		if (remote) {
			for (int i = 0; i < entitiesList.size(); i++) {
				if (entitiesList.get(i) instanceof Arrow) {
					Arrow shotArrow = (Arrow) entitiesList.get(i);
					effectBlock = shotArrow.getLocation().getBlock();
					setCircle(player, world, actBlock, effectBlock);
				}
			}
		} else
			setCircle(player, world, actBlock, effectBlock);
	}

	public static void setCircle(Player player, World world, Block actBlock, Block effectBlock) {
		Block northSin = actBlock.getRelative(0, 0, -3);
		Block southSin = actBlock.getRelative(0, 0, 3);
		Block eastSin = actBlock.getRelative(3, 0, 0);
		Block westSin = actBlock.getRelative(-3, 0, 0);
		int n = 0;
		int s = 0;
		int e = 0;
		int w = 0;
		int[] intArray = new int[4];
		if (northSin.getRelative(BlockFace.NORTH).getType() == Material.REDSTONE_WIRE)
			n++;
		if (northSin.getRelative(BlockFace.SOUTH).getType() == Material.REDSTONE_WIRE)
			n++;
		if (northSin.getRelative(BlockFace.EAST).getType() == Material.REDSTONE_WIRE)
			n++;
		if (northSin.getRelative(BlockFace.WEST).getType() == Material.REDSTONE_WIRE)
			n++;
		intArray[0] = n;
		if (southSin.getRelative(BlockFace.NORTH).getType() == Material.REDSTONE_WIRE)
			s++;
		if (southSin.getRelative(BlockFace.SOUTH).getType() == Material.REDSTONE_WIRE)
			s++;
		if (southSin.getRelative(BlockFace.EAST).getType() == Material.REDSTONE_WIRE)
			s++;
		if (southSin.getRelative(BlockFace.WEST).getType() == Material.REDSTONE_WIRE)
			s++;
		intArray[1] = s;
		if (eastSin.getRelative(BlockFace.NORTH).getType() == Material.REDSTONE_WIRE)
			e++;
		if (eastSin.getRelative(BlockFace.SOUTH).getType() == Material.REDSTONE_WIRE)
			e++;
		if (eastSin.getRelative(BlockFace.EAST).getType() == Material.REDSTONE_WIRE)
			e++;
		if (eastSin.getRelative(BlockFace.WEST).getType() == Material.REDSTONE_WIRE)
			e++;
		intArray[2] = e;
		if (westSin.getRelative(BlockFace.NORTH).getType() == Material.REDSTONE_WIRE)
			w++;
		if (westSin.getRelative(BlockFace.SOUTH).getType() == Material.REDSTONE_WIRE)
			w++;
		if (westSin.getRelative(BlockFace.EAST).getType() == Material.REDSTONE_WIRE)
			w++;
		if (westSin.getRelative(BlockFace.WEST).getType() == Material.REDSTONE_WIRE)
			w++;
		intArray[3] = w;
		Arrays.sort(intArray);
		String arrayString = Arrays.toString(intArray);
		try {
			// exempt player from AntiCheat check
			if (Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null) {
				AnticheatAPI.exemptPlayer(player, CheckType.FAST_PLACE);
				AnticheatAPI.exemptPlayer(player, CheckType.FAST_BREAK);
				AnticheatAPI.exemptPlayer(player, CheckType.LONG_REACH);
				AnticheatAPI.exemptPlayer(player, CheckType.NO_SWING);
			}

			setCircleEffects(player, world, actBlock, effectBlock, arrayString);

			// unexempt player from AntiCheat check
			if (Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null) {
				AnticheatAPI.unexemptPlayer(player, CheckType.FAST_PLACE);
				AnticheatAPI.unexemptPlayer(player, CheckType.FAST_BREAK);
				AnticheatAPI.unexemptPlayer(player, CheckType.LONG_REACH);
				AnticheatAPI.unexemptPlayer(player, CheckType.NO_SWING);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public static void setCircleEffects(Player player, World world, Block actBlock, Block effectBlock, String arrayString) throws IOException {
		int cost = 0;
		if (!hasLearnedCircle(player, arrayString)) {
			if (learnCircle(player, arrayString, actBlock)) {
				player.sendMessage("You have successfully learned the circle " + arrayString);
				return;
			}
		}
		if (arrayString.equals("0"))
			return;
		if (arrayString.equals("[1, 1, 3, 3]") && player.hasPermission("geometricmagic.set.1133")) {
			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle " + arrayString + "!");
				return;
			}

			List<Entity> repairEntities = player.getNearbyEntities(9, 10, 9);
			for (int i = 0; i < repairEntities.size(); i++) {
				if (repairEntities.get(i) instanceof Item) {
					Item droppedItem = (Item) repairEntities.get(i);

					// Item data value
					int itemCode = droppedItem.getItemStack().getTypeId();

					// Enchantments
					Map<Enchantment, Integer> effects = droppedItem.getItemStack().getEnchantments();

					// Get cost
					if ((256 <= itemCode && itemCode <= 258) || (267 <= itemCode && itemCode <= 279) || (283 <= itemCode && itemCode <= 286) || (290 <= itemCode && itemCode <= 294)
							|| (298 <= itemCode && itemCode <= 317) || itemCode == 259 || itemCode == 346 || itemCode == 359 || itemCode == 261) {
						if ((256 <= itemCode && itemCode <= 258) || itemCode == 267 || itemCode == 292)
							cost = droppedItem.getItemStack().getDurability();
						if ((268 <= itemCode && itemCode <= 271) || itemCode == 290)
							cost = droppedItem.getItemStack().getDurability();
						if ((272 <= itemCode && itemCode <= 275) || itemCode == 291)
							cost = droppedItem.getItemStack().getDurability();
						if ((276 <= itemCode && itemCode <= 279) || itemCode == 293)
							cost = droppedItem.getItemStack().getDurability();
						if ((283 <= itemCode && itemCode <= 286) || itemCode == 294)
							cost = droppedItem.getItemStack().getDurability();
						if (itemCode == 298)
							cost = droppedItem.getItemStack().getDurability();
						if (itemCode == 299)
							cost = droppedItem.getItemStack().getDurability();
						if (itemCode == 300)
							cost = droppedItem.getItemStack().getDurability();
						if (itemCode == 301)
							cost = droppedItem.getItemStack().getDurability();
						if (itemCode == 306)
							cost = droppedItem.getItemStack().getDurability();
						if (itemCode == 307)
							cost = droppedItem.getItemStack().getDurability();
						if (itemCode == 308)
							cost = droppedItem.getItemStack().getDurability();
						if (itemCode == 309)
							cost = droppedItem.getItemStack().getDurability();
						if (itemCode == 310)
							cost = droppedItem.getItemStack().getDurability();
						if (itemCode == 311)
							cost = droppedItem.getItemStack().getDurability();
						if (itemCode == 312)
							cost = droppedItem.getItemStack().getDurability();
						if (itemCode == 313)
							cost = droppedItem.getItemStack().getDurability();
						if (itemCode == 314)
							cost = droppedItem.getItemStack().getDurability();
						if (itemCode == 315)
							cost = droppedItem.getItemStack().getDurability();
						if (itemCode == 316)
							cost = droppedItem.getItemStack().getDurability();
						if (itemCode == 317)
							cost = droppedItem.getItemStack().getDurability();
						if (itemCode == 259)
							cost = droppedItem.getItemStack().getDurability();
						if (itemCode == 346)
							cost = droppedItem.getItemStack().getDurability();
						if (itemCode == 359)
							cost = droppedItem.getItemStack().getDurability();
						if (itemCode == 261)
							cost = droppedItem.getItemStack().getDurability();
						cost = cost / 50;

						// Make sure cost is not more than 20
						if (cost > 20)
							cost = 20;

						if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
							player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
							ItemStack newItem = new ItemStack(itemCode, 1);

							// enchant the item
							newItem.addEnchantments(effects);

							droppedItem.remove();
							effectBlock.getWorld().dropItem(effectBlock.getLocation(), newItem);
						} else {
							player.sendMessage("You feel so hungry...");
							return;
						}
					}
				}
			}
		} else if (arrayString.equals("[1, 2, 2, 2]") && player.hasPermission("geometricmagic.set.1222")) {

			String costConfig = plugin.getConfig().getString("setcircles.1222.cost").toString();
			cost = Integer.parseInt(costConfig);
			if (cost > 20)
				cost = 20;

			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle " + arrayString + "!");
				return;
			}
			if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
				ItemStack oneRedstone = new ItemStack(331, 1);
				Item redStack = effectBlock.getWorld().dropItem(effectBlock.getLocation(), oneRedstone);
				List<Entity> entityList = redStack.getNearbyEntities(5, 10, 5);
				for (int i = 0; i < entityList.size(); i++) {
					if (entityList.get(i) instanceof Item) {
						Item droppedItem = (Item) entityList.get(i);

						// check if player has permission to break blocks here
						// first
						if (!checkBlockBreakSimulation(droppedItem.getLocation(), player)) {
							// player.sendMessage("You don't have permission to do that there.");
							return;
						}

						int valueArray = getBlockValue(plugin, droppedItem.getItemStack().getTypeId());

						int pay = (valueArray * droppedItem.getItemStack().getAmount());

						if (getTransmutationCostSystem(plugin).equalsIgnoreCase("vault")) {
							Economy econ = GeometricMagic.getEconomy();
							if (pay > 0) {
								econ.depositPlayer(player.getName(), pay);
							} else if (pay < 0) {
								econ.withdrawPlayer(player.getName(), pay * -1);
							}
						} else if (getTransmutationCostSystem(plugin).equalsIgnoreCase("xp")) {
							player.setLevel((valueArray * droppedItem.getItemStack().getAmount()) + player.getLevel());
						}
						/*
						 * player.setLevel((valueArray[droppedItem.getItemStack()
						 * .getTypeId()] * droppedItem.getItemStack()
						 * .getAmount()) + player.getLevel());
						 */
						droppedItem.remove();
					}
				}
				redStack.remove();
			} else {
				player.sendMessage("You feel so hungry...");
				return;
			}
		} else if (arrayString.equals("[1, 2, 3, 3]") && player.hasPermission("geometricmagic.set.1233")) {

			String costConfig = plugin.getConfig().getString("setcircles.1233.cost").toString();
			cost = Integer.parseInt(costConfig);
			if (cost > 20)
				cost = 20;

			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle " + arrayString + "!");
				return;
			}
			ItemStack onePortal = new ItemStack(90, 1);
			int fires = 0;
			List<Entity> entityList = player.getNearbyEntities(10, 10, 10);
			for (int i = 0; i < entityList.size(); i++) {
				if (entityList.get(i) instanceof Item) {
					Item sacrifice = (Item) entityList.get(i);

					// check if player has permission to break blocks here first
					if (!checkBlockBreakSimulation(sacrifice.getLocation(), player)) {
						// player.sendMessage("You don't have permission to do that there.");
						return;
					}

					if (sacrifice.getItemStack().getType() == Material.FIRE) {
						fires += sacrifice.getItemStack().getAmount();
						sacrifice.remove();
					}
				}
			}
			if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				if (fires >= 64) {
					fires -= 64;
					player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
					effectBlock.getWorld().dropItem(effectBlock.getLocation(), onePortal);
				}
			} else {
				player.sendMessage("You feel so hungry...");
			}
			ItemStack diamondStack = new ItemStack(264, fires);
			effectBlock.getWorld().dropItem(effectBlock.getLocation(), diamondStack);
		} else if (arrayString.equals("[1, 2, 3, 4]") && player.hasPermission("geometricmagic.set.1234")) {

			String costConfig = plugin.getConfig().getString("setcircles.1234.cost").toString();
			cost = Integer.parseInt(costConfig);
			if (cost > 20)
				cost = 20;

			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle " + arrayString + "!");
				return;
			}
			if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
				player.sendMessage(ChatColor.GREEN + "The four elements, like man alone, are weak. But together they form the strong fifth element: boron -Brother Silence");
				String amountConfig = plugin.getConfig().getString("setcircles.1234.amount").toString();
				int amount = Integer.parseInt(amountConfig);
				ItemStack oneRedstone = new ItemStack(331, amount);

				effectBlock.getWorld().dropItem(effectBlock.getLocation(), oneRedstone);

			} else {
				player.sendMessage("You feel so hungry...");
				return;
			}
		} else if (arrayString.equals("[2, 2, 2, 3]") && player.hasPermission("geometricmagic.set.2223")) {

			String costConfig = plugin.getConfig().getString("setcircles.2223.cost").toString();
			cost = Integer.parseInt(costConfig);
			if (cost > 20)
				cost = 20;

			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle " + arrayString + "!");
				return;
			}
			ItemStack oneRedstone = new ItemStack(331, 1);
			if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
				Item redStack = effectBlock.getWorld().dropItem(effectBlock.getLocation(), oneRedstone);
				int size = setCircleSize(actBlock);
				List<Entity> entityList = redStack.getNearbyEntities(size + 5, 128, size + 5);

				for (int i = 0; i < entityList.size(); i++) {
					if (entityList.get(i) instanceof Player) {
						HumanEntity victim = (HumanEntity) entityList.get(i);

						// check if player has permission to break blocks here
						// first
						if (!checkBlockBreakSimulation(victim.getLocation(), player)) {
							// player.sendMessage("You don't have permission to do that there.");
							return;
						}

						if (!victim.equals(player)) {
							victim.getWorld().strikeLightningEffect(victim.getLocation());
							if (victim.getInventory().contains(Material.FIRE)) {
								for (int k = 0; k < player.getInventory().getSize(); k++) {
									if (player.getInventory().getItem(i).getType() == Material.FIRE) {
										// System.out.println("removed a fire");
										int amount = player.getInventory().getItem(k).getAmount();
										player.getInventory().getItem(k).setAmount(amount - 1);
										if (amount - 1 <= 0) {
											player.getInventory().clear(k);
										}
									}
								}
							} else
								victim.damage(20);
							if (victim.isDead()) {
								ItemStack oneFire = new ItemStack(51, 1);
								victim.getWorld().dropItem(actBlock.getLocation(), oneFire);
							}
						}
					}
					if (entityList.get(i) instanceof Villager) {
						Villager victim = (Villager) entityList.get(i);
						victim.getWorld().strikeLightningEffect(victim.getLocation());
						victim.damage(20);
						if (victim.isDead()) {
							ItemStack oneFire = new ItemStack(51, 1);
							victim.getWorld().dropItem(actBlock.getLocation(), oneFire);
						}
					}
				}
				redStack.remove();
			} else {
				player.sendMessage("You feel so hungry...");
				return;
			}
		} else if (arrayString.equals("[2, 2, 2, 4]") && player.hasPermission("geometricmagic.set.2224")) {

			String costConfig = plugin.getConfig().getString("setcircles.2224.cost").toString();
			cost = Integer.parseInt(costConfig);
			if (cost > 20)
				cost = 20;

			if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
				Location spawnLoc = effectBlock.getLocation();
				spawnLoc.add(0.5, 1, 0.5);
				effectBlock.getWorld().spawn(spawnLoc, Enderman.class);
			} else {
				player.sendMessage("You feel so hungry...");
				return;
			}
		} else if (arrayString.equals("[2, 2, 4, 4]") && player.hasPermission("geometricmagic.set.2244")) {
			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle " + arrayString + "!");
				return;
			}

			String costConfig = plugin.getConfig().getString("setcircles.2244.cost").toString();
			cost = Integer.parseInt(costConfig);
			if (cost > 20)
				cost = 20;

			Location actPoint = effectBlock.getLocation();
			int na = 0, nb = 0, ea = 0, eb = 0, sa = 0, sb = 0, wa = 0, wb = 0;
			Block curBlock = effectBlock.getRelative(0, 0, -1);
			while (curBlock.getRelative(0, 0, -1).getType() == Material.REDSTONE_WIRE) {
				na++;
				curBlock = curBlock.getRelative(0, 0, -1);
			}
			Block fineBlock = curBlock;
			while (fineBlock.getRelative(-1, 0, 0).getType() == Material.REDSTONE_WIRE) {
				nb++;
				fineBlock = fineBlock.getRelative(-1, 0, 0);
			}
			curBlock = effectBlock.getRelative(1, 0, 0);
			while (curBlock.getRelative(1, 0, 0).getType() == Material.REDSTONE_WIRE) {
				ea++;
				curBlock = curBlock.getRelative(1, 0, 0);
			}
			fineBlock = curBlock;
			while (fineBlock.getRelative(0, 0, -1).getType() == Material.REDSTONE_WIRE) {
				eb++;
				fineBlock = fineBlock.getRelative(0, 0, -1);
			}
			curBlock = effectBlock.getRelative(0, 0, 1);
			while (curBlock.getRelative(0, 0, 1).getType() == Material.REDSTONE_WIRE) {
				sa++;
				curBlock = curBlock.getRelative(0, 0, 1);
			}
			fineBlock = curBlock;
			while (fineBlock.getRelative(1, 0, 0).getType() == Material.REDSTONE_WIRE) {
				sb++;
				fineBlock = fineBlock.getRelative(1, 0, 0);
			}
			curBlock = effectBlock.getRelative(-1, 0, 0);
			while (curBlock.getRelative(-1, 0, 0).getType() == Material.REDSTONE_WIRE) {
				wa++;
				curBlock = curBlock.getRelative(-1, 0, 0);
			}
			fineBlock = curBlock;
			while (fineBlock.getRelative(0, 0, 1).getType() == Material.REDSTONE_WIRE) {
				wb++;
				fineBlock = fineBlock.getRelative(0, 0, 1);
			}
			// north negative z, south positive z, east positive x, west
			// negative x
			int z = ((sa * 100 + sb) - (na * 100 + nb));
			int x = ((ea * 100 + eb) - (wa * 100 + wb));
			double actPointX = actPoint.getX();
			double actPointZ = actPoint.getZ();
			Location teleLoc = actPoint.add(x, 0, z);
			double distance = Math.sqrt(Math.pow(teleLoc.getX() - actPointX, 2) + Math.pow(teleLoc.getZ() - actPointZ, 2));
			double mathRandX = philosopherStoneModifier(player) * distance / 10 * Math.random();
			double mathRandZ = philosopherStoneModifier(player) * distance / 10 * Math.random();
			double randX = (teleLoc.getX() - (0.5 * mathRandX)) + (mathRandX);
			double randZ = (teleLoc.getZ() - (0.5 * mathRandZ)) + (mathRandZ);
			teleLoc.setX(randX);
			teleLoc.setZ(randZ);
			while (teleLoc.getWorld().getChunkAt(teleLoc).isLoaded() == false) {
				teleLoc.getWorld().getChunkAt(teleLoc).load(true);
			}
			int highestBlock = teleLoc.getWorld().getHighestBlockYAt(teleLoc) + 1;
			// System.out.println( mathRandX + " " + mathRandZ );
			player.sendMessage("Safe teleportation altitude is at " + highestBlock);
			return;
		} else if (arrayString.equals("[2, 3, 3, 3]") && player.hasPermission("geometricmagic.set.2333")) {

			String costConfig = plugin.getConfig().getString("setcircles.2333.cost").toString();
			cost = Integer.parseInt(costConfig);
			if (cost > 20)
				cost = 20;

			int size = setCircleSize(actBlock);
			cost = cost + size / 2;

			// Make sure cost is not more than 20
			if (cost > 20)
				cost = 20;

			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle " + arrayString + "!");
				return;
			}
			if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));

				// check if player has permission to break blocks here first
				if (!checkBlockBreakSimulation(effectBlock.getLocation(), player)) {
					// player.sendMessage("You don't have permission to do that there.");
					return;
				}

				effectBlock.getWorld().createExplosion(effectBlock.getLocation(), (4 + size));
			} else {
				player.sendMessage("You feel so hungry...");
				return;
			}
		} else if (arrayString.equals("[3, 3, 3, 4]") && player.hasPermission("geometricmagic.set.3334")) {

			String costConfig = plugin.getConfig().getString("setcircles.3334.cost").toString();
			cost = Integer.parseInt(costConfig);
			if (cost > 20)
				cost = 20;

			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle " + arrayString + "!");
				return;
			}
			if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));

				String configSize = plugin.getConfig().getString("setcircles.3334.size").toString();
				Integer circleSize = Integer.parseInt(configSize);

				alchemyFiller(Material.AIR, Material.FIRE, (byte) 0, effectBlock.getRelative((circleSize / 2) * -1, 0, (circleSize / 2) * -1).getLocation(),
						effectBlock.getRelative(circleSize / 2, circleSize, circleSize / 2).getLocation(), player, false);

			} else {
				player.sendMessage("You feel so hungry...");
				return;
			}
		} else if (arrayString.equals("[3, 3, 4, 4]") && player.hasPermission("geometricmagic.set.3344")) {

			String costConfig = plugin.getConfig().getString("setcircles.3344.cost").toString();
			cost = Integer.parseInt(costConfig);
			if (cost > 20)
				cost = 20;

			int size = setCircleSize(actBlock);
			cost = cost + size / 2;

			// Make sure cost is not more than 20
			if (cost > 20)
				cost = 20;

			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle " + arrayString + "!");
				return;
			}
			if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));

				// check if player has permission to break blocks here first
				if (!checkBlockBreakSimulation(effectBlock.getLocation(), player)) {
					// player.sendMessage("You don't have permission to do that there.");
					return;
				}

				effectBlock.getWorld().createExplosion(effectBlock.getLocation(), size, true);

			} else {
				player.sendMessage("You feel so hungry...");
				return;
			}
		} else if (arrayString.equals("[3, 4, 4, 4]") && player.hasPermission("geometricmagic.set.3444")) {

			String costConfig = plugin.getConfig().getString("setcircles.3444.cost").toString();
			cost = Integer.parseInt(costConfig);
			if (cost > 20)
				cost = 20;

			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle " + arrayString + "!");
				return;
			}
			if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				try {
					player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
					humanTransmutation(player);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} else {
				player.sendMessage("You feel so hungry...");
				return;
			}
		} else if (arrayString.equals("[0, 1, 1, 1]") && player.hasPermission("geometricmagic.set.0111")) {

			// using x111 because yml doesn't like 0 as first character
			String costConfig = plugin.getConfig().getString("setcircles.x111.cost").toString();
			cost = Integer.parseInt(costConfig);
			if (cost > 20)
				cost = 20;

			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle " + arrayString + "!");
				return;
			} else if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
				Location playerSpawn = player.getBedSpawnLocation();
				if (playerSpawn != null) {
					if (playerSpawn.getBlock().getType() == Material.AIR) {
						player.teleport(playerSpawn);
					} else {
						if (new Location(player.getWorld(), playerSpawn.getX() + 1, playerSpawn.getY(), playerSpawn.getZ()).getBlock().getType() == Material.AIR) {
							player.teleport(new Location(player.getWorld(), playerSpawn.getX() + 1, playerSpawn.getY(), playerSpawn.getZ()));
						} else if (new Location(player.getWorld(), playerSpawn.getX() - 1, playerSpawn.getY(), playerSpawn.getZ()).getBlock().getType() == Material.AIR) {
							player.teleport(new Location(player.getWorld(), playerSpawn.getX() - 1, playerSpawn.getY(), playerSpawn.getZ()));
						} else if (new Location(player.getWorld(), playerSpawn.getX(), playerSpawn.getY(), playerSpawn.getZ() + 1).getBlock().getType() == Material.AIR) {
							player.teleport(new Location(player.getWorld(), playerSpawn.getX(), playerSpawn.getY(), playerSpawn.getZ() + 1));
						} else if (new Location(player.getWorld(), playerSpawn.getX(), playerSpawn.getY(), playerSpawn.getZ() - 1).getBlock().getType() == Material.AIR) {
							player.teleport(new Location(player.getWorld(), playerSpawn.getX(), playerSpawn.getY(), playerSpawn.getZ() - 1));
						} else {
							player.sendMessage("Your bed is not safe to teleport to!");
							player.setFoodLevel((int) (player.getFoodLevel() + (cost * philosopherStoneModifier(player))));
						}
					}
				} else {
					player.sendMessage("You do not have a spawn set!");
					player.setFoodLevel((int) (player.getFoodLevel() + (cost * philosopherStoneModifier(player))));
				}
			} else {
				player.sendMessage("You feel so hungry...");
				return;
			}
		} else if (arrayString.equals("[0, 0, 4, 4]") && player.hasPermission("geometricmagic.set.0044")) {

			// using x044 because yml doesn't like 0 as first character
			String costConfig = plugin.getConfig().getString("setcircles.x044.cost").toString();
			cost = Integer.parseInt(costConfig);
			if (cost > 20)
				cost = 20;

			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle " + arrayString + "!");
				return;
			} else if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
				Location spawnLoc = effectBlock.getLocation();

				// check if player has permission to break blocks here first
				if (!checkBlockBreakSimulation(spawnLoc, player)) {
					// player.sendMessage("You don't have permission to do that there.");
					return;
				}

				spawnLoc.add(0.5, 1, 0.5);
				effectBlock.getWorld().spawn(spawnLoc, Pig.class);
			} else {
				player.sendMessage("You feel so hungry...");
				return;
			}
		} else if (arrayString.equals("[0, 1, 4, 4]") && player.hasPermission("geometricmagic.set.0144")) {

			// using x144 because yml doesn't like 0 as first character
			String costConfig = plugin.getConfig().getString("setcircles.x144.cost").toString();
			cost = Integer.parseInt(costConfig);
			if (cost > 20)
				cost = 20;

			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle " + arrayString + "!");
				return;
			} else if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
				Location spawnLoc = effectBlock.getLocation();

				// check if player has permission to break blocks here first
				if (!checkBlockBreakSimulation(spawnLoc, player)) {
					// player.sendMessage("You don't have permission to do that there.");
					return;
				}

				spawnLoc.add(0.5, 1, 0.5);
				effectBlock.getWorld().spawn(spawnLoc, Sheep.class);
			} else {
				player.sendMessage("You feel so hungry...");
				return;
			}
		} else if (arrayString.equals("[0, 2, 4, 4]") && player.hasPermission("geometricmagic.set.0244")) {

			// using x244 because yml doesn't like 0 as first character
			String costConfig = plugin.getConfig().getString("setcircles.x244.cost").toString();
			cost = Integer.parseInt(costConfig);
			if (cost > 20)
				cost = 20;

			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle " + arrayString + "!");
				return;
			} else if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
				Location spawnLoc = effectBlock.getLocation();

				// check if player has permission to break blocks here first
				if (!checkBlockBreakSimulation(spawnLoc, player)) {
					// player.sendMessage("You don't have permission to do that there.");
					return;
				}

				spawnLoc.add(0.5, 1, 0.5);
				effectBlock.getWorld().spawn(spawnLoc, Cow.class);
			} else {
				player.sendMessage("You feel so hungry...");
				return;
			}
		} else if (arrayString.equals("[0, 3, 4, 4]") && player.hasPermission("geometricmagic.set.0344")) {

			// using x344 because yml doesn't like 0 as first character
			String costConfig = plugin.getConfig().getString("setcircles.x344.cost").toString();
			cost = Integer.parseInt(costConfig);
			if (cost > 20)
				cost = 20;

			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle " + arrayString + "!");
				return;
			} else if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
				Location spawnLoc = effectBlock.getLocation();

				// check if player has permission to break blocks here first
				if (!checkBlockBreakSimulation(spawnLoc, player)) {
					// player.sendMessage("You don't have permission to do that there.");
					return;
				}

				spawnLoc.add(0.5, 1, 0.5);
				effectBlock.getWorld().spawn(spawnLoc, Chicken.class);
			} else {
				player.sendMessage("You feel so hungry...");
				return;
			}
		} else {
			player.sendMessage("You do not have permission to use " + arrayString + " or set circle does not exist");
		}
		effectBlock.getWorld().strikeLightningEffect(effectBlock.getLocation());
	}

	public static int setCircleSize(Block actBlock) {
		// limit sizes
		String limitSizeConfig = plugin.getConfig().getString("setcircles.limitsize").toString();
		int limitsize = Integer.parseInt(limitSizeConfig);

		int na = 0, nb = 0, ea = 0, eb = 0, sa = 0, sb = 0, wa = 0, wb = 0, nc = 0, ec = 0, sc = 0, wc = 0;
		Block curBlock = actBlock.getRelative(0, 0, -5);
		while (curBlock.getRelative(0, 0, -1).getType() == Material.REDSTONE_WIRE || na == limitsize) {
			na++;
			curBlock = curBlock.getRelative(0, 0, -1);
		}
		Block fineBlock = curBlock;
		while (fineBlock.getRelative(-1, 0, 0).getType() == Material.REDSTONE_WIRE || nb == limitsize) {
			nb++;
			fineBlock = fineBlock.getRelative(-1, 0, 0);
		}
		fineBlock = curBlock;
		while (fineBlock.getRelative(1, 0, 0).getType() == Material.REDSTONE_WIRE || nc == limitsize) {
			nc++;
			fineBlock = fineBlock.getRelative(1, 0, 0);
		}
		curBlock = actBlock.getRelative(5, 0, 0);
		while (curBlock.getRelative(1, 0, 0).getType() == Material.REDSTONE_WIRE || ea == limitsize) {
			ea++;
			curBlock = curBlock.getRelative(1, 0, 0);
		}
		fineBlock = curBlock;
		while (fineBlock.getRelative(0, 0, -1).getType() == Material.REDSTONE_WIRE || eb == limitsize) {
			eb++;
			fineBlock = fineBlock.getRelative(0, 0, -1);
		}
		fineBlock = curBlock;
		while (fineBlock.getRelative(0, 0, 1).getType() == Material.REDSTONE_WIRE || ec == limitsize) {
			ec++;
			fineBlock = fineBlock.getRelative(0, 0, 1);
		}
		curBlock = actBlock.getRelative(0, 0, 5);
		while (curBlock.getRelative(0, 0, 1).getType() == Material.REDSTONE_WIRE || sa == limitsize) {
			sa++;
			curBlock = curBlock.getRelative(0, 0, 1);
		}
		fineBlock = curBlock;
		while (fineBlock.getRelative(1, 0, 0).getType() == Material.REDSTONE_WIRE || sb == limitsize) {
			sb++;
			fineBlock = fineBlock.getRelative(1, 0, 0);
		}
		fineBlock = curBlock;
		while (fineBlock.getRelative(-1, 0, 0).getType() == Material.REDSTONE_WIRE || sc == limitsize) {
			sc++;
			fineBlock = fineBlock.getRelative(-1, 0, 0);
		}
		curBlock = actBlock.getRelative(-5, 0, 0);
		while (curBlock.getRelative(-1, 0, 0).getType() == Material.REDSTONE_WIRE || wa == limitsize) {
			wa++;
			curBlock = curBlock.getRelative(-1, 0, 0);
		}
		fineBlock = curBlock;
		while (fineBlock.getRelative(0, 0, 1).getType() == Material.REDSTONE_WIRE || wb == limitsize) {
			wb++;
			fineBlock = fineBlock.getRelative(0, 0, 1);
		}
		fineBlock = curBlock;
		while (fineBlock.getRelative(0, 0, -1).getType() == Material.REDSTONE_WIRE || wc == limitsize) {
			wc++;
			fineBlock = fineBlock.getRelative(0, 0, -1);
		}
		int size = 0;
		if (wa == ea && na == sa && wb == eb && nb == sb && wc == ec && nc == sc && wa == na) {
			size = wa;
		}
		return size;
	}

	public static void alchemyCheck(Material a, Material b, byte toData, Location circleStart, Location circleEnd, Location start, Location end, Player player, int width) {
		Block startBlock = circleStart.getBlock();
		int xIteration = 0;
		int yIteration = 0;
		int zIteration = 0;
		if (circleStart.getX() < circleEnd.getX()) {
			if (circleStart.getZ() < circleEnd.getZ()) {
				// east
				// System.out.println("alchemyCheck east");
				while (startBlock.getY() <= circleEnd.getY()) {
					while (startBlock.getX() <= circleEnd.getX()) {
						while (startBlock.getZ() <= circleEnd.getZ()) {
							if (startBlock.getType() != Material.AIR) {
								alchemyFiller(a, b, toData, start.getBlock().getRelative(xIteration * width, yIteration * width, zIteration * width).getLocation(),
										start.getBlock().getRelative(xIteration * width + width - 1, yIteration * width + width - 1, (zIteration * width + (width - 1))).getLocation(), player, true);
							}
							zIteration++;
							startBlock = startBlock.getRelative(0, 0, 1);
						}
						xIteration++;
						startBlock = circleStart.getBlock().getRelative(xIteration, yIteration, 0);
						zIteration = 0;
					}
					yIteration++;
					xIteration = 0;
					startBlock = circleStart.getBlock().getRelative(0, yIteration, 0);
				}
			} else {
				// north
				// System.out.println("alchemyCheck north");
				while (startBlock.getY() <= circleEnd.getY()) {
					while (startBlock.getZ() >= circleEnd.getZ()) {
						while (startBlock.getX() <= circleEnd.getX()) {
							if (startBlock.getType() != Material.AIR) {
								alchemyFiller(a, b, toData, start.getBlock().getRelative(xIteration * width, yIteration * width, zIteration * width).getLocation(),
										start.getBlock().getRelative(xIteration * width + width - 1, yIteration * width + width - 1, (zIteration * width - (width - 1))).getLocation(), player, true);
							}
							xIteration++;
							// System.out.println("xloop " + xIteration);
							startBlock = startBlock.getRelative(1, 0, 0);
						}
						zIteration--;
						// System.out.println("zloop " + zIteration);
						startBlock = circleStart.getBlock().getRelative(0, yIteration, zIteration);
						xIteration = 0;
					}
					yIteration++;
					// System.out.println("yloop " + yIteration);
					zIteration = 0;
					startBlock = circleStart.getBlock().getRelative(0, yIteration, 0);
				}
			}
		} else {
			if (circleStart.getZ() > circleEnd.getZ()) {
				// west
				// System.out.println("alchemyCheck west");
				while (startBlock.getY() <= circleEnd.getY()) {
					while (startBlock.getX() >= circleEnd.getX()) {
						while (startBlock.getZ() >= circleEnd.getZ()) {
							if (startBlock.getType() != Material.AIR) {
								alchemyFiller(a, b, toData, start.getBlock().getRelative(xIteration * width, yIteration * width, zIteration * width).getLocation(),
										start.getBlock().getRelative(xIteration * width - (width - 1), yIteration * width + width - 1, (zIteration * width - (width - 1))).getLocation(), player, true);
							}
							zIteration--;
							startBlock = startBlock.getRelative(0, 0, -1);
						}
						xIteration--;
						startBlock = circleStart.getBlock().getRelative(xIteration, yIteration, 0);
						zIteration = 0;
					}
					yIteration++;
					xIteration = 0;
					startBlock = circleStart.getBlock().getRelative(0, yIteration, 0);
				}
			} else {
				// south
				// System.out.println("alchemyCheck south");
				while (startBlock.getY() <= circleEnd.getY()) {
					while (startBlock.getZ() <= circleEnd.getZ()) {
						while (startBlock.getX() >= circleEnd.getX()) {
							if (startBlock.getType() != Material.AIR) {
								alchemyFiller(a, b, toData, start.getBlock().getRelative(xIteration * width, yIteration * width, zIteration * width).getLocation(),
										start.getBlock().getRelative(xIteration * width - (width - 1), yIteration * width + width - 1, (zIteration * width + (width - 1))).getLocation(), player, true);
							}
							xIteration--;
							// System.out.println("xloop");
							startBlock = startBlock.getRelative(-1, 0, 0);
						}
						zIteration++;
						// System.out.println("zloop");
						startBlock = circleStart.getBlock().getRelative(0, yIteration, zIteration);
						xIteration = 0;
					}
					yIteration++;
					// System.out.println("yloop");
					zIteration = 0;
					startBlock = circleStart.getBlock().getRelative(0, yIteration, 0);
				}
			}
		}
		return;
	}

	public static void alchemyFiller(Material a, Material b, byte toData, Location start, Location end, Player player, boolean charge) {
		// System.out.println("alchemyFiller");
		Block startBlock = start.getBlock();
		int xIteration = 0;
		int yIteration = 0;
		int zIteration = 0;

		if (start.getX() < end.getX()) {
			if (start.getZ() < end.getZ()) {
				// east
				// System.out.println("alchemyFiller east");
				while (startBlock.getY() <= end.getY()) {
					while (startBlock.getX() <= end.getX()) {
						while (startBlock.getZ() <= end.getZ()) {
							transmuteBlock(a, b, toData, startBlock, player, charge);
							startBlock = startBlock.getRelative(0, 0, 1);
						}
						xIteration++;
						startBlock = start.getBlock().getRelative(xIteration, yIteration, 0);
					}
					yIteration++;
					xIteration = 0;
					startBlock = start.getBlock().getRelative(0, yIteration, 0);
				}
			} else {
				// north
				// System.out.println("alchemyFiller north");
				while (startBlock.getY() <= end.getY()) {
					while (startBlock.getZ() >= end.getZ()) {
						while (startBlock.getX() <= end.getX()) {
							transmuteBlock(a, b, toData, startBlock, player, charge);
							startBlock = startBlock.getRelative(1, 0, 0);
						}
						zIteration--;
						startBlock = start.getBlock().getRelative(0, yIteration, zIteration);
					}
					yIteration++;
					zIteration = 0;
					startBlock = start.getBlock().getRelative(0, yIteration, 0);
				}
			}
		} else {
			if (start.getZ() > end.getZ()) {
				// west
				// System.out.println("alchemyFiller west");
				while (startBlock.getY() <= end.getY()) {
					while (startBlock.getX() >= end.getX()) {
						while (startBlock.getZ() >= end.getZ()) {
							transmuteBlock(a, b, toData, startBlock, player, charge);
							startBlock = startBlock.getRelative(0, 0, -1);
						}
						xIteration--;
						startBlock = start.getBlock().getRelative(xIteration, yIteration, 0);
					}
					yIteration++;
					xIteration = 0;
					startBlock = start.getBlock().getRelative(0, yIteration, 0);
				}
			} else {
				// south
				// System.out.println("alchemyFiller south");
				while (startBlock.getY() <= end.getY()) {
					while (startBlock.getZ() <= end.getZ()) {
						while (startBlock.getX() >= end.getX()) {
							transmuteBlock(a, b, toData, startBlock, player, charge);
							startBlock = startBlock.getRelative(-1, 0, 0);
							// System.out.println("xloopfiller");
						}
						zIteration++;
						// System.out.println("zloopfiller");
						startBlock = start.getBlock().getRelative(0, yIteration, zIteration);
					}
					yIteration++;
					// System.out.println("yloopfiller");
					zIteration = 0;
					startBlock = start.getBlock().getRelative(0, yIteration, 0);
				}
			}
		}

		return;
	}

	public static double getBalance(Player player) {

		if (getTransmutationCostSystem(plugin).equalsIgnoreCase("vault")) {
			Economy econ = GeometricMagic.getEconomy();

			double balance = econ.getBalance(player.getName());

			return balance;
		} else if (getTransmutationCostSystem(plugin).equalsIgnoreCase("xp")) {
			double balance = player.getLevel();

			return balance;
		}
		return 0;
	}

	public static void transmuteBlock(Material a, Material b, byte toData, Block startBlock, Player player, boolean charge) {

		int pay = calculatePay(a, b, player);

		if (startBlock.getType() == a) {

			if (-1 * getBalance(player) < pay || !charge) {

				// Block break
				if (a != Material.AIR && b == Material.AIR && a != Material.CHEST && a != Material.WALL_SIGN && a != Material.SIGN_POST && a != Material.FURNACE && a != Material.BURNING_FURNACE
						&& a != Material.BREWING_STAND && a != Material.WOODEN_DOOR && a != Material.IRON_DOOR_BLOCK && a != Material.MOB_SPAWNER) {

					Location blockLocation = startBlock.getLocation();

					if (checkBlockBreakSimulation(blockLocation, player)) {
						// Change block
						startBlock.setType(b);
						if (toData != 0)
							startBlock.setData(toData);

						if (charge) {
							if (getTransmutationCostSystem(plugin).equalsIgnoreCase("vault")) {

								Economy econ = GeometricMagic.getEconomy();

								// Deposit or withdraw to players Vault account
								if (pay > 0) {
									econ.depositPlayer(player.getName(), pay);
								} else if (pay < 0) {
									econ.withdrawPlayer(player.getName(), pay * -1);
								}
							} else if (getTransmutationCostSystem(plugin).equalsIgnoreCase("xp")) {
								player.setLevel((int) (player.getLevel() + pay));
							}
						}
					}
				}

				// Block place
				else if (a == Material.AIR && b != Material.AIR && b != Material.MOB_SPAWNER) {

					Location blockLocation = startBlock.getLocation();
					int blockID = b.getId();
					byte blockData = 0;

					if (checkBlockPlaceSimulation(blockLocation, blockID, blockData, blockLocation, player)) {
						// Change block
						startBlock.setType(b);
						if (toData != 0)
							startBlock.setData(toData);

						if (charge) {
							if (getTransmutationCostSystem(plugin).equalsIgnoreCase("vault")) {

								Economy econ = GeometricMagic.getEconomy();

								// Deposit or withdraw to players Vault account
								if (pay > 0) {
									econ.depositPlayer(player.getName(), pay);
								} else if (pay < 0) {
									econ.withdrawPlayer(player.getName(), pay * -1);
								}
							} else if (getTransmutationCostSystem(plugin).equalsIgnoreCase("xp")) {
								player.setLevel((int) (player.getLevel() + pay));
							}
						}
					}
				}

				// Block break and place
				else if (a != Material.AIR && b != Material.AIR && a != Material.CHEST && a != Material.WALL_SIGN && a != Material.SIGN_POST && a != Material.FURNACE && a != Material.BURNING_FURNACE
						&& a != Material.BREWING_STAND && a != Material.WOODEN_DOOR && a != Material.MOB_SPAWNER && b != Material.MOB_SPAWNER && a != Material.IRON_DOOR_BLOCK) {

					Location blockLocation = startBlock.getLocation();
					int blockID = b.getId();
					byte blockData = 0;

					if (checkBlockBreakSimulation(blockLocation, player) && checkBlockPlaceSimulation(blockLocation, blockID, blockData, blockLocation, player)) {
						// Change block
						startBlock.setType(b);
						if (toData != 0)
							startBlock.setData(toData);

						if (charge) {
							if (getTransmutationCostSystem(plugin).equalsIgnoreCase("vault")) {

								Economy econ = GeometricMagic.getEconomy();

								// Deposit or withdraw to players Vault account
								if (pay > 0) {
									econ.depositPlayer(player.getName(), pay);
								} else if (pay < 0) {
									econ.withdrawPlayer(player.getName(), pay * -1);
								}
							} else if (getTransmutationCostSystem(plugin).equalsIgnoreCase("xp")) {
								player.setLevel((int) (player.getLevel() + pay));
							}
						}
					}
				}

				// output to console
				else if ((a != Material.AIR && b != Material.AIR) || a == Material.MOB_SPAWNER || b == Material.MOB_SPAWNER) {
					System.out.println("[GeometricMagic] " + player.getName() + " tried to transmute a blacklisted material:");
					System.out.println("[GeometricMagic] " + a.name() + " into " + b.name());
				}
				return;
			} else
				return;
		} else
			return;
	}

	public static int calculatePay(Material a, Material b, Player player) {
		int pay = (int) (getBlockValue(plugin, a.getId()) - getBlockValue(plugin, b.getId()));
		return pay;
	}

	public static double philosopherStoneModifier(Player player) {
		double modifier = 1;
		int stackCount = 0;
		PlayerInventory inventory = player.getInventory();
		for (int i = 0; i < inventory.getSize(); i++) {
			if (inventory.getItem(i) != null && inventory.getItem(i).getType() == Material.PORTAL)
				stackCount += inventory.getItem(i).getAmount();
		}
		String multiplier = plugin.getConfig().getString("philosopherstone.modifier").toString();
		float multiplierModifier = Float.parseFloat(multiplier);

		modifier = 1 / (Math.pow(2, stackCount) * multiplierModifier);
		return modifier;
	}

	public static void humanTransmutation(Player player) throws IOException {
		if (new File("plugins/GeometricMagic/").mkdirs())
			System.out.println("[GeometricMagic] Sacrifices file created.");
		File myFile = new File("plugins/GeometricMagic/sacrifices.txt");
		if (myFile.exists()) {
			Scanner inputFile = new Scanner(myFile);
			while (inputFile.hasNextLine()) {
				String name = inputFile.nextLine();
				if (name.equals(player.getDisplayName())) {
					FileWriter dWriter = new FileWriter("plugins/GeometricMagic/sacrificed.txt", true);
					PrintWriter dFile = new PrintWriter(dWriter);
					dFile.println(player.getDisplayName());
					dFile.close();
					return;
				}
			}
			inputFile.close();
		} else {
			PrintWriter outputFile = new PrintWriter("plugins/GeometricMagic/sacrifices.txt");
			System.out.println("[GeometricMagic] Sacrifices file created.");
			outputFile.close();
		}
		FileWriter fWriter = new FileWriter("plugins/GeometricMagic/sacrifices.txt", true);
		PrintWriter outputFile = new PrintWriter(fWriter);
		outputFile.println(player.getDisplayName());
		outputFile.println(0);
		player.sendMessage("You have committed the taboo! Crafting is your sacrifice, knowledge your reward.");
		outputFile.close();
	}

	public static boolean hasLearnedCircle(Player player, String circle) throws IOException {
		File myFile = new File("plugins/GeometricMagic/" + player.getName() + ".txt");
		if (!myFile.exists()) {
			return false;
		}
		Scanner inputFile = new Scanner(myFile);
		while (inputFile.hasNextLine()) {
			String name = inputFile.nextLine();
			if (name.equals(circle)) {
				inputFile.close();
				return true;
			}
		}
		inputFile.close();
		return false;
	}

	public static boolean learnCircle(Player player, String circle, Block actBlock) throws IOException {
		boolean status = false;
		// System.out.println("learnCircle");
		ItemStack oneRedstone = new ItemStack(331, 1);
		Item redStack = actBlock.getWorld().dropItem(actBlock.getLocation(), oneRedstone);
		List<Entity> entityList = redStack.getNearbyEntities(2, 10, 2);
		for (int i = 0; i < entityList.size(); i++) {
			if (entityList.get(i) instanceof Enderman) {
				if (new File("plugins/GeometricMagic/").mkdirs())
					System.out.println("[GeometricMagic] File created for " + player.getName());
				File myFile = new File("plugins/GeometricMagic/" + player.getName() + ".txt");
				if (myFile.exists()) {
					FileWriter fWriter = new FileWriter("plugins/GeometricMagic/" + player.getName() + ".txt", true);
					PrintWriter outputFile = new PrintWriter(fWriter);
					outputFile.println(circle);
					outputFile.close();
				} else {
					PrintWriter outputFile = new PrintWriter("plugins/GeometricMagic/" + player.getName() + ".txt");
					outputFile.println(circle);
					outputFile.close();
				}
				status = true;
			}
		}
		redStack.remove();
		return status;
	}

	public static String getTransmutationCostSystem(GeometricMagic plugin) {
		return plugin.getConfig().getString("transmutation.cost").toString();
	}

	public static Integer getBlockValue(GeometricMagic plugin, int i) {
		return plugin.getConfig().getInt("values." + i);
	}

	// Lyneira's Code Start
	public static boolean checkBlockPlaceSimulation(Location target, int typeId, byte data, Location placedAgainst, Player player) {
		Block placedBlock = target.getBlock();
		BlockState replacedBlockState = placedBlock.getState();
		int oldType = replacedBlockState.getTypeId();
		byte oldData = replacedBlockState.getRawData();

		// Set the new state without physics.
		placedBlock.setTypeIdAndData(typeId, data, false);
		BlockPlaceEvent placeEvent = new BlockPlaceEvent(placedBlock, replacedBlockState, placedAgainst.getBlock(), null, player, true);
		((PluginManager) getPluginManager(plugin)).callEvent(placeEvent);

		// Revert to the old state without physics.
		placedBlock.setTypeIdAndData(oldType, oldData, false);
		if (placeEvent.isCancelled())
			return false;
		return true;
	}

	public static boolean checkBlockBreakSimulation(Location target, Player player) {
		Block block = target.getBlock();
		BlockBreakEvent breakEvent = new BlockBreakEvent(block, player);
		((PluginManager) getPluginManager(plugin)).callEvent(breakEvent);
		if (breakEvent.isCancelled())
			return false;
		return true;
	}

	// Lyneira's Code End

	public static Object getPluginManager(GeometricMagic plugin) {
		return plugin.getServer().getPluginManager();
	}
}
