package me.detraismc.spmmotrader.config;

import me.detraismc.spmmotrader.SpMMOTrader;
import me.detraismc.spmmotrader.util.TextUtil;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class ConfigManager {

    private static ConfigManager instance;

    private final SpMMOTrader plugin;
    private final File dataFolder;

    private FileConfiguration config;
    private FileConfiguration guiConfig;
    private final Map<String, FileConfiguration> moduleConfigs = new HashMap<>();
    private final Map<String, ModuleData> moduleDataCache = new HashMap<>();

    public ConfigManager(SpMMOTrader plugin) {
        this.plugin = plugin;
        this.dataFolder = plugin.getDataFolder();
        instance = this;
    }

    public static ConfigManager getInstance() {
        return instance;
    }

    public void loadConfigs() {
        saveDefaults();
        loadConfigFile();
        loadGUIConfig();
        loadModules();
        buildCache();
    }

    public void reloadConfigs() {
        moduleConfigs.clear();
        moduleDataCache.clear();
        loadConfigs();
    }

    private void saveDefaults() {
        plugin.saveDefaultConfig();
        saveResource("gui.yml");
        File moduleDir = new File(dataFolder, "module");
        if (!moduleDir.exists()) {
            moduleDir.mkdirs();
            saveResource("module/blacksmith_shop.yml");
            saveResource("module/cosmetic_shop.yml");
            saveResource("module/key_fragment.yml");
            saveResource("module/potion_shop.yml");
        }
    }

    private void saveResource(String path) {
        File file = new File(dataFolder, path);
        if (!file.exists()) {
            plugin.saveResource(path, false);
        }
    }

    private void loadConfigFile() {
        File file = new File(dataFolder, "config.yml");
        if (!file.exists()) {
            plugin.saveDefaultConfig();
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    private void loadGUIConfig() {
        File file = new File(dataFolder, "gui.yml");
        if (!file.exists()) {
            saveResource("gui.yml");
        }
        guiConfig = YamlConfiguration.loadConfiguration(file);
    }

    private void loadModules() {
        File moduleDir = new File(dataFolder, "module");
        if (!moduleDir.exists()) {
            moduleDir.mkdirs();
            return;
        }

        File[] files = moduleDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            String moduleName = file.getName().replace(".yml", "");
            FileConfiguration moduleConfig = YamlConfiguration.loadConfiguration(file);
            moduleConfigs.put(moduleName, moduleConfig);
        }
    }

    private void buildCache() {
        for (Map.Entry<String, FileConfiguration> entry : moduleConfigs.entrySet()) {
            try {
                ModuleData data = new ModuleData(entry.getValue());
                moduleDataCache.put(entry.getKey(), data);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to parse module: " + entry.getKey(), e);
            }
        }
    }

    public List<String> getModuleList() {
        return new ArrayList<>(moduleConfigs.keySet());
    }

    public ModuleData getModuleData(String moduleName) {
        return moduleDataCache.get(moduleName);
    }

    public ModuleData getModuleDataFromConfig(String moduleName) {
        FileConfiguration moduleConfig = moduleConfigs.get(moduleName);
        if (moduleConfig == null) return null;
        return new ModuleData(moduleConfig);
    }

    public FileConfiguration getConfig(String name) {
        switch (name) {
            case "dmmot_config":
                return config;
            case "dmmot_gui":
                return guiConfig;
            default:
                if (name.startsWith("dmmot_module_")) {
                    String moduleName = name.substring("dmmot_module_".length());
                    return moduleConfigs.get(moduleName);
                }
                return moduleConfigs.get(name);
        }
    }

    public FileConfiguration getModuleConfig(String moduleName) {
        return moduleConfigs.get(moduleName);
    }

    public String getMessage(String path) {
        String prefix = config.getString("prefix", "");
        String text = config.getString(path, "");
        return TextUtil.legacyColorize(prefix + text);
    }

    public void playSound(Player player, String soundKey) {
        String soundName = config.getString(soundKey, "UI_BUTTON_CLICK");
        float volume = (float) config.getDouble(soundKey + "-volume", 1.0);
        float pitch = (float) config.getDouble(soundKey + "-pitch", 1.0);
        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player, sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound configured for '" + soundKey + "': " + soundName);
        }
    }
}
