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

import net.h31ix.anticheat.api.AnticheatAPI;
import net.h31ix.anticheat.manage.CheckType;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class GeometricMagicTransmutationThread implements Runnable {
	private GeometricMagic plugin;
	private long rate;
	private Material a;
	private byte fromData;
	private Material b;
	private byte toData;
	private Location start;
	private Location end;
	private String playerName;
	private boolean charge;
	
	public GeometricMagicTransmutationThread(GeometricMagic instance,
			long rateLong,
			Material aMat,
			byte fromDataByte,
			Material bMat,
			byte toDataByte,
			Location startLoc,
			Location endLoc,
			String playerNameString,
			boolean chargeBool) {
		plugin = instance;
		rate = rateLong;
		a = aMat;
		fromData = fromDataByte;
		b = bMat;
		toData = toDataByte;
		start = startLoc;
		end = endLoc;
		playerName = playerNameString;
		charge = chargeBool;
	}
	
	public void run() {
		int xIteration = 0;
		int yIteration = 0;
		int zIteration = 0;
		final Location startOriginal = start;

		if (startOriginal.getX() < end.getX()) {
			if (startOriginal.getZ() < end.getZ()) {
				// east
				// System.out.println("alchemyFiller east");
				while (start.getY() <= end.getY()) {
					while (start.getX() <= end.getX()) {
						while (start.getZ() <= end.getZ()) {
							transmuteBlock(a, fromData, b, toData, start, playerName, charge);
							try {
								Thread.sleep(rate);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							start = start.add(0, 0, 1);
						}
						xIteration++;
						start = startOriginal.add(xIteration, yIteration, 0);
					}
					yIteration++;
					xIteration = 0;
					start = startOriginal.add(0, yIteration, 0);
				}
			} else {
				// north
				// System.out.println("alchemyFiller north");
				while (start.getY() <= end.getY()) {
					while (start.getZ() >= end.getZ()) {
						while (start.getX() <= end.getX()) {
							transmuteBlock(a, fromData, b, toData, start, playerName, charge);
							try {
								Thread.sleep(rate);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							start = start.add(1, 0, 0);
						}
						zIteration--;
						start = startOriginal.add(0, yIteration, zIteration);
					}
					yIteration++;
					zIteration = 0;
					start = startOriginal.add(0, yIteration, 0);
				}
			}
		} else {
			if (startOriginal.getZ() > end.getZ()) {
				// west
				// System.out.println("alchemyFiller west");
				while (start.getY() <= end.getY()) {
					while (start.getX() >= end.getX()) {
						while (start.getZ() >= end.getZ()) {
							transmuteBlock(a, fromData, b, toData, start, playerName, charge);
							try {
								Thread.sleep(rate);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							start = start.add(0, 0, -1);
						}
						xIteration--;
						start = startOriginal.add(xIteration, yIteration, 0);
					}
					yIteration++;
					xIteration = 0;
					start = startOriginal.add(0, yIteration, 0);
				}
			} else {
				// south
				// System.out.println("alchemyFiller south");
				while (start.getY() <= end.getY()) {
					while (start.getZ() <= end.getZ()) {
						while (start.getX() >= end.getX()) {
							transmuteBlock(a, fromData, b, toData, start, playerName, charge);
							try {
								Thread.sleep(rate);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							start = start.add(-1, 0, 0);
							// System.out.println("xloopfiller");
						}
						zIteration++;
						// System.out.println("zloopfiller");
						start = startOriginal.add(0, yIteration, zIteration);
					}
					yIteration++;
					// System.out.println("yloopfiller");
					zIteration = 0;
					start = startOriginal.add(0, yIteration, 0);
				}
			}
		}
		
		return;
	}
	
	public void transmuteBlock(final Material a, final byte fromData, final Material b, final byte toData,
										final Location startBlockLoc, final String playerName, final boolean charge) {
		System.out.println("[DEBUG] transmuteBlock void");
		
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				System.out.println("[DEBUG] Scheduled task");
				Player player = plugin.getServer().getPlayer(playerName);
				Block startBlock = startBlockLoc.getBlock();
				double pay = GeometricMagicPlayerListener.calculatePay(a, fromData, b, toData, player);
				
				// exempt player from AntiCheat check
				if (Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null) {
					AnticheatAPI.exemptPlayer(player, CheckType.FAST_PLACE);
					AnticheatAPI.exemptPlayer(player, CheckType.FAST_BREAK);
					AnticheatAPI.exemptPlayer(player, CheckType.LONG_REACH);
					AnticheatAPI.exemptPlayer(player, CheckType.NO_SWING);
				}
				
				if (player != null) {
					System.out.println("[DEBUG] Player != null");
					System.out.println("[DEBUG] startBlockType=" + startBlock.getType().toString() + ", a=" + a.toString() + ", startBlockData=" + String.valueOf(startBlock.getData()) + ", fromData=" + String.valueOf(fromData));
					if (startBlock.getType() == a && startBlock.getData() == fromData) {
						System.out.println("[DEBUG] startBlock = A");

						if (-1 * GeometricMagicPlayerListener.getBalance(player) < pay || !charge) {
							System.out.println("[DEBUG] -1 * bal < pay || !charge");

							// Block break
							if (a != Material.AIR && b == Material.AIR) {
								System.out.println("[DEBUG] Break");
								
								if (!GeometricMagicPlayerListener.checkBreakBlacklist(a.getId())) {
									
									Location blockLocation = startBlock.getLocation();

									if (GeometricMagicPlayerListener.checkBlockBreakSimulation(blockLocation, player)) {
										// Change block
										startBlock.setType(b);
										if (toData != 0)
											startBlock.setData(toData);

										if (charge) {
											if (GeometricMagicPlayerListener.getTransmutationCostSystem(plugin).equalsIgnoreCase("vault")) {

												Economy econ = GeometricMagic.getEconomy();

												// Deposit or withdraw to players Vault account
												if (pay > 0) {
													econ.depositPlayer(player.getName(), pay);
												} else if (pay < 0) {
													econ.withdrawPlayer(player.getName(), pay * -1);
												}
											} else if (GeometricMagicPlayerListener.getTransmutationCostSystem(plugin).equalsIgnoreCase("xp")) {
												player.setLevel((int) (player.getLevel() + pay));
											}
										}
									}
								}
								
								else {
									player.sendMessage(ChatColor.RED + "[GeometricMagic] That block is blacklisted");
									return;
								}
							}

							// Block place
							else if (a == Material.AIR && b != Material.AIR) {
								System.out.println("[DEBUG] Place");
								
								if (!GeometricMagicPlayerListener.checkPlaceBlacklist(b.getId())) {
									
									Location blockLocation = startBlock.getLocation();
									int blockID = b.getId();
									byte blockData = toData;

									if (GeometricMagicPlayerListener.checkBlockPlaceSimulation(blockLocation, blockID, blockData, blockLocation, player)) {
										// Change block
										startBlock.setType(b);
										if (toData != 0)
											startBlock.setData(toData);

										if (charge) {
											if (GeometricMagicPlayerListener.getTransmutationCostSystem(plugin).equalsIgnoreCase("vault")) {

												Economy econ = GeometricMagic.getEconomy();

												// Deposit or withdraw to players Vault account
												if (pay > 0) {
													econ.depositPlayer(player.getName(), pay);
												} else if (pay < 0) {
													econ.withdrawPlayer(player.getName(), pay * -1);
												}
											} else if (GeometricMagicPlayerListener.getTransmutationCostSystem(plugin).equalsIgnoreCase("xp")) {
												player.setLevel((int) (player.getLevel() + pay));
											}
										}
									}
								}
								
								else {
									player.sendMessage(ChatColor.RED + "[GeometricMagic] That block is blacklisted");
									return;
								}
							}

							// Block break and place
							else if (a != Material.AIR && b != Material.AIR) {
								System.out.println("[DEBUG] Break and place");

								if (!GeometricMagicPlayerListener.checkBreakBlacklist(a.getId()) && !GeometricMagicPlayerListener.checkPlaceBlacklist(b.getId())) {
									
									Location blockLocation = startBlock.getLocation();
									int blockID = b.getId();
									byte blockData = toData;

									if (GeometricMagicPlayerListener.checkBlockBreakSimulation(blockLocation, player)
											&& GeometricMagicPlayerListener.checkBlockPlaceSimulation(blockLocation, blockID, blockData, blockLocation, player)) {
										// Change block
										startBlock.setType(b);
										if (toData != 0)
											startBlock.setData(toData);

										if (charge) {
											if (GeometricMagicPlayerListener.getTransmutationCostSystem(plugin).equalsIgnoreCase("vault")) {

												Economy econ = GeometricMagic.getEconomy();

												// Deposit or withdraw to players Vault account
												if (pay > 0) {
													econ.depositPlayer(player.getName(), pay);
												} else if (pay < 0) {
													econ.withdrawPlayer(player.getName(), pay * -1);
												}
											} else if (GeometricMagicPlayerListener.getTransmutationCostSystem(plugin).equalsIgnoreCase("xp")) {
												player.setLevel((int) (player.getLevel() + pay));
											}
										}
									}
								}
								
								else {
									player.sendMessage(ChatColor.RED + "[GeometricMagic] That block is blacklisted");
									return;
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
						// System.out.println("[GeometricMagic] DEBUG - Block Data: " + (int) startBlock.getData() + ", A Data: " + (int) fromData + ", B Data: " + (int) toData);
						return;
				}
				
				// unexempt player from AntiCheat check
				if (Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null) {
					AnticheatAPI.unexemptPlayer(player, CheckType.FAST_PLACE);
					AnticheatAPI.unexemptPlayer(player, CheckType.FAST_BREAK);
					AnticheatAPI.unexemptPlayer(player, CheckType.LONG_REACH);
					AnticheatAPI.unexemptPlayer(player, CheckType.NO_SWING);
				}
			}
		}, 0L);
	}
}