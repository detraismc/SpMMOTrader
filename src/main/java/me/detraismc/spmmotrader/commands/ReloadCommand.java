package me.detraismc.spmmotrader.commands;

import me.detraismc.spmmotrader.config.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigManager.getInstance().reloadConfigs();
        sender.sendMessage(ConfigManager.getInstance().getMessage("message-reload"));
        return true;
    }
}
