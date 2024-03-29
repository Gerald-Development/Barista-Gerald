package main.java.de.voidtech.gerald.commands.management;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.persistence.entity.Server;
import main.java.de.voidtech.gerald.service.GeraldConfigService;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.Permission;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Command
public class PrefixCommand extends AbstractCommand {

    @Autowired
    private ServerService serverService;

    @Autowired
    private GeraldConfigService config;

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        if (context.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            Server server = serverService.getServer(context.getGuild().getId());

            if (args.size() > 0) {
                String prefix = args.get(0);

                server.setPrefix(prefix);
                serverService.saveServer(server);

                context.reply(String.format("Prefix was changed to `%s`", prefix));
            } else {
                server.setPrefix(config.getDefaultPrefix());
                serverService.saveServer(server);
                context.reply(String.format("Prefix has been reset to `%s`", config.getDefaultPrefix()));
            }
        }
    }

    @Override
    public String getDescription() {
        return "Set the prefix of Gerald!";
    }

    @Override
    public String getUsage() {
        return "prefix %";
    }

    @Override
    public String getName() {
        return "prefix";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.MANAGEMENT;
    }

    @Override
    public boolean isDMCapable() {
        return false;
    }

    @Override
    public boolean requiresArguments() {
        return false;
    }

    @Override
    public String[] getCommandAliases() {
        return new String[0];
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }

    @Override
    public boolean isSlashCompatible() {
        return true;
    }

}
