package com.not2excel.api.command;

import com.not2excel.api.command.handler.DefaultHandler;
import com.not2excel.api.command.objects.*;
import com.not2excel.api.logging.LevelLogger;
import com.not2excel.api.reflection.ClassEnumerator;
import com.not2excel.api.reflection.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicComparator;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Richmond Steele, kh498
 * @since 12/17/13 All rights Reserved Please read included LICENSE file
 */
@SuppressWarnings("unused")
public class CommandManager {
    private final Plugin plugin;
    private final Map<Integer, List<QueuedCommand>> queuedCommands = new ConcurrentHashMap<>();
    private final Map<String, RegisteredCommand> registeredCommands = new ConcurrentHashMap<>();
    private final LevelLogger logger;
    private CommandMap commandMap;
    public static CommandManager instance;

    private final static boolean DEBUG = false;

    public CommandManager(final Plugin plugin) {
        this.plugin = plugin;
        this.logger = LevelLogger.getInstance();
        this.logger.setLogType("CommandHandlerAPI");
        this.logger.setTimeStamped(false);
        instance = this;
    }

    public void registerHelp() {
        final Set<HelpTopic> help = new TreeSet<>(HelpTopicComparator.helpTopicComparatorInstance());
        for (final String s : this.registeredCommands.keySet()) {
            final Command cmd = this.commandMap.getCommand(s);
            if (cmd != null) {
                final HelpTopic topic = new GenericCommandHelpTopic(cmd);
                help.add(topic);
            }
        }
        final IndexHelpTopic topic =
            new IndexHelpTopic(this.plugin.getName(), "All commands for " + this.plugin.getName(), null, help,
                               "Below is a list of all " + this.plugin.getName() + " commands:");
        Bukkit.getServer().getHelpMap().addTopic(topic);
    }

    public void registerCommands() {
        final Class<?>[] classes = ClassEnumerator.getInstance().getClassesFromThisJar(this.plugin);
        if (classes == null || classes.length == 0) {
            this.logger.log("No classes can be found!");
            return;
        }
        for (final Class<?> clazz : classes) {
            if (CommandListener.class.isAssignableFrom(clazz) && !clazz.isInterface() && !clazz.isEnum() &&
                !clazz.isAnnotation()) {
                registerCommands(clazz);
            }
        }
        processQueuedCommands();
    }

    private void processQueuedCommands() {
        synchronized (this.queuedCommands) {
            if (DEBUG) {
                if (!this.queuedCommands.isEmpty()) {
                    this.logger.log("Processing Queued commands.");
                }
                else {
                    this.logger.log("There are no Queued commands.");
                }
            }
            int MAX_ITERATION = 0;
            for (final int i : this.queuedCommands.keySet()) {
                if (i > MAX_ITERATION) {
                    MAX_ITERATION = i;
                }
            }
            for (int i = 1; i <= MAX_ITERATION; i++) {
                final List<QueuedCommand> queuedCommandList = this.queuedCommands.get(i);
                if (queuedCommandList == null || queuedCommandList.isEmpty()) {
                    continue;
                }
                for (final QueuedCommand queue : queuedCommandList) {
                    final CommandHandler commandHandler = queue.getMethod().getAnnotation(CommandHandler.class);
                    final String[] list = commandHandler.command().split("\\.");
                    final RegisteredCommand registered;
                    synchronized (this.registeredCommands) {
                        if (!this.registeredCommands.containsKey(list[0])) {
                            if (DEBUG) { this.logger.log("Registering Empty Base Command: " + list[0]); }
                            final RegisteredCommand registeredEmpty = new RegisteredCommand(null);
                            registeredEmpty.setCommand(list[0]);
                            synchronized (this.registeredCommands) {
                                this.registeredCommands.put(list[0], registeredEmpty);
                            }
                            final AbstractCommand abstractCmd = new AbstractCommand(list[0]);
                            abstractCmd.setDescription("Use '/" + list[0] + " help' to view the subcommands.");
                            abstractCmd.setPermission("");
                            abstractCmd.setPermissionMessage("You don't have permission to do that.");
                            abstractCmd.setUsage("/" + list[0] + " <command>");
                            abstractCmd.executor = registeredEmpty;
                            registerBaseCommand(abstractCmd);
                            continue;
                        }
                        registered = this.registeredCommands.get(list[0]);
                    }
                    registerChild(queue, commandHandler, registered, list[list.length - 1], false);
                    for (final String s : commandHandler.aliases()) {
                        registerChild(queue, commandHandler, registered, s, true);
                    }
                }
            }
            this.queuedCommands.clear();
        }
    }

