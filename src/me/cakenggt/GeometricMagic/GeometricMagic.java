package me.cakenggt.GeometricMagic;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class GeometricMagic extends JavaPlugin {
	public static GeometricMagic plugin;
	private Listener playerListener;
	private Listener entityListener;
	private Listener deathListener;
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
    	if(cmd.getName().equalsIgnoreCase("setcircle")){ // If the player typed /set then do the following...
    		//[1, 1, 3, 3]
    		Player player = null;
    		if (sender instanceof Player) {
    			player = (Player) sender;
    		}
    		if (player == null) {
    			sender.sendMessage("this command can only be run by a player");
    			return false;
    		}
    		if (args.length == 0){
    			ItemStack oneFlint = new ItemStack(318, 1);
				player.getWorld().dropItem(player.getLocation(), oneFlint);
    			return true;
    		}
    		if (args.length != 1){
    			sender.sendMessage(cmd.getUsage());
    			return false;
    		}
    		if (args[0].length() != 4 && args[0].length() != 1){
    			sender.sendMessage(cmd.getUsage());
    			return false;
    		}
    		if (args[0].length() == 1 && args[0].equalsIgnoreCase("0")){
    			sender.sendMessage("Casting circles on right click now disabled, set right click to a viable circle to enable");
    			String inputString = args[0];
    			try {
    				sacrificeCircle(sender, inputString);
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    			return true;
    		}
    		else{
    			String inputString = "[" + args[0].charAt(0) + ", " + args[0].charAt(1) + ", " + args[0].charAt(2) + ", " + args[0].charAt(3) + "]";
    			try {
    				sacrificeCircle(sender, inputString);
    			} catch (IOException e) {
    				e.printStackTrace();
			}
    		return true;
    		}
    	} //If this has happened the function will break and return true. if this hasn't happened the a value of false will be returned.
    	return false; 
    }
    
    public void sacrificeCircle (CommandSender sender, String inputString) throws IOException{
    	//System.out.println("sacrificeCircle for " + inputString);
    	File myFile = new File("plugins/GeometricMagic/sacrifices.txt");
    	if (myFile.exists()){
            Scanner inputFileCheck = new Scanner(myFile);
            int j = 0;
            while (inputFileCheck.hasNext()) {
            	inputFileCheck.nextLine();
            	j++;
            }
            int size = (j + 1) / 2;
            //System.out.println("size of sacrifices file " + size);
            String[] nameArray = new String[size];
            String[] circleArray = new String[size];
            inputFileCheck.close();
            //System.out.println("inputFileCheck closed");
            Scanner inputFile = new Scanner(myFile);
            //System.out.println("inputFile opened");
            for (int i = 0; i < size; i++) {
                nameArray[i] = inputFile.nextLine();
                circleArray[i] = inputFile.nextLine();
            }
            //System.out.println("nameArray[0] is " + nameArray[0]);
            //System.out.println("circleArray[0] is " + circleArray[0]);
            for (int i = 0; i < size; i++) {
                if (nameArray[i].equalsIgnoreCase(sender.getName())){
                		circleArray[i] = inputString;
                		sender.sendMessage("set-circle " + inputString + " added successfully!");
                }
            }
            //System.out.println("nameArray[0] is " + nameArray[0]);
            //System.out.println("circleArray[0] is " + circleArray[0]);
            inputFile.close();
            PrintWriter outputFile = new PrintWriter("plugins/GeometricMagic/sacrifices.txt");
            for (int i = 0; i < size; i++) {
                outputFile.println(nameArray[i]);
                outputFile.println(circleArray[i]);
            }
			outputFile.close();
		}
		else{
			return;
		}
    }

	
    public void onDisable() {
        // TODO: Place any custom disable code here.
        System.out.println(this + " is now disabled!");
    }
    
    public void onEnable() {
        // TODO: Place any custom enable code here, such as registering events
    	playerListener = new GeometricMagicPlayerListener();
    	entityListener = new GeometricMagicDamageListener();
    	deathListener = new GeometricMagicPlayerDeathListener();
    	getServer().getPluginManager().registerEvents(playerListener, this);
    	getServer().getPluginManager().registerEvents(entityListener, this);
    	getServer().getPluginManager().registerEvents(deathListener, this);
    	ShapelessRecipe portalRecipe = new ShapelessRecipe(new ItemStack(Material.FIRE, 64)).addIngredient(Material.PORTAL);
    	getServer().addRecipe(portalRecipe);
        System.out.println(this + " is now enabled!");
    }
  
    
    




}