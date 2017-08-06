package com.not2excel.api.command.objects;

import com.not2excel.api.command.CommandHandler;
import com.not2excel.api.command.handler.CommandException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Richmond Steele, kh498
 * @since 12/17/13 All rights Reserved Please read included LICENSE file
 */
@SuppressWarnings({"unused", "SameParameterValue", "WeakerAccess"})
public class CommandInfo {

    private static final Pattern FLAG_PATTERN = Pattern.compile("^-[a-zA-Z*]$");
    private final RegisteredCommand registeredCommand;
    private final ParentCommand parentCommand;
    private final CommandHandler commandHandler;
    private final CommandSender sender;
    private final String command;
    private final String usage;
    private final String permission;
    private final boolean playersOnly;
    private final Set<Character> flags;
    private List<String> args;
    private boolean hasAsteriskFlag;
    private String fullUsage;

    public CommandInfo(final RegisteredCommand registeredCommand, final ParentCommand parentCommand,
                       final CommandHandler commandHandler, final CommandSender sender, final String command,
                       final List<String> cmdArgs, final String usage, final String permission) {
        this.registeredCommand = registeredCommand;
        this.parentCommand = parentCommand;
        this.commandHandler = commandHandler;
        this.sender = sender;
        this.command = command;
        this.args = cmdArgs;
        this.usage = usage;
        this.permission = permission;

        if (commandHandler == null) {
            Bukkit.getLogger().warning("CommandHandler is null, playersOnly is set to the default value false");
            this.playersOnly = false;
        }
        else {
            this.playersOnly = commandHandler.playerOnly();


        }

        if (matchesFlagPattern(command)) {
            throw new IllegalArgumentException("A sub command cannot be a valid flag!");
        }

        this.flags = new HashSet<>();
        /*
         Iterate through tempArgs and look for flags. (eks -f or -R)
         */
        for (final String arg : cmdArgs) {
            if (arg.length() == 0) {
                continue;
            }
            if (matchesFlagPattern(arg)) {
                if (!this.hasAsteriskFlag && arg.charAt(1) == '*') {
                    this.hasAsteriskFlag = true;
                }
                this.flags.add(arg.charAt(1));
            }
        }
    }

    /**
     * @param str
     *     The string to check
     *
     * @return If str is formatted as a flag
     */
    public static boolean matchesFlagPattern(final String str) {
        return FLAG_PATTERN.matcher(str).matches();
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

    /**
     * @return The player who executed the command or {@code null} if console
     */
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

    public String getExplainedUsage() {
        if (this.commandHandler == null) {
            return this.getUsage();
        }
        if (this.fullUsage == null) {
            final String baseCmd = this.commandHandler.command().replaceAll("\\.", " ");

            final StringBuilder usage = new StringBuilder('/' + baseCmd);
            final ChildCommand parentAsChild = this.parentCommand.getParentAsChild();
            if (!"".equals(this.commandHandler.usage())) {
                usage.append(' ').append(this.commandHandler.usage());
            }
            if (parentAsChild != null) {
                usage.append(' ').append(parentAsChild.getDisplayFlags());
                usage.append(parentAsChild.getDisplayFlagDesc());
            }
            this.fullUsage = usage.toString();
        }
        return this.fullUsage;
    }

    /**
     * @return The description of the command
     */
    public String getDescription() {
        if (this.commandHandler == null) {
            return "";
        }
        return this.commandHandler.description();
    }

    /**
     * @param index
     *     The argument you want
     *
     * @return the argument at {@code index}
     *
     * @throws CommandException
     *     if {@code index} is greater than the size of arguments
     */
    public String getIndex(final int index) throws CommandException {
        if (index >= this.args.size() || index < 0) {
            throw new CommandException(this.sender, ChatColor.RED + "Invalid index number");
        }
        return this.args.get(index);
    }

    public String getIndex(final int index, final String defaultString) {
        if (index >= this.args.size() || index < 0) {
            return defaultString;
        }
        return this.args.get(index);
    }

    /**
     * Try and parse an argument as an integer
     *
     * @param index
     *     The argument you want
     *
     * @return the argument at {@code index} as int
     *
     * @throws CommandException
     *     if {@code index} is invalid or the argument is not an integer
     */
    public int getInt(final int index) throws CommandException {
        if (index >= this.args.size() || index < 0) {
            throw new CommandException(this.sender, ChatColor.RED + "Invalid index number");
        }
        final int returnValue;
        try {
            returnValue = Integer.parseInt(this.args.get(index));
        } catch (final NumberFormatException e) {
            throw new CommandException(this.sender, ChatColor.RED + "Index " + ChatColor.GOLD + "%d" + ChatColor.RED +
                                                    " is not an Integer", index);
        }
        return returnValue;
    }

    /**
     * Try and parse an argument as an integer
     *
     * @param index
     *     The argument you want
     * @param defaultValue
     *     if a {@code CommandException} happens return this value
     *
     * @return the argument at {@code index} as int or {@code defaultValue} if a a {@code CommandException} is
     * encountered
     */
    public int getInt(final int index, final int defaultValue) {
        if (index >= this.args.size() || index < 0) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(this.args.get(index));
        } catch (final NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Try and parse an argument as an double
     *
     * @param index
     *     The argument you want
     *
     * @return the argument at {@code index} as double
     *
     * @throws CommandException
     *     if {@code index} is invalid or the argument is not an double
     */
    public double getDouble(final int index) throws CommandException {
        if (index >= this.args.size() || index < 0) {
            throw new CommandException(this.sender, ChatColor.RED + "Invalid index number");
        }
        final double returnValue;
        try {
            returnValue = Double.parseDouble(this.args.get(index));
        } catch (final NumberFormatException e) {
            throw new CommandException(this.sender, ChatColor.RED + "Index " + ChatColor.GOLD + "%d" + ChatColor.RED +
                                                    " is not an Double", index);
        }
        return returnValue;
    }

    /**
     * Try and parse an argument as an double
     *
     * @param index
     *     The argument you want
     * @param defaultValue
     *     if a {@code CommandException} happens return this value
     *
     * @return the argument at {@code index} as double or {@code defaultValue} if a a {@code CommandException} is
     * encountered
     */
    public double getDouble(final int index, final double defaultValue) {
        if (index >= this.args.size() || index < 0) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(this.args.get(index));
        } catch (final NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Try and parse an argument as an online player
     *
     * @param index
     *     The argument you want
     *
     * @return the argument at {@code index} as online player
     *
     * @throws CommandException
     *     if {@code index} is invalid or the argument is not an online player
     */
    public Player getOnlinePlayer(final int index) throws CommandException {
        if (index >= this.args.size() || index < 0) {
            throw new CommandException(this.sender, ChatColor.RED + "Invalid index number");
        }
        final Player returnValue;
        try {
            returnValue = Bukkit.getPlayer(this.args.get(index));
        } catch (final NumberFormatException e) {
            throw new CommandException(this.sender, ChatColor.RED + "Index " + ChatColor.GOLD + "%d" + ChatColor.RED +
                                                    " is not a online player", index);
        }
        return returnValue;
    }

    /**
     * Try and parse an argument as an offline player
     *
     * @param index
     *     The argument you want
     *
     * @return the argument at {@code index} as offline player
     *
     * @throws CommandException
     *     if {@code index} is invalid or the argument is not a offline player
     */
    public OfflinePlayer getOfflinePlayer(final int index) throws CommandException {
        if (index >= this.args.size() || index < 0) {
            throw new CommandException(this.sender, ChatColor.RED + "Invalid index number");
        }
        final OfflinePlayer returnValue;
        try {
            returnValue = Bukkit.getOfflinePlayer(this.args.get(index));
        } catch (final NumberFormatException e) {
            throw new CommandException(this.sender, ChatColor.RED + "Index " + ChatColor.GOLD + "%d" + ChatColor.RED +
                                                    " is not a player", index);
        }
        return returnValue;
    }

    public String joinArgs(final int index) throws CommandException {
        if (index >= this.args.size() || index < 0) {
            throw new CommandException(this.sender, ChatColor.RED + "Invalid index number");
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
     * @param flag
     *     The flag you want to check is present
     *
     * @return {@code true} if the flag has been found or {@link #hasAsteriskFlag()} is {@code true}, {@code false}
     * otherwise.
     */
    public boolean hasFlag(final char flag) {
        return this.hasAsteriskFlag || this.flags.contains(flag);
    }

    /**
     * @param s
     *     A string containing the flags to check
     *
     * @return true if one of the chars in the string matches the flag in the present command
     */
    public boolean hasOneOfFlags(final String s) {
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
