CommandAPI 
==========
__About:__ This is a CommandAPI that I developed so that I can avoid having to statically register any command whether using reflection and a CommandExecutor, or simply putting the commands into my plugin.yml. This is a purely annotation based API.  The functionality to statically add commands is there, I just have not implemented any ways that are easy for a user to do so. 

__How to use:__ Using this CommandAPI is super simple, and requires minimum 3 lines to register the commands, and obviously the commands themselves.

First in either your onEnable() or onLoad() you're going to want to do this:
```java
CommandManager commandManager = new CommandManager(this); //this == plugin instance
commandManager.registerCommands(testingCommand); //registers commands from anywhere in the plugin jar
commandManager.registerHelp(); //registers a generated helptopic to bukkit
//so the /help PluginName displays our plugin's registered commands
```

Example commands to be registered: Here are some test commands to display how commands should be written to allow registration.  CommandListener is a required interface for any class you wish commands to be registered from.  This is to allow shrinkage of classes searched for commands, and increase registration time.
A real example can be found [here](https://gist.github.com/kh498/45af9f07ec6884c259a84687c788786a)
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
         Do not have the command and/or subcommand in the usage, that is built in.
         */
           @CommandHandler(command = "test.test2", permission = "test.test2", noPermission = "No access!",
                        aliases = {"2", "testing"}, usage = "<player>",
                        description = "Testing out all of the CommandHandler's attribute values")
        public static void testingCommand2(CommandInfo info)
        {
            info.getSender().sendMessage("Test2 worked");
        }
        
        /*
         A flag is a single character such as {@code -f} that will alter the behaviour of the command. flags can only
          be any english character (a-z and A-Z) including * as a catch all.
         
         Defines if there can be arbitrary variables. If set to true the command cannot have any unknown variables.
         The arguments will either be a subcommand or a flag, if not an error is thrown. This means that flags are
         ignored and can be used.
     
         It is suggested that this is set to true if you only want flags as arguments.
         */
        @CommandHandler(command = "test.reset",
                        flags = "kr",
                        flagDesc = {"-k resets kingdoms", "-r resets reficules"},
                        strictArgs = true,
                        description = "resets stuff!")
        public static void testingCommand2(CommandInfo info)
        {
            //user gave the argument -f or -*
            if (info.hasFlag('k')) {
                // Do some resetting
            }
            //returns true if one of the chars in the input string matches one of the flags the user gave
            if (info.hasOneOfFlags("kr")) {
                // reload or something
            }
        }
    }
```

__Finally:__ Please leave any comments, suggestions, and/or bugs you may find while using this CommandAPI.
