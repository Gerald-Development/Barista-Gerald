package main.java.de.voidtech.gerald.util;

import java.util.function.Predicate;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class RAESameUserPredicate implements Predicate<MessageReactionAddEvent> {

	private final User originalUser;
	
	public RAESameUserPredicate(User user) {
		originalUser = user;
	}
	
	@Override
	public boolean test(MessageReactionAddEvent t) {
		return t.getUser().getId().equals(originalUser.getId());
	}
}