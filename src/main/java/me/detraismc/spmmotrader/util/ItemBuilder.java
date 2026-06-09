package me.detraismc.spmmotrader.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public final class ItemBuilder {

    private ItemBuilder() {}

    public static ItemStack fromSection(ConfigurationSection section) {
        if (section == null) return new ItemStack(Material.STONE);

        String materialName = section.getString("item");
        if (materialName == null || materialName.equalsIgnoreCase("MMOITEMS")) {
            return new ItemStack(Material.STONE);
        }

        Material material = Material.matchMaterial(materialName);
        if (material == null) material = Material.STONE;

        int amount = section.getInt("amount", 1);
        boolean enchanted = section.getBoolean("enchanted", false);
        int modelData = section.getInt("modeldata", 0);
        String name = section.getString("name");
        List<String> lore = section.getStringList("lore");
        String skullValue = section.getString("skullvalue");

        ItemStack item;

        if (material == Material.PLAYER_HEAD && skullValue != null && !skullValue.isEmpty()) {
            item = getSkullItem(skullValue);
        } else {
            item = new ItemStack(material, amount);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        if (name != null && !name.isEmpty()) {
            meta.displayName(TextUtil.colorize(name));
        }

        if (!lore.isEmpty()) {
            List<net.kyori.adventure.text.Component> components = new ArrayList<>();
            for (String line : lore) {
                components.add(TextUtil.colorize(line));
            }
            meta.lore(components);
        }

        if (modelData > 0) {
            meta.setCustomModelData(modelData);
        }

        if (enchanted) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getSkullItem(String base64) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta == null || base64 == null || base64.isEmpty()) return skull;

        try {
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), "");
            PlayerTextures textures = profile.getTextures();

            byte[] decoded = Base64.getDecoder().decode(base64);
            String json = new String(decoded, StandardCharsets.UTF_8);
            String urlKey = "\"url\":\"";
            int urlStart = json.indexOf(urlKey);
            if (urlStart != -1) {
                urlStart += urlKey.length();
                int urlEnd = json.indexOf("\"", urlStart);
                if (urlEnd != -1) {
                    String skinUrl = json.substring(urlStart, urlEnd);
                    textures.setSkin(new URL(skinUrl));
                    meta.setPlayerProfile((com.destroystokyo.paper.profile.PlayerProfile) profile);
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to set skull texture", e);
        }

        skull.setItemMeta(meta);
        return skull;
    }

    public static ItemStack getYamlGUIItem2(String configName, String path) {
        org.bukkit.configuration.file.FileConfiguration config = me.detraismc.spmmotrader.config.ConfigManager.getInstance().getConfig(configName);
        if (config == null) return new ItemStack(Material.STONE);

        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) return new ItemStack(Material.STONE);

        return fromSection(section);
    }
}
