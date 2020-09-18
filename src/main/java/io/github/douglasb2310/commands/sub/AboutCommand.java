package io.github.douglasb2310.commands.sub;

import io.github.douglasb2310.utils.CommandInterface;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Calendar;

public class AboutCommand implements CommandInterface {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String Label, String[] args) {
        String aboutmsg = "";
        aboutmsg +="&3[OldArrowsNBows]&f v1.2 by DouglasB2310 - MIT License / Copyright © {0}"
                .replace("{0}", Calendar.getInstance().get(Calendar.YEAR) + "");
        aboutmsg += "\n&3[OldArrowsNBows]&f https://github.com/douglasb2310/OldArrowsNBows";
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aboutmsg));
        return true;
    }
}
