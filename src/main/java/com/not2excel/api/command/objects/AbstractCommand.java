package com.not2excel.api.command.objects;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * @author Richmond Steele
 * @since 12/16/13
 * All rights Reserved
 * Please read included LICENSE file
 */
public class AbstractCommand extends Command {
    public CommandExecutor executor;

    public AbstractCommand(final String name) {
        super(name);
    }

    @Override
    public boolean execute(final CommandSender commandSender, final String s, final String[] strings) {
        return this.executor != null && this.executor.onCommand(commandSender, this, s, strings);
    }
}
