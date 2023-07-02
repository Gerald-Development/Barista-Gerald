package main.java.de.voidtech.gerald.util;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.function.Predicate;

public class MRESameUserPredicate implements Predicate<MessageReceivedEvent> {

	private final User originalUser;
	
	public MRESameUserPredicate(User user) {
		originalUser = user;
	}
	
	@Override
	public boolean test(MessageReceivedEvent t) {
		return t.getAuthor().getId().equals(originalUser.getId());
	}
}