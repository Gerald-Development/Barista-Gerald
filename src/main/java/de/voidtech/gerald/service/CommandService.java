package main.java.de.voidtech.gerald.service;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.entities.GeraldLogger;
import main.java.de.voidtech.gerald.util.CustomCollectors;
import main.java.de.voidtech.gerald.util.LevenshteinCalculator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Service
public class CommandService
{
	private static final GeraldLogger LOGGER = LogService.GetLogger(CommandService.class.getSimpleName());

    private static final int LEVENSHTEIN_THRESHOLD = 1;

    @Autowired
    private GeraldConfig config;

    @Autowired
    private ServerService serverService;

    @Autowired
    private List<AbstractCommand> commands;

    public final HashMap<String, String> aliases = new HashMap<>();

    public void handleChatCommandOnDemand(Message message) {
        String prefix = getPrefix(message);

        if (!shouldHandleAsChatCommand(prefix, message)) return;

        String messageContent = message.getContentRaw().substring(prefix.length());
        List<String> messageArray = Arrays.asList(messageContent.trim().split("\\s+"));

        boolean isPrivateMessage = message.getChannel().getType().equals(ChannelType.PRIVATE);
        
        CommandContext cmdContext = new CommandContext.CommandContextBuilder(false)
                .channel(message.getChannel())
                .mentionedRoles(isPrivateMessage ? null : message.getMentionedRoles())
                .mentionedChannels(isPrivateMessage ? null : message.getMentionedChannels())
                .mentionedMembers(isPrivateMessage ? null : message.getMentionedMembers())
                .privateMessage(isPrivateMessage)
                .args(messageArray.subList(1, messageArray.size()))
                .member(message.getMember())
                .user(message.getAuthor())
                .message(message)
                .build();

        AbstractCommand commandOpt = commands.stream()
                .filter(command -> command.getName().equals(findCommand(messageArray.get(0))))
                .collect(CustomCollectors.toSingleton());

        if (commandOpt == null) {
            LOGGER.log(Level.INFO, "Command not found: " + messageArray.get(0));
            tryLevenshteinOptions(message, messageArray.get(0));
            return;
        }
            handleCommand(commandOpt, cmdContext);
        }

    public void handleCommand(AbstractCommand command, CommandContext context)
    {
        if (context.getChannel().getType() == ChannelType.PRIVATE && !command.isDMCapable()) {
            context.getChannel().sendMessage("**You can only use this command in guilds!**").queue();
            return;
        }

        command.run(context, context.getArgs());

        LOGGER.log(Level.INFO, "Command executed: " + command.getName() + " - From " + context.getAuthor().getAsTag() + "- ID: " + context.getAuthor().getId());
    }

    private String findCommand(String prompt) {
        String commandToBeFound = "";
        if (aliases.containsKey(prompt.toLowerCase())) {
            commandToBeFound = aliases.get(prompt.toLowerCase());
        } else {
            commandToBeFound = prompt.toLowerCase();
        }
        return commandToBeFound;
    }
    
	private MessageEmbed createLevenshteinEmbed(List<String> possibleOptions) {
		EmbedBuilder levenshteinResultEmbed = new EmbedBuilder()
				.setColor(Color.RED)
				.setTitle("I couldn't find that command! Did you mean `" + String.join("` or `", possibleOptions) + "`?");
		return levenshteinResultEmbed.build();
	}

    private void tryLevenshteinOptions(Message message, String commandName) {
        List<String> possibleOptions = new ArrayList<>();
        possibleOptions = commands.stream()
                .map(AbstractCommand::getName)
                .filter(name -> LevenshteinCalculator.calculate(commandName, name) <= LEVENSHTEIN_THRESHOLD)
                .collect(Collectors.toList());
        if (!possibleOptions.isEmpty())
            message.getChannel().sendMessageEmbeds(createLevenshteinEmbed(possibleOptions)).queue();
    }

    private boolean shouldHandleAsChatCommand(String prefix, Message message)
    {
        String messageRaw = message.getContentRaw();
        return messageRaw.startsWith(prefix) && messageRaw.length() > prefix.length();
    }

    private String getPrefix(Message message) {
        if (message.getChannelType() == ChannelType.PRIVATE) {
            return config.getDefaultPrefix();
        }
        String customPrefix = serverService.getServer(message.getGuild().getId()).getPrefix();

        if(customPrefix == null) return config.getDefaultPrefix();
        else return customPrefix;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadAliases() {
        for (AbstractCommand command: commands) {
            List<String> commandAliases = new ArrayList<>();
            if (command.getCommandAliases() != null)
                commandAliases = Arrays.asList(command.getCommandAliases());
            for (String alias: commandAliases) {
                aliases.put(alias, command.getName());
            }
        }
        LOGGER.log(Level.INFO, "Command aliases have been loaded");
    }
}
