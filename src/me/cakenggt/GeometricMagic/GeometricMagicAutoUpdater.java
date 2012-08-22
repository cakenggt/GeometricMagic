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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;



public class GeometricMagicAutoUpdater implements Runnable {
	GeometricMagic plugin;
	private int pluginVersion;
	
	public GeometricMagicAutoUpdater(GeometricMagic instance, int v) {
		plugin = instance;
		pluginVersion = v;
	}
	
	public void run() {
		String rawVersion = null;
		
		try {
			URL url = new URL("http://dl.dropbox.com/u/56151340/BukkitPlugins/GeometricMagic/latest.txt");
			Scanner scanner = new Scanner(url.openStream());
			rawVersion = scanner.next();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int latestVersion = Integer.parseInt(rawVersion.replace(".", ""));
		
		if (latestVersion > pluginVersion) {
			plugin.upToDate = false;
			System.out.println("[GeometricMagic] A newer version of GeometricMagic is available!");
		}
		else
			System.out.println("[GeometricMagic] Plugin is up to date!");
	}
}