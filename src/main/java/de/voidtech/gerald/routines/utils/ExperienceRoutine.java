package main.java.de.voidtech.gerald.routines.utils;

import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Routine;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.routines.RoutineCategory;
import main.java.de.voidtech.gerald.service.ExperienceService;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;

@Routine
public class ExperienceRoutine extends AbstractRoutine {

	@Autowired
	private ExperienceService xpService;
	
	@Override
	public void executeInternal(Message message) {
		if (message.getChannel().getType().equals(ChannelType.PRIVATE)) return;
		xpService.updateUserExperience(message.getAuthor().getId(), message.getGuild().getId(), message.getChannel().getId());
	}

	@Override
	public String getName() {
		return "r-xp";
	}

	@Override
	public String getDescription() {
		return "Allows Gerald to update user experience and levels";
	}

	@Override
	public RoutineCategory getRoutineCategory() {
		return RoutineCategory.UTILS;
	}

	@Override
	public boolean allowsBotResponses() {
		return false;
	}

	@Override
	public boolean canBeDisabled() {
		return true;
	}

}
