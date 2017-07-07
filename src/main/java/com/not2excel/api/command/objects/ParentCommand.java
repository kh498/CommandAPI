package com.not2excel.api.command.objects;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Richmond Steele
 * @since 12/18/13
 * All rights Reserved
 * Please read included LICENSE file
 */
@SuppressWarnings("WeakerAccess")
public class ParentCommand {
    private final Map<String, ChildCommand> childCommands = new ConcurrentHashMap<>();
    private ChildCommand parentAsChild;
    private final Map<String, ChildCommand> childBaseCommands = new ConcurrentHashMap<>();


    /**
     * @param subCommand The subcommand to be added
     * @param child      The child of this parent
     */
    public void addChild(final String subCommand, final ChildCommand child) {
        {
            synchronized (this.childCommands) {
                this.childCommands.put(subCommand.toLowerCase(), child);
            }
        }
    }

    /**
     * @param childStr key whose presence in this map is to be tested, case insensitive
     *
     * @return true if this map contains a mapping for the specified child
     */
    public boolean hasChild(final String childStr) {
        synchronized (this.childCommands) {
            return this.childCommands.containsKey(childStr.toLowerCase());
        }
    }

    /**
     * @param childStr the child whose associated value is to be returned, case insensitive
     *
     * @return the value to which the specified key is mapped, or null if this map contains no mapping for the child
     */
    public ChildCommand getChild(final String childStr) {
        synchronized (this.childCommands) {
            return this.childCommands.get(childStr.toLowerCase());
        }
    }

    /**
     * All the childcommands, no filter if they are an alias or not
     *
     * @return All this parents child commands, the key is the subCommand and the value is the ChildCommand object
     */
    public Map<String, ChildCommand> getAllChildCommands() {
        return this.childCommands;
    }

    /**
     * Only return the basecommands of this parents child command
     *
     * @return This base parents child commands, the key is the subCommand and the value is the ChildCommand object
     */
    public Map<String, ChildCommand> getNoAliasesChildCommands() {
        if (this.childBaseCommands.isEmpty() && !this.childCommands.isEmpty()) {
            for (final Map.Entry<String, ChildCommand> entry : this.childCommands.entrySet()) {
                if (!entry.getValue().isAlias()) {
                    this.childBaseCommands.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return this.childBaseCommands;
    }

    public ChildCommand getParentAsChild() {
        return this.parentAsChild;
    }

    void setParentAsChild(final ChildCommand parentAsChild) {
        //noinspection VariableNotUsedInsideIf
        if (this.parentAsChild != null) {
            throw new IllegalArgumentException("the child has already been set!");
        }
        this.parentAsChild = parentAsChild;
    }
}
