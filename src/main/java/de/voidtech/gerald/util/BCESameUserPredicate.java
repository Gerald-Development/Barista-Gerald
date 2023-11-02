package main.java.de.voidtech.gerald.util;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.function.Predicate;

public class BCESameUserPredicate implements Predicate<ButtonInteractionEvent> {

    private final User originalUser;

    public BCESameUserPredicate(User user) {
        originalUser = user;
    }

    @Override
    public boolean test(ButtonInteractionEvent t) {
        return t.getUser().getId().equals(originalUser.getId());
    }
}