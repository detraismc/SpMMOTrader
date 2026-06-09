package me.detraismc.spmmotrader.commands;

import me.detraismc.spmmotrader.config.ConfigManager;
import me.detraismc.spmmotrader.gui.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OpenMMOTraderCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ConfigManager.getInstance().getMessage("error-mmotrader"));
            return true;
        }

        String module = args[0];
        List<String> moduleList = ConfigManager.getInstance().getModuleList();

        if (!moduleList.contains(module)) {
            sender.sendMessage(ConfigManager.getInstance().getMessage("error-mmotrader-module"));
            return true;
        }

        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found.");
                return true;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage("§cUsage: /openmmotrader [module] [player]");
            return true;
        }

        GUIManager.getInstance().openTrader(target, module, 1);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> modules = ConfigManager.getInstance().getModuleList();
            String partial = args[0].toLowerCase();
            return modules.stream()
                .filter(m -> m.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
        }
        if (args.length == 2) {
            String partial = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(n -> n.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
