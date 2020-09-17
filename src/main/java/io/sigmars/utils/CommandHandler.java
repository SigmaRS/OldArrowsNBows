package io.sigmars.utils;

import io.sigmars.OldArrowsNBows;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;

public class CommandHandler implements CommandExecutor {
    private OldArrowsNBows oldArrowsNBows;
    private static HashMap<String, CommandInterface> commands = new HashMap<>();

    public void registerCmd(String name, CommandInterface cmd){
        commands.put(name, cmd);
    }

    public boolean exists(String name){
        return commands.containsKey(name);
    }

    public CommandInterface getExecutor(String name){
        return commands.get(name);
    }
    // -- template ---
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String Label, String[] args) {
        if(args.length == 0) {
            getExecutor("about").onCommand(sender, cmd, Label, args);
            return false;
        }
        if(args.length > 0) {
            if (exists(args[0])) {
                getExecutor(args[0]).onCommand(sender, cmd, Label, args);
            } else {
                sender.sendMessage(oldArrowsNBows.getConfig().getString("messages.not_exists"));
            }
        }
        return false;
    }
}