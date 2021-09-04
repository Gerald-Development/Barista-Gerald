package main.java.de.voidtech.gerald.util;

import java.util.function.Predicate;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

public class BCESameUserPredicate implements Predicate<ButtonClickEvent> {

	private final Member originalUser;
	
	public BCESameUserPredicate(Member member) {
		originalUser = member;
	}
	
	@Override
	public boolean test(ButtonClickEvent t) {
		return t.getMember().getId().equals(originalUser.getId());
	}
}