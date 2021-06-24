package main.java.de.voidtech.gerald.commands.fun;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.entities.MemeBlocklist;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class MemeCommand extends AbstractCommand {
	
	private static final String API_URL = "https://fyouron-api.herokuapp.com/";
	private static final Logger LOGGER = Logger.getLogger(MemeCommand.class.getName());	
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private ServerService serverService;
	
	private MemeBlocklist getBlocklist(long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			MemeBlocklist blocklist = (MemeBlocklist) session.createQuery("FROM MemeBlocklist WHERE ServerID = :serverID")
                    .setParameter("serverID", serverID)
                    .uniqueResult();
			return blocklist;
		}
	}
	
	private boolean blocklistExists(long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			MemeBlocklist blocklist = (MemeBlocklist) session.createQuery("FROM MemeBlocklist WHERE ServerID = :serverID")
                    .setParameter("serverID", serverID)
                    .uniqueResult();
			return blocklist != null;
		}
	}
	
	private MemeBlocklist getOrCreateBlocklist(long serverID) {
		if (!blocklistExists(serverID)) {
			try(Session session = sessionFactory.openSession())
			{
				session.getTransaction().begin();
				
				MemeBlocklist blocklist = new MemeBlocklist(serverID, "");
				session.saveOrUpdate(blocklist);
				session.getTransaction().commit();
			}
		}
		
		return getBlocklist(serverID);
	}
	
	private void updateBlocklist(String blocklistString, MemeBlocklist blocklistEntity) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			
			blocklistEntity.setBlocklist(blocklistString);
			
			session.saveOrUpdate(blocklistEntity);
			session.getTransaction().commit();
		}
	}
	
	private JSONObject assemblePayloadWithCaptions(List<String> args, String messageText) {
		List<String> captionsList = new ArrayList<String>(Arrays.asList(messageText.split("-")));
		String templateName = captionsList.get(0);
		
		captionsList.remove(0);
		
		JSONObject payload = new JSONObject();
		
		payload.put("template_name", templateName);
		payload.put("text", captionsList.toArray());
		
		return payload;		
	}
	
	private JSONObject assemblePayloadWithoutCaptions(String messageText) {
		JSONObject payload = new JSONObject();
		payload.put("template_name", messageText);
		return payload;
	}
	
	private String postPayload(JSONObject JSONPayload) {
		String payload = JSONPayload.toString();
		
		try {
			URL memeCommandURL = new URL(API_URL);
			HttpURLConnection con = (HttpURLConnection) memeCommandURL.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setDoOutput(true);
			con.setRequestProperty("Accept", "application/json");
			
			try (OutputStream os = con.getOutputStream()) {
				byte[] input = payload.getBytes("utf-8");
				os.write(input, 0, input.length);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Error during CommandExecution: " + e.getMessage());
			}
			
			try (OutputStream os = con.getOutputStream(); BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
				
				String response = in.lines().collect(Collectors.joining());		
				
				return response.substring(1, response.length() - 1);
				
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Error during CommandExecution: " + e.getMessage());
			}
			
			con.disconnect();
		} catch (IOException e1) {
			LOGGER.log(Level.SEVERE, "Error during CommandExecution: " + e1.getMessage());
		}
		return "template not found";
	}
	
	private boolean payloadIsUnblocked(JSONObject payload, long id, Message message) {
		if (blocklistExists(id)) {
			MemeBlocklist blocklistEntity = getBlocklist(serverService.getServer(message.getGuild().getId()).getId());
			String blocklistString = blocklistEntity.getBlocklist();
			List<String> blocklist = Arrays.asList(blocklistString.split(","));
			
			return !blocklist.contains(payload.get("template_name"));
			
		} else {
			return true;
		}
	}
	
	private void deliverMeme(Message message, JSONObject payload) {
		String apiResponse = postPayload(payload);
		if (apiResponse.equals("template not found")) {
			message.getChannel().sendMessage("Couldn't find that template :(").queue();
		} else {
			MessageEmbed memeImageEmbed = new EmbedBuilder()
					.setColor(Color.ORANGE)
					.setTitle("Image URL", apiResponse)
					.setImage(apiResponse)
					.setFooter("Requested By " + message.getAuthor().getAsTag(), message.getAuthor().getAvatarUrl())
					.build();
			message.getChannel().sendMessage(memeImageEmbed).queue();
		}	
	}
	
	private void sendMeme(Message message, List<String> args) {
		String messageText = String.join(" ", args);
		JSONObject payload = null;
		
		if (messageText.contains("-")) {
			payload = assemblePayloadWithCaptions(args, messageText);			
		} else {
			payload = assemblePayloadWithoutCaptions(messageText);
		}
		if (message.getChannel().getType() != ChannelType.PRIVATE) {
			if (payloadIsUnblocked(payload, serverService.getServer(message.getGuild().getId()).getId(), message)) {
				deliverMeme(message, payload);
			} else {
				message.getChannel().sendMessage("**This template has been blocked**").queue();
			}	
		} else {
			deliverMeme(message, payload);
		}
	}

	private void listBlockedMemes(Message message) {
		if (blocklistExists(serverService.getServer(message.getGuild().getId()).getId())) {
			MemeBlocklist blocklistEntity = getBlocklist(serverService.getServer(message.getGuild().getId()).getId());
			String blocklistString = blocklistEntity.getBlocklist().replaceAll(",", "\n");
			
			message.getChannel().sendMessage("**Blocked Meme Templates:**\n" + blocklistString).queue();
		} else {
			message.getChannel().sendMessage("**There is no blocklist yet!**").queue();
		}
	}

	private void unblockMeme(Message message, List<String> args) {
		if (message.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
			if (blocklistExists(serverService.getServer(message.getGuild().getId()).getId())) {
				MemeBlocklist blocklistEntity = getBlocklist(serverService.getServer(message.getGuild().getId()).getId());
				String blocklistString = blocklistEntity.getBlocklist();
				List<String> blocklist = new ArrayList<String>(Arrays.asList(blocklistString.split(",")));
				
				List<String> modifiableArgs = args.subList(1, args.size());
				String templateToRemove = String.join(" ", modifiableArgs);
				
				if (templateToRemove.equals("")) {
					message.getChannel().sendMessage("**You need to specify a template!**").queue();
				} else {
					if (blocklist.contains(templateToRemove)) {
						blocklist.remove(templateToRemove);
						blocklistString = String.join(",", blocklist);
						updateBlocklist(blocklistString, blocklistEntity);
						message.getChannel().sendMessage("'" + templateToRemove + "' **has been removed from the blocklist**").queue();
					} else {
						message.getChannel().sendMessage("**This template is not blocked!**").queue();
					}	
				}
			} else {
				message.getChannel().sendMessage("**There is no blocklist yet!**").queue();
			}
		}
	}

	private void blockMeme(Message message, List<String> args) {
		if (message.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
			MemeBlocklist blocklistEntity = getOrCreateBlocklist(serverService.getServer(message.getGuild().getId()).getId());
			String blocklistString = blocklistEntity.getBlocklist();
			List<String> blocklist = new ArrayList<String>(Arrays.asList(blocklistString.split(",")));
			
			List<String> modifiableArgs = args.subList(1, args.size());
			String templateToAdd = String.join(" ", modifiableArgs);
			
			if (templateToAdd.equals("")) {
				message.getChannel().sendMessage("**You need to specify a template!**").queue();
			} else {
				if (blocklist.contains(templateToAdd)) {
					message.getChannel().sendMessage("**This template is already blocked!**").queue();
				} else {
					blocklist.add(templateToAdd);
					blocklistString = String.join(",", blocklist);
					updateBlocklist(blocklistString, blocklistEntity);
					message.getChannel().sendMessage("'" + templateToAdd + "' **has been added to the blocklist**").queue();
				}		
			}
		}		
	}

	@Override
	public void executeInternal(Message message, List<String> args) {
		switch (args.get(0)) {
		case "block":
			blockMeme(message, args);
			break;
		case "unblock":
			unblockMeme(message, args);
			break;
		case "blocklist":
			listBlockedMemes(message);
			break;
		default:
			sendMeme(message, args);	
		}
	}

	@Override
	public String getDescription() {
		return "Allows you to request meme templates and add optional text to them. Note: caption text must be seperated by a '-'";
	}

	@Override
	public String getUsage() {
		return "meme [template name] [-text] [-text] .. [-text]\n"
			 + "meme block [template name]\n"
			 + "meme unblock [template name]\n"
			 + "meme blocklist";
	}

	@Override
	public String getName() {
		return "meme";
	}

	@Override
	public CommandCategory getCommandCategory() {
		return CommandCategory.FUN;
	}

	@Override
	public boolean isDMCapable() {
		return true;
	}

	@Override
	public boolean requiresArguments() {
		return true;
	}
	
	@Override
	public String[] getCommandAliases() {
		String[] aliases = {"mememaker", "makememe"};
		return aliases;
	}
	
	@Override
	public boolean canBeDisabled() {
		return true;
	}
}
