package main.java.de.voidtech.gerald.commands.info;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;

@Command
public class RoutineCommand extends AbstractCommand {
    @Autowired
    private List<AbstractRoutine> routines;

    @Override
    public void executeInternal(Message message, List<String> args) {
        long routineCount = routines.size();

        EmbedBuilder routineInformation = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("Barista Gerald - Routines")
                .setThumbnail(message.getJDA().getSelfUser().getAvatarUrl())
                .setFooter("Routine Count: "+ routineCount + " | Note: Some routines cannot be disabled. The commands they power require them to function. Try disabling the command instead!");
        for (AbstractRoutine routine: routines) {
            routineInformation.addField(routine.getName(), String.format("```Description: %s\nCan be disabled: %s```", routine.getDescription(), routine.canBeDisabled()), false);
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
        return CommandCategory.INFO;
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
