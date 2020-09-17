package io.sigmars.commands.sub;

import io.sigmars.OldArrowsNBows;
import io.sigmars.utils.CommandInterface;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleCommand implements CommandInterface {
    private OldArrowsNBows oldArrowsNBows;
    public ToggleCommand(OldArrowsNBows oldArrowsNBows) {
        this.oldArrowsNBows = oldArrowsNBows;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String Label, String[] args) {
        final boolean PERMISSIONS_ENABLED = oldArrowsNBows.getConfig().getBoolean("general.permissions_enabled");
        final boolean TOGGLE_ALLOWED = oldArrowsNBows.getConfig().getBoolean("no_permissions_plugin.toggle_allowed");

        if (args.length == 0){
            if (sender instanceof Player) {
                if (sender.hasPermission("oldarrowsnbows.toggle") | (!PERMISSIONS_ENABLED & TOGGLE_ALLOWED)) {
                    Player commandPlayer = ((Player) sender).getPlayer();
                    oldArrowsNBows.toggleRapidfire(commandPlayer, true);
                    return true;
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', oldArrowsNBows.getConfig().getString("messages.no_permission")));
                    return false;
                }
            } else {
                sender.sendMessage(oldArrowsNBows.getConfig().getString("messages.console_toggle_help"));
                return false;
            }
        }
        if (args.length == 1){
            if (!(sender instanceof Player) | sender.hasPermission("oldarrows.toggleothers")) {
                Player target = oldArrowsNBows.getServer().getPlayer(args[0]);
                oldArrowsNBows.toggleRapidfire(target, false);
                return true;
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', oldArrowsNBows.getConfig().getString("messages.no_permission")));
            return false;
        }
        return false;
    }
}
