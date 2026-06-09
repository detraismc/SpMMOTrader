package me.detraismc.spmmotrader.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class ModuleData {

    private final String guiName;
    private final int guiRows;
    private final Material pane1Item;
    private final Material pane2Item;
    private final List<Integer> pane1Slots;
    private final List<Integer> pane2Slots;
    private final List<Integer> itemSlots;
    private final int nextPageSlot;
    private final int prevPageSlot;
    private final List<String> traderItemIds;
    private final Map<String, ConfigurationSection> traderItemSections;
    private final Map<String, ConfigurationSection> customItemSections;
    private final List<String> customItemKeys;

    public ModuleData(FileConfiguration config) {
        this.guiName = config.getString("gui-name", "Shop");
        this.guiRows = config.getInt("gui-rows", 6);

        String pane1Str = config.getString("item-pane1", "BLACK_STAINED_GLASS_PANE");
        this.pane1Item = Material.matchMaterial(pane1Str);
        String pane2Str = config.getString("item-pane2", "GRAY_STAINED_GLASS_PANE");
        this.pane2Item = Material.matchMaterial(pane2Str);

        this.pane1Slots = config.getIntegerList("pane1-slots");
        this.pane2Slots = config.getIntegerList("pane2-slots");
        this.itemSlots = config.getIntegerList("item-slots");
        this.nextPageSlot = config.getInt("item-next-slot", 53);
        this.prevPageSlot = config.getInt("item-prev-slot", 51);

        ConfigurationSection itemsSection = config.getConfigurationSection("trader-items");
        if (itemsSection != null) {
            this.traderItemIds = new ArrayList<>(itemsSection.getKeys(false));
            this.traderItemSections = new LinkedHashMap<>();
            for (String key : traderItemIds) {
                traderItemSections.put(key, itemsSection.getConfigurationSection(key));
            }
        } else {
            this.traderItemIds = new ArrayList<>();
            this.traderItemSections = new HashMap<>();
        }

        ConfigurationSection customSection = config.getConfigurationSection("item-custom");
        if (customSection != null) {
            this.customItemKeys = new ArrayList<>(customSection.getKeys(false));
            this.customItemSections = new LinkedHashMap<>();
            for (String key : customItemKeys) {
                customItemSections.put(key, customSection.getConfigurationSection(key));
            }
        } else {
            this.customItemKeys = new ArrayList<>();
            this.customItemSections = new HashMap<>();
        }
    }

    public String getGuiName() { return guiName; }
    public int getGuiRows() { return guiRows; }
    public Material getPane1Item() { return pane1Item; }
    public Material getPane2Item() { return pane2Item; }
    public List<Integer> getPane1Slots() { return pane1Slots; }
    public List<Integer> getPane2Slots() { return pane2Slots; }
    public List<Integer> getItemSlots() { return itemSlots; }
    public int getNextPageSlot() { return nextPageSlot; }
    public int getPrevPageSlot() { return prevPageSlot; }
    public List<String> getTraderItemIds() { return traderItemIds; }
    public ConfigurationSection getTraderItemSection(String id) { return traderItemSections.get(id); }
    public List<String> getCustomItemKeys() { return customItemKeys; }
    public ConfigurationSection getCustomItemSection(String key) { return customItemSections.get(key); }
}
