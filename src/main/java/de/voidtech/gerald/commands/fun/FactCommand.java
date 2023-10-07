package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.service.HttpClientService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Command
public class FactCommand extends AbstractCommand {

    private static final String REQUEST_URL = "https://uselessfacts.jsph.pl/random.json?language=en";
    @Autowired
    private HttpClientService httpClientService;

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        String factOpt = httpClientService.getAndReturnJson(REQUEST_URL).getString("text");
        if (factOpt != null) context.reply(factOpt);
    }

    @Override
    public String getDescription() {
        return "slaps some random facts on the table";
    }

    @Override
    public String getUsage() {
        return "fact";
    }

    @Override
    public String getName() {
        return "fact";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.FUN;
    }

    @Override
    public boolean isDMCapable() {
        return true;
    }

    @Override
    public boolean requiresArguments() {
        return false;
    }

    @Override
    public String[] getCommandAliases() {
        return new String[]{"uselessfact"};
    }

    @Override
    public boolean canBeDisabled() {
        return true;
    }

    @Override
    public boolean isSlashCompatible() {
        return true;
    }

}
