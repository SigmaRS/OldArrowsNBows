package io.github.douglasb2310.utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface CommandInterface {
    boolean onCommand(CommandSender sender, Command cmd, String Label, String[] args);
}
