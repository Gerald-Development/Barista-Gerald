package main.java.de.voidtech.gerald.commands.management;

import java.awt.Color;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

@Command
public class RoutineCommand extends AbstractCommand {
    
	@Autowired
    private List<AbstractRoutine> routines;
    
	@Autowired
	private ServerService serverService;

    @Override
    public void executeInternal(Message message, List<String> args) {
        long routineCount = routines.size();
        Server server = serverService.getServer(message.getGuild().getId());

        EmbedBuilder routineInformation = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("Barista Gerald - Routines")
                .setThumbnail(message.getJDA().getSelfUser().getAvatarUrl())
                .setFooter("Routine Count: "+ routineCount + " | Note: Some routines cannot be disabled. The commands they power require them to function. Try disabling the command instead!");
        for (AbstractRoutine routine: routines) {
            routineInformation.addField(routine.getName(), String.format("Description: %s\nCan be disabled: %s\nIs disabled Here: %s",
            		routine.getDescription(),
            		routine.canBeDisabled() ? ":white_check_mark:" : ":x:",
            		server.getRoutineBlacklist().contains(routine.getName()) ? ":white_check_mark:" : ":x:"),
            		false);
        }
        message.getChannel().sendMessage(routineInformation.build()).queue();
    }

    @Override
    public String getDescription() {
        return "List all routines";
    }

    @Override
    public String getUsage() {
        return "routine";
    }

    @Override
    public String getName() {
        return "routine";
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
        String[] aliases = {"routines", "r"};
        return aliases;
    }
    
	@Override
	public boolean canBeDisabled() {
		return false;
	}
}
