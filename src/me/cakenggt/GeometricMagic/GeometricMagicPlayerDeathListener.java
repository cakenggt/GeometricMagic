package me.cakenggt.GeometricMagic;


import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
//import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;



public class GeometricMagicPlayerDeathListener implements Listener {
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDeath(EntityDeathEvent event) {
		if (event.getEntity() instanceof Player){
			Player player = (Player) event.getEntity();
			int level = player.getLevel();
			while (level > 5){
				if (level > 13824){
					ItemStack oneDiamondBlock = new ItemStack(Material.DIAMOND_BLOCK, 1);
					player.getWorld().dropItem(player.getLocation(), oneDiamondBlock);
					level -= 13824;
				}
				else if (level > 1568){
					ItemStack one84 = new ItemStack(84, 1);
					player.getWorld().dropItem(player.getLocation(), one84);
					level -= 1568;
				}
				else if (level > 153){
					ItemStack one29 = new ItemStack(29, 1);
					player.getWorld().dropItem(player.getLocation(), one29);
					level -= 153;
				}
				else if (level > 12){
					ItemStack one70 = new ItemStack(70, 1);
					player.getWorld().dropItem(player.getLocation(), one70);
					level -= 12;
				}
				else if (level > 1){
					ItemStack one2 = new ItemStack(2, 1);
					player.getWorld().dropItem(player.getLocation(), one2);
					level -= 1;
				}
			}
		}
	}
}