package com.not2excel.api.command.objects;

import com.not2excel.api.command.CommandHandler;
import com.not2excel.api.command.handler.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Richmond Steele, kh498
 * @since 12/17/13
 * All rights Reserved
 * Please read included LICENSE file
 */
@SuppressWarnings("unused")
public class CommandInfo {
    private final RegisteredCommand registeredCommand;
    private final ParentCommand parentCommand;
    private final CommandHandler commandHandler;
    private final CommandSender sender;
    private final String command;
    private final String usage;
    private final String permission;
    private List<String> args;
    private final boolean playersOnly;
    private final Set<Character> flags;
    private boolean hasAsteriskFlag;

    private static final Pattern FLAG = Pattern.compile("^-[a-zA-Z*]$");

    public CommandInfo(final RegisteredCommand registeredCommand, final ParentCommand parentCommand,
                       final CommandHandler commandHandler, final CommandSender sender, final String command,
                       final List<String> args, final String usage, final String permission) {
        this.registeredCommand = registeredCommand;
        this.parentCommand = parentCommand;
        this.commandHandler = commandHandler;
        this.sender = sender;
        this.command = command;
        this.args = args;
        this.usage = usage;
        this.permission = permission;
        this.playersOnly = commandHandler.playerOnly();

        if (FLAG.matcher(command).matches()) {
            throw new IllegalArgumentException("A sub command cannot be a valid flag!");
        }

        this.flags = new HashSet<>();
        /*
         Iterate through tempArgs and look for flags. (eks -f or -R)
         */
        for (final String str : args) {
            final int length = str.length();
            if (length == 0) { continue; }
            if (FLAG.matcher(str).matches()) {
                if (!this.hasAsteriskFlag && str.charAt(1) == '*') {
                    this.hasAsteriskFlag = true;
                }
                this.flags.add(str.charAt(1));
            }
        }
    }

    public RegisteredCommand getRegisteredCommand() {
        return this.registeredCommand;
    }

    public ParentCommand getParentCommand() {
        return this.parentCommand;
    }

    public CommandHandler getCommandHandler() {
        return this.commandHandler;
    }

    public CommandSender getSender() {
        return this.sender;
    }

    public Player getPlayer() {
        if (isPlayer()) {
            return (Player) this.sender;
        }
        return null;
    }

    public boolean isPlayer() {
        return this.sender instanceof Player;
    }

    public String getCommand() {
        return this.command;
    }

    public List<String> getArgs() {
        return this.args;
    }

    public void setArgs(final List<String> args) {
        this.args = args;
    }

    public int getArgsLength() {
        return this.args.size();
    }

    public String getPermission() {
        if (this.commandHandler == null) {
            return this.permission;
        }
        return this.commandHandler.permission();
    }

    public String noPermission() {
        if (this.commandHandler == null) {
            return "";
        }
        return this.commandHandler.noPermission();
    }

    public String getUsage() {
        if (this.commandHandler == null) {
            return this.usage;
        }
        return this.commandHandler.usage();
    }

    public String getDescription() {
        if (this.commandHandler == null) {
            return "";
        }
        return this.commandHandler.description();
    }

    public String getIndex(final int index) throws CommandException {
        if (index > this.args.size()) {
            throw new CommandException(this.sender, "<red>Invalid index number");
        }
        return this.args.get(index);
    }

    public String getIndex(final int index, final String defaultString) {
        if (index > this.args.size()) {
            return defaultString;
        }
        return this.args.get(index);
    }

    public int getInt(final int index) throws CommandException {
        if (index > this.args.size()) {
            throw new CommandException(this.sender, "<red>Invalid index number");
        }
        final int returnValue;
        try {
            returnValue = Integer.parseInt(this.args.get(index));
        } catch (final NumberFormatException e) {
            throw new CommandException(this.sender, "<red>Index <gold>%d<red> is not an Integer", index);
        }
        return returnValue;
    }

    public int getInt(final int index, final int defaultValue) {
        if (index > this.args.size()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(this.args.get(index));
        } catch (final NumberFormatException e) {
            return defaultValue;
        }
    }

    public double getDouble(final int index) throws CommandException {
        if (index > this.args.size()) {
            throw new CommandException(this.sender, "<red>Invalid index number");
        }
        final double returnValue;
        try {
            returnValue = Double.parseDouble(this.args.get(index));
        } catch (final NumberFormatException e) {
            throw new CommandException(this.sender, "<red>Index <gold>%d<red> is not an Double", index);
        }
        return returnValue;
    }

    public double getDouble(final int index, final double defaultValue) {
        if (index > this.args.size()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(this.args.get(index));
        } catch (final NumberFormatException e) {
            return defaultValue;
        }
    }

    public String joinArgs(final int index) throws CommandException {
        if (index > this.args.size()) {
            throw new CommandException(this.sender, "<red>Invalid index number");
        }
        final StringBuilder builder = new StringBuilder();
        for (int i = index; i < this.args.size(); ++i) {
            final String arg = this.args.get(i);
            if (i != index) {
                builder.append(" ");
            }
            builder.append(arg);
        }
        return builder.toString();
    }

    public boolean playersOnly() {
        return this.playersOnly;
    }

    /**
     * @return All flags found in the arguments of the command
     */
    public Set<Character> getFlags() {
        return this.flags;
    }

    /**
     * @param flag The flag you want to check is present
     *
     * @return {@code true} if the flag has been found or {@link #hasAsteriskFlag()} is {@code true}, {@code false} otherwise.
     */
    public boolean hasFlag(final char flag) {
        return this.hasAsteriskFlag || this.flags.contains(flag);
    }

    public boolean hasFlags(final String s) {
        if (this.hasAsteriskFlag) {
            return true;
        }

        for (final char c : s.toCharArray()) {
            if (this.flags.contains(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return if true then all flags should be seen as present
     */
    public boolean hasAsteriskFlag() {
        return this.hasAsteriskFlag;
    }
}
