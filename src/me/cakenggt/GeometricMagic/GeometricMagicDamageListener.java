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