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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class GeometricMagic extends JavaPlugin {
	private Listener playerListener;
	private Listener entityListener;
	private static Economy economy;
	File configFile;

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		// If the player typed /setcircle then do the following...
		if (cmd.getName().equalsIgnoreCase("setcircle")) {
			Player player = null;

			if (sender instanceof Player) {
				player = (Player) sender;

				if (!player.hasPermission("geometricmagic.command.setcircle")) {
					player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
					return true;
				}
				
				boolean sacrificed = false;

				if (!player.hasPermission("geometricmagic.bypass.sacrifice")) {
					try {
						sacrificed = checkSacrificed(player);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}

				if (sacrificed) {
					player.sendMessage("You have sacrificed your alchemy abilities forever.");
					return true;
				}

				boolean sacrifices = false;
				try {
					sacrifices = checkSacrifices(player);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				// check if player has flint and is using the proper arguments
				boolean hasFlint = player.getInventory().contains(Material.FLINT);
				if ((args.length != 1 || (args[0].length() != 4 && args[0].length() != 1)) && hasFlint && sacrifices) {
					sender.sendMessage(ChatColor.RED + cmd.getUsage());
					return true;
				} else if (args.length == 0 && !hasFlint && sacrifices) {
					// they don't have flint so give them one 
					ItemStack oneFlint = new ItemStack(318, 1);
					player.getWorld().dropItem(player.getLocation(), oneFlint);
					return true;
				} else if (!sacrifices) {
					player.sendMessage(ChatColor.RED + "You must perform a human sacrifice if you wish to use this command.");
					return true;
				}

				if (args[0].length() == 1 && args[0].equalsIgnoreCase("0")) {
					sender.sendMessage("Casting circles on right click now disabled, set right click to a viable circle to enable");
					String inputString = args[0];
					try {
						sacrificeCircle(sender, inputString);
					} catch (IOException e) {
						e.printStackTrace();
					}
					return true;

				} else {
					String inputString = "[" + args[0].charAt(0) + ", " + args[0].charAt(1) + ", " + args[0].charAt(2) + ", " + args[0].charAt(3) + "]";
					try {
						sacrificeCircle(sender, inputString);
					} catch (IOException e) {
						e.printStackTrace();
					}
					return true;
				}
			}

			if (player == null) {
				sender.sendMessage("This command can only be run by a player");
				return false;
			}
		} else if (cmd.getName().equalsIgnoreCase("circles")) {
			if (sender.hasPermission("geometricmagic.command.circles")) {
				sender.sendMessage(ChatColor.GREEN + "1133" + ChatColor.RESET + " Repair Circle");
				sender.sendMessage(ChatColor.GREEN + "1222" + ChatColor.RESET + " Conversion Circle");
				sender.sendMessage(ChatColor.GREEN + "1233" + ChatColor.RESET + " Philosopher's Stone Circle");
				sender.sendMessage(ChatColor.GREEN + "1234" + ChatColor.RESET + " Boron Circle");
				sender.sendMessage(ChatColor.GREEN + "2223" + ChatColor.RESET + " Soul Circle");
				sender.sendMessage(ChatColor.GREEN + "2224" + ChatColor.RESET + " Homunculus Circle");
				sender.sendMessage(ChatColor.GREEN + "2244" + ChatColor.RESET + " Safe Teleportation Circle");
				sender.sendMessage(ChatColor.GREEN + "2333" + ChatColor.RESET + " Explosion Circle");
				sender.sendMessage(ChatColor.GREEN + "3334" + ChatColor.RESET + " Fire Circle");
				sender.sendMessage(ChatColor.GREEN + "3344" + ChatColor.RESET + " Fire Explosion Circle");
				sender.sendMessage(ChatColor.GREEN + "3444" + ChatColor.RESET + " Human Transmutation Circle");
				sender.sendMessage(ChatColor.GREEN + "0111" + ChatColor.RESET + " Bed Circle");
				sender.sendMessage(ChatColor.GREEN + "0044" + ChatColor.RESET + " Pig Circle");
				sender.sendMessage(ChatColor.GREEN + "0144" + ChatColor.RESET + " Sheep Circle");
				sender.sendMessage(ChatColor.GREEN + "0244" + ChatColor.RESET + " Cow Circle");
				sender.sendMessage(ChatColor.GREEN + "0344" + ChatColor.RESET + " Chicken Circle");
			} else
				sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
			return true;
		} else if (cmd.getName().equalsIgnoreCase("geometricmagic")) {
			if (sender.hasPermission("geometricmagic.command.geometricmagic")) {
				if (args.length == 0) {
					sender.sendMessage(ChatColor.GREEN + "*********** GeometricMagic Help ***********");
					sender.sendMessage(ChatColor.GREEN + "*" + ChatColor.GRAY + "************* User Commands *************");
					sender.sendMessage(ChatColor.GREEN + "*" + ChatColor.GRAY + "*" + ChatColor.YELLOW + " /geometricmagic" + ChatColor.WHITE + " - Display this help dialogue");
					sender.sendMessage(ChatColor.GREEN + "*" + ChatColor.GRAY + "*" + ChatColor.YELLOW + " /setcircle <####>" + ChatColor.WHITE + " - Bind set circle #### to flint. 0 resets");
					sender.sendMessage(ChatColor.GREEN + "*" + ChatColor.GRAY + "*" + ChatColor.YELLOW + " /circles" + ChatColor.WHITE + " - List all set circles");
					sender.sendMessage(ChatColor.GREEN + "*" + ChatColor.GRAY + "*****************************************");
					if (sender.hasPermission("geometricmagic.command.geometricmagic.reload")) {
						sender.sendMessage(ChatColor.GREEN + "*" + ChatColor.RED + "************* Admin Commands ************");
						sender.sendMessage(ChatColor.GREEN + "*" + ChatColor.RED + "*" + ChatColor.YELLOW + " /geometricmagic reload" + ChatColor.WHITE + " - Reload config file");
						sender.sendMessage(ChatColor.GREEN + "*" + ChatColor.RED + "*****************************************");
					}
					sender.sendMessage(ChatColor.GREEN + "******************************************");
					return true;
				}
				else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("reload")) {
						if (sender.hasPermission("geometricmagic.command.geometricmagic.reload")) {
							reloadConfig();
							sender.sendMessage(ChatColor.GREEN + "Config reload successfully!");
							return true;
						}
						else {
							sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
							return true;
						}
					}
					else
						return false;
				}
				else if (args.length > 1)
					return false;
			}
			else {
				sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
				return true;
			}
		}
		// If this has happened the function will break and return true. if this
		// hasn't happened the a value of false will be returned.
		return false;
	}

	public void sacrificeCircle(CommandSender sender, String inputString) throws IOException {
		// System.out.println("sacrificeCircle for " + inputString);
		File myFile = new File("plugins/GeometricMagic/sacrifices.txt");
		if (myFile.exists()) {
			Scanner inputFileCheck = new Scanner(myFile);
			int j = 0;
			while (inputFileCheck.hasNext()) {
				inputFileCheck.nextLine();
				j++;
			}
			int size = (j + 1) / 2;
			// System.out.println("size of sacrifices file " + size);
			String[] nameArray = new String[size];
			String[] circleArray = new String[size];
			inputFileCheck.close();
			// System.out.println("inputFileCheck closed");
			Scanner inputFile = new Scanner(myFile);
			// System.out.println("inputFile opened");
			for (int i = 0; i < size; i++) {
				nameArray[i] = inputFile.nextLine();
				circleArray[i] = inputFile.nextLine();
			}
			// System.out.println("nameArray[0] is " + nameArray[0]);
			// System.out.println("circleArray[0] is " + circleArray[0]);
			for (int i = 0; i < size; i++) {
				if (nameArray[i].equalsIgnoreCase(sender.getName())) {
					circleArray[i] = inputString;
					sender.sendMessage("set-circle " + inputString + " added successfully!");
				}
			}
			// System.out.println("nameArray[0] is " + nameArray[0]);
			// System.out.println("circleArray[0] is " + circleArray[0]);
			inputFile.close();
			PrintWriter outputFile = new PrintWriter("plugins/GeometricMagic/sacrifices.txt");
			for (int i = 0; i < size; i++) {
				outputFile.println(nameArray[i]);
				outputFile.println(circleArray[i]);
			}
			outputFile.close();
		} else {
			return;
		}
	}

	public static boolean checkSacrifices(Player player) throws IOException {
		File myFile = new File("plugins/GeometricMagic/sacrifices.txt");
		if (!myFile.exists()) {
			return false;
		}
		Scanner inputFile = new Scanner(myFile);
		while (inputFile.hasNextLine()) {
			String name = inputFile.nextLine();
			if (name.equals(player.getName())) {
				// close this before we return
				inputFile.close();
				return true;
			}
			inputFile.nextLine();
		}
		inputFile.close();
		return false;
		// playername
		// [1, 1, 1, 2]
	}

	public static boolean checkSacrificed(Player player) throws IOException {
		File myFile = new File("plugins/GeometricMagic/sacrificed.txt");
		if (!myFile.exists()) {
			return false;
		}
		Scanner inputFile = new Scanner(myFile);
		while (inputFile.hasNextLine()) {
			String name = inputFile.nextLine();
			if (name.equals(player.getName())) {
				// close this before we return
				inputFile.close();
				return true;
			}
		}
		inputFile.close();
		return false;
		// playername
	}

	@Override
	public void onDisable() {
		System.out.println(this + " is now disabled!");
	}

	@Override
	public void onEnable() {

		configFile = new File(getDataFolder(), "config.yml");

		// Copy default config file if it doesn't exist
		if (!configFile.exists()) {
			configFile.getParentFile().mkdirs();
			copy(this.getResource("config.yml"), configFile);
		}

		// Copy config defaults
		getConfig().options().copyDefaults(true);

		// Transmutation mode: Vault
		if (getConfig().getString("transmutation.cost").toString().equalsIgnoreCase("vault")) {
			// Vault Support
			if (!setupEconomy()) {
				System.out.println("[" + this + "] ERROR: You have your transmutation system set to Vault, and yet you don't have Vault. Disabling plugin!");
				getServer().getPluginManager().disablePlugin(this);
			} else {
				System.out.println("[" + this + "] Transmutation cost system set to Vault");

				// Register events
				playerListener = new GeometricMagicPlayerListener(this);
				entityListener = new GeometricMagicDamageListener(this);
				getServer().getPluginManager().registerEvents(playerListener, this);
				getServer().getPluginManager().registerEvents(entityListener, this);
				ShapelessRecipe portalRecipe = new ShapelessRecipe(new ItemStack(Material.FIRE, 64)).addIngredient(Material.PORTAL);
				getServer().addRecipe(portalRecipe);
				System.out.println(this + " is now enabled!");
			}
		}
		// Transmutation mode: XP
		else if (getConfig().getString("transmutation.cost").toString().equalsIgnoreCase("xp")) {
			System.out.println("[" + this + "] Transmutation cost system set to XP");

			// Register events
			playerListener = new GeometricMagicPlayerListener(this);
			entityListener = new GeometricMagicDamageListener(this);
			getServer().getPluginManager().registerEvents(playerListener, this);
			getServer().getPluginManager().registerEvents(entityListener, this);
			ShapelessRecipe portalRecipe = new ShapelessRecipe(new ItemStack(Material.FIRE, 64)).addIngredient(Material.PORTAL);
			getServer().addRecipe(portalRecipe);
			System.out.println(this + " is now enabled!");
		}
		// Transmutation mode: Unknown
		else {
			System.out.println("[" + this + "] ERROR: You have your transmutation cost system set to an unknown value. Disabling plugin!");
			getServer().getPluginManager().disablePlugin(this);
		}

		// Plugin metrics
		startPluginMetrics();
	}

	// Vault Support
	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return (economy != null);
	}

	// Vault Support
	public static Economy getEconomy() {
		return economy;
	}

	// Copy method
	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void startPluginMetrics() {
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
		}
	}
}