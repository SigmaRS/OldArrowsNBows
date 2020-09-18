package io.github.douglasb2310.commands.sub;

import io.github.douglasb2310.OldArrowsNBows;
import io.github.douglasb2310.utils.CommandInterface;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleCommand implements CommandInterface {
    private final OldArrowsNBows oldArrowsNBows;
    public ToggleCommand(OldArrowsNBows oldArrowsNBows) {
        this.oldArrowsNBows = oldArrowsNBows;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String Label, String[] args) {
        final boolean PERMISSIONS_ENABLED = oldArrowsNBows.getConfig().getBoolean("general.permissions_enabled");
        final boolean TOGGLE_ALLOWED = oldArrowsNBows.getConfig().getBoolean("no_permissions_plugin.toggle_allowed");

        final String MSG_NO_PERMISSION = oldArrowsNBows.getConfig().getString("messages.no_permission");
        final String MSG_HELP_CONSOLE = oldArrowsNBows.getConfig().getString("messages.console_toggle_help");

        if (args.length == 0){
            if (sender instanceof Player) {
                if (sender.hasPermission("oldarrowsnbows.toggle") | (!PERMISSIONS_ENABLED & TOGGLE_ALLOWED)) {
                    Player commandPlayer = ((Player) sender).getPlayer();
                    oldArrowsNBows.toggleRapidfire(commandPlayer, true);
                    return true;
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', MSG_NO_PERMISSION));
                    return false;
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', MSG_HELP_CONSOLE));
                return true;
            }
        }
        if (args.length == 1){
            if (sender.hasPermission("oldarrows.toggleothers") | !(sender instanceof Player)) {
                Player target = oldArrowsNBows.getServer().getPlayer(args[0]);
                oldArrowsNBows.toggleRapidfire(target, false);
                return true;
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', MSG_NO_PERMISSION));
            return false;
        }
        return false;
    }
}
