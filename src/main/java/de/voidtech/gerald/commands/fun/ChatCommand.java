package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.persistence.entity.ChatChannel;
import main.java.de.voidtech.gerald.persistence.repository.ChatChannelRepository;
import main.java.de.voidtech.gerald.service.ChatbotService;
import net.dv8tion.jda.api.Permission;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Command
public class ChatCommand extends AbstractCommand {

    @Autowired
    private ChatbotService chatBot;

    @Autowired
    private ChatChannelRepository repository;

    private boolean chatChannelEnabled(String channelID) {
        return repository.getChatChannelByChannelId(channelID) != null;
    }

    private void enableChatChannel(String channelID) {
        repository.save(new ChatChannel(channelID));
    }

    private void disableChatChannel(String channelID) {
        repository.deleteChatChannelByChannelId(channelID);
    }

    private void enableChannelCheckpoint(CommandContext context) {
        if (context.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            if (chatChannelEnabled(context.getChannel().getId())) {
                context.reply("**GeraldAI is already enabled here!**");
            } else {
                enableChatChannel(context.getChannel().getId());
                context.reply("**GeraldAI has been enabled! He will now automatically reply to your messages.**");
            }
        }
    }

    private void disableChannelCheckpoint(CommandContext context) {
        if (context.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
            if (chatChannelEnabled(context.getChannel().getId())) {
                disableChatChannel(context.getChannel().getId());
                context.reply("**GeraldAI has been disabled! He will no longer automatically reply to your messages.**");
            } else {
                context.reply("**GeraldAI is already disabled!**");
            }
        }
    }

    @Override
    public void executeInternal(CommandContext context, List<String> args) {

        switch (args.get(0)) {
            case "enable":
                enableChannelCheckpoint(context);
                break;
            case "disable":
                disableChannelCheckpoint(context);
                break;
            default:
                context.getChannel().sendTyping().queue();
                String reply = chatBot.getReply(String.join(" ", args), context.getGuild().getId());
                context.reply(reply);
                break;
        }

    }

    @Override
    public String getDescription() {
        return "This command allows you to talk to Gerald. You can also talk to him by pinging him";
    }

    @Override
    public String getUsage() {
        return "chat enable\n"
                + "chat disable\n"
                + "chat [a lovely message]";
    }

    @Override
    public String getName() {
        return "chat";
    }

    @Override
    public CommandCategory getCommandCategory() {
        return CommandCategory.FUN;
    }

    @Override
    public boolean isDMCapable() {
        return false;
    }

    @Override
    public boolean requiresArguments() {
        return true;
    }

    @Override
    public String[] getCommandAliases() {
        return new String[]{"ai", "talk", "gavin"};
    }

    @Override
    public boolean canBeDisabled() {
        return true;
    }

    @Override
    public boolean isSlashCompatible() {
        return true;
    }

}
