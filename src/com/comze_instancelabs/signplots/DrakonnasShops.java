package com.comze_instancelabs.signplots;

import java.io.IOException;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class DrakonnasShops implements Listener {
	
	WorldGuardPlugin worldGuard = (WorldGuardPlugin) getWorldGuard();
	public static Economy econ = null;
	public Plugin icraft;
	
	
	public DrakonnasShops(Economy e, Plugin t){
		econ = e;
		icraft = t;
	}
	
	public Plugin getWorldGuard(){
    	return Bukkit.getPluginManager().getPlugin("WorldGuard");
    }
	
	@EventHandler
    public void onSignUse(PlayerInteractEvent event)
    {
        if (event.hasBlock() && event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            if (event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN)
            {
                Sign s = (Sign) event.getClickedBlock().getState();
 
                for (int i = 0; i < s.getLines().length - 1; i++)
                {
                    if (s.getLine(i).equalsIgnoreCase("§2SELL"))
                    {
                        if(econ.getBalance(event.getPlayer().getName()) < 500){
                        	event.getPlayer().sendMessage(ChatColor.GOLD + "[DrakonnasShops] " + ChatColor.DARK_RED + "You don't have enough money!");
                        }else{
	                        String rg = s.getLine(i + 1);
	                    	
	                    	DefaultDomain domain = new DefaultDomain();
	                        domain.addPlayer(event.getPlayer().getName());
	                     
	                        worldGuard.getRegionManager(event.getPlayer().getWorld()).getRegion(rg).setOwners(domain);
	                        try {
								worldGuard.getRegionManager(event.getPlayer().getWorld()).save();
							} catch (ProtectionDatabaseException e) {
								e.printStackTrace();
							}
	                        
	                        EconomyResponse r = econ.withdrawPlayer(event.getPlayer().getName(), Double.parseDouble(s.getLine(2)));
    	                    if(!r.transactionSuccess()) {
    	                    	event.getPlayer().sendMessage(String.format("An error occured: %s", r.errorMessage));
    	                    }
	                        
    	                    String price = s.getLine(2);
    	                    
    	                    s.setLine(i, "§4SOLD!");
    	                    s.setLine(i+1, rg);
    	                    s.setLine(i+2, event.getPlayer().getName());
    	                    s.update();

    	                    addGreeting(event.getPlayer(), rg);
    	                    
	                        event.getPlayer().sendMessage(ChatColor.GOLD + "[DrakonnasShops] " + ChatColor.DARK_GREEN + "You have bought the region " + rg + " for " + price +  " Draks.");
                        
                        }

                    } //end of if s.getline .. [BOAT]
                }
            }
        }
    }
	
	@EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player p = event.getPlayer();
        if(event.getLine(0).toLowerCase().contains("/s")){
        	if (event.getPlayer().hasPermission("drakonnasshops.create"))
            {
        		if(!event.getLine(1).equalsIgnoreCase("")){
            		String rg = event.getLine(1);
            		if(ProtectedRegion.isValidId(rg) && worldGuard.getRegionManager(event.getPlayer().getWorld()).getRegion(rg) != null){
            			if(worldGuard.getRegionManager(event.getPlayer().getWorld()).getRegion(rg).getOwners().size() < 1){
            				p.sendMessage(ChatColor.GOLD + "[DrakonnasShops] " + ChatColor.DARK_GREEN + "Successfully created the region " + event.getLine(1).toString() + "!");
            				event.setLine(0, "§2SELL");
            				event.setLine(1, rg);
            				if(event.getLine(2).equalsIgnoreCase("")){
            					event.setLine(2, "500");
            				}
	        				//ProtectedRegion rg_ = worldGuard.getRegionManager(event.getPlayer().getWorld()).getRegion(rg);
	        				addFlags(p, rg);
            			}else{
            				p.sendMessage(ChatColor.GOLD + "[DrakonnasShops] " + ChatColor.DARK_RED +"This region already has an owner.");
            				event.getBlock().breakNaturally();
            			}
            		}else{
            			p.sendMessage(ChatColor.GOLD + "[DrakonnasShops] " + ChatColor.DARK_RED +"A region with this ID couldn't be found.");
            			event.getBlock().breakNaturally();
            		}
        		}else{
	        		ApplicableRegionSet set = WGBukkit.getRegionManager(event.getPlayer().getWorld()).getApplicableRegions(event.getBlock().getLocation());
	        		String rg = "";
	        		for (ProtectedRegion region : set) {
	        		    rg = region.getId();
	        		}
	        		if(ProtectedRegion.isValidId(rg) && worldGuard.getRegionManager(event.getPlayer().getWorld()).getRegion(rg) != null){
	        			if(worldGuard.getRegionManager(event.getPlayer().getWorld()).getRegion(rg).getOwners().size() < 1){
	        				p.sendMessage(ChatColor.GOLD + "[DrakonnasShops] " + ChatColor.DARK_GREEN + "Successfully created the region " + rg + "!");
	        				event.setLine(0, "§2SELL");
	        				event.setLine(1, rg);
	        				if(event.getLine(2).equalsIgnoreCase("")){
            					event.setLine(2, "500");
            				}
	        				addFlags(p, rg);
	        			}else{
	        				p.sendMessage(ChatColor.GOLD + "[DrakonnasShops] " + ChatColor.DARK_RED +"This region already has an owner.");
	        				event.getBlock().breakNaturally();
	        			}
	        		}else{
	        			p.sendMessage(ChatColor.GOLD + "[DrakonnasShops] " + ChatColor.DARK_RED +"A region with this ID couldn't be found.");
	        			event.getBlock().breakNaturally();
	        		}
        		}
    		
            }else{
            	event.getBlock().breakNaturally();
            	//event.setLine(0, "INVALID");
            	p.sendMessage(ChatColor.GOLD + "[DrakonnasShops] " + ChatColor.DARK_RED +"You don't have permissions to sell regions.");
            }
        }
    }
	
	
	public void addFlags(Player p, String rg){
		/*Flag fl1 = new BlockMaterialFlag("any");
		CustomSetFlag ALLOW_BLOCK_FLAG1 = new CustomSetFlag("deny-blocks", fl1);
		wgc.addCustomFlag(ALLOW_BLOCK_FLAG1);
		Flag fl = new BlockMaterialFlag("sign_post");
		CustomSetFlag ALLOW_BLOCK_FLAG = new CustomSetFlag("allow-blocks", fl);
		wgc.addCustomFlag(ALLOW_BLOCK_FLAG);*/
		icraft.getServer().dispatchCommand(p , "region flag " + rg + " deny-blocks ANY");
		icraft.getServer().dispatchCommand(p , "region flag " + rg + " allow-blocks SIGN_POST");
		icraft.getServer().dispatchCommand(p , "region flag " + rg + " allow-blocks WALL_SIGN");
		icraft.getServer().dispatchCommand(p , "region setpriority " + rg + " 2");
	}
	
	public void addGreeting(Player p, String rg){
		icraft.getServer().dispatchCommand(icraft.getServer().getConsoleSender() , "region flag " + rg + " -w " + p.getWorld().getName() + " greeting &GWelcome to " + p.getName() + "'s Shop!");
	}
	
	public void removeGreeting(Player p, String rg){
		worldGuard.getRegionManager(p.getWorld()).getRegion(rg).setFlag(DefaultFlag.GREET_MESSAGE, null);
	}

	/*public void onPlayerJoin(PlayerJoinEvent event){
		Player p = event.getPlayer();
		if(icraft.getConfig().contains(p.getName() + ".rg")){
			ProtectedRegion rg = 
		}
	}*/
	
	public boolean checkDays(){
		SimpleDateFormat sdfToDate = new SimpleDateFormat("dd.MM.yyyy");
		Date datecurrent = new Date();
		String daysdate = icraft.getConfig().getString("players.daysleft");
		Date date1 = null;
		try {
			date1 = sdfToDate.parse(daysdate);
			System.out.println(date1);
		} catch (ParseException ex2){
			ex2.printStackTrace();
		}
		Integer between = this.daysBetween(datecurrent, date1);
		icraft.getLogger().info(Integer.toString(between));
		if(between > 30){
			return true;
		}else{
			return false;
		}
	}
	
		
	public int daysBetween(Date d1, Date d2){
	    long differenceMilliSeconds = d2.getTime() - d1.getTime();
	    long days = differenceMilliSeconds / 1000 / 60 / 60 / 24;
	    return (int) days;
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event){
		if(event.getBlock().getType() == Material.SIGN_POST || event.getBlock().getType() == Material.WALL_SIGN){
			if(event.getPlayer().isOp()){
	    		Sign s = (Sign)event.getBlock().getState();
	    		if(s.getLine(0).equalsIgnoreCase("§4SOLD!")){
	    			ApplicableRegionSet set = WGBukkit.getRegionManager(event.getPlayer().getWorld()).getApplicableRegions(event.getBlock().getLocation());
		    		String rg = "";
		    		for (ProtectedRegion region : set) {
		    			if(region.getPriority() > 1){
		    				rg = region.getId();
		    			}
		    		}
	    			ProtectedRegion rg_ = worldGuard.getRegionManager(event.getPlayer().getWorld()).getRegion(rg);
	    			DefaultDomain domain = new DefaultDomain();
	                domain.removaAll();
	                
	                //ProtectedRegion rg_ = worldGuard.getRegionManager(event.getPlayer().getWorld()).getRegion(rg);

	                worldGuard.getRegionManager(event.getPlayer().getWorld()).getRegion(rg).setOwners(domain);
	                removeGreeting(event.getPlayer(), rg);
	                
	                event.getPlayer().sendMessage("§2Successfully deleted shop.");
	    		}
			}else{
				Sign s = (Sign)event.getBlock().getState();
				if(s.getLine(0).equalsIgnoreCase("§4SOLD!")){
	    			event.setCancelled(true);
	    		}
			}
		}
	}
	
}
