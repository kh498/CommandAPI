package com.not2excel.api.command.handler;

import com.not2excel.api.command.objects.CommandInfo;
import org.bukkit.ChatColor;

/**
 * @author Richmond Steele
 * @since 12/17/13
 * All rights Reserved
 * Please read included LICENSE file
 */
public class ErrorHandler implements Handler {
    @Override
    public void handleCommand(final CommandInfo info) throws CommandException {
        throw new CommandException(ChatColor.RED + "Failed to handle command properly.");
    }
}
