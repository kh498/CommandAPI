package com.not2excel.api.command.objects;

import com.not2excel.api.command.CommandHandler;
import com.not2excel.api.command.handler.CommandException;
import com.not2excel.api.command.handler.DefaultHandler;
import com.not2excel.api.command.handler.Handler;
import com.not2excel.api.util.Colorizer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Richmond Steele, kh498
 * @since 12/17/13
 * All rights Reserved
 * Please read included LICENSE file
 */
public class RegisteredCommand extends ParentCommand implements CommandExecutor, Handler {
    private final QueuedCommand queuedCommand;
    private String command = "";
    private Handler handler = this;

    public RegisteredCommand(final QueuedCommand queuedCommand) {
        this.queuedCommand = queuedCommand;
        this.handler = new DefaultHandler(queuedCommand);
    }
    private static void recursivelyDisplayChildUsage(final CommandSender sender, final ParentCommand parentCommand,
                                                     String prefix) {
        for (final Entry<String, ChildCommand> entry : parentCommand.getChildCommands().entrySet()) {
            final ChildCommand childCommand = entry.getValue();
            if (!childCommand.isAlias()) {
                final String description = childCommand.getDescription();
                final String flags = childCommand.getDisplayFlags();

                Colorizer.send(sender, "<yellow>/%s %s %s<gray>%s", prefix, entry.getKey(), flags, description);
                System.out.println("childCmd: " + childCommand);
                if (!childCommand.getChildCommands().isEmpty()) {
                    prefix += " " + entry.getKey();
                    recursivelyDisplayChildUsage(sender, childCommand, prefix);
                }
            }
        }
    }
    private static List<String> sortQuotedArgs(final List<String> args) {
        return sortEnclosedArgs(args, '"');
    }

    private static List<String> sortEnclosedArgs(final List<String> args, final char c) {
        final List<String> strings = new ArrayList<>(args.size());
        for (int i = 0; i < args.size(); ++i) {
            String arg = args.get(i);
            if (arg.length() == 0) {
                continue;
            }
            if (arg.charAt(0) == c) {
                int j;
                final StringBuilder builder = new StringBuilder();
                for (j = i; j < args.size(); ++j) {
                    final String arg2 = args.get(j);
                    if (arg2.charAt(arg2.length() - 1) == c && arg2.length() >= 1) {
                        builder.append(j != i ? " " : "").append(arg2.substring(j == i ? 1 : 0, arg2.length() - 1));
                        break;
                    }
                    else {
                        builder.append(j == i ? arg2.substring(1) : " " + arg2);
                    }
                }
                if (j < args.size()) {
                    arg = builder.toString();
                    i = j;
                }
            }
            strings.add(arg);
        }
        return strings;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String s, final String[] args) {
        try {
            final CommandHandler commandHandler = getMethod().getAnnotation(CommandHandler.class);
            final List<String> strings = Arrays.asList(args);
            this.handler.handleCommand(
                new CommandInfo(this, this, commandHandler, sender, s, sortQuotedArgs(strings), commandHandler.usage(),
                                commandHandler.permission()));
        } catch (final CommandException e) {
            Colorizer.send(sender, "<red>Failed to handle command properly.");
        }
        return true;
    }

    @Override
    public void handleCommand(final CommandInfo info) throws CommandException {
        try {
            this.getMethod().invoke(this.queuedCommand.getObject(), info);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void displayDefaultUsage(final CommandInfo info) {
        final CommandSender sender = info.getSender();
        final String command = info.getCommand();
        final ParentCommand parentCommand = info.getParentCommand();
        final String prefix;

        Colorizer.send(sender, "<red>Usage: %s", info.getUsage());
        if (command.equals(getCommand())) {
            prefix = command;
        }
        else {
            final String baseCmd = info.getCommandHandler().command().replaceAll("\\.", " ");
            final StringBuilder builder = new StringBuilder(baseCmd);
            prefix = recursivelyAddToPrefix(builder, command, parentCommand.getChildCommands()).toString();
        }
        recursivelyDisplayChildUsage(sender, parentCommand, prefix);
    }
    /**
     * recursively create the complete subcommand map of a command
     */
    private static StringBuilder recursivelyAddToPrefix(final StringBuilder builder, final String parentCommand,
                                                        final Map<String, ChildCommand> childCommandMap) {
        for (final Entry<String, ChildCommand> entry : childCommandMap.entrySet()) {
            if (entry.getKey().equals(parentCommand)) {
                builder.append(" ").append(entry.getKey());
                final ChildCommand cc = entry.getValue();
                recursivelyAddToPrefix(builder, cc.getCommand(), cc.getChildCommands());
            }
        }
        return builder;
    }
    private Method getMethod() {
        return this.queuedCommand.getMethod();
    }

    public CommandHandler getCommandHandler() {
        return getMethod().getAnnotation(CommandHandler.class);
    }

    public Handler getHandler() {
        return this.handler;
    }

    public void setHandler(final Handler handler) {
        this.handler = handler;
    }

    public String getPermission() {
        if (this.queuedCommand == null) {
            return "";
        }
        else {
            return getCommandHandler().permission();
        }
    }

    public String getCommand() {
        if (this.queuedCommand == null) {
            return this.command;
        }
        else {
            return getCommandHandler().command();
        }
    }

    public void setCommand(final String command) {
        this.command = command;
    }
}
