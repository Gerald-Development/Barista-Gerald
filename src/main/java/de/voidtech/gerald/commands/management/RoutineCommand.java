package main.java.de.voidtech.gerald.commands.management;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;

@Command
public class RoutineCommand extends AbstractCommand {
    
	@Autowired
    private List<AbstractRoutine> routines;
    
	@Autowired
	private ServerService serverService;
	
	private static final String TRUE_EMOTE = "\u2705";
	private static final String FALSE_EMOTE = "\u274C";

    @Override
    public void executeInternal(CommandContext context, List<String> args) {
        long routineCount = routines.size();
        Server server = serverService.getServer(context.getGuild().getId());

        EmbedBuilder routineInformation = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("Barista Gerald - Routines")
                .setThumbnail(context.getJDA().getSelfUser().getAvatarUrl())
                .setFooter("Routine Count: "+ routineCount + " | Note: Some routines cannot be disabled. The commands they power require them to function. Try disabling the command instead!");
        
        for (AbstractRoutine routine: routines) {
            routineInformation.addField(routine.getName(), String.format("```Description: %s\nCan be disabled:  %s\nIs disabled Here: %s```",
            		routine.getDescription(),
            		routine.canBeDisabled() ? TRUE_EMOTE : FALSE_EMOTE,
            		server.getRoutineBlacklist().contains(routine.getName()) ? TRUE_EMOTE : FALSE_EMOTE),
            		false);
        }
        context.reply(routineInformation.build());
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
        return new String[]{"routines", "r"};
    }
    
	@Override
	public boolean canBeDisabled() {
		return false;
	}
}
