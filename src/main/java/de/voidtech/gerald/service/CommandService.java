package main.java.de.voidtech.gerald.service;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.util.CustomCollectors;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class CommandService
{
    private static final Logger LOGGER = Logger.getLogger(CommandService.class.getName());

    private static final int LEVENSHTEIN_THRESHOLD = 1;

    @Autowired
    private GeraldConfig config;

    @Autowired
    private ServerService serverService;

    @Autowired
    private List<AbstractCommand> commands;

    @Autowired
    private List<AbstractRoutine> routines;

    @Autowired
    private LevenshteinService levenshteinService;

    public final HashMap<String, String> aliases = new HashMap<>();

    public void handleChatCommandOnDemand(Message message) {
        String prefix = getPrefix(message);

        if (!shouldHandleAsChatCommand(prefix, message)) return;

        String messageContent = message.getContentRaw().substring(prefix.length());
        List<String> messageArray = Arrays.asList(messageContent.trim().split("\\s+"));

        CommandContext cmdContext = new CommandContext.CommandContextBuilder(false)
                .channel(message.getChannel())
                .mentionedRoles(message.getMentionedRoles())
                .mentionedChannels(message.getMentionedChannels())
                .mentionedMembers(message.getMentionedMembers())
                .args(messageArray.subList(1, messageArray.size()))
                .member(message.getMember())
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

    private void tryLevenshteinOptions(Message message, String commandName) {
        List<String> possibleOptions = new ArrayList<>();
        possibleOptions = commands.stream()
                .map(AbstractCommand::getName)
                .filter(name -> levenshteinService.calculate(commandName, name) <= LEVENSHTEIN_THRESHOLD)
                .collect(Collectors.toList());
        if (!possibleOptions.isEmpty())
            message.getChannel().sendMessageEmbeds(levenshteinService.createLevenshteinEmbed(possibleOptions)).queue();
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
