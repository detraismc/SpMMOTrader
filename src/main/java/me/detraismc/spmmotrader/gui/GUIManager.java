package me.detraismc.spmmotrader.gui;

import me.detraismc.spmmotrader.config.ConfigManager;
import me.detraismc.spmmotrader.config.ModuleData;
import me.detraismc.spmmotrader.util.ItemBuilder;
import me.detraismc.spmmotrader.util.MMOItemsHook;
import me.detraismc.spmmotrader.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GUIManager {

    private static GUIManager instance;
    private final Map<UUID, GUISession> sessions = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public GUIManager() {
        instance = this;
    }

    public static GUIManager getInstance() {
        return instance;
    }

    public Map<UUID, GUISession> getSessions() {
        return sessions;
    }

    public void removeSession(UUID uuid) {
        sessions.remove(uuid);
        cooldowns.remove(uuid);
    }

    public boolean checkCooldown(Player player) {
        long cooldownMs = ConfigManager.getInstance().getConfig("dmmot_config").getInt("cooldown", 200);
        if (cooldownMs <= 0) return true;

        long now = System.currentTimeMillis();
        Long last = cooldowns.get(player.getUniqueId());
        if (last != null && (now - last) < cooldownMs) {
            return false;
        }
        cooldowns.put(player.getUniqueId(), now);
        return true;
    }

    public void openTrader(Player player, String moduleName, int page) {
        ModuleData moduleData = ConfigManager.getInstance().getModuleData(moduleName);
        if (moduleData == null) {
            player.sendMessage(ConfigManager.getInstance().getMessage("error-mmotrader-module"));
            return;
        }

        Inventory inv = Bukkit.createInventory(null, moduleData.getGuiRows() * 9, TextUtil.colorize(moduleData.getGuiName()));

        Material pane1Mat = moduleData.getPane1Item();
        if (pane1Mat == null) pane1Mat = Material.BLACK_STAINED_GLASS_PANE;
        Material pane2Mat = moduleData.getPane2Item();
        if (pane2Mat == null) pane2Mat = Material.GRAY_STAINED_GLASS_PANE;
        ItemStack pane1 = new ItemStack(pane1Mat);
        ItemStack pane2 = new ItemStack(pane2Mat);
        ItemMeta meta1 = pane1.getItemMeta();
        if (meta1 != null) {
            meta1.displayName(Component.text("\u00A7f"));
            pane1.setItemMeta(meta1);
        }
        ItemMeta meta2 = pane2.getItemMeta();
        if (meta2 != null) {
            meta2.displayName(Component.text("\u00A7f"));
            pane2.setItemMeta(meta2);
        }

        for (int slot : moduleData.getPane1Slots()) {
            if (slot >= 0 && slot < inv.getSize()) {
                inv.setItem(slot, pane1);
            }
        }
        for (int slot : moduleData.getPane2Slots()) {
            if (slot >= 0 && slot < inv.getSize()) {
                inv.setItem(slot, pane2);
            }
        }

        List<String> itemIds = moduleData.getTraderItemIds();
        List<Integer> guiSlots = moduleData.getItemSlots();
        int guiSlotsSize = guiSlots.size();

        int startNum = page > 1 ? ((page - 1) * guiSlotsSize) + 1 : 1;
        int num = startNum;

        for (int slotIndex = 0; slotIndex < guiSlotsSize && num <= itemIds.size(); slotIndex++) {
            int slot = guiSlots.get(slotIndex);
            String itemId = itemIds.get(num - 1);
            if (slot >= 0 && slot < inv.getSize()) {
                ItemStack displayItem = buildTraderDisplayItem(player, moduleData, itemId);
                inv.setItem(slot, displayItem);
            }
            num++;
        }

        if (page > 1) {
            ItemStack prevButton = ItemBuilder.getYamlGUIItem2("dmmot_gui", "item-page.prev");
            int prevSlot = moduleData.getPrevPageSlot();
            if (prevSlot >= 0 && prevSlot < inv.getSize()) {
                inv.setItem(prevSlot, prevButton);
            }
        }

        if (itemIds.size() > page * guiSlotsSize) {
            ItemStack nextButton = ItemBuilder.getYamlGUIItem2("dmmot_gui", "item-page.next");
            int nextSlot = moduleData.getNextPageSlot();
            if (nextSlot >= 0 && nextSlot < inv.getSize()) {
                inv.setItem(nextSlot, nextButton);
            }
        }

        for (String customKey : moduleData.getCustomItemKeys()) {
            ConfigurationSection customSection = moduleData.getCustomItemSection(customKey);
            if (customSection == null) continue;
            int slot = customSection.getInt("slot", -1);
            if (slot < 0 || slot >= inv.getSize()) continue;
            ItemStack customItem = buildCustomItem(moduleData, customKey, customSection);
            inv.setItem(slot, customItem);
        }

        player.openInventory(inv);
        sessions.put(player.getUniqueId(), new GUISession(moduleName, page, moduleData.getGuiName()));
    }

    private ItemStack buildTraderDisplayItem(Player player, ModuleData moduleData, String itemId) {
        ConfigurationSection section = moduleData.getTraderItemSection(itemId);
        if (section == null) return new ItemStack(Material.STONE);

        String material = section.getString("item");
        int amount = section.getInt("amount", 1);
        List<String> loreList = section.getStringList("lore");
        int moneyCost = section.getInt("money-cost", 0);

        String joinedLore = String.join(";", loreList);

        ItemStack displayItem;

        if ("MMOITEMS".equalsIgnoreCase(material)) {
            String mmoType = section.getString("mmo-display.mmoitems-type");
            String mmoId = section.getString("mmo-display.mmoitems-id");
            ItemStack mmoItem = MMOItemsHook.getItem(mmoType, mmoId);
            if (mmoItem == null) {
                return new ItemStack(Material.STONE);
            }
            displayItem = mmoItem.clone();
            displayItem.setAmount(amount);

            if (displayItem.getItemMeta() != null) {
                List<Component> itemLore = displayItem.getItemMeta().lore();
                StringBuilder mmoLoreBuilder = new StringBuilder();
                if (itemLore != null) {
                    for (Component c : itemLore) {
                        if (mmoLoreBuilder.length() > 0) mmoLoreBuilder.append(";");
                        mmoLoreBuilder.append(LegacyComponentSerializer.legacySection().serialize(c));
                    }
                }
                String mmoLoreText = mmoLoreBuilder.toString();
                joinedLore = joinedLore.replace("<mmoitems-lore>", mmoLoreText);
            }
        } else {
            Material mat = Material.matchMaterial(material);
            if (mat == null) return new ItemStack(Material.STONE);

            if (mat == Material.PLAYER_HEAD) {
                String skullValue = section.getString("skullvalue");
                if (skullValue != null && !skullValue.isEmpty()) {
                    displayItem = ItemBuilder.getSkullItem(skullValue);
                } else {
                    displayItem = new ItemStack(mat);
                }
            } else {
                displayItem = new ItemStack(mat);
            }
            displayItem.setAmount(amount);
        }

        joinedLore = joinedLore.replace("<money-cost>", TextUtil.formatNumber(moneyCost));

        ConfigurationSection mmoCostSection = section.getConfigurationSection("mmo-cost");
        if (mmoCostSection != null) {
            Set<String> costKeys = mmoCostSection.getKeys(false);
            if (!costKeys.isEmpty()) {
                String costDisplay = section.getString("mmo-cost-display", "");
                List<String> costLines = new ArrayList<>();
                for (String key : costKeys) {
                    String costMmoType = mmoCostSection.getString(key + ".mmoitems-type");
                    String costMmoId = mmoCostSection.getString(key + ".mmoitems-id");
                    int costAmount = mmoCostSection.getInt(key + ".amount", 1);
                    ItemStack costItem = MMOItemsHook.getItem(costMmoType, costMmoId);
                    String costLine = costDisplay;
                    costLine = costLine.replace("<amount>", String.valueOf(costAmount));
                    if (costItem != null && costItem.getItemMeta() != null) {
                        String itemName = LegacyComponentSerializer.legacySection().serialize(costItem.getItemMeta().displayName());
                        costLine = costLine.replace("<item>", itemName);
                    } else {
                        costLine = costLine.replace("<item>", costMmoId);
                    }
                    costLines.add(costLine);
                }
                String joinedCost = String.join(";", costLines);
                joinedLore = joinedLore.replace("<mmo-cost>", joinedCost);
            }
        }

        String[] loreParts = joinedLore.split(";", -1);
        List<Component> finalLore = new ArrayList<>();
        for (String part : loreParts) {
            finalLore.add(TextUtil.colorize(part));
        }

        ItemMeta meta = displayItem.getItemMeta();
        if (meta == null) return displayItem;

        if (!"MMOITEMS".equalsIgnoreCase(material)) {
            String name = section.getString("name");
            if (name != null && !name.isEmpty()) {
                meta.displayName(TextUtil.colorize(name));
            }

            int modelData = section.getInt("modeldata", 0);
            if (modelData > 0) {
                meta.setCustomModelData(modelData);
            }

            boolean enchanted = section.getBoolean("enchanted", false);
            if (enchanted) {
                meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, false);
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            }
        }

        meta.lore(finalLore);
        displayItem.setItemMeta(meta);
        return displayItem;
    }

    private ItemStack buildCustomItem(ModuleData moduleData, String key, ConfigurationSection section) {
        return ItemBuilder.fromSection(section);
    }
}
