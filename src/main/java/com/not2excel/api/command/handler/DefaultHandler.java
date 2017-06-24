package com.not2excel.api.command.handler;

import com.not2excel.api.command.CommandHandler;
import com.not2excel.api.command.objects.ChildCommand;
import com.not2excel.api.command.objects.CommandInfo;
import com.not2excel.api.command.objects.ParentCommand;
import com.not2excel.api.command.objects.QueuedCommand;
import org.bukkit.ChatColor;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

/**
 * @author Richmond Steele, William Reed, kh498
 * @since 12/18/13
 * All rights Reserved
 * Please read included LICENSE file
 */
public class DefaultHandler implements Handler {
    private final QueuedCommand queue;

    public DefaultHandler(final QueuedCommand queue) {
        this.queue = queue;
    }

    @Override
    public void handleCommand(final CommandInfo info) throws CommandException {
        final List<String> args = info.getArgs();
        final ParentCommand parentCommand = info.getParentCommand();
        if (args.size() == 0 || parentCommand.getChildCommands().size() == 0) {
            if (this.queue != null) {
                sendCommand(info);
            }
            else {
                info.getRegisteredCommand().displayDefaultUsage(info);
            }
        }
        else if (args.size() > 0) {
            if (("help".equalsIgnoreCase(args.get(0)) && !parentCommand.getChildCommands().containsKey("help")) ||
                ("?".equalsIgnoreCase(args.get(0)) && !parentCommand.getChildCommands().containsKey("?"))) {
//                final CommandHandler ch = this.queue.getMethod().getAnnotation(CommandHandler.class);
                info.getRegisteredCommand().displayDefaultUsage(info);
                return;
            }
            final ChildCommand child = parentCommand.getChildCommands().get(args.get(0));
            if (child == null) {
                //needed to send parent command instead of throwing errors so that parent command can process args
                try {
                    sendCommand(info);
                } catch (final CommandException e) {
                    e.printStackTrace();
                }
                return;
            }
            if (!child.checkPermission(info.getSender())) {
                info.getSender().sendMessage(ChatColor.RED + child.getCommandHandler().noPermission());
                return;
            }
            final CommandInfo cmdInfo =
                new CommandInfo(info.getRegisteredCommand(), child, child.getCommandHandler(), info.getSender(),
                                args.get(0), args.size() == 1 ? Collections.emptyList() : args.subList(1, args.size()),
                                info.getUsage(), info.getPermission());
            try {
                child.getHandler().handleCommand(cmdInfo);
            } catch (final CommandException e) {
                info.getSender().sendMessage(ChatColor.RED + "Failed to handle command properly.");
            }
        }
    }

    private void sendCommand(final CommandInfo info) throws CommandException {
        final CommandHandler ch = this.queue.getMethod().getAnnotation(CommandHandler.class);

        if (ch.strictArgs() && ch.max() == 0 && info.getArgsLength() == 0) {
            info.getRegisteredCommand().displayDefaultUsage(info);
            return;
        }

        if (info.getArgsLength() < info.getCommandHandler().min()) {
            sendHelpScreen(info, "Too few arguments.");
            return;
        }
        if (info.getCommandHandler().max() != -1 && info.getArgsLength() > info.getCommandHandler().max()) {
            sendHelpScreen(info, "Too many arguments.");
            return;
        }
        if (!info.getSender().hasPermission(info.getCommandHandler().permission())) {
            info.getSender().sendMessage(ChatColor.RED + info.getCommandHandler().noPermission());
            return;
        }
        if (info.playersOnly() && !info.isPlayer()) {
            //maybe make this configurable some how
            info.getSender().sendMessage(ChatColor.RED + "This command can only be executed in game.");
            return;
        }

        if (!info.hasAsteriskFlag()) {
            for (final char flag : info.getFlags()) {
                if (!ch.flags().contains(String.valueOf(flag))) {
                    sendHelpScreen(info, "Unknown flag: " + flag);
                    return;
                }
            }
        }

        if (ch.strictArgs()) {
            for (final String arg : info.getArgs()) {
                if (!CommandInfo.isValidFlag(arg)) {
                    sendHelpScreen(info, "Unknown subcommand: " + arg);
                    return;
                }
            }

        }

        try {
            this.queue.getMethod().invoke(this.queue.getObject(), info);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static void sendHelpScreen(final CommandInfo info, final String errorMsg) {
        info.getSender().sendMessage(ChatColor.RED + errorMsg);
        info.getRegisteredCommand().displayDefaultUsage(info);
    }
}
