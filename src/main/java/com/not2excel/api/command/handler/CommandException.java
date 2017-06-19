package com.not2excel.api.command.handler;

import org.bukkit.command.CommandSender;

/**
 * @author Richmond Steele
 * @since 12/17/13
 * All rights Reserved
 * Please read included LICENSE file
 */
public class CommandException extends Exception {

    private static final long serialVersionUID = 7841254778605849087L;

    CommandException(final String s) {
        super(s);
    }

    public CommandException(final CommandSender sender, final String s) {
        super(s);
        sender.sendMessage(s);
    }

    public CommandException(final CommandSender sender, final String s, final Object... objects) {
        super(s);
        sender.sendMessage(String.format(s, objects));
    }
}
