package me.cakenggt.GeometricMagic;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.entity.*;
//import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class GeometricMagicPlayerListener implements Listener {
	public static Economy economy = null;

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		// System.out.println("is playerinteractevent");
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK
				&& event.getAction() != Action.RIGHT_CLICK_AIR) {
			// System.out.println("doesn't equal click block or click air");
			return;
		}
		boolean sacrifices = false;
		boolean sacrificed = false;
		try {
			sacrifices = checkSacrifices(event.getPlayer());
			sacrificed = checkSacrificed(event.getPlayer());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if (sacrificed) {
			// System.out.println("is sacrificed");
			return;
		}
		Block actBlock = event.getPlayer().getLocation().getBlock();
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			// System.out.println("right clicked block");
			if (event.getClickedBlock().getType() == Material.WORKBENCH
					&& sacrifices)
				event.getClickedBlock().setType(Material.AIR);
			actBlock = event.getClickedBlock();
		}
		if (event.getAction() == Action.RIGHT_CLICK_AIR
				&& event.getPlayer().getItemInHand().getType() == Material.FLINT) {
			// System.out.println("is leftclickair");
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

	public static void isCircle(Player player, World world, Block actBlock)
			throws IOException {
		// System.out.println("isCircle?");
		if (actBlock.getType() == Material.REDSTONE_WIRE
				&& player.getItemInHand().getAmount() == 0
				&& !checkSacrificed(player)) {
			// System.out.println("isCircle");
			circleChooser(player, world, actBlock);
		}
		boolean sacrifices = checkSacrifices(player);
		if (player.getItemInHand().getType() == Material.FLINT && sacrifices
				&& !checkSacrificed(player)) {
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
			// System.out.println("isCircle not clicked redstone");
			setCircleEffects(player, player.getWorld(), player.getLocation()
					.getBlock(), actBlock, circle);
		} else
			return;
	}

	public static void circleChooser(Player player, World world, Block actBlock) {
		// System.out.println("circleChooser");
		Block northBlock = actBlock.getRelative(0, 0, -1);
		Block southBlock = actBlock.getRelative(0, 0, 1);
		Block eastBlock = actBlock.getRelative(1, 0, 0);
		Block westBlock = actBlock.getRelative(-1, 0, 0);
		if (northBlock.getType() == Material.REDSTONE_WIRE
				&& southBlock.getType() == Material.REDSTONE_WIRE
				&& eastBlock.getType() == Material.REDSTONE_WIRE
				&& westBlock.getType() == Material.REDSTONE_WIRE) {
			if (player.hasPermission("circle.teleportation")) {
				// System.out.println("teleportation");
				teleportationCircle(player, world, actBlock);
			} else
				player.sendMessage("You do not have permission to use this circle");
		} else if (northBlock.getType() != Material.REDSTONE_WIRE
				&& southBlock.getType() != Material.REDSTONE_WIRE
				&& eastBlock.getType() != Material.REDSTONE_WIRE
				&& westBlock.getType() != Material.REDSTONE_WIRE
				&& actBlock.getRelative(-3, 0, 0).getType() != Material.REDSTONE_WIRE
				&& actBlock.getRelative(3, 0, 0).getType() != Material.REDSTONE_WIRE
				&& actBlock.getRelative(0, 0, -3).getType() != Material.REDSTONE_WIRE
				&& actBlock.getRelative(0, 0, 3).getType() != Material.REDSTONE_WIRE) {
			if (player.hasPermission("circle.micro")) {
				// System.out.println("micro");
				microCircle(player, world, actBlock);
			} else
				player.sendMessage("You do not have permission to use this circle");
		} else if ((northBlock.getType() == Material.REDSTONE_WIRE
				&& southBlock.getType() == Material.REDSTONE_WIRE
				&& eastBlock.getType() != Material.REDSTONE_WIRE && westBlock
				.getType() != Material.REDSTONE_WIRE)
				|| (northBlock.getType() != Material.REDSTONE_WIRE
						&& southBlock.getType() != Material.REDSTONE_WIRE
						&& eastBlock.getType() == Material.REDSTONE_WIRE && westBlock
						.getType() == Material.REDSTONE_WIRE)) {

			// transmutation circle size permissions
			// - allows use of all circles smaller than then the max
			// size permission node they have
			int circleSize = 1;
			if (player.hasPermission("circle.transmutation.*")
					|| player.hasPermission("circle.transmutation.9")) {
				circleSize = 9;
			} else if (player.hasPermission("circle.transmutation.7")) {
				circleSize = 7;
			} else if (player.hasPermission("circle.transmutation.5")) {
				circleSize = 5;
			} else if (player.hasPermission("circle.transmutation.3")) {
				circleSize = 3;
			} else if (player.hasPermission("circle.transmutation.1")) {
				circleSize = 1;
			} else {
				circleSize = 0;
				player.sendMessage("You do not have permission to use this circle");
			}

			//System.out.println("circleSize:" + circleSize);

			if (circleSize > 0) {
				transmutationCircle(player, world, actBlock, circleSize);
			}

		} else if (northBlock.getType() != Material.REDSTONE_WIRE
				&& southBlock.getType() != Material.REDSTONE_WIRE
				&& eastBlock.getType() != Material.REDSTONE_WIRE
				&& westBlock.getType() != Material.REDSTONE_WIRE
				&& actBlock.getRelative(-3, 0, 0).getType() == Material.REDSTONE_WIRE
				&& actBlock.getRelative(3, 0, 0).getType() == Material.REDSTONE_WIRE
				&& actBlock.getRelative(0, 0, -3).getType() == Material.REDSTONE_WIRE
				&& actBlock.getRelative(0, 0, 3).getType() == Material.REDSTONE_WIRE) {
			if (player.hasPermission("circle.set")) {
				// System.out.println("set");
				setCircleRemote(player, world, actBlock);
			} else
				player.sendMessage("You do not have permission to use this circle");
		} else
			return;
	}

	public static void teleportationCircle(Player player, World world,
			Block actBlock) {

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
		for(int c = 0; c < na; c++) {
			curBlock = curBlock.getRelative(0, 0, -1);
			curBlock.setType(Material.AIR);
		}
		fineBlock = curBlock;
		for(int c = 0; c < nb; c++) {
			fineBlock = fineBlock.getRelative(-1, 0, 0);
			fineBlock.setType(Material.AIR);
		}
		fineBlock = curBlock;
		for(int c = 0; c < nc; c++) {
			fineBlock = fineBlock.getRelative(1, 0, 0);
			fineBlock.setType(Material.AIR);
		}

		curBlock = actBlock.getRelative(1, 0, 0);
		for(int c = 0; c < ea; c++) {
			curBlock = curBlock.getRelative(1, 0, 0);
			curBlock.setType(Material.AIR);
		}
		fineBlock = curBlock;
		for(int c = 0; c < eb; c++) {
			fineBlock = fineBlock.getRelative(0, 0, -1);
			fineBlock.setType(Material.AIR);
		}
		fineBlock = curBlock;
		for(int c = 0; c < ec; c++) {
			fineBlock = fineBlock.getRelative(0, 0, 1);
			fineBlock.setType(Material.AIR);
		}

		curBlock = actBlock.getRelative(0, 0, 1);
		for(int c = 0; c < sa; c++) {
			curBlock = curBlock.getRelative(0, 0, 1);
			curBlock.setType(Material.AIR);
		}
		fineBlock = curBlock;
		for(int c = 0; c < sb; c++) {
			fineBlock = fineBlock.getRelative(1, 0, 0);
			fineBlock.setType(Material.AIR);
		}
		fineBlock = curBlock;
		for(int c = 0; c < sc; c++) {
			fineBlock = fineBlock.getRelative(-1, 0, 0);
			fineBlock.setType(Material.AIR);
		}

		curBlock = actBlock.getRelative(-1, 0, 0);
		for(int c = 0; c < wa; c++) {
			curBlock = curBlock.getRelative(-1, 0, 0);
			curBlock.setType(Material.AIR);
		}
		fineBlock = curBlock;
		for(int c = 0; c < wb; c++) {
			fineBlock = fineBlock.getRelative(0, 0, 1);
			fineBlock.setType(Material.AIR);
		}
		fineBlock = curBlock;
		for(int c = 0; c < wc; c++) {
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

		double distance = Math.sqrt(Math.pow(teleLoc.getX() - actPointX, 2)
				+ Math.pow(teleLoc.getZ() - actPointZ, 2));

		double mathRandX = philosopherStoneModifier(player) * distance / 10
				* Math.random();
		double mathRandZ = philosopherStoneModifier(player) * distance / 10
				* Math.random();

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

		ItemStack redstonePile = new ItemStack(331, 5 + na + nb + nc + sa + sb
				+ sc + ea + eb + ec + wa + wb + wc);

		teleLoc.getWorld().dropItem(teleLoc, redstonePile);

		actBlock.getWorld().strikeLightningEffect(actBlock.getLocation());
		actBlock.getWorld().strikeLightningEffect(teleLoc);

		return;
	}

	public static void microCircle(Player player, World world, Block actBlock) {
		Economy econ = GeometricMagic.getEconomy();

		player.sendMessage("You have " + econ.format(getBalance(player)));

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

	public static void transmutationCircle(Player player, World world,
			Block actBlock, int circleSize) {
		int halfWidth = 0;
		int fullWidth = 0;
		Location startLoc = actBlock.getLocation();
		Location endLoc = actBlock.getLocation();
		Location circleStart = actBlock.getLocation();
		Location circleEnd = actBlock.getLocation();
		Material fromType = actBlock.getType();
		Material toType = actBlock.getType();
		if (actBlock.getRelative(0, 0, -1).getType() == Material.REDSTONE_WIRE
				&& actBlock.getRelative(0, 0, 1).getType() == Material.REDSTONE_WIRE) {
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
				fromType = actBlock.getLocation()
						.add(halfWidth - 1, 0, -1 * (halfWidth + 1)).getBlock()
						.getType();
				Block toBlock = actBlock.getLocation()
						.add(halfWidth - 1, 0, halfWidth + 1).getBlock();
				toType = toBlock.getType();
				byte toData = toBlock.getData();
				startLoc = actBlock.getLocation().add(fullWidth, 0,
						-1 * dimensionOfEffect / 2);
				// System.out.println(startLoc);
				endLoc = actBlock.getLocation().add(
						fullWidth + dimensionOfEffect - 1,
						dimensionOfEffect - 1, dimensionOfEffect / 2 - 1);
				// System.out.println(endLoc);
				circleStart = actBlock.getLocation().add(1, 0,
						-1 * (halfWidth - 2));
				// System.out.println(circleStart);
				circleEnd = actBlock.getLocation().add(fullWidth - 2,
						fullWidth - 3, halfWidth - 2);
				// System.out.println(circleEnd);
				alchemyCheck(fromType, toType, toData, circleStart, circleEnd,
						startLoc, endLoc, player, fullWidth - 2);
			} else if (actBlock.getRelative(-1 * (fullWidth - 1), 0, 0)
					.getType() == Material.REDSTONE_WIRE) {
				// west
				// System.out.println("transmutationCircle west");
				fromType = actBlock.getLocation()
						.add(-1 * (halfWidth - 1), 0, halfWidth + 1).getBlock()
						.getType();
				Block toBlock = actBlock.getLocation()
						.add((-1) * (halfWidth - 1), 0, (-1) * (halfWidth + 1))
						.getBlock();
				toType = toBlock.getType();
				byte toData = toBlock.getData();
				startLoc = actBlock.getLocation().add(-1 * fullWidth, 0,
						dimensionOfEffect / 2);
				endLoc = actBlock.getLocation().add(
						-1 * (fullWidth + dimensionOfEffect) + 1,
						dimensionOfEffect - 1, -1 * dimensionOfEffect / 2 + 1);
				circleStart = actBlock.getLocation()
						.add(-1, 0, (halfWidth - 2));
				circleEnd = actBlock.getLocation().add(-1 * (fullWidth - 2),
						fullWidth - 3, -1 * (halfWidth - 2));
				alchemyCheck(fromType, toType, toData, circleStart, circleEnd,
						startLoc, endLoc, player, fullWidth - 2);
			}
		} else if (actBlock.getRelative(1, 0, 0).getType() == Material.REDSTONE_WIRE
				&& actBlock.getRelative(-1, 0, 0).getType() == Material.REDSTONE_WIRE) {
			halfWidth = 0;
			while (actBlock.getRelative(halfWidth, 0, 0).getType() == Material.REDSTONE_WIRE) {
				if (halfWidth > circleSize) {
					break;
				}
				halfWidth++;
			}
			fullWidth = (halfWidth * 2) - 1;
			//System.out
			//		.println("half is " + halfWidth + " full is " + fullWidth);
			int dimensionOfEffect = (fullWidth - 2) * (fullWidth - 2);
			if (actBlock.getRelative(0, 0, -1 * (fullWidth - 1)).getType() == Material.REDSTONE_WIRE) {
				// north
				// System.out.println("transmutationCircle north");
				fromType = actBlock.getLocation()
						.add(-1 * (halfWidth + 1), 0, -1 * (halfWidth - 1))
						.getBlock().getType();
				Block toBlock = actBlock.getLocation()
						.add(halfWidth + 1, 0, -1 * (halfWidth - 1)).getBlock();
				toType = toBlock.getType();
				byte toData = toBlock.getData();
				startLoc = actBlock.getLocation().add(
						-1 * dimensionOfEffect / 2, 0, -1 * fullWidth);
				endLoc = actBlock.getLocation().add(dimensionOfEffect / 2 - 1,
						dimensionOfEffect - 1,
						-1 * (dimensionOfEffect + fullWidth) + 1);
				circleStart = actBlock.getLocation().add(-1 * (halfWidth - 2),
						0, -1);
				circleEnd = actBlock.getLocation().add((halfWidth - 2),
						fullWidth - 3, -1 * (fullWidth - 2));
				alchemyCheck(fromType, toType, toData, circleStart, circleEnd,
						startLoc, endLoc, player, fullWidth - 2);
			} else if (actBlock.getRelative(0, 0, (fullWidth - 1)).getType() == Material.REDSTONE_WIRE) {
				// south
				// System.out.println("transmutationCircle south");
				fromType = actBlock.getLocation()
						.add(halfWidth + 1, 0, halfWidth - 1).getBlock()
						.getType();
				Block toBlock = actBlock.getLocation()
						.add(-1 * (halfWidth + 1), 0, halfWidth - 1).getBlock();
				toType = toBlock.getType();
				byte toData = toBlock.getData();
				startLoc = actBlock.getLocation().add(dimensionOfEffect / 2, 0,
						fullWidth);
				endLoc = actBlock.getLocation().add(
						-1 * dimensionOfEffect / 2 + 1, dimensionOfEffect - 1,
						fullWidth + dimensionOfEffect - 1);
				circleStart = actBlock.getLocation().add(halfWidth - 2, 0, 1);
				circleEnd = actBlock.getLocation().add(-1 * (halfWidth - 2),
						fullWidth - 3, (fullWidth - 2));
				alchemyCheck(fromType, toType, toData, circleStart, circleEnd,
						startLoc, endLoc, player, fullWidth - 2);
			}
		}
		actBlock.getWorld().strikeLightningEffect(actBlock.getLocation());
	}

	public static void setCircleRemote(Player player, World world,
			Block actBlock) {
		Boolean remote = false;
		Block effectBlock = actBlock;
		List<Entity> entitiesList = player.getNearbyEntities(242, 20, 242);
		for (int i = 0; i < entitiesList.size(); i++) {
			if (entitiesList.get(i) instanceof Arrow) {
				Arrow shotArrow = (Arrow) entitiesList.get(i);
				if (shotArrow.getLocation().getBlock().getX() == actBlock
						.getLocation().getBlock().getX()
						&& shotArrow.getLocation().getBlock().getZ() == actBlock
								.getLocation().getBlock().getZ()) {
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

	public static void setCircle(Player player, World world, Block actBlock,
			Block effectBlock) {
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
			setCircleEffects(player, world, actBlock, effectBlock, arrayString);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public static void setCircleEffects(Player player, World world,
			Block actBlock, Block effectBlock, String arrayString)
			throws IOException {
		int cost = 0;
		if (!hasLearnedCircle(player, arrayString)) {
			if (learnCircle(player, arrayString, actBlock)) {
				player.sendMessage("You have successfully learned the circle "
						+ arrayString);
				return;
			}
		}
		if (arrayString.equals("0"))
			return;
		if (arrayString.equals("[1, 1, 3, 3]")
				&& player.hasPermission("circle.set.1133")) {
			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle "
						+ arrayString + "!");
				return;
			}
			List<Entity> repairEntities = player.getNearbyEntities(9, 10, 9);
			for (int i = 0; i < repairEntities.size(); i++) {
				if (repairEntities.get(i) instanceof Item) {
					Item droppedItem = (Item) repairEntities.get(i);
					if ((256 <= droppedItem.getItemStack().getTypeId() && droppedItem
							.getItemStack().getTypeId() <= 258)
							|| (267 <= droppedItem.getItemStack().getTypeId() && droppedItem
									.getItemStack().getTypeId() <= 279)
							|| (283 <= droppedItem.getItemStack().getTypeId() && droppedItem
									.getItemStack().getTypeId() <= 286)
							|| (290 <= droppedItem.getItemStack().getTypeId() && droppedItem
									.getItemStack().getTypeId() <= 294)
							|| (298 <= droppedItem.getItemStack().getTypeId() && droppedItem
									.getItemStack().getTypeId() <= 317)
							|| droppedItem.getItemStack().getTypeId() == 259
							|| droppedItem.getItemStack().getTypeId() == 346
							|| droppedItem.getItemStack().getTypeId() == 359
							|| droppedItem.getItemStack().getTypeId() == 261) {
						if ((256 <= droppedItem.getItemStack().getTypeId() && droppedItem
								.getItemStack().getTypeId() <= 258)
								|| droppedItem.getItemStack().getTypeId() == 267
								|| droppedItem.getItemStack().getTypeId() == 292)
							cost = droppedItem.getItemStack().getDurability();
						if ((268 <= droppedItem.getItemStack().getTypeId() && droppedItem
								.getItemStack().getTypeId() <= 271)
								|| droppedItem.getItemStack().getTypeId() == 290)
							cost = droppedItem.getItemStack().getDurability();
						if ((272 <= droppedItem.getItemStack().getTypeId() && droppedItem
								.getItemStack().getTypeId() <= 275)
								|| droppedItem.getItemStack().getTypeId() == 291)
							cost = droppedItem.getItemStack().getDurability();
						if ((276 <= droppedItem.getItemStack().getTypeId() && droppedItem
								.getItemStack().getTypeId() <= 279)
								|| droppedItem.getItemStack().getTypeId() == 293)
							cost = droppedItem.getItemStack().getDurability();
						if ((283 <= droppedItem.getItemStack().getTypeId() && droppedItem
								.getItemStack().getTypeId() <= 286)
								|| droppedItem.getItemStack().getTypeId() == 294)
							cost = droppedItem.getItemStack().getDurability();
						if (droppedItem.getItemStack().getTypeId() == 298)
							cost = droppedItem.getItemStack().getDurability();
						if (droppedItem.getItemStack().getTypeId() == 299)
							cost = droppedItem.getItemStack().getDurability();
						if (droppedItem.getItemStack().getTypeId() == 300)
							cost = droppedItem.getItemStack().getDurability();
						if (droppedItem.getItemStack().getTypeId() == 301)
							cost = droppedItem.getItemStack().getDurability();
						if (droppedItem.getItemStack().getTypeId() == 306)
							cost = droppedItem.getItemStack().getDurability();
						if (droppedItem.getItemStack().getTypeId() == 307)
							cost = droppedItem.getItemStack().getDurability();
						if (droppedItem.getItemStack().getTypeId() == 308)
							cost = droppedItem.getItemStack().getDurability();
						if (droppedItem.getItemStack().getTypeId() == 309)
							cost = droppedItem.getItemStack().getDurability();
						if (droppedItem.getItemStack().getTypeId() == 310)
							cost = droppedItem.getItemStack().getDurability();
						if (droppedItem.getItemStack().getTypeId() == 311)
							cost = droppedItem.getItemStack().getDurability();
						if (droppedItem.getItemStack().getTypeId() == 312)
							cost = droppedItem.getItemStack().getDurability();
						if (droppedItem.getItemStack().getTypeId() == 313)
							cost = droppedItem.getItemStack().getDurability();
						if (droppedItem.getItemStack().getTypeId() == 314)
							cost = droppedItem.getItemStack().getDurability();
						if (droppedItem.getItemStack().getTypeId() == 315)
							cost = droppedItem.getItemStack().getDurability();
						if (droppedItem.getItemStack().getTypeId() == 316)
							cost = droppedItem.getItemStack().getDurability();
						if (droppedItem.getItemStack().getTypeId() == 317)
							cost = droppedItem.getItemStack().getDurability();
						if (droppedItem.getItemStack().getTypeId() == 259)
							cost = droppedItem.getItemStack().getDurability();
						if (droppedItem.getItemStack().getTypeId() == 346)
							cost = droppedItem.getItemStack().getDurability();
						if (droppedItem.getItemStack().getTypeId() == 359)
							cost = droppedItem.getItemStack().getDurability();
						if (droppedItem.getItemStack().getTypeId() == 261)
							cost = droppedItem.getItemStack().getDurability();
						cost = cost / 50;
						if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
							player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
							ItemStack newItem = new ItemStack(droppedItem
									.getItemStack().getTypeId(), 1);
							droppedItem.remove();
							effectBlock.getWorld().dropItem(
									effectBlock.getLocation(), newItem);
						} else
							return;
					}
				}
			}
		} else if (arrayString.equals("[1, 2, 2, 2]")
				&& player.hasPermission("circle.set.1222")) {
			cost = 1;
			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle "
						+ arrayString + "!");
				return;
			}
			if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
				ItemStack oneRedstone = new ItemStack(331, 1);
				Item redStack = effectBlock.getWorld().dropItem(
						effectBlock.getLocation(), oneRedstone);
				List<Entity> entityList = redStack.getNearbyEntities(5, 10, 5);
				for (int i = 0; i < entityList.size(); i++) {
					if (entityList.get(i) instanceof Item) {
						Item droppedItem = (Item) entityList.get(i);
						calculatePay(Material.AIR, Material.AIR);
						int[] valueArray = new int[2266];
						getValueArray(valueArray);

						int pay = (valueArray[droppedItem.getItemStack()
								.getTypeId()] * droppedItem.getItemStack()
								.getAmount());

						Economy econ = GeometricMagic.getEconomy();
						if (pay > 0) {
							econ.depositPlayer(player.getName(), pay);
						} else if (pay < 0) {
							econ.withdrawPlayer(player.getName(), pay * -1);
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
			} else
				return;
		} else if (arrayString.equals("[1, 2, 3, 3]")
				&& player.hasPermission("circle.set.1233")) {
			cost = 20;
			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle "
						+ arrayString + "!");
				return;
			}
			ItemStack onePortal = new ItemStack(90, 1);
			int fires = 0;
			List<Entity> entityList = player.getNearbyEntities(10, 10, 10);
			for (int i = 0; i < entityList.size(); i++) {
				if (entityList.get(i) instanceof Item) {
					Item sacrifice = (Item) entityList.get(i);
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
					effectBlock.getWorld().dropItem(effectBlock.getLocation(),
							onePortal);
				}
			}
			ItemStack diamondStack = new ItemStack(264, fires);
			effectBlock.getWorld().dropItem(effectBlock.getLocation(),
					diamondStack);
		} else if (arrayString.equals("[1, 2, 3, 4]")
				&& player.hasPermission("circle.set.1234")) {
			cost = 1;
			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle "
						+ arrayString + "!");
				return;
			}
			if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
				player.sendMessage(ChatColor.GREEN
						+ "The four elements, like man alone, are weak. But together they form the strong fifth element: boron -Brother Silence");
				ItemStack oneRedstone = new ItemStack(331, 10);
				effectBlock.getWorld().dropItem(effectBlock.getLocation(),
						oneRedstone);
			} else
				return;
		} else if (arrayString.equals("[2, 2, 2, 3]")
				&& player.hasPermission("circle.set.2223")) {
			cost = 10;
			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle "
						+ arrayString + "!");
				return;
			}
			ItemStack oneRedstone = new ItemStack(331, 1);
			if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
				Item redStack = effectBlock.getWorld().dropItem(
						effectBlock.getLocation(), oneRedstone);
				int size = setCircleSize(actBlock);
				List<Entity> entityList = redStack.getNearbyEntities(size + 5,
						128, size + 5);
				for (int i = 0; i < entityList.size(); i++) {
					if (entityList.get(i) instanceof Player) {
						HumanEntity victim = (HumanEntity) entityList.get(i);
						if (!victim.equals(player)) {
							victim.getWorld().strikeLightningEffect(
									victim.getLocation());
							if (victim.getInventory().contains(Material.FIRE)) {
								for (int k = 0; k < player.getInventory()
										.getSize(); k++) {
									if (player.getInventory().getItem(i)
											.getType() == Material.FIRE) {
										// System.out.println("removed a fire");
										int amount = player.getInventory()
												.getItem(k).getAmount();
										player.getInventory().getItem(k)
												.setAmount(amount - 1);
										if (amount - 1 <= 0) {
											player.getInventory().clear(k);
										}
									}
								}
							} else
								victim.damage(20);
							if (victim.isDead()) {
								ItemStack oneFire = new ItemStack(51, 1);
								victim.getWorld().dropItem(
										actBlock.getLocation(), oneFire);
							}
						}
					}
					if (entityList.get(i) instanceof Villager) {
						Villager victim = (Villager) entityList.get(i);
						victim.getWorld().strikeLightningEffect(
								victim.getLocation());
						victim.damage(20);
						if (victim.isDead()) {
							ItemStack oneFire = new ItemStack(51, 1);
							victim.getWorld().dropItem(actBlock.getLocation(),
									oneFire);
						}
					}
				}
				redStack.remove();
			} else
				return;
		} else if (arrayString.equals("[2, 2, 2, 4]")
				&& player.hasPermission("circle.set.2224")) {
			cost = 10;
			if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
				Location spawnLoc = effectBlock.getLocation();
				spawnLoc.add(0.5, 0, 0.5);
				effectBlock.getWorld().spawn(spawnLoc, Enderman.class);
			} else
				return;
		} else if (arrayString.equals("[2, 2, 4, 4]")
				&& player.hasPermission("circle.set.2244")) {
			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle "
						+ arrayString + "!");
				return;
			}
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
			double distance = Math.sqrt(Math.pow(teleLoc.getX() - actPointX, 2)
					+ Math.pow(teleLoc.getZ() - actPointZ, 2));
			double mathRandX = philosopherStoneModifier(player) * distance / 10
					* Math.random();
			double mathRandZ = philosopherStoneModifier(player) * distance / 10
					* Math.random();
			double randX = (teleLoc.getX() - (0.5 * mathRandX)) + (mathRandX);
			double randZ = (teleLoc.getZ() - (0.5 * mathRandZ)) + (mathRandZ);
			teleLoc.setX(randX);
			teleLoc.setZ(randZ);
			while (teleLoc.getWorld().getChunkAt(teleLoc).isLoaded() == false) {
				teleLoc.getWorld().getChunkAt(teleLoc).load(true);
			}
			int highestBlock = teleLoc.getWorld().getHighestBlockYAt(teleLoc) + 1;
			// System.out.println( mathRandX + " " + mathRandZ );
			player.sendMessage("Safe teleportation altitude is at "
					+ highestBlock);
			return;
		} else if (arrayString.equals("[2, 3, 3, 3]")
				&& player.hasPermission("circle.set.2333")) {
			cost = 2;
			int size = setCircleSize(actBlock);
			cost = 2 + size / 2;
			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle "
						+ arrayString + "!");
				return;
			}
			if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
				effectBlock.getWorld().createExplosion(
						effectBlock.getLocation(), (4 + size));
			} else
				return;
		} else if (arrayString.equals("[3, 3, 3, 4]")
				&& player.hasPermission("circle.set.3334")) {
			cost = 2;
			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle "
						+ arrayString + "!");
				return;
			}
			if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				alchemyFiller(Material.AIR, Material.FIRE, (byte) 0,
						effectBlock.getRelative(-10, 0, -10).getLocation(),
						effectBlock.getRelative(10, 20, 10).getLocation(),
						player);
			} else
				return;
		} else if (arrayString.equals("[3, 3, 4, 4]")
				&& player.hasPermission("circle.set.3344")) {
			cost = 4;
			int size = setCircleSize(actBlock);
			cost = 4 + size / 2;
			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle "
						+ arrayString + "!");
				return;
			}
			if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
				effectBlock.getWorld().createExplosion(
						effectBlock.getLocation(), 8 + size, true);
			} else
				return;
		} else if (arrayString.equals("[3, 4, 4, 4]")
				&& player.hasPermission("circle.set.3444")) {
			cost = 20;
			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle "
						+ arrayString + "!");
				return;
			}
			if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				try {
					player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
					humanTransmutation(player);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} else
				return;
		} else if (arrayString.equals("[0, 1, 1, 1]")
				&& player.hasPermission("circle.set.0111")) {
			cost = 16;
			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle "
						+ arrayString + "!");
				return;
			} else if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
				Location playerSpawn = player.getBedSpawnLocation();
				if (playerSpawn != null) {
					if (playerSpawn.getBlock().getType() == Material.AIR) {
						player.teleport(playerSpawn);
					} else {
						if (new Location(player.getWorld(),
								playerSpawn.getX() + 1, playerSpawn.getY(),
								playerSpawn.getZ()).getBlock().getType() == Material.AIR) {
							player.teleport(new Location(player.getWorld(),
									playerSpawn.getX() + 1, playerSpawn.getY(),
									playerSpawn.getZ()));
						} else if (new Location(player.getWorld(),
								playerSpawn.getX() - 1, playerSpawn.getY(),
								playerSpawn.getZ()).getBlock().getType() == Material.AIR) {
							player.teleport(new Location(player.getWorld(),
									playerSpawn.getX() - 1, playerSpawn.getY(),
									playerSpawn.getZ()));
						} else if (new Location(player.getWorld(),
								playerSpawn.getX(), playerSpawn.getY(),
								playerSpawn.getZ() + 1).getBlock().getType() == Material.AIR) {
							player.teleport(new Location(player.getWorld(),
									playerSpawn.getX(), playerSpawn.getY(),
									playerSpawn.getZ() + 1));
						} else if (new Location(player.getWorld(),
								playerSpawn.getX(), playerSpawn.getY(),
								playerSpawn.getZ() - 1).getBlock().getType() == Material.AIR) {
							player.teleport(new Location(player.getWorld(),
									playerSpawn.getX(), playerSpawn.getY(),
									playerSpawn.getZ() - 1));
						} else {
							player.sendMessage("Your bed is not safe to teleport to!");
							player.setFoodLevel((int) (player.getFoodLevel() + (cost * philosopherStoneModifier(player))));
						}
					}
				} else {
					player.sendMessage("You do not have a spawn set!");
					player.setFoodLevel((int) (player.getFoodLevel() + (cost * philosopherStoneModifier(player))));
				}
			} else
				return;
		} else if (arrayString.equals("[0, 0, 4, 4]")
				&& player.hasPermission("circle.set.0044")) {
			cost = 10;
			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle "
						+ arrayString + "!");
				return;
			} else if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
				Location spawnLoc = effectBlock.getLocation();
				spawnLoc.add(0.5, 0, 0.5);
				effectBlock.getWorld().spawn(spawnLoc, Pig.class);
			} else
				return;
		} else if (arrayString.equals("[0, 1, 4, 4]")
				&& player.hasPermission("circle.set.0144")) {
			cost = 10;
			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle "
						+ arrayString + "!");
				return;
			} else if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
				Location spawnLoc = effectBlock.getLocation();
				spawnLoc.add(0.5, 0, 0.5);
				effectBlock.getWorld().spawn(spawnLoc, Sheep.class);
			} else
				return;
		} else if (arrayString.equals("[0, 2, 4, 4]")
				&& player.hasPermission("circle.set.0244")) {
			cost = 10;
			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle "
						+ arrayString + "!");
				return;
			} else if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
				Location spawnLoc = effectBlock.getLocation();
				spawnLoc.add(0.5, 0, 0.5);
				effectBlock.getWorld().spawn(spawnLoc, Cow.class);
			} else
				return;
		} else if (arrayString.equals("[0, 3, 4, 4]")
				&& player.hasPermission("circle.set.0344")) {
			cost = 10;
			if (!hasLearnedCircle(player, arrayString)) {
				player.sendMessage("You have not yet learned circle "
						+ arrayString + "!");
				return;
			} else if (player.getFoodLevel() >= (cost * philosopherStoneModifier(player))) {
				player.setFoodLevel((int) (player.getFoodLevel() - (cost * philosopherStoneModifier(player))));
				Location spawnLoc = effectBlock.getLocation();
				spawnLoc.add(0.5, 0, 0.5);
				effectBlock.getWorld().spawn(spawnLoc, Chicken.class);
			} else
				return;
		} else {
			player.sendMessage("You do not have permission to use "
					+ arrayString + " or set circle does not exist");
		}
		effectBlock.getWorld().strikeLightningEffect(effectBlock.getLocation());
	}

	public static int setCircleSize(Block actBlock) {
		int na = 0, nb = 0, ea = 0, eb = 0, sa = 0, sb = 0, wa = 0, wb = 0, nc = 0, ec = 0, sc = 0, wc = 0;
		Block curBlock = actBlock.getRelative(0, 0, -5);
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
		curBlock = actBlock.getRelative(5, 0, 0);
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
		curBlock = actBlock.getRelative(0, 0, 5);
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
		curBlock = actBlock.getRelative(-5, 0, 0);
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
		int size = 0;
		if (wa == ea && na == sa && wb == eb && nb == sb && wc == ec
				&& nc == sc && wa == na) {
			size = wa;
		}
		return size;
	}

	public static void alchemyCheck(Material a, Material b, byte toData,
			Location circleStart, Location circleEnd, Location start,
			Location end, Player player, int width) {
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
								alchemyFiller(
										a,
										b,
										toData,
										start.getBlock()
												.getRelative(
														xIteration * width,
														yIteration * width,
														zIteration * width)
												.getLocation(),
										start.getBlock()
												.getRelative(
														xIteration * width
																+ width - 1,
														yIteration * width
																+ width - 1,
														(zIteration * width + (width - 1)))
												.getLocation(), player);
							}
							zIteration++;
							startBlock = startBlock.getRelative(0, 0, 1);
						}
						xIteration++;
						startBlock = circleStart.getBlock().getRelative(
								xIteration, yIteration, 0);
						zIteration = 0;
					}
					yIteration++;
					xIteration = 0;
					startBlock = circleStart.getBlock().getRelative(0,
							yIteration, 0);
				}
			} else {
				// north
				// System.out.println("alchemyCheck north");
				while (startBlock.getY() <= circleEnd.getY()) {
					while (startBlock.getZ() >= circleEnd.getZ()) {
						while (startBlock.getX() <= circleEnd.getX()) {
							if (startBlock.getType() != Material.AIR) {
								alchemyFiller(
										a,
										b,
										toData,
										start.getBlock()
												.getRelative(
														xIteration * width,
														yIteration * width,
														zIteration * width)
												.getLocation(),
										start.getBlock()
												.getRelative(
														xIteration * width
																+ width - 1,
														yIteration * width
																+ width - 1,
														(zIteration * width - (width - 1)))
												.getLocation(), player);
							}
							xIteration++;
							// System.out.println("xloop " + xIteration);
							startBlock = startBlock.getRelative(1, 0, 0);
						}
						zIteration--;
						// System.out.println("zloop " + zIteration);
						startBlock = circleStart.getBlock().getRelative(0,
								yIteration, zIteration);
						xIteration = 0;
					}
					yIteration++;
					// System.out.println("yloop " + yIteration);
					zIteration = 0;
					startBlock = circleStart.getBlock().getRelative(0,
							yIteration, 0);
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
								alchemyFiller(
										a,
										b,
										toData,
										start.getBlock()
												.getRelative(
														xIteration * width,
														yIteration * width,
														zIteration * width)
												.getLocation(),
										start.getBlock()
												.getRelative(
														xIteration * width
																- (width - 1),
														yIteration * width
																+ width - 1,
														(zIteration * width - (width - 1)))
												.getLocation(), player);
							}
							zIteration--;
							startBlock = startBlock.getRelative(0, 0, -1);
						}
						xIteration--;
						startBlock = circleStart.getBlock().getRelative(
								xIteration, yIteration, 0);
						zIteration = 0;
					}
					yIteration++;
					xIteration = 0;
					startBlock = circleStart.getBlock().getRelative(0,
							yIteration, 0);
				}
			} else {
				// south
				// System.out.println("alchemyCheck south");
				while (startBlock.getY() <= circleEnd.getY()) {
					while (startBlock.getZ() <= circleEnd.getZ()) {
						while (startBlock.getX() >= circleEnd.getX()) {
							if (startBlock.getType() != Material.AIR) {
								alchemyFiller(
										a,
										b,
										toData,
										start.getBlock()
												.getRelative(
														xIteration * width,
														yIteration * width,
														zIteration * width)
												.getLocation(),
										start.getBlock()
												.getRelative(
														xIteration * width
																- (width - 1),
														yIteration * width
																+ width - 1,
														(zIteration * width + (width - 1)))
												.getLocation(), player);
							}
							xIteration--;
							// System.out.println("xloop");
							startBlock = startBlock.getRelative(-1, 0, 0);
						}
						zIteration++;
						// System.out.println("zloop");
						startBlock = circleStart.getBlock().getRelative(0,
								yIteration, zIteration);
						xIteration = 0;
					}
					yIteration++;
					// System.out.println("yloop");
					zIteration = 0;
					startBlock = circleStart.getBlock().getRelative(0,
							yIteration, 0);
				}
			}
		}
		return;
	}

	public static void alchemyFiller(Material a, Material b, byte toData,
			Location start, Location end, Player player) {
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
							transmuteBlock(a, b, toData, startBlock, player);
							startBlock = startBlock.getRelative(0, 0, 1);
						}
						xIteration++;
						startBlock = start.getBlock().getRelative(xIteration,
								yIteration, 0);
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
							transmuteBlock(a, b, toData, startBlock, player);
							startBlock = startBlock.getRelative(1, 0, 0);
						}
						zIteration--;
						startBlock = start.getBlock().getRelative(0,
								yIteration, zIteration);
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
							transmuteBlock(a, b, toData, startBlock, player);
							startBlock = startBlock.getRelative(0, 0, -1);
						}
						xIteration--;
						startBlock = start.getBlock().getRelative(xIteration,
								yIteration, 0);
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
							transmuteBlock(a, b, toData, startBlock, player);
							startBlock = startBlock.getRelative(-1, 0, 0);
							// System.out.println("xloopfiller");
						}
						zIteration++;
						// System.out.println("zloopfiller");
						startBlock = start.getBlock().getRelative(0,
								yIteration, zIteration);
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

		Economy econ = GeometricMagic.getEconomy();

		double balance = econ.getBalance(player.getName());

		return balance;
	}

	public static void transmuteBlock(Material a, Material b, byte toData,
			Block startBlock, Player player) {

		Economy econ = GeometricMagic.getEconomy();

		double balance = getBalance(player);

		int pay = calculatePay(a, b);
		pay = (int) (pay * philosopherStoneModifier(player));

		BlockState startBlockState = startBlock.getState();
		if (startBlock.getType() == a) {

			if (-1 * balance < pay) {

				// Create fake block break event for compatibility with logging
				// plugins
				if (a != Material.AIR && b == Material.AIR
						&& a != Material.CHEST && a != Material.WALL_SIGN
						&& a != Material.SIGN_POST && a != Material.FURNACE
						&& a != Material.BURNING_FURNACE
						&& a != Material.BREWING_STAND
						&& a != Material.WOODEN_DOOR
						&& a != Material.IRON_DOOR_BLOCK) {
					BlockBreakEvent break_event = new BlockBreakEvent(
							startBlock, player);
					Bukkit.getServer().getPluginManager()
							.callEvent(break_event);

					// change block
					startBlock.setType(b);
					if (toData != 0)
						startBlock.setData(toData);

					// deposit or withdraw to players Vault account
					if (pay > 0) {
						econ.depositPlayer(player.getName(), pay);
					} else if (pay < 0) {
						econ.withdrawPlayer(player.getName(), pay * -1);
					}
				}

				// Create fake block place event for compatibility with logging
				// plugins
				else if (a == Material.AIR && b != Material.AIR) {

					// change block
					startBlock.setType(b);
					if (toData != 0)
						startBlock.setData(toData);

					// deposit or withdraw to players Vault account
					if (pay > 0) {
						econ.depositPlayer(player.getName(), pay);
					} else if (pay < 0) {
						econ.withdrawPlayer(player.getName(), pay * -1);
					}

					BlockPlaceEvent place_event = new BlockPlaceEvent(
							startBlock, startBlockState, startBlock,
							new ItemStack(b.getId()), player, true);
					Bukkit.getServer().getPluginManager()
							.callEvent(place_event);
				}

				// Create fake block break and place events for compatibility
				// with logging plugins
				else if (a != Material.AIR && b != Material.AIR
						&& a != Material.CHEST && a != Material.WALL_SIGN
						&& a != Material.SIGN_POST && a != Material.FURNACE
						&& a != Material.BURNING_FURNACE
						&& a != Material.BREWING_STAND
						&& a != Material.WOODEN_DOOR
						&& a != Material.IRON_DOOR_BLOCK) {
					BlockBreakEvent break_event = new BlockBreakEvent(
							startBlock, player);
					Bukkit.getServer().getPluginManager()
							.callEvent(break_event);

					// change block
					startBlock.setType(b);
					if (toData != 0)
						startBlock.setData(toData);

					// deposit or withdraw to players Vault account
					if (pay > 0) {
						econ.depositPlayer(player.getName(), pay);
					} else if (pay < 0) {
						econ.withdrawPlayer(player.getName(), pay * -1);
					}

					BlockPlaceEvent place_event = new BlockPlaceEvent(
							startBlock, startBlockState, startBlock,
							new ItemStack(b.getId()), player, true);
					Bukkit.getServer().getPluginManager()
							.callEvent(place_event);
				}
				// System.out.println("transmuted block");
				// System.out.println(startBlock.getX() + " " +
				// startBlock.getY() + " " + startBlock.getZ());
				else if (a != Material.AIR && b != Material.AIR) {
					System.out.println("[GeometricMagic] " + player.getName()
							+ " tried to transmute a blacklisted material:");
					System.out.println("[GeometricMagic] " + a.name()
							+ " into " + b.name());
				}
				return;
			} else
				return;
		} else
			return;
	}

	public static int calculatePay(Material a, Material b) {
		int[] valueArray = new int[2266];
		// array index is block id, value in array is xp
		Arrays.fill(valueArray, 0);
		valueArray[0] = 0;
		valueArray[1] = 6;
		valueArray[2] = 1;
		valueArray[3] = 1;
		valueArray[4] = 4;
		valueArray[5] = 4;
		valueArray[6] = 8;
		valueArray[7] = 0;
		valueArray[8] = 0;
		valueArray[9] = 1;
		valueArray[10] = 0;
		valueArray[11] = 200;
		valueArray[12] = 1;
		valueArray[13] = 2;
		valueArray[14] = 384;
		valueArray[15] = 96;
		valueArray[16] = 16;
		valueArray[17] = 16;
		valueArray[18] = 1;
		valueArray[19] = 0;
		valueArray[20] = 3;
		valueArray[21] = 144;
		valueArray[22] = 216;
		valueArray[23] = 51;
		valueArray[24] = 4;
		valueArray[25] = 40;
		valueArray[26] = 48;
		valueArray[27] = 384;
		valueArray[28] = 96;
		valueArray[29] = 153;
		valueArray[30] = 24;
		valueArray[31] = 1;
		valueArray[32] = 1;
		valueArray[33] = 132;
		valueArray[34] = 0;
		valueArray[35] = 12;
		valueArray[36] = 0;
		valueArray[37] = 48;
		valueArray[38] = 48;
		valueArray[39] = 32;
		valueArray[40] = 32;
		valueArray[41] = 3456;
		valueArray[42] = 864;
		valueArray[43] = 8;
		valueArray[44] = 4;
		valueArray[45] = 24;
		valueArray[46] = 484;
		valueArray[47] = 96;
		valueArray[48] = 4;
		valueArray[49] = 192;
		valueArray[50] = 4;
		valueArray[51] = 1;
		valueArray[52] = 0;
		valueArray[53] = 6;
		valueArray[54] = 32;
		valueArray[55] = 8;
		valueArray[56] = 1536;
		valueArray[57] = 13824;
		valueArray[58] = 16;
		valueArray[59] = 4;
		valueArray[60] = 1;
		valueArray[61] = 32;
		valueArray[62] = 32;
		valueArray[63] = 0;
		valueArray[64] = 24;
		valueArray[65] = 7;
		valueArray[66] = 36;
		valueArray[67] = 6;
		valueArray[68] = 0;
		valueArray[69] = 6;
		valueArray[70] = 12;
		valueArray[71] = 576;
		valueArray[72] = 8;
		valueArray[73] = 32;
		valueArray[74] = 32;
		valueArray[75] = 10;
		valueArray[76] = 10;
		valueArray[77] = 12;
		valueArray[78] = 1;
		valueArray[79] = 4;
		valueArray[80] = 4;
		valueArray[81] = 8;
		valueArray[82] = 16;
		valueArray[83] = 8;
		valueArray[84] = 1568;
		valueArray[85] = 6;
		valueArray[86] = 64;
		valueArray[87] = 1;
		valueArray[88] = 2;
		valueArray[89] = 128;
		valueArray[90] = 0;
		valueArray[91] = 68;
		valueArray[92] = 163;
		valueArray[93] = 46;
		valueArray[94] = 46;
		valueArray[95] = 0;
		valueArray[96] = 12;
		valueArray[97] = 0;
		valueArray[98] = 6;
		valueArray[99] = 32;
		valueArray[100] = 32;
		valueArray[101] = 36;
		valueArray[102] = 1;
		valueArray[103] = 64;
		valueArray[104] = 16;
		valueArray[105] = 7;
		valueArray[106] = 1;
		valueArray[107] = 16;
		valueArray[108] = 36;
		valueArray[109] = 9;
		valueArray[110] = 1;
		valueArray[111] = 0;
		valueArray[112] = 0;
		valueArray[113] = 0;
		valueArray[114] = 0;
		valueArray[115] = 8;
		valueArray[116] = 0;
		valueArray[117] = 0;
		valueArray[118] = 0;
		valueArray[119] = 0;
		valueArray[120] = 0;
		valueArray[121] = 1;
		valueArray[122] = 0;
		int valueDifference = valueArray[a.getId()] - valueArray[b.getId()];
		return valueDifference;
	}

	public static int[] getValueArray(int[] valueArray) {
		// int[] valueArray = new int[2266];
		// array index is block id, value in array is xp
		Arrays.fill(valueArray, 0);
		valueArray[0] = 0;
		valueArray[1] = 6;
		valueArray[2] = 1;
		valueArray[3] = 1;
		valueArray[4] = 4;
		valueArray[5] = 4;
		valueArray[6] = 8;
		valueArray[7] = 0;
		valueArray[8] = 0;
		valueArray[9] = 1;
		valueArray[10] = 0;
		valueArray[11] = 200;
		valueArray[12] = 1;
		valueArray[13] = 2;
		valueArray[14] = 384;
		valueArray[15] = 96;
		valueArray[16] = 16;
		valueArray[17] = 16;
		valueArray[18] = 1;
		valueArray[19] = 0;
		valueArray[20] = 3;
		valueArray[21] = 144;
		valueArray[22] = 216;
		valueArray[23] = 51;
		valueArray[24] = 4;
		valueArray[25] = 40;
		valueArray[26] = 48;
		valueArray[27] = 384;
		valueArray[28] = 96;
		valueArray[29] = 153;
		valueArray[30] = 24;
		valueArray[31] = 1;
		valueArray[32] = 1;
		valueArray[33] = 132;
		valueArray[34] = 0;
		valueArray[35] = 12;
		valueArray[36] = 0;
		valueArray[37] = 48;
		valueArray[38] = 48;
		valueArray[39] = 32;
		valueArray[40] = 32;
		valueArray[41] = 3456;
		valueArray[42] = 864;
		valueArray[43] = 8;
		valueArray[44] = 4;
		valueArray[45] = 24;
		valueArray[46] = 484;
		valueArray[47] = 96;
		valueArray[48] = 4;
		valueArray[49] = 192;
		valueArray[50] = 4;
		valueArray[51] = 1;
		valueArray[52] = 0;
		valueArray[53] = 6;
		valueArray[54] = 32;
		valueArray[55] = 8;
		valueArray[56] = 1536;
		valueArray[57] = 13824;
		valueArray[58] = 16;
		valueArray[59] = 4;
		valueArray[60] = 1;
		valueArray[61] = 32;
		valueArray[62] = 32;
		valueArray[63] = 0;
		valueArray[64] = 24;
		valueArray[65] = 7;
		valueArray[66] = 36;
		valueArray[67] = 6;
		valueArray[68] = 0;
		valueArray[69] = 6;
		valueArray[70] = 12;
		valueArray[71] = 576;
		valueArray[72] = 8;
		valueArray[73] = 32;
		valueArray[74] = 32;
		valueArray[75] = 10;
		valueArray[76] = 10;
		valueArray[77] = 12;
		valueArray[78] = 1;
		valueArray[79] = 4;
		valueArray[80] = 4;
		valueArray[81] = 8;
		valueArray[82] = 16;
		valueArray[83] = 8;
		valueArray[84] = 1568;
		valueArray[85] = 6;
		valueArray[86] = 64;
		valueArray[87] = 1;
		valueArray[88] = 2;
		valueArray[89] = 128;
		valueArray[90] = 0;
		valueArray[91] = 68;
		valueArray[92] = 163;
		valueArray[93] = 46;
		valueArray[94] = 46;
		valueArray[95] = 0;
		valueArray[96] = 12;
		valueArray[97] = 0;
		valueArray[98] = 6;
		valueArray[99] = 32;
		valueArray[100] = 32;
		valueArray[101] = 36;
		valueArray[102] = 1;
		valueArray[103] = 64;
		valueArray[104] = 16;
		valueArray[105] = 7;
		valueArray[106] = 1;
		valueArray[107] = 16;
		valueArray[108] = 36;
		valueArray[109] = 9;
		valueArray[110] = 1;
		valueArray[111] = 0;
		valueArray[112] = 0;
		valueArray[113] = 0;
		valueArray[114] = 0;
		valueArray[115] = 8;
		valueArray[116] = 0;
		valueArray[117] = 0;
		valueArray[118] = 0;
		valueArray[119] = 0;
		valueArray[120] = 0;
		valueArray[121] = 1;
		valueArray[122] = 0;
		return valueArray;
	}

	public static double philosopherStoneModifier(Player player) {
		double modifier = 1;
		int stackCount = 0;
		PlayerInventory inventory = player.getInventory();
		for (int i = 0; i < inventory.getSize(); i++) {
			if (inventory.getItem(i) != null
					&& inventory.getItem(i).getType() == Material.PORTAL)
				stackCount += inventory.getItem(i).getAmount();
		}
		modifier = 1 / (Math.pow(2, stackCount));
		return modifier;
	}

	public static boolean checkSacrifices(Player player) throws IOException {
		File myFile = new File("plugins/GeometricMagic/sacrifices.txt");
		if (!myFile.exists()) {
			return false;
		}
		Scanner inputFile = new Scanner(myFile);
		while (inputFile.hasNextLine()) {
			String name = inputFile.nextLine();
			if (name.equals(player.getName()))
				return true;
			inputFile.nextLine();
		}
		inputFile.close();
		return false;
		// playername
		// [1, 1, 1, 2]
	}

	public static void humanTransmutation(Player player) throws IOException {
		if (new File("plugins/GeometricMagic/").mkdirs())
			System.out.println("sacrifices file created");
		File myFile = new File("plugins/GeometricMagic/sacrifices.txt");
		if (myFile.exists()) {
			Scanner inputFile = new Scanner(myFile);
			while (inputFile.hasNextLine()) {
				String name = inputFile.nextLine();
				if (name.equals(player.getDisplayName())) {
					FileWriter dWriter = new FileWriter(
							"plugins/GeometricMagic/sacrificed.txt", true);
					PrintWriter dFile = new PrintWriter(dWriter);
					dFile.println(player.getDisplayName());
					dFile.close();
					return;
				}
			}
			inputFile.close();
		} else {
			PrintWriter outputFile = new PrintWriter(
					"plugins/GeometricMagic/sacrifices.txt");
			System.out.println("sacrifices file created");
			outputFile.close();
		}
		FileWriter fWriter = new FileWriter(
				"plugins/GeometricMagic/sacrifices.txt", true);
		PrintWriter outputFile = new PrintWriter(fWriter);
		outputFile.println(player.getDisplayName());
		outputFile.println(0);
		player.sendMessage("You have committed the taboo! Crafting is your sacrifice, knowledge your reward.");
		outputFile.close();
	}

	public static boolean checkSacrificed(Player player) throws IOException {
		File myFile = new File("plugins/GeometricMagic/sacrificed.txt");
		if (!myFile.exists()) {
			return false;
		}
		Scanner inputFile = new Scanner(myFile);
		while (inputFile.hasNextLine()) {
			String name = inputFile.nextLine();
			if (name.equals(player.getName()))
				return true;
		}
		inputFile.close();
		return false;
		// playername
	}

	public static boolean hasLearnedCircle(Player player, String circle)
			throws IOException {
		File myFile = new File("plugins/GeometricMagic/" + player.getName()
				+ ".txt");
		if (!myFile.exists()) {
			return false;
		}
		Scanner inputFile = new Scanner(myFile);
		while (inputFile.hasNextLine()) {
			String name = inputFile.nextLine();
			if (name.equals(circle))
				return true;
		}
		inputFile.close();
		return false;
	}

	public static boolean learnCircle(Player player, String circle,
			Block actBlock) throws IOException {
		boolean status = false;
		// System.out.println("learnCircle");
		ItemStack oneRedstone = new ItemStack(331, 1);
		Item redStack = actBlock.getWorld().dropItem(actBlock.getLocation(),
				oneRedstone);
		List<Entity> entityList = redStack.getNearbyEntities(2, 10, 2);
		for (int i = 0; i < entityList.size(); i++) {
			if (entityList.get(i) instanceof Enderman) {
				if (new File("plugins/GeometricMagic/").mkdirs())
					System.out.println("file created for " + player.getName());
				File myFile = new File("plugins/GeometricMagic/"
						+ player.getName() + ".txt");
				if (myFile.exists()) {
					FileWriter fWriter = new FileWriter(
							"plugins/GeometricMagic/" + player.getName()
									+ ".txt", true);
					PrintWriter outputFile = new PrintWriter(fWriter);
					outputFile.println(circle);
					outputFile.close();
				} else {
					PrintWriter outputFile = new PrintWriter(
							"plugins/GeometricMagic/" + player.getName()
									+ ".txt");
					outputFile.println(circle);
					outputFile.close();
				}
				status = true;
			}
		}
		redStack.remove();
		return status;
	}
}
