CommandAPI 
==========
## About
This is a CommandAPI that I developed so that I can avoid having to statically register any command whether using reflection and a CommandExecutor, or simply putting the commands into my plugin.yml. This is a purely annotation based API.  The functionality to statically add commands is there, I just have not implemented any ways that are easy for a user to do so.

## Maven

```
<repository>
    <id>CommandAPI</id>
    <url>https://raw.github.com/kh498/CommandAPI/mvn-repo/</url>
    <snapshots>
        <enabled>true</enabled>
        <updatePolicy>always</updatePolicy>
    </snapshots>
</repository>
```

```
<dependency>
    <groupId>com.not2excel.api</groupId>
    <artifactId>CommandAPI</artifactId>
    <version>2.0</version>
</dependency>
```

## How to use
Using this CommandAPI is super simple, and requires minimum 3 lines to register the commands, and obviously the commands themselves.

First in either your onEnable() or onLoad() you're going to want to do this:
```java
final CommandManager commandManager = new CommandManager(plugin); //where plugin is a plugin instance
// Automatically finds all classes that implements the CommandListener.class and registers their commands
commandManager.registerCommands();
//registers a generated help topic to bukkit
commandManager.registerHelp();
//so the /help PluginName displays our plugin's registered commands
```

## attribute values explained

__command__: _(String)_ This is the name of the command. eg command /test will have _command = "test"_

__aliases__: _(String[], default: {})_ A list of aliases for this command

__permission__: _(String, default: "")_ The permission a players need to execute the command

__noPermission__: _(String, default: "You don't have permission to do that.")_ The string displayed when the player doesn't have the permission for this command

__usage__: _(String, default: "")_ The arbitrary arguments of the command

__description__: _(String, default: "")_ A description of what the command does

__min__: _(int, default: 0)_ The minimum number of arguments this command can have

__max__: _(int, default: -1)_ The maximum number og arguments this command can have

__playerOnly__: _(boolean, default: false)_ If only players can excecute this command

__flags__: _(String, default: "")_ The flags of this command (see flags example below)

__strictArgs__: _(boolean, default: false)_ If only known subcommand are allowed as arguments (see first example below)

__flagDesc__: _(String[], default: {})_ The description of what each flag does (see flags example below)


## Example commands
Example commands to be registered: Here are some test commands to display how commands should be written to allow registration.  CommandListener is a required interface for any class you wish commands to be registered from.  This is to allow shrinkage of classes searched for commands, and increase registration time.
A real example can be found [here](https://gist.github.com/kh498/45af9f07ec6884c259a84687c788786a)
```java
import com.not2excel.api.command.CommandHandler;
import com.not2excel.api.command.CommandListener;
import com.not2excel.api.command.objects.CommandInfo;

public class TestCommand implements CommandListener //CommandListener is required
{
    /*
     * command is the only required field for the annotation
     *
     * The base command is required (bug in 2.0). If you want the base command to display the
     * help screen when called without any arguments add the attribute values "strictArgs = true" and "max = 0"
     *
     * Do NOT register the command in plugin.yml as it is all handled by this API!
     */
    @CommandHandler(command = "test")
    public static void testingCommand(final CommandInfo info) {
        info.getSender().sendMessage("Test worked");
    }

    /*
     * A dot in the command string marks this as a sub command. It can go infinitely deep.
     * Do not have the command and/or subcommand in the usage, that is built in.
     */
    @CommandHandler(command = "test.test2",
                    permission = "test.test2",
                    noPermission = "No access!",
                    aliases = {"2", "testing"},
                    usage = "<player>",
                    flags = "f",
                    flagDesc = "Activate some feature",
                    description = "Testing out (almost) all of the CommandHandler's attribute values")
    public static void testingCommand2(final CommandInfo info) {
        info.getSender().sendMessage("Test2 worked");
    }

    /*
     * A flag is a single character such as {@code -f} that will alter the behaviour of the command. flags can only
     * be any english character (a-z and A-Z) including * as a catch all.
     *
     * Defines if there can be arbitrary variables. If set to true the command cannot have any unknown variables.
     * The arguments will either be a subcommand or a flag, if not an error is thrown. This means that flags are
     * ignored and can be used.
     *
     * It is suggested that this is set to true if you only want flags as arguments.
     */
    @CommandHandler(command = "test.reset",
                    flags = "kr",
                    flagDesc = {"-k resets kingdoms", "-r resets reficules"},
                    strictArgs = true,
                    // only allow flags as arguments
                    description = "resets stuff!")
    public static void testingCommand3(final CommandInfo info) {
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
