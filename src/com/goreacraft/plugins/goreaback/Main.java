package com.goreacraft.plugins.goreaback;



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Iterables;



/**
 * @author goreacraft
 *
 */
public class Main extends JavaPlugin implements Listener
{
	
	public final Logger logger = Logger.getLogger("minecraft");
	boolean isVault = false;
	
    public static Economy econ = null;
    public static Permission perms = null;
    public static Chat chat = null;
	static File DeathsLogsFile;
	static YamlConfiguration playerDeathData;
	static List<String> aliases;
	public static JavaPlugin plugin;
	static HashMap<String, List<String>> map = new HashMap<String, List<String>>();
	static HashMap<String, Long> coldowns = new HashMap<String, Long>();
	static HashMap<String, Long> coldowns2 = new HashMap<String, Long>();
	static HashMap<String, Integer> warmup = new HashMap<String, Integer>();
	static List<String> help = Arrays.asList( "?", "help");
	static Integer taskid;
	static int finesse;
	static List<String> wornedlava = new ArrayList<String>();
	
	static long timeseconds;
  
	Set<String> names;

	@Override
    public void onEnable()
    { 
		finesse = getConfig().getInt("Gback Warmup finesse on move");
    	PluginDescriptionFile pdfFile = this.getDescription();
    	this.logger.info(pdfFile.getName() + " Version " + pdfFile.getVersion() + " has been enabled! " + pdfFile.getWebsite());
    	getConfig().options().copyDefaults(true);
    	getConfig().options().header("If you need help with this plugin you can contact goreacraft on teamspeak ip: goreacraft.com\n Website http://www.goreacraft.com");
    	saveConfig();
    	plugin = (JavaPlugin) this.getServer().getPluginManager().getPlugin(pdfFile.getName());
    	
    	aliases = getCommand("goreaback").getAliases();
    	//aliases = getConfig().getStringList("Aliases");
    	 	    	
    	DeathsLogsFile = new File(getDataFolder(), "Deaths.yml");
    	
    	Bukkit.getServer().getPluginManager().registerEvents(this, this);
    	Bukkit.getServer().getPluginManager().registerEvents(new WarmupListeners(), this);
    	    	
    	playerDeathData = YamlConfiguration.loadConfiguration(DeathsLogsFile);

    	names = playerDeathData.getKeys(false);
    	
    	if (getConfig().getBoolean("Check on start for old deaths"))
    	{
    		CheckTimeOnDeaths();
    	}
    	if (getConfig().getBoolean("Load deaths on start"))
    	{
    		loadDeaths();
    	}
    	
    	
    	
    	//====================================== VAULT STUFF =====================================================
    	 if (setupEconomy() ) {
    		 if (getConfig().getBoolean("More info in logs"))
    			 logger.info(String.format("[%s] vault found, enabling the economy module. ", getDescription().getName()));
             isVault= true;

             
         } else if (getConfig().getBoolean("More info in logs"))logger.warning(String.format("[%s] The economy module Disabled due to no Vault found! Get Vault from: http://dev.bukkit.org/bukkit-plugins/vault/", getDescription().getName()));
  
    
    
    	//====================================== METRICS STUFF =====================================================
    	 try {
    		    Metrics metrics = new Metrics(this);
    		    metrics.start();
    		} catch (IOException e) {
    		    // Failed to submit the stats :-(
    		}

		new Updater(77997);

    }
   
    
    
