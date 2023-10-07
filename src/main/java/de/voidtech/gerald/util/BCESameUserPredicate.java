package main.java.de.voidtech.gerald.util;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.function.Predicate;

public class BCESameUserPredicate implements Predicate<ButtonInteractionEvent> {

    private final Member originalUser;

    public BCESameUserPredicate(Member member) {
        originalUser = member;
    }

    @Override
    public boolean test(ButtonInteractionEvent t) {
        return t.getMember().getId().equals(originalUser.getId());
    }
}