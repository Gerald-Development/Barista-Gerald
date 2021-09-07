package main.java.de.voidtech.gerald.listeners;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.service.CommandService;
import main.java.de.voidtech.gerald.util.CustomCollectors;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class SlashCommandListener implements EventListener {

    @Autowired
    private CommandService commandService;

    @Autowired
    private List<AbstractCommand> commands;

    private static final Logger LOGGER = Logger.getLogger(SlashCommandListener.class.getName());

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        //TODO: (from: Franziska): This whole class is VERY WorkInProgress. Not at all finalized, only for testing purposes
        if (event instanceof SlashCommandEvent) {
            SlashCommandEvent slashCommandEvent = (SlashCommandEvent) event;

            List<Member> mentionedMembers = slashCommandEvent
                    .getOptions()
                    .stream()
                    .filter(optionMapping -> optionMapping.getType().equals(OptionType.USER))
                    .map(OptionMapping::getAsMember)
                    .collect(Collectors.toList());

            CommandContext context = new CommandContext.CommandContextBuilder(true)
                    .slashCommandEvent(slashCommandEvent)
                    .channel(slashCommandEvent.getChannel())
                    .member(slashCommandEvent.getMember())
                    .mentionedMembers(mentionedMembers)
                    .build();


            AbstractCommand commandOpt = commands.stream()
                    .filter(command -> command.getName().equals(slashCommandEvent.getName()))
                    .collect(CustomCollectors.toSingleton());

            if (commandOpt == null) {
                LOGGER.log(Level.INFO, "Command not found: " + slashCommandEvent.getName());
                return;
            }

            commandService.handleCommand(commandOpt, context);
        }
    }
}
