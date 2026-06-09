package me.detraismc.spmmotrader.util;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.logging.Level;

public final class MMOItemsHook {

    private static boolean enabled = false;
    private static Object pluginInstance;
    private static Method getItemMethod;

    private MMOItemsHook() {}

    public static boolean isEnabled() {
        return enabled;
    }

    public static void init() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("MMOItems");
        if (plugin == null) {
            enabled = false;
            return;
        }

        try {
            Class<?> mmoItemsClass = Class.forName("net.Indyuce.mmoitems.MMOItems");
            pluginInstance = mmoItemsClass.getDeclaredField("plugin").get(null);
            getItemMethod = mmoItemsClass.getMethod("getItem", String.class, String.class);
            enabled = true;
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to hook into MMOItems", e);
            enabled = false;
        }
    }

    public static ItemStack getItem(String type, String id) {
        if (!enabled || pluginInstance == null || getItemMethod == null) return null;
        try {
            Object result = getItemMethod.invoke(pluginInstance, type, id);
            if (result instanceof ItemStack) {
                return ((ItemStack) result).clone();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
