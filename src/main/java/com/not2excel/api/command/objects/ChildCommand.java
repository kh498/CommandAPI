package com.not2excel.api.command.objects;

import com.not2excel.api.command.CommandHandler;
import com.not2excel.api.command.Flag;
import com.not2excel.api.command.handler.ErrorHandler;
import com.not2excel.api.command.handler.Handler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.HashSet;
import java.util.Set;

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
    private Handler handler;
    private String displayFlag = "";
    private String displayFlagDesc = "";
    private String fullUsage;
    private final Set<Character> flags;

    public ChildCommand(final CommandHandler commandHandler, final boolean isAlias) {
        setParentAsChild(this);
        this.commandHandler = commandHandler;
        this.isAlias = isAlias;

        this.flags = new HashSet<>();
        if (commandHandler != null) {
            for (final Flag flag : commandHandler.flags()) {
                this.flags.add(flag.flag());
            }
        }
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

    /**
     * @return All valid flags for this command
     */
    public Set<Character> getFlags() {
        return this.flags;
    }


    /**
     * @param checkFlag The flag to check
     *
     * @return The flag corresponding ot the char
     */
    public Flag getFlagAnnotation(final char checkFlag) {
        if (this.commandHandler == null) {return null;}
        for (final Flag flag : this.commandHandler.flags()) {
            if (flag.flag() == checkFlag) {
                return flag;
            }
        }
        return null;
    }

    /**
     * @param c The character to check
     *
     * @return If this command has a flag with the character {@code c}
     */
    public boolean hasFlag(final char c) {
        return this.flags.contains(c);
    }

    String getDisplayFlags() {
        if (this.displayFlag.isEmpty() && !getFlags().isEmpty()) {
            final StringBuilder flagsBuilder = new StringBuilder().append(ChatColor.GOLD);
            for (final char c : this.getFlags()) {
                flagsBuilder.append('-').append(c).append(' ');
            }
            this.displayFlag = flagsBuilder.toString();
        }
        return this.displayFlag;
    }

    String getDisplayFlagDesc() {
        if (!this.flags.isEmpty()) {
            final StringBuilder flagsDescBuilder = new StringBuilder(ChatColor.GRAY + "\n");

            final Flag[] flags1 = this.commandHandler.flags();
            for (int i = 0; i < flags1.length; i++) {
                flagsDescBuilder.append("     ").append(flags1[i].usage());
                if (i + 1 < flags1.length) {
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
