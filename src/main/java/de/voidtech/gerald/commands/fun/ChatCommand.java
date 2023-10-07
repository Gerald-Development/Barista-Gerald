package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.GlobalConstants;
import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.persistence.entity.ChatChannel;
import main.java.de.voidtech.gerald.persistence.repository.ChatChannelRepository;
import main.java.de.voidtech.gerald.service.ChatbotService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
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

    private void sendHparams(CommandContext context) {
        JSONObject hparams = chatBot.getHparams();
        if (hparams.toMap().containsKey("Error")) {
            String reply = hparams.getString("Error");
            context.reply(reply);
        } else {
            String title = String.format("Hyper-Parameters for %s", chatBot.getModelName().getString("ModelName"));
            EmbedBuilder eb = new EmbedBuilder()
                    .setColor(Color.ORANGE)
                    .setTitle(title, GlobalConstants.LINKTREE_URL)
                    .addField("Number of Model Layers", String.valueOf(hparams.getInt("NUM_LAYERS")), false)
                    .addField("Number of Units", String.valueOf(hparams.getInt("UNITS")), false)
                    .addField("Dff", String.valueOf(hparams.getInt("D_MODEL")), false)
                    .addField("Number of Attention Heads", String.valueOf(hparams.getInt("NUM_HEADS")), false)
                    .addField("Layer Dropout", String.valueOf(hparams.getInt("DROPOUT")), false)
                    .addField("Maximum Sequence Length", String.valueOf(hparams.getInt("MAX_LENGTH")), false)
                    .addField("Vocabulary Size", hparams.getString("TOKENIZER"), false)
                    .addField("Using Mixed_Precision", String.valueOf(hparams.getBoolean("FLOAT16")), false)
                    .addField("Number of Epochs Trained For", String.valueOf(hparams.getInt("EPOCHS")), false)
                    .setThumbnail(context.getJDA().getSelfUser().getAvatarUrl())
                    .setFooter("Paper for reference to what these mean: https://arxiv.org/pdf/1706.03762.pdf");
            MessageEmbed reply = eb.build();
            context.reply(reply);
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
            case "hparams":
                sendHparams(context);
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
        return "This command allows you to talk to our custom-made chatbot Gavin! Brought to you by Scot_Survivor#2756";
    }

    @Override
    public String getUsage() {
        return "chat enable\n"
                + "chat disable\n"
                + "chat hparams\n"
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
