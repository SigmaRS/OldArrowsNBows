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
        final String MSG_TOGGLED_FOR = oldArrowsNBows.getConfig().getString("messages.toggled_for");

        if (args.length == 1){
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
        if (args.length == 2){
            if (!(sender instanceof Player) | sender.hasPermission("oldarrows.toggleothers")) {
                Player target = oldArrowsNBows.getServer().getPlayer(args[1]);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', MSG_TOGGLED_FOR
                        .replace("{0}", args[1])
                        .replace("{1}","" + !(oldArrowsNBows.isRapidfireEnabled(oldArrowsNBows.getServer().getPlayer(args[1]))))
                ));
                oldArrowsNBows.toggleRapidfire(target, false);
                return true;
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', MSG_NO_PERMISSION));
            return false;
        }
        return false;
    }
}
