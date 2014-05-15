package com.goreacraft.plugins.goreaback;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class WarmupListeners implements Listener {
	
	
	@EventHandler
	   public void onPlayerMove(PlayerMoveEvent e)
	   {
		
		Player player = e.getPlayer();
		String name = player.getName();	
		if (Main.warmup.containsKey(name))
				{
					if (e.getFrom().distance(e.getTo()) > Main.finesse)
					{
			    	 cancelwarmup(player);
			    	 player.sendMessage(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.RESET + ": " + ChatColor.RED + "Teleportation has been canceled.");
					}
				}
	   }
	@EventHandler
	   public void onPlayerDamage(EntityDamageEvent e)
	   {
		if ((e.getEntity() instanceof Player) )
				{
					Player player =(Player) e.getEntity();
					if (Main.warmup.containsKey(player.getName())) 
					{			
						cancelwarmup(player);
						player.sendMessage(ChatColor.DARK_AQUA + "[GoreaBack]" + ChatColor.RESET + ": " + ChatColor.RED + "Teleportation has been canceled.");
					}
				}
	     }
	@EventHandler
	   public void onPlayerQuit(PlayerKickEvent e)
	   {
					Player player = e.getPlayer();
					if (Main.warmup.containsKey(player.getName())) 
					{			
						cancelwarmup(player);
						
					}
	     }
	@EventHandler
	   public void onPlayerQuit(PlayerQuitEvent e)
	   {
					Player player = e.getPlayer();
					if (Main.warmup.containsKey(player.getName())) 
					{			
						cancelwarmup(player);
						
					}
	     }


	  public void cancelwarmup(Player player){
		  
		  String name = player.getName();		  
		  int sss = Main.warmup.get(name);
		 
		 if( !(Bukkit.getScheduler().getPendingTasks().indexOf(sss) == -1))
		 {			 
			 Bukkit.getScheduler().cancelTask(sss);
		 }
		  Main.warmup.remove(name);
	  }
	
	
}
