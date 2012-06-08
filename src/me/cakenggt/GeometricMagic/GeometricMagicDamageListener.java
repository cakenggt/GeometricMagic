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

import org.bukkit.Material;
import org.bukkit.entity.*;
//import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class GeometricMagicDamageListener implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event) {
		// System.out.println("entity damage event");
		Entity entity = event.getEntity();
		int damage = event.getDamage();
		if (entity instanceof Player) {
			// System.out.println("instance of player");
			Player player = (Player) entity;
			int health = player.getHealth();
			while (player.getInventory().contains(Material.FIRE)) {
				// System.out.println("contains fire");
				if (health - damage <= 0) {
					boolean removedFire = false;
					for (int i = 0; i < player.getInventory().getSize()
							&& !removedFire; i++) {
						if (player.getInventory().getItem(i).getType() == Material.FIRE) {
							// System.out.println("removed a fire");
							int amount = player.getInventory().getItem(i)
									.getAmount();
							player.getInventory().getItem(i)
									.setAmount(amount - 1);
							if (amount - 1 <= 0) {
								player.getInventory().clear(i);
							}
							damage = damage - health;
							health = 20;
							player.setHealth(20);
							removedFire = true;
						}
					}
				} else {
					event.setDamage(damage);
					return;
				}
			}
			return;
		}
	}
}