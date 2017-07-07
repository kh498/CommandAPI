package com.not2excel.api.command.objects;

import com.not2excel.api.command.CommandManager;
import com.not2excel.api.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Richmond Steele, kh498
 * @since 12/16/13
 * All rights Reserved
 * Please read included LICENSE file
 */
public class AbstractCommand extends Command {
    public CommandExecutor executor;

    public AbstractCommand(final String name) {
        super(name);
    }

    @Override
    public boolean execute(final CommandSender commandSender, final String s, final String[] strings) {
        return this.executor != null && this.executor.onCommand(commandSender, this, s, strings);
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String alias,
                                    final String[] args) throws IllegalArgumentException {
        final CommandManager cmdManager = CommandManager.getInstance();
        final Map<String, RegisteredCommand> registeredCmds = cmdManager.getRegisteredCommands();
        final RegisteredCommand regCmd = registeredCmds.get(this.getName());
        if (regCmd != null) {
            if (args.length == 0) {
                return new ArrayList<>(regCmd.getNoAliasesChildCommands().keySet());
            }

            if (args.length == 1) {
                return new ArrayList<String>() {{
                    for (final String subCmd : regCmd.getNoAliasesChildCommands().keySet()) {
                        //The first character of both is equal and the subCmd contains args[0]
                        if (StringUtils.containsIgnoreCase(subCmd, args[0])) {
                            if (args[0].toCharArray().length > 0 &&
                                !StringUtil.equalsIgnoreCase(args[0].charAt(0), subCmd.charAt(0))) {
                                continue;
                            }
                            add(subCmd);
                        }
                    }
                }};
            }
        }
        return null;
    }
}