    @Override
    public void onDisable()
    {      
    	PluginDescriptionFile pdfFile = this.getDescription();
    	this.logger.info(pdfFile.getName() + " Version " + pdfFile.getVersion() + " has been disabled!" + pdfFile.getWebsite());
    	try
	       {
	    	   playerDeathData.save(DeathsLogsFile);
	    	   
	       }
	       catch (Exception ex)
	       {
	    	   
	       }
    }
    
    
   
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event)
    {
    	Player player = event.getEntity().getPlayer();    	
    	String world = event.getEntity().getWorld().getName();
    	double pX = event.getEntity().getLocation().getX();
    	double pY = event.getEntity().getLocation().getY();
    	double pZ = event.getEntity().getLocation().getZ();
    	timeseconds = Calendar.getInstance().getTimeInMillis() / 1000 ;

    	
    	List<String> loc = new ArrayList<String>();
    	loc.add(world.toString());
    	
    	loc.add(Double.toString(pX));
    	loc.add(Double.toString(pY));
    	loc.add(Double.toString(pZ));
    	loc.add(Long.toString(timeseconds));
    	
    	playerDeathData.createSection(player.getName());
    	playerDeathData.set(player.getName(), loc);
 
		map.put(player.getName(), loc );
	
    	if (getConfig().getBoolean("Save to file on every death event"))
    	{
    	try
	       {
    		playerDeathData.save(DeathsLogsFile);
	    	   
	       }
	       catch (Exception ex)
	       {
	    	   
	       }
    	}
        
        if(player.hasPermission("gorea.back") && getConfig().getString("Death Message") != null){
        	
        	}
        	String price = messageondeath();
        	player.sendMessage(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.RESET + ": " + ChatColor.GOLD + getConfig().getString("Death Message") + price);
        }
    private String messageondeath() {
    	if (isVault){
    		
		
		return " For " + getConfig().getString("Currency") + getConfig().getString("Price");
	} else {
		return "";
	}
	}




    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (sender instanceof Player)
        {
            final Player player = (Player) sender;
            String name = player.getName();
            double coldown = getConfig().getDouble("Gback cooldown");
            double coldown2 = getConfig().getDouble("Gback to another player coldown");
            final long timpacum = new Long(Calendar.getInstance().getTimeInMillis() / 1000);
           // player.sendMessage(label);
           // player.sendMessage(aliases.toString());
            
            if (label.contentEquals("goreaback"))
            {
            	
            	showhelpplayer(player);
            	player.sendMessage(getCommand("goreaback").getDescription());
            	return false;
            }
            
            if (aliases.contains(label))
            {
            	if (args.length == 0)
            	{
		            if(sender.hasPermission("gorea.back") || sender.isOp())
		            {
		            	if (map.containsKey(player.getName())) 
		            	{
		            		
		            			if (coldowns.containsKey(name))
		            			{
		            				
			            					if((timpacum - coldowns.get(name)) < coldown && !player.hasPermission("gorea.bypass.cooldown"))
						            			{
			            						double timpramas = new Double (coldown - (timpacum - coldowns.get(name)));
			            						player.sendMessage(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.RESET + ": " + ChatColor.YELLOW + "You have to wait " + (int)timpramas + " seconds before you can use this command again.");
			            						return true;
			            						}						            				
						            			
		            			}
		            			
		            			//Location below = new Location(getServer().getWorld((String) map.get(name).get(0)),Integer.parseInt(map.get(name).get(1)), Integer.parseInt(map.get(name).get(2)), Integer.parseInt(map.get(name).get(3)));
		            			//Location headlevel = new Location(getServer().getWorld((String) map.get(name).get(0)),Integer.parseInt(map.get(name).get(1)), Integer.parseInt(map.get(name).get(2))-1, Integer.parseInt(map.get(name).get(3)));
		            			//Material below = getBlockAt(location.x, location.y - 1, location.z).getType();
		            		if  (getConfig().getBoolean("Detect lava"))
		            		{
		            			if (!wornedlava.contains(name))
					           {
		            			
		            			int x;
		            			int y;
		            			int z;
		            			x = (int)Double.parseDouble(map.get(name).get(1));
		            			y = (int)Double.parseDouble(map.get(name).get(2));
		            			z = (int)Double.parseDouble(map.get(name).get(3));
		            		//	player.sendMessage("Detect lava: " + getConfig().getBoolean("Detect lava") );
		            			
		            			//player.sendMessage("location: " + map.get(name));
		            			for (int xi=-1; xi<= 1;xi++)
		            			{
		            				for (int yi=-1; yi<= 1;yi++)
		            				{
		            					for (int zi=-1; zi<= 1;zi++)
		            					{
				            				Block loc = Bukkit.getWorld((String) map.get(name).get(0)).getBlockAt(x+xi,y+ yi, z+zi);
				            				
				            				if( loc.getType().equals(Material.LAVA) ||  loc.getType().equals(Material.STATIONARY_LAVA))
					            			{
					            			//	player.sendMessage("checked for lava " + xi + " " + yi  + " " + zi);
					            				
					            					wornedlava.add(name);
					            					player.sendMessage(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.RESET + ": " + ChatColor.RED + "Lava detected on destination" + ChatColor.YELLOW + " , type the command again in the next " + ChatColor.GOLD +  getConfig().getString("Warn lava cooldown") + "s" + ChatColor.YELLOW + " to force teleport.");
					            					
					            					return true;
					            				 
					            				
					            					
					            				}
					            			}
		            					}
		            				}
					           }else{ wornedlava.remove(name);}
		            			
		            		}
		            		//	Block below = Bukkit.getWorld((String) map.get(name).get(0)).getBlockAt(xi, yi, zi);
		            		//	player.sendMessage("headlevel: " + headlevel.getType());
		            			//player.sendMessage("below: " + below.getType());
		            			
		            		//}
		            		if  (getConfig().getBoolean("Detect void"))
		            		{
		            			if(Double.parseDouble(map.get(name).get(2))<0)
		            			{
		            			
		            			player.sendMessage(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.RESET + ": " + ChatColor.RED + "Void detected on destination");
		            			return true;
		            			}
		            		}
		            	if (isVault )
            			{
            					if (!(player.hasPermission("gorea.bypass.pay")) && (econ.getBalance(player.getName()) < getConfig().getInt("Price")) )
            					{
				            		player.sendMessage(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.RESET + ": " + ChatColor.YELLOW + "You are missing " + ChatColor.GOLD +  getConfig().getString("Currency") + (getConfig().getInt("Price")-econ.getBalance(player.getName())) + ChatColor.YELLOW + " to use this command. ");
            						return true;				            		
            					} 
            					
            			}
		            			
				            	if (!warmup.containsKey(name))
				            	{
				            		
				            		if (player.hasPermission("gorea.bypass.warmup"))
				            		{
				            			tpplayer(name);
				            			return true;
				            		}
				            		
				            		//================================================ warmup
				            		taskid = new BukkitRunnable() {
				            			@Override
										public void run() {
											
				            				
				            				
				            				
				            				int task = this.getTaskId();
				            				for (String name: warmup.keySet())
				            				{				            		           
												if (task==(warmup.get(name)) && player.isOnline()) {
				            		            	try {
				            		            		Player player = findPlayerByString(name);
				            		            		if (isVault && !(player.hasPermission("gorea.bypass.pay")))
				        		            			{				            		            			
				            		            			econ.withdrawPlayer(name, getConfig().getInt("Price"));
				            		            			player.sendMessage(String.format(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.RESET + ": "  + ChatColor.YELLOW  +  getConfig().getString("Currency") + ChatColor.YELLOW + "%s have been taken from your account, you have now %s" ,getConfig().getInt("Price"),econ.format(econ.getBalance(player.getName())) ));
				        		            			}				            		            		
				            		            		
				            		            		tpplayer(name);
				            		            		warmup.remove(name);
				            		            		coldowns.put(name, timpacum);
				            		            		
				            		            		}												
												       catch (Exception ex)
												       {
												    	   System.out.println("[GoreaBack]:" + ex);
												       }				            		            	
				            		            	
				            		            } else {     
				            		            	System.out.println("[WARNING][GoreaBack]:" +  "Failed to teleport player " + name + ", he went offline!?");
				            		            }
				            				} 			            			
				            			}
										
				                    }.runTaskLater(plugin, getConfig().getInt("Gback wormup")*20).getTaskId();
				                    warmup.put(name, taskid);				                  
				                  
				                   
				                    player.sendMessage(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.RESET + ": " + ChatColor.YELLOW + String.format("Please wait for " + ChatColor.GOLD + "%s "  + ChatColor.YELLOW + " seconds to warmup the command.", getConfig().get("Gback warmup")));
					            	
				            		
				            	
				            	} else {
				            	//-----------------------------------------------
				            		
				            		player.sendMessage(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.RESET + ": " + ChatColor.YELLOW + "Teleportation is on cooldown, please wait.");
				            		//warmup.remove(name);
				            		return true;
				            		
				            	}
				            	
				            }	  
			            	else { 
				            		player.sendMessage(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.YELLOW + ": " + "You dont have the death location saved yet, hope you die soon! ;)");
			            		}
	                
	            	} else { 
	            		player.sendMessage(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.YELLOW + ": " + "You dont have the permission 'gorea.back'");
	            		}
	            } else 
	            	if (args.length == 1)
		            	{
	            		
	            		if (sender.hasPermission("gorea.reload") || sender.isOp()) 
	            		{
				            if ( args[0].equals("reload"))
			            	{
			            		pluginreload();
			            		
			            		
			            		System.out.println("[GoreaBack] reloaded from in-game command by: " + name);
			            		player.sendMessage(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.YELLOW + ": " + " Config files reloaded!");
			            		return true;
			            	}
		                	
	            		}
	            		
	            		if (sender.hasPermission("gorea.list") || sender.isOp()) 
	            		{
				            if ( args[0].equals("list"))
			            	{
				            	
								player.sendMessage(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.YELLOW + ": " + " Registered death locations: " + map.keySet());
				            	
			            		return true;
			            	}
		                	
	            		}
	            		
	            		
				            if ( help.contains(args[0]))
			            	{ 
				            	showhelpplayer(player);
			            		return true;
			            	}
		                	
	            		
	            		
	            		if(sender.hasPermission("gorea.backonothers") || sender.isOp())
			            {    
	            			if(map.containsKey(args[0]))
	            			{
	            				if (coldowns2.containsKey(name)){
		            				
	            					if((timpacum - coldowns2.get(name)) < coldown2 && !player.hasPermission("gorea.bypass.backonothers"))
				            			{
	            						double timpramas = new Double (coldown2 - (timpacum - coldowns2.get(name)));
	            						player.sendMessage(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.RESET + ": " + ChatColor.YELLOW + "You have to wait " + (int)timpramas + " seconds before you can use this command again.");
	            						return true;
	            						}						            				
				            			
            			}
	            				
	            				player.sendMessage(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.YELLOW + ": " + "You teleported to " + args[0] + "'s death location");
	            				
	            				player.teleport( locationFromList(map.get(args[0])));
	            				coldowns2.put(name, (long) timpacum);
	            			
	            			} else {
	            				
	            					player.sendMessage(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.YELLOW + ": " + "There is no death register with this name: " + args[0]);
	            				
	            					}	            			
			            } else { 
	            			player.sendMessage(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.YELLOW + ": " + "You dont have the permission 'gorea.backothers' to use /gback <player_name>"); 
	            			}
	            		
	            		
	            		
	            	} 
        	
        	else if (args.length == 2)
            	{ 
        		if(sender.hasPermission("gorea.backothers") || sender.isOp())
	            {    
        			
        		
        			if(map.containsKey(args[1]))
        			{
        				if (findPlayerByString(args[0]) != null)
        				{
        					player.sendMessage(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.YELLOW + ": " + "You teleported " + args[0] + " to " + args[1] + "'s death location");
        					
        					findPlayerByString(args[0]).teleport(locationFromList(map.get(args[1])));
        					
        					
        				} else {
        					player.sendMessage(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.YELLOW + ": " + "Player you want to teleport to is not online or does not exist: " + args[0]);
        					
        					
        				}
        			} else {            				
        				player.sendMessage(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.YELLOW + ": " + "There is no death register with this name: " + args[1]);
        					
        					}
	            }
        		else player.sendMessage(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.YELLOW + ": " + "You dont have the permission 'gorea.backothers' to use /gback <player_target> <player_destination>");
        		
        	
        	} else { 
        		player.sendMessage(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.YELLOW + ": " + "Too many arguments, usage: ");
        		return false;
        			}  
            }
        } 
        if (!(sender instanceof Player)) 
        {
        	if (aliases.contains(label))
	        {
        		if (args.length == 0){
        			showhelp();
        		} else
        	
        	if (args.length == 1)
        	{    		
	            if ( args[0].equals("reload"))
            	{
            		pluginreload();
            		
            		System.out.println("[GoreaBack]:  Config files reloaded!");
            		return true;
            	
            	
            	} else
	            
	            if ( args[0].equals("list"))
            	{
	            	
	            	System.out.println("[GoreaBack]: Registered death locations: " + map.keySet());
	            	
            		return true;
            	} else
	            if (args[0].equals("help"))
	            {
	            	showhelp();
	            } else
	            
	            
	            if(map.containsKey(args[0]))
    			{
	            	
	            	findPlayerByString(args[0]).teleport(locationFromList(map.get(args[1])));
    				
    			} else {
    				
    				System.out.println(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.YELLOW + ": " + "There is no death register with this name: " + args[0]);
    				
    					}	
        	} else
        
	        
	        	
	        if (args.length == 2)
	        { 
	    		if(map.containsKey(args[1]))
	    		{
	    			
	    			if (findPlayerByString(args[0]) != null)
	    			{
	    					System.out.println("[GoreaBack]: You teleported " + args[0] + " to " + args[1] + "'s death location");
	    					
	    					findPlayerByString(args[0]).teleport(locationFromList(map.get(args[1])));
	    					
	    				} else {
	    					System.out.println(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.YELLOW + ": " + "Player you want to teleport to is not online or does not exist: " + args[0]);
	    						}
	    			} else {            				
	    				System.out.println(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.YELLOW + ": " + "There is no death register with this name: " + args[1]);
	    					}
	            }else return false;
	        }
        	 
        }
        
		return true; 
    }
      
    
    

    public HashMap<String, List<String>> loadDeaths() {
    	
    	map.clear();
    	playerDeathData = YamlConfiguration.loadConfiguration(DeathsLogsFile);
            Set<String> names = playerDeathData.getKeys(false);
            if (names.size() > 0)
            {
            	
            
            //System.out.println("[GoreaBack] Loading from Deaths.yml " + names.size() + " players death locations.");
            
            for(String name : names) 
            {         
                List<String> coord =  playerDeathData.getStringList(name);                
                map.put(name, coord);   
               
            }
           // System.out.println("[GoreaBack] Successfully loaded " + map.size() + " players death locations.");
            } else {System.out.println("[GoreaBack] Death.yml is emptie = ignoring it.");}
		return map;
		
    }
    
    
    public void CheckTimeOnDeaths() {
    	if(playerDeathData != null ) 
		{
    		
    		if (names.size() > 0)
            {
            System.out.println("[GoreaBack] Checking " + names.size() + " players death locations.");
            List<String> fordelete = new ArrayList<String>();
            List<String> forrepair = new ArrayList<String>();
            
            for(String name : names) 
            {       
            	//System.out.println("-------------- " + name);  
                List<Long> coord =  playerDeathData.getLongList(name);                
                
                timeseconds = Calendar.getInstance().getTimeInMillis() / 1000 ;
                Long deathtime = timeseconds;
                try {
                	deathtime = coord.get(3);
                }
                 
	 	       catch (Exception ex)
	 	       {
	 	    	  forrepair.add(name);
	 	    	  
	 	    	   if ( getConfig().getBoolean("Debug death times"))
	 	    	   { 	    		 
		 	    		  
		 	    		      
		 	    		List<String> loc = new ArrayList<String>();
		 	    		loc.add(Iterables.getFirst(playerDeathData.getStringList(name), "world"));
		 	    		loc.add(getServer().getWorlds().get(0).toString());
		 	    		loc.add(Double.toString(coord.get(0)));
		 	    		loc.add(Double.toString(coord.get(1)));
		 	    		loc.add(Double.toString(coord.get(2)));
		 	    		loc.add(Long.toString(timeseconds));
		 	    			 playerDeathData.set(name, loc);
		 	    			 
		 	    			try
		 	    	       {
		 	        		playerDeathData.save(DeathsLogsFile);
		 	    	    	   
		 	    	       }
		 	    	       catch (Exception ex1)
		 	    	       {
		 	    	    	   
		 	    	       }
	 	    		  
	 	    		 
	 	    		  
	 	    	   } else { 
	 	    		   System.out.println("[WARNING][GoreaBack] In file Deaths.yml the player " + name + " does not have a time of death registered");
	 	    	   }
	 	       }
                
                if (timeseconds - deathtime > getConfig().getInt("Min Time To Store Deaths"))
                { 
                	fordelete.add(name);
                	
                	playerDeathData.set(name, null);
                	//System.out.println("+++++++++++++++++++++ Setting to null" + name);
                	try
         	       {
             		playerDeathData.save(DeathsLogsFile);
             		//System.out.println("+++++++++++++++++++++ SAVING TO FILE"  + name);
         	       }
         	       catch (Exception ex)
         	       {
         	    	   
         	       }
                	//map.remove(name);
                	
                }
                  	
            }
           // System.out.println("-------------------" + fordelete.size() + "   " + fordelete); 
            
            if ( fordelete.size() >0)  {
            	System.out.println("[GoreaBack] Removed " + fordelete.size() + " old entries in Death.yml.");
            }
            if ( forrepair.size() >0)  {
            	System.out.println("[GoreaBack] Repaired " + forrepair.size() + " entries in file Deaths.yml");
            }
            //playerDeathData.createSection("", map);
        	
            
        	fordelete.clear();
        	forrepair.clear();
        	
        	try
    	       {
        		playerDeathData.save(DeathsLogsFile);
    	    	   
    	       }
    	       catch (Exception ex)
    	       {
    	    	   
    	       }
        	
   
           
		}
		}
    	
    }
    
 
	private Location locationFromList(List<String> list){

		World world = getServer().getWorld((String) list.get(0));

		Location loc = new Location ( world, Double.parseDouble(list.get(1)), Double.parseDouble(list.get(2)), Double.parseDouble(list.get(3)));
		return loc;
	}
	
	private Player findPlayerByString(String name) 
	{
		for ( Player player : Bukkit.getServer().getOnlinePlayers())
		{
			if(player.getName().equals(name)) 
			{
				return player;
			}
		}
		
		return null;
	}
	
	private void pluginreload() {
		if(!(plugin == null)){		
			
			
			plugin.reloadConfig();
			
			if (getConfig().getBoolean("Remove old deaths on plugin reload"))
			{
			CheckTimeOnDeaths();
			}
			loadDeaths();
			
			
		} 
		
	}
		private void showhelp(){
			System.out.println( "...................... Plugin made by: ...............................");
        	System.out.println( "     o   \\ o /  _ o             \\ /               o_   \\ o /    o");
        	System.out.println( "    /|\\    |     /\\   __o        |       o__     /\\      |     /|\\");
        	System.out.println( "    / \\   / \\    | \\  /) |      /o\\     |  (\\   / |    / \\   / \\");
        	System.out.println( "......................... GoreaCraft  ................................");
        	System.out.println("");
        	System.out.println( "Aliases: " +  aliases + " you can use any of this instead of '/gb'");
        	String cost;
			if (isVault )
			{
        		cost =  " Costs " + getConfig().getInt("Price") ;
			} else 
			{cost =" ";}
			System.out.println("/gb : Teleports you back do death location." + cost);
        	
			System.out.println("/gb <player_name>: Teleports you to another player death location");
			System.out.println("/gb <player_target> <player_destination>: Teleports a player to another player death location");
			System.out.println("/gb list : Lists all current death locations");
			System.out.println("/gb reload : Reloads Config.yml and Deaths.yml");
		}
		
		private void showhelpplayer(Player player){
			
			player.sendMessage( ChatColor.YELLOW + "......................................................." + ChatColor.GOLD + " Plugin made by: "+ ChatColor.YELLOW + ".......................................................");
        	player.sendMessage( ChatColor.YELLOW + "     o   \\ o /  _ o              \\ /               o_   \\ o /   o");
        	player.sendMessage( ChatColor.YELLOW + "    /|\\     |      /\\   __o        |        o__    /\\      |     /|\\");
        	player.sendMessage( ChatColor.YELLOW + "    / \\   / \\    | \\  /) |       /o\\       |  (\\   / |    / \\   / \\");
        	player.sendMessage( ChatColor.YELLOW + "......................................................." + ChatColor.GOLD + ChatColor.BOLD + " GoreaCraft  "+ ChatColor.YELLOW + ".......................................................");
        	
        	player.sendMessage("");
        	player.sendMessage( ChatColor.YELLOW + "Aliases: " + ChatColor.LIGHT_PURPLE +  aliases  + ChatColor.RESET +  " you can use any of this instead of '/gb'");
        	String cost;
			if (isVault )
			{
        		cost =  " Costs " + getConfig().getInt("Price") ;
			} else 
			{cost =" ";}
        	player.sendMessage( ChatColor.YELLOW + "/gb :" + ChatColor.RESET + " Teleports you back do death location." + cost);
        	
        	player.sendMessage( ChatColor.YELLOW + "/gb <player_name>:" + ChatColor.RESET + " Teleports you to another player death location");
        	player.sendMessage( ChatColor.YELLOW + "/gb <player_target> <player_destination>:" + ChatColor.RESET + " Teleports a player to another player death location");
        	player.sendMessage( ChatColor.YELLOW + "/gb list :" + ChatColor.RESET + " Lists all current death locations");
        	player.sendMessage( ChatColor.YELLOW + "/gb reload :" + ChatColor.RESET + " Reloads Config.yml and Deaths.yml");
        	player.sendMessage( ChatColor.YELLOW + "/gb 'help'/'?' :" + ChatColor.RESET + " Shows this.");
		}
		
		private void tpplayer(String name)
		{			
			Player player = findPlayerByString(name);
			
			player.sendMessage(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.RESET + ": " + ChatColor.YELLOW + "You telported back to your last death location.");
			player.teleport(locationFromList(map.get(name)));
		}
	
	//====================================== VAULT STUFF =====================================================
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
	

}

	