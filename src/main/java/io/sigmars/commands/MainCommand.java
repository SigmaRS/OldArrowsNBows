package io.sigmars.commands;

import io.sigmars.utils.CommandInterface;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class MainCommand implements CommandInterface {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String Label, String[] args) {
        return false;
    }
}
