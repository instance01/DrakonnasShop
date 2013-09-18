package com.comze_instancelabs.signplots;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class Main extends JavaPlugin{
	public static Economy econ = null;
	
	
	@Override
    public void onEnable(){
		getLogger().info("Initializing DrakonnasShops . . .");
		
		if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - No iConomy dependency found! Disabling Economy.", getDescription().getName()));
        }
		
		getServer().getPluginManager().registerEvents(new DrakonnasShops(econ, this), this);

    }

    
    
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