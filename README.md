CommandAPI 
==========
__About:__ This is a CommandAPI that I developed so that I can avoid having to statically register any command whether using reflection and a CommandExecutor, or simply putting the commands into my plugin.yml. This is a purely annotation based API.  The functionality to statically add commands is there, I just have not implemented any ways that are easy for a user to do so. 

__How to use:__ Using this CommandAPI is super simple, and only requires 3 lines to register the commands, and obviously the commands themselves.

First in either your onEnable() or onLoad() you're going to want to do this:
```java
CommandManager commandManager = new CommandManager(this); //this == plugin instance
commandManager.registerCommands(); //registers commands from anywhere in the plugin jar
commandManager.registerHelp(); //registers a generated helptopic to bukkit
//so the /help PluginName displays our plugin's registered commands
```

Example commands to be registered: Here are some test commands to display how commands should be written to allow registration.  CommandListener is a required interface for any class you wish commands to be registered from.  This is to allow shrinkage of classes searched for commands, and increase registration time.
```java
public class TestCommand implements CommandListener //CommandListener is required
{

    /*
    command is the only required field for the annotation

    The base command is not required. If not created, a default one will be generated and will direct to the usage
    upon command use
    
    Do not register the command in plugin.yml as it is all handled by this api
     */
    @CommandHandler(command = "test")
    public static void testingCommand(CommandInfo info)
    {
        info.getSender().sendMessage("Test worked");
    }

    /*
        A dot in the command string marks this as a sub command. It can go infinitely deep.
        
     */
       @CommandHandler(command = "test.test2", permission = "test.test2", noPermission = "LOL no permissions",
                    aliases = {"2", "testing"}, usage = "/test test2 <player>",
                    description = "Testing out all of the CommandHandler's attribute values")
    public static void testingCommand2(CommandInfo info)
    {
        info.getSender().sendMessage("Test2 worked");
    }
}
```

__Finally:__ Please leave any comments, suggestions, and/or bugs you may find while using this CommandAPI.
