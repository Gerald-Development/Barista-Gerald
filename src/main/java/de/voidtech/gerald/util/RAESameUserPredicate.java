package main.java.de.voidtech.gerald.util;

import java.util.function.Predicate;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

public abstract class RAESameUserPredicate implements Predicate<GuildMessageReactionAddEvent> {

	private User originalUser;
	
	public RAESameUserPredicate(User user) {
		originalUser = user;
	}
	
	@Override
	public boolean test(GuildMessageReactionAddEvent t) {
		return t.getUser().getId().equals(originalUser.getId());
	}
}