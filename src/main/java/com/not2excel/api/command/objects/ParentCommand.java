package com.not2excel.api.command.objects;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Richmond Steele
 * @since 12/18/13
 * All rights Reserved
 * Please read included LICENSE file
 */
public class ParentCommand {
    private final Map<String, ChildCommand> childCommands = new ConcurrentHashMap<>();

    public void addChild(final String s, final ChildCommand child) {
        {
            synchronized (this.childCommands) {
                this.childCommands.put(s.toLowerCase(), child);
            }
        }
    }

    public boolean hasChild(final String s) {
        synchronized (this.childCommands) {
            return this.childCommands.containsKey(s.toLowerCase());

        }
    }

    public ChildCommand getChild(final String s) {
        synchronized (this.childCommands) {
            return this.childCommands.get(s.toLowerCase());
        }
    }

    public Map<String, ChildCommand> getChildCommands() {
        return this.childCommands;
    }
}
