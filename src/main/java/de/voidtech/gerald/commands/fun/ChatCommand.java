package main.java.de.voidtech.gerald.commands.fun;

import main.java.de.voidtech.gerald.GlobalConstants;
import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.entities.ChatChannel;
import main.java.de.voidtech.gerald.service.ChatbotService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;

@Command
public class ChatCommand extends AbstractCommand{
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private ChatbotService chatBot;
	
	private boolean chatChannelEnabled(String channelID) {
		try(Session session = sessionFactory.openSession())
		{
			ChatChannel channel = (ChatChannel) session.createQuery("FROM ChatChannel WHERE ChannelID = :channelID")
                    .setParameter("channelID", channelID)
                    .uniqueResult();
			return channel != null;
		}
	}
	
	private void enableChatChannel(String channelID) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			
			ChatChannel channel = new ChatChannel(channelID);
			
			session.saveOrUpdate(channel);
			session.getTransaction().commit();
		}
	}
	
	private void disableChatChannel(String channelID) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			session.createQuery("DELETE FROM ChatChannel WHERE ChannelID = :channelID")
				.setParameter("channelID", channelID)
				.executeUpdate();
			session.getTransaction().commit();
		}
	}
	
	private void enableChannelCheckpoint(CommandContext context) {
		if (context.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
			if (chatChannelEnabled(context.getChannel().getId())) {
				context.getChannel().sendMessage("**GeraldAI is already enabled here!**").queue();
			} else {
				enableChatChannel(context.getChannel().getId());
				context.getChannel().sendMessage("**GeraldAI has been enabled! He will now automatically reply to your messages.**").queue();
			}	
		}
	}
	
	private void disableChannelCheckpoint(CommandContext context) {
		if (context.getMember().hasPermission(Permission.MANAGE_CHANNEL)) {
			if (chatChannelEnabled(context.getChannel().getId())) {
				disableChatChannel(context.getChannel().getId());
				context.getChannel().sendMessage("**GeraldAI has been disabled! He will no longer automatically reply to your messages.**").queue();
			} else {
				context.getChannel().sendMessage("**GeraldAI is already disabled!**").queue();
			}
		}
	}
	private void sendHparams(CommandContext context) {
		JSONObject hparams = chatBot.getHparams();
		if (hparams.toMap().containsKey("Error")) 
		{
			String reply = hparams.getString("Error");
			context.getChannel().sendMessage(reply).queue();
		}
		else {
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
			context.getChannel().sendMessageEmbeds(reply).queue();
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
				context.getChannel().sendMessage(reply).queue();
				break;
		}
		
	}

	@Override
	public String getDescription() {
		return "This command allows you to talk to our Chat AI! (Powered by Gavin) You will need the Manage Channels permission to set up a channel!";
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
		return new String[]{"ai", "geraldai", "geraldchat", "gavin"};
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}

}
