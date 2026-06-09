package me.detraismc.spmmotrader;

import me.detraismc.spmmotrader.commands.OpenMMOTraderCommand;
import me.detraismc.spmmotrader.commands.ReloadCommand;
import me.detraismc.spmmotrader.config.ConfigManager;
import me.detraismc.spmmotrader.gui.GUIManager;
import me.detraismc.spmmotrader.gui.GUIListener;
import me.detraismc.spmmotrader.util.GitHubUpdateChecker;
import me.detraismc.spmmotrader.util.MMOItemsHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class SpMMOTrader extends JavaPlugin {

    private static SpMMOTrader instance;
    private Economy economy;
    private ConfigManager configManager;
    private GUIManager guiManager;

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
        this.configManager.loadConfigs();

        setupEconomy();
        MMOItemsHook.init();

        this.guiManager = new GUIManager();

        OpenMMOTraderCommand openCmd = new OpenMMOTraderCommand();
        getCommand("openmmotrader").setExecutor(openCmd);
        getCommand("openmmotrader").setTabCompleter(openCmd);
        getCommand("mmotraderreload").setExecutor(new ReloadCommand());

        getServer().getPluginManager().registerEvents(new GUIListener(economy), this);

        new GitHubUpdateChecker(this, "DetraisMC/SpMMOTrader").check();

        getLogger().info("SpMMOTrader has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("SpMMOTrader has been disabled!");
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault not found. Economy features will be disabled.");
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().warning("No economy provider found. Economy features will be disabled.");
            return;
        }
        economy = rsp.getProvider();
        getLogger().info("Hooked into " + economy.getName() + " via Vault!");
    }

    public Economy getEconomy() {
        return economy;
    }

    public static SpMMOTrader getInstance() {
        return instance;
    }
}
