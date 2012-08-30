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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// This class is basically intended for use as an automatic config updater, since
// the normal saveConfig() screws up all of the formatting. Unfortunately, this
// method also screws up the formatting a little bit, but it works slightly better.

public class GeometricMagicConfigUpdater {
	static GeometricMagic plugin;
	
	public static void updateConfig(GeometricMagic instance) throws IOException {
		plugin = instance;
		
		// If the config is not updated
		if (!isUpdated()) {
			// If the config is not updated to version 2.7.3
			if (!plugin.getConfig().isSet("version")) {
				System.out.println("[GeometricMagic] Updating config to v2.7.3...");
				
				File myFile = new File("plugins/GeometricMagic/config.yml");
				if (myFile.exists()) {
					FileWriter fileWriter = new FileWriter(myFile, true);
					PrintWriter file = new PrintWriter(fileWriter);
					file.println();
					file.println("# DO NOT MODIFY THIS VALUE!");
					file.println("version: 2.7.3");
					file.println();
					file.println("# GENERAL SECTION");
					file.println("general:");
					file.println();
					file.println("    # Automatic notification of plugin updates");
					file.println("    auto-update-notify: true");
					file.close();
					
					plugin.reloadConfig();
				}
				else
					return;
				
				if (!isUpdated()) {
					updateConfig(instance);
					return;
				}
			}
			else if (plugin.getConfig().getString("version").equals("2.7.3")) {
				System.out.println("[GeometricMagic] Updating config to v2.7.4...");
				List<String> strList = new ArrayList<String>();
				
				File myFile = new File("plugins/GeometricMagic/config.yml");
				if (myFile.exists()) {
					Scanner in = new Scanner(myFile);
					while (in.hasNextLine()) {
						String nextLine = in.nextLine();
						if (!nextLine.contains("version:")
								&& !nextLine.contains("# DO NOT MODIFY THIS VALUE!"))
							strList.add(nextLine);
						else
							in.nextLine();
					}
					in.close();
					
					Files.delete(Paths.get("plugins/GeometricMagic/config.yml"));
					PrintWriter out = new PrintWriter(new File("plugins/GeometricMagic/config.yml"));
					for (String s : strList) {
						out.println(s);
					}
					
					out.println();
					out.println("# DO NOT MODIFY THIS VALUE!");
					out.println("version: 2.7.4");
					out.close();
					
					plugin.reloadConfig();
				}
				else
					return;
				
				if (!isUpdated()) {
					updateConfig(instance);
					return;
				}
			}
			else if (plugin.getConfig().getString("version").equals("2.7.4")) {
				System.out.println("[GeometricMagic] Updating config to v2.7.5...");
				List<String> strList = new ArrayList<String>();
				
				File myFile = new File("plugins/GeometricMagic/config.yml");
				if (myFile.exists()) {
					Scanner in = new Scanner(myFile);
					while (in.hasNextLine()) {
						String nextLine = in.nextLine();
						if (!nextLine.contains("version:")
								&& !nextLine.contains("# DO NOT MODIFY THIS VALUE!"))
							strList.add(nextLine);
						else
							in.nextLine();
					}
					in.close();
					
					Files.delete(Paths.get("plugins/GeometricMagic/config.yml"));
					PrintWriter out = new PrintWriter(new File("plugins/GeometricMagic/config.yml"));
					for (String s : strList) {
						out.println(s);
					}
					
					out.println();
					out.println("# DO NOT MODIFY THIS VALUE!");
					out.println("version: 2.7.5");
					out.close();
					
					plugin.reloadConfig();
				}
				else
					return;
				
				if (!isUpdated()) {
					updateConfig(instance);
					return;
				}
			}
			else if (plugin.getConfig().getString("version").equals("2.7.5")) {
				System.out.println("[GeometricMagic] Updating config to v2.7.6...");
				List<String> strList = new ArrayList<String>();
				
				File myFile = new File("plugins/GeometricMagic/config.yml");
				if (myFile.exists()) {
					Scanner in = new Scanner(myFile);
					while (in.hasNextLine()) {
						String nextLine = in.nextLine();
						if (!nextLine.contains("version:")
								&& !nextLine.contains("# DO NOT MODIFY THIS VALUE!")
								&& !nextLine.contains("# TRANSMUTATION SECTION")
								&& !nextLine.contains("transmutation:")
								&& !nextLine.contains("    # Cool down time between transmutations in seconds")
								&& !nextLine.contains("    cooldown:")
								&& !nextLine.contains("    # What system to use for transmutation cost")
								&& !nextLine.contains("    # Possible values: xp, vault (requires Vault)")
								&& !nextLine.contains("    cost: xp")
								&& !nextLine.contains("    # Apply Philosopher's Stone to transmutes (true or false)")
								&& !nextLine.contains("    stone: true"))
							strList.add(nextLine);
						else
							in.nextLine();
					}
					in.close();
					
					Files.delete(Paths.get("plugins/GeometricMagic/config.yml"));
					PrintWriter out = new PrintWriter(new File("plugins/GeometricMagic/config.yml"));
					for (String s : strList) {
						out.println(s);
					}
					
					out.println();
					out.println("# DO NOT MODIFY THIS VALUE!");
					out.println("version: 2.7.6");
					out.println();
					out.println("# TRANSMUTATION SECTION");
					out.println("transmutation:");
					out.println();
					out.println("    # Rate (in milliseconds) at which blocks are updated");
					out.println("    rate: 10");
					out.println();
					out.println("    # Cool down time between transmutations in seconds");
					out.println("    cooldown: 10");
					out.println();
					out.println("    # What system to use for transmutation cost");
					out.println("    # Possible values: xp, vault (requires Vault)");
					out.println("    cost: xp");
					out.println();
					out.println("    # Apply Philosopher's Stone to transmutes (true or false)");
					out.println("    stone: true");
					out.println();
					out.close();
					
					plugin.reloadConfig();
				}
				else
					return;
				
				if (!isUpdated()) {
					updateConfig(instance);
					return;
				}
			}
			else if (plugin.getConfig().getString("version").equals("2.7.6")) {
				System.out.println("[GeometricMagic] Updating config to v2.8.0...");
				List<String> strList = new ArrayList<String>();
				
				File myFile = new File("plugins/GeometricMagic/config.yml");
				if (myFile.exists()) {
					Scanner in = new Scanner(myFile);
					while (in.hasNextLine()) {
						String nextLine = in.nextLine();
						if (!nextLine.contains("version:")
								&& !nextLine.contains("# DO NOT MODIFY THIS VALUE!"))
							strList.add(nextLine);
						else
							in.nextLine();
					}
					in.close();
					
					Files.delete(Paths.get("plugins/GeometricMagic/config.yml"));
					PrintWriter out = new PrintWriter(new File("plugins/GeometricMagic/config.yml"));
					for (String s : strList) {
						out.println(s);
					}
					
					out.println();
					out.println("# DO NOT MODIFY THIS VALUE!");
					out.println("version: 2.8.0");
					out.close();
					
					plugin.reloadConfig();
				}
				else
					return;
				
				if (!isUpdated()) {
					updateConfig(instance);
					return;
				}
			}
		}
		// If the config is updated
		else {
			System.out.println("[GeometricMagic] No config update needed");
			return;
		}
		
		System.out.println("[GeometricMagic] Config updated successfully!");
	}
	
	public static boolean isUpdated() {
		if (plugin.getConfig().isSet("version")
				&& plugin.getDescription().getVersion()
					.equals(plugin.getConfig().getString("version"))) {
			return true;
		}
		else {
			return false;
		}
	}
}