package com.not2excel.api.command.objects;

import com.not2excel.api.command.CommandHandler;
import com.not2excel.api.command.handler.ErrorHandler;
import com.not2excel.api.command.handler.Handler;
import org.bukkit.command.CommandSender;

/**
 * @author Richmond Steele, kh498
 * @since 12/17/13
 * All rights Reserved
 * Please read included LICENSE file
 */
public class ChildCommand extends ParentCommand {
    private final CommandHandler commandHandler;
    protected String command = "";
    protected String usage = "";
    protected String description = "";
    protected String permission = "";
    protected String flags = "";
    private Handler handler;

    public ChildCommand(final CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    public CommandHandler getCommandHandler() {
        return this.commandHandler;
    }

    public Handler getHandler() {
        if (this.handler == null) {
            return new ErrorHandler();
        }
        return this.handler;
    }

    public void setHandler(final Handler handler) {
        this.handler = handler;
    }

    public boolean checkPermission(final CommandSender sender) {
        return this.commandHandler == null || "".equals(this.commandHandler.permission()) ||
               sender.hasPermission(this.commandHandler.permission());
    }

    public String getDescription() {
        if (this.commandHandler == null) {
            return this.description;
        }
        else {
            return this.commandHandler.description();
        }
    }

    public String getUsage() {
        if (this.commandHandler == null) {
            return this.usage;
        }
        else {
            return this.commandHandler.usage();
        }
    }

    public String getPermission() {
        if (this.commandHandler == null) {
            return this.permission;
        }
        else {
            return this.commandHandler.permission();
        }
    }

    public String getCommand() {
        if (this.commandHandler == null) {
            return this.command;
        }
        else {
            final String[] list = this.commandHandler.command().split("\\.");
            return list[list.length - 1 <= 0 ? 0 : list.length - 1];
        }
    }

    public String getFlags() {
        if (this.commandHandler == null) {
            return this.flags;
        }
        else {
            return this.commandHandler.flags();
        }
    }
}
