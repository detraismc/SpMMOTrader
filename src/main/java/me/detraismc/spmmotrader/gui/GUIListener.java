package me.detraismc.spmmotrader.gui;

import me.detraismc.spmmotrader.config.ConfigManager;
import me.detraismc.spmmotrader.config.ModuleData;
import me.detraismc.spmmotrader.util.MMOItemsHook;
import me.detraismc.spmmotrader.util.TextUtil;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GUIListener implements Listener {

    private final Economy economy;

    public GUIListener(Economy economy) {
        this.economy = economy;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        UUID uuid = player.getUniqueId();
        GUISession session = GUIManager.getInstance().getSessions().get(uuid);
        if (session == null) return;

        if (!event.getView().title().equals(TextUtil.colorize(session.getGuiName()))) return;

        event.setCancelled(true);

        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().equals(player.getInventory())) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) return;

        int slot = event.getSlot();
        int page = session.getPage();
        String moduleName = session.getModule();

        ModuleData moduleData = ConfigManager.getInstance().getModuleData(moduleName);
        if (moduleData == null) return;

        if (!GUIManager.getInstance().checkCooldown(player)) return;

        int prevSlot = moduleData.getPrevPageSlot();
        int nextSlot = moduleData.getNextPageSlot();

        if (slot == prevSlot) {
            if (page > 1) {
                ConfigManager.getInstance().playSound(player, "sound-click");
                GUIManager.getInstance().openTrader(player, moduleName, page - 1);
            }
            return;
        }

        if (slot == nextSlot) {
            List<String> itemIds = moduleData.getTraderItemIds();
            int guiSlotsSize = moduleData.getItemSlots().size();
            if (itemIds.size() > page * guiSlotsSize) {
                ConfigManager.getInstance().playSound(player, "sound-click");
                GUIManager.getInstance().openTrader(player, moduleName, page + 1);
            }
            return;
        }

        List<Integer> guiSlots = moduleData.getItemSlots();
        if (guiSlots.contains(slot)) {
            handleItemPurchase(player, moduleData, slot, page, moduleName);
            return;
        }

        for (String customKey : moduleData.getCustomItemKeys()) {
            ConfigurationSection customSection = moduleData.getCustomItemSection(customKey);
            if (customSection == null) continue;
            int customSlot = customSection.getInt("slot", -1);
            if (slot == customSlot) {
                handleCustomItemClick(player, customSection);
                return;
            }
        }
    }

    private void handleItemPurchase(Player player, ModuleData moduleData, int slot, int page, String moduleName) {
        List<Integer> guiSlots = moduleData.getItemSlots();
        List<String> itemIds = moduleData.getTraderItemIds();
        int guiSlotsSize = guiSlots.size();

        int startNum = page > 1 ? ((page - 1) * guiSlotsSize) + 1 : 1;
        int slotIndex = guiSlots.indexOf(slot);
        if (slotIndex < 0) return;
        int itemIndex = startNum + slotIndex;
        if (itemIndex < 1 || itemIndex > itemIds.size()) return;

        String itemId = itemIds.get(itemIndex - 1);
        ConfigurationSection itemSection = moduleData.getTraderItemSection(itemId);
        if (itemSection == null) return;

        if (!hasRequiredItems(player, itemSection)) {
            ConfigManager.getInstance().playSound(player, "sound-error");
            player.sendMessage(ConfigManager.getInstance().getMessage("error-mmotrader-noitem"));
            return;
        }

        int moneyCost = itemSection.getInt("money-cost", 0);
        if (moneyCost > 0) {
            if (economy == null || !economy.has(player, moneyCost)) {
                ConfigManager.getInstance().playSound(player, "sound-error");
                player.sendMessage(ConfigManager.getInstance().getMessage("error-mmotrader-noitem"));
                return;
            }
        }

        removeRequiredItems(player, itemSection);

        if (moneyCost > 0) {
            economy.withdrawPlayer(player, moneyCost);
            String moneyMsg = ConfigManager.getInstance().getMessage("message-mmotrader-money");
            moneyMsg = moneyMsg.replace("{money_cost}", TextUtil.formatNumber(moneyCost));
            player.sendMessage(moneyMsg);
        }

        List<String> commands = itemSection.getStringList("commands");
        for (String cmd : commands) {
            String prepared = cmd.replace("<player>", player.getName());
            if (prepared.startsWith("/")) {
                prepared = prepared.substring(1);
            }
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), prepared);
        }

        ConfigManager.getInstance().playSound(player, "sound-buy");
        GUIManager.getInstance().openTrader(player, moduleName, page);
    }

    private boolean hasRequiredItems(Player player, ConfigurationSection itemSection) {
        ConfigurationSection mmoCostSection = itemSection.getConfigurationSection("mmo-cost");
        if (mmoCostSection == null) return true;

        Set<String> costKeys = mmoCostSection.getKeys(false);
        for (String key : costKeys) {
            String mmoType = mmoCostSection.getString(key + ".mmoitems-type");
            String mmoId = mmoCostSection.getString(key + ".mmoitems-id");
            int amount = mmoCostSection.getInt(key + ".amount", 1);

            ItemStack requiredItem = MMOItemsHook.getItem(mmoType, mmoId);
            if (requiredItem == null) return false;

            int hasAmount = 0;
            for (ItemStack invItem : player.getInventory().getContents()) {
                if (invItem != null && invItem.isSimilar(requiredItem)) {
                    hasAmount += invItem.getAmount();
                }
            }
            if (hasAmount < amount) return false;
        }
        return true;
    }

    private void removeRequiredItems(Player player, ConfigurationSection itemSection) {
        ConfigurationSection mmoCostSection = itemSection.getConfigurationSection("mmo-cost");
        if (mmoCostSection == null) return;

        Set<String> costKeys = mmoCostSection.getKeys(false);
        for (String key : costKeys) {
            String mmoType = mmoCostSection.getString(key + ".mmoitems-type");
            String mmoId = mmoCostSection.getString(key + ".mmoitems-id");
            int amount = mmoCostSection.getInt(key + ".amount", 1);

            ItemStack requiredItem = MMOItemsHook.getItem(mmoType, mmoId);
            if (requiredItem == null) continue;

            int remaining = amount;
            ItemStack[] contents = player.getInventory().getContents();
            for (int i = 0; i < contents.length && remaining > 0; i++) {
                ItemStack invItem = contents[i];
                if (invItem != null && invItem.isSimilar(requiredItem)) {
                    int stackAmount = invItem.getAmount();
                    if (stackAmount <= remaining) {
                        remaining -= stackAmount;
                        player.getInventory().setItem(i, null);
                    } else {
                        ItemStack clone = invItem.clone();
                        clone.setAmount(stackAmount - remaining);
                        player.getInventory().setItem(i, clone);
                        remaining = 0;
                    }
                }
            }

            String text = ConfigManager.getInstance().getMessage("message-mmotrader-removeitem");
            text = text.replace("{amount}", String.valueOf(amount));
            String itemName = TextUtil.legacyColorize(requiredItem.getItemMeta() != null && requiredItem.getItemMeta().hasDisplayName()
                    ? LegacyComponentSerializer.legacySection().serialize(requiredItem.getItemMeta().displayName())
                    : requiredItem.getType().name());
            text = text.replace("{item}", itemName);
            player.sendMessage(text);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        GUIManager.getInstance().removeSession(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        GUIManager.getInstance().removeSession(event.getPlayer().getUniqueId());
    }

    private void handleCustomItemClick(Player player, ConfigurationSection customSection) {
        List<String> commands = customSection.getStringList("commands");
        if (commands.isEmpty()) return;

        for (String cmd : commands) {
            cmd = cmd.replace("<player>", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }

        ConfigManager.getInstance().playSound(player, "sound-click");
    }
}
