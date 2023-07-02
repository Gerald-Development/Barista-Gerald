package main.java.de.voidtech.gerald.util;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.Objects;
import java.util.function.Predicate;

public class RAESameUserPredicate implements Predicate<MessageReactionAddEvent> {

	private final User originalUser;
	
	public RAESameUserPredicate(User user) {
		originalUser = user;
	}
	
	@Override
	public boolean test(MessageReactionAddEvent t) {
		return t.getEmoji().getType().equals(Emoji.Type.UNICODE) && t.getUser().getId().equals(originalUser.getId());
	}
}