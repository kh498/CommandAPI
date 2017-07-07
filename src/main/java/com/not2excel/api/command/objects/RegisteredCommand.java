package com.not2excel.api.command.objects;

import com.not2excel.api.command.CommandHandler;
import com.not2excel.api.command.handler.CommandException;
import com.not2excel.api.command.handler.DefaultHandler;
import com.not2excel.api.command.handler.Handler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    private static void displayChildUsage(final CommandInfo info) {
        //TODO Display aliases
        for (final Entry<String, ChildCommand> entry : info.getParentCommand().getAllChildCommands().entrySet()) {
            final ChildCommand childCommand = entry.getValue();
            if (!childCommand.isAlias()) {
                final String description = childCommand.getDescription();
                final String Usage = childCommand.getLightExplainedUsage();
                if (!Usage.isEmpty()) {
                    info.getSender().sendMessage(ChatColor.YELLOW + Usage + ChatColor.GRAY + description);
                }
            }
        }
    }

    private static List<String> sortQuotedArgs(final List<String> args) {
        final List<String> strings = new ArrayList<>(args.size());
        for (int i = 0; i < args.size(); ++i) {
            String arg = args.get(i);
            if (arg.length() == 0) {
                continue;
            }
            if (arg.charAt(0) == '"') {
                int j;
                final StringBuilder builder = new StringBuilder();
                for (j = i; j < args.size(); ++j) {
                    final String arg2 = args.get(j);
                    if (arg2.charAt(arg2.length() - 1) == '"' && arg2.length() >= 1) {
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
            sender.sendMessage(ChatColor.RED + "Failed to handle command properly.");
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

    public static void displayDefaultUsage(final CommandInfo info) {
        final CommandSender sender = info.getSender();
        sender.sendMessage(ChatColor.RED + "Usage: " + info.getExplainedUsage());
        displayChildUsage(info);
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