    private void registerChild(final QueuedCommand queue, final CommandHandler commandHandler,
                               final RegisteredCommand registered, final String s, final boolean isAlias) {
        final ChildCommand child = new ChildCommand(commandHandler, isAlias);
        final ParentCommand parentCommand = recursivelyFindInnerMostParent(commandHandler.command(), registered, 1);
        final String[] list = commandHandler.command().split("\\.");
        if (list.length == 2) {
            registered.addChild(s, child);
            registered.getChild(s).setHandler(new DefaultHandler(queue));
            if (DEBUG) { this.logger.log("Registered queued command: " + commandHandler.command()); }
            return;
        }

        final List<String> indexer = Arrays.asList(list);
        final int index = indexer.indexOf(registered.getCommandHandler().command());
        StringBuilder s1 = new StringBuilder(list[index + 1]);
        final DefaultChildCommand dummyChild = new DefaultChildCommand(s1.toString());

        if (parentCommand.getClass().equals(registered.getClass())) {
            if (!registered.getCommandHandler().command().equals(list[list.length - 2 <= 0 ? 0 : list.length - 2])) {
                dummyChild.setPermission(registered.getPermission());
                registered.addChild(s1.toString(), dummyChild);
                registered.getChild(s1.toString()).setHandler(new DefaultHandler(null));
                if (DEBUG) {
                    this.logger.log("Generated and Registered DummyChild: " + s1.substring(0, s1.length() - 1));
                }
                return;
            }
            registered.addChild(s, child);
            registered.getChild(s).setHandler(new DefaultHandler(queue));
            if (DEBUG) { this.logger.log("Registered queued command: " + commandHandler.command()); }
        }
        else if (parentCommand.getClass().equals(DefaultChildCommand.class)) {
            final DefaultChildCommand defChildCmd = (DefaultChildCommand) parentCommand;
            if (!defChildCmd.getCommand().equals(list[list.length - 2 <= 0 ? 0 : list.length - 2])) {
                dummyChild.setPermission(defChildCmd.getPermission());
                defChildCmd.addChild(s1.toString(), dummyChild);
                defChildCmd.getChild(s1.toString()).setHandler(new DefaultHandler(null));
                if (DEBUG) {
                    this.logger.log("Generated and Registered DummyChild: " + s1.substring(0, s1.length() - 1));
                }
                return;
            }
            defChildCmd.addChild(s, child);
            defChildCmd.getChild(s).setHandler(new DefaultHandler(queue));
            if (DEBUG) { this.logger.log("Registered queued command: " + commandHandler.command()); }
        }
        else {
            final ChildCommand childCmd = (ChildCommand) parentCommand;
            if (!childCmd.getCommand().equals(list[list.length - 2 <= 0 ? 0 : list.length - 2])) {
                dummyChild.setPermission(childCmd.getPermission());
                childCmd.addChild(s1.toString(), dummyChild);
                childCmd.getChild(s1.toString()).setHandler(new DefaultHandler(null));
                if (DEBUG) {
                    this.logger.log("Generated and Registered DummyChild: " + s1.substring(0, s1.length() - 1));
                }
                return;
            }
            childCmd.addChild(s, child);
            childCmd.getChild(s).setHandler(new DefaultHandler(queue));
            if (DEBUG) { this.logger.log("Registered queued command: " + commandHandler.command()); }
        }

        registerChild(queue, commandHandler, registered, s, isAlias);
        s1 = new StringBuilder();
        for (final String s2 : indexer) {
            if (indexer.indexOf(s2) < index + 1) {
                s1.append(s2).append(".");
            }
        }
    }

