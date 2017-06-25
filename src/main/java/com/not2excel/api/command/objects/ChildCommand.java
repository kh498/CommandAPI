package com.not2excel.api.command.objects;

import com.not2excel.api.command.CommandHandler;
import com.not2excel.api.command.handler.ErrorHandler;
import com.not2excel.api.command.handler.Handler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * @author Richmond Steele, kh498
 * @since 12/17/13
 * All rights Reserved
 * Please read included LICENSE file
 */
public class ChildCommand extends ParentCommand {
    private final CommandHandler commandHandler;
    private final boolean isAlias;
    protected String command = "";
    protected String usage = "";
    protected String description = "";
    protected String permission = "";
    protected String flags = "";
    private Handler handler;
    private String displayFlag = "";
    private final String[] flagDesc = {};
    private String displayFlagDesc = "";
    private String fullUsage;

    public ChildCommand(final CommandHandler commandHandler, final boolean isAlias) {
        setParentAsChild(this);
        this.commandHandler = commandHandler;
        this.isAlias = isAlias;
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

    public String[] getFlagsDesc() {
        if (this.commandHandler == null) {
            return this.flagDesc;
        }
        else {
            return this.commandHandler.flagDesc();
        }
    }

    String getDisplayFlags() {
        if (this.displayFlag.isEmpty() && !getFlags().isEmpty()) {
            final StringBuilder flagsBuilder = new StringBuilder().append(ChatColor.GOLD);
            for (final char c : this.getFlags().toCharArray()) {
                flagsBuilder.append('-').append(c).append(' ');
            }
            this.displayFlag = flagsBuilder.toString();
        }
        return this.displayFlag;
    }

    String getDisplayFlagDesc() {
        final int length = getFlagsDesc().length;
        if (!getFlags().isEmpty() && length != 0) {
            final StringBuilder flagsDescBuilder = new StringBuilder(ChatColor.GRAY + "\n");

            for (int i = 0; i < length; i++) {
                flagsDescBuilder.append("     ").append(getFlagsDesc()[i]);
                if (i + 1 < length) {
                    flagsDescBuilder.append("\n");
                }
            }
            this.displayFlagDesc = flagsDescBuilder.toString();
        }
        return this.displayFlagDesc;
    }

    String getLightExplainedUsage() {
        if (this.commandHandler == null) {
            return getUsage();
        }
        if (this.fullUsage == null) {
            final String baseCmd = this.commandHandler.command().replaceAll("\\.", " ");

            final StringBuilder usage = new StringBuilder('/' + baseCmd);
            if (!"".equals(this.commandHandler.usage())) {
                usage.append(' ').append(this.commandHandler.usage());
            }
            usage.append(' ').append(getDisplayFlags());

            this.fullUsage = usage.toString();
        }
        return this.fullUsage;
    }

    public boolean isAlias() {
        return this.isAlias;
    }

    @Override
    public String toString() {
        return "ChildCommand{" + "command='" + getCommand() + '\'' + ", usage='" + getUsage() + '\'' +
               ", description='" + getDescription() + '\'' + ", permission='" + getPermission() + '\'' + ", flags='" +
               getFlags() + '\'' + ", isAlias='" + this.isAlias + '\'' + '}';
    }

}
