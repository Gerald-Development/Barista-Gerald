package main.java.de.voidtech.gerald.listeners;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;

@Component
public class SlashCommandListener implements EventListener {

    //@Autowired
    //private CommandService commandService;

    //@Autowired
    //private List<AbstractCommand> commands;

    //private static final Logger LOGGER = Logger.getLogger(SlashCommandListener.class.getName());

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        
    	
    	//TODO: (from: Franziska): This whole class is VERY WorkInProgress. Not at all finalized, only for testing purposes
    	
    	//TODO (from Seb): I don't have the human will to do slash commands yet. I will get the commandContext working properly and
    	//we can tell waterloo it is in development.
    	
        
    	//if (event instanceof SlashCommandEvent) {
            //SlashCommandEvent slashCommandEvent = (SlashCommandEvent) event;
    	
            //List<Member> mentionedMembers = slashCommandEvent
                    //.getOptions()
                    //.stream()
                    //.filter(optionMapping -> optionMapping.getType().equals(OptionType.USER))
                    //.map(OptionMapping::getAsMember)
                    //.collect(Collectors.toList());

              //CommandContext context = new CommandContext.CommandContextBuilder(true)
                    //.slashCommandEvent(slashCommandEvent)
                    //.channel(slashCommandEvent.getChannel())
                    //.member(slashCommandEvent.getMember())
                    //.mentionedMembers(mentionedMembers)
                    //.privateMessage(slashCommandEvent.getChannel().getType().equals(ChannelType.PRIVATE))
                    //.user(slashCommandEvent.getUser())
                    //.build();


            //AbstractCommand commandOpt = commands.stream()
                    //.filter(command -> command.getName().equals(slashCommandEvent.getName()))
                    //.collect(CustomCollectors.toSingleton());

            //if (commandOpt == null) {
                //LOGGER.log(Level.INFO, "Command not found: " + slashCommandEvent.getName());
                //return;
            //}

            //commandService.handleCommand(commandOpt, context);
        //}
    }
}