    private static ParentCommand recursivelyFindInnerMostParent(final String command, final ParentCommand parentCommand,
                                                                int start) {
        final String[] list = command.split("\\.");
        if (start > list.length - 1) {
            return parentCommand;
        }
        if (parentCommand.hasChild(list[start])) {
            return recursivelyFindInnerMostParent(command, parentCommand.getChild(list[start]), ++start);
        }
        else {
            return parentCommand;
        }
    }

    /**
     * This is here for legacy reasons. Wont do anything upon runtime.
     *
     * @deprecated Use {@link #registerCommands() } instead as it will load
     * find all classes that has {@link CommandListener} implemented.
     */
    @Deprecated
    public void registerCommands(final Object classObject) { }

    private void registerCommands(final Class<?> clazz) {
        if (!CommandListener.class.isAssignableFrom(clazz)) {
            this.logger.log("Failed to register command, CommandListener.class is not assignable from " + clazz);
            return;
        }
        for (final Method method : clazz.getDeclaredMethods()) {
            if (DEBUG) { this.logger.log("Testing if method '" + method.getName() + "' is a CommandHandler"); }
            final CommandHandler commandHandler = method.getAnnotation(CommandHandler.class);
            if (commandHandler == null || !method.getParameterTypes()[0].equals(CommandInfo.class)) {
                continue;
            }
            if (DEBUG) { this.logger.log("Method '" + method.getName() + "' is a CommandHandler"); }
            Object object = clazz;
            if (Modifier.isStatic(method.getModifiers())) {
                object = null;
            }
            if (commandHandler.command().contains(".")) {
                queueCommand(object, method, commandHandler);
            }
            else {
                registerBaseCommand(object, method, commandHandler);
            }
        }
        processQueuedCommands();
    }

    private void registerBaseCommand(final Object classObject, final Method method,
                                     final CommandHandler commandHandler) {
        if (DEBUG) { this.logger.log("Registering Base Command: " + commandHandler.command()); }
        final QueuedCommand queue = new QueuedCommand(classObject, method);
        final RegisteredCommand registered = new RegisteredCommand(queue);
        synchronized (this.registeredCommands) {
            this.registeredCommands.put(commandHandler.command(), registered);
        }
        final AbstractCommand abstractCmd = new AbstractCommand(commandHandler.command());
        abstractCmd.setAliases(Arrays.asList(commandHandler.aliases()));
        abstractCmd.setDescription(commandHandler.description());
        abstractCmd.setPermission(commandHandler.permission());
        abstractCmd.setPermissionMessage(commandHandler.noPermission());
        abstractCmd.setUsage(commandHandler.usage());
        abstractCmd.executor = registered;
        registerBaseCommand(abstractCmd);
    }

    private void registerBaseCommand(final AbstractCommand command) {
        this.logger.log("Registering command '" + command.getName() + '\'');
        if (getCommandMap().getCommand(command.getName()) == null) {
            getCommandMap().register(this.plugin.getName(), command);
        }
    }

    private void queueCommand(final Object classObject, final Method method, final CommandHandler commandHandler) {
        synchronized (this.queuedCommands) {
            if (DEBUG) { this.logger.log("Queueing Command: " + commandHandler.command()); }
            final QueuedCommand queue = new QueuedCommand(classObject, method);
            final int numberOfChildren = commandHandler.command().split("\\.").length - 1;
            List<QueuedCommand> queueList = this.queuedCommands.get(numberOfChildren);
            if (queueList == null) {
                queueList = new LinkedList<>();
            }
            if (!queueList.contains(queue)) {
                queueList.add(queue);
            }
            this.queuedCommands.put(numberOfChildren, queueList);
        }
    }

    private CommandMap getCommandMap() {
        if (this.commandMap == null) {
            if (this.plugin.getServer().getPluginManager() instanceof SimplePluginManager) {
                try {
                    final Object field =
                        ReflectionUtils.getField(this.plugin.getServer().getPluginManager(), "commandMap");
                    this.commandMap = (SimpleCommandMap) field;
                } catch (final NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return this.commandMap;
    }

    public Plugin getPlugin() {
        return this.plugin;
    }
    public static CommandManager getInstance() {
        return instance;
    }
    public Map<String, RegisteredCommand> getRegisteredCommands() {
        return this.registeredCommands;
    }
}
