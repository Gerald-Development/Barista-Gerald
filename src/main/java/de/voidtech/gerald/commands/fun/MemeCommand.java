package main.java.de.voidtech.gerald.commands.fun;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import main.java.de.voidtech.gerald.entities.MemeBlocklistRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Command;
import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandCategory;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.entities.MemeBlocklist;
import main.java.de.voidtech.gerald.service.GeraldConfig;
import main.java.de.voidtech.gerald.service.LogService;
import main.java.de.voidtech.gerald.service.ServerService;
import main.java.de.voidtech.gerald.util.GeraldLogger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Command
public class MemeCommand extends AbstractCommand {
	
	private static final GeraldLogger LOGGER = LogService.GetLogger(MemeCommand.class.getSimpleName());
	
	@Autowired
	private MemeBlocklistRepository repository;
	
	@Autowired
	private ServerService serverService;
	
	@Autowired
	private GeraldConfig config;
	
	private MemeBlocklist getBlocklist(long serverID) {
		return repository.getBlocklist(serverID);
	}
	
	private boolean blocklistExists(long serverID) {
		return getBlocklist(serverID) != null;
	}
	
	private MemeBlocklist getOrCreateBlocklist(long serverID) {
		if (!blocklistExists(serverID)) {
			repository.save(new MemeBlocklist(serverID, ""));
		}
		
		return getBlocklist(serverID);
	}
	
	private void updateBlocklist(String blocklistString, MemeBlocklist blocklistEntity) {
		blocklistEntity.setBlocklist(blocklistString);
		repository.save(blocklistEntity);
	}
	
	private JSONObject assemblePayloadWithCaptions(String messageText) {
		List<String> captionsList = new ArrayList<>(Arrays.asList(messageText.split("-")));
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
			URL memeCommandURL = new URL(config.getMemeApiURL());
			HttpURLConnection con = (HttpURLConnection) memeCommandURL.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setDoOutput(true);
			con.setRequestProperty("Accept", "application/json");
			
			try (OutputStream os = con.getOutputStream()) {
				byte[] input = payload.getBytes(StandardCharsets.UTF_8);
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
	
	private boolean payloadIsUnblocked(JSONObject payload, long id, CommandContext context) {
		if (blocklistExists(id)) {
			MemeBlocklist blocklistEntity = getBlocklist(serverService.getServer(context.getGuild().getId()).getId());
			String blocklistString = blocklistEntity.getBlocklist();
			List<String> blocklist = Arrays.asList(blocklistString.split(","));
			
			return !blocklist.contains(payload.get("template_name"));
			
		} else {
			return true;
		}
	}
	
	private void deliverMeme(CommandContext context, JSONObject payload) {
		String apiResponse = postPayload(payload);
		if (apiResponse.equals("template not found")) {
			context.reply("Couldn't find that template :(");
		} else {
			MessageEmbed memeImageEmbed = new EmbedBuilder()
					.setColor(Color.ORANGE)
					.setTitle("Image URL", apiResponse)
					.setImage(apiResponse)
					.setFooter("Requested By " + context.getAuthor().getAsTag(), context.getAuthor().getAvatarUrl())
					.build();
			context.reply(memeImageEmbed);
		}	
	}
	
	private void sendMeme(CommandContext context, List<String> args) {
		String messageText = String.join(" ", args);
		JSONObject payload;
		
		if (messageText.contains("-")) {
			payload = assemblePayloadWithCaptions(messageText);
		} else {
			payload = assemblePayloadWithoutCaptions(messageText);
		}
		if (context.getChannel().getType() != ChannelType.PRIVATE) {
			if (payloadIsUnblocked(payload, serverService.getServer(context.getGuild().getId()).getId(), context)) {
				deliverMeme(context, payload);
			} else {
				context.reply("**This template has been blocked**");
			}	
		} else {
			deliverMeme(context, payload);
		}
	}

	private void listBlockedMemes(CommandContext context) {
		if (blocklistExists(serverService.getServer(context.getGuild().getId()).getId())) {
			MemeBlocklist blocklistEntity = getBlocklist(serverService.getServer(context.getGuild().getId()).getId());
			String blocklistString = blocklistEntity.getBlocklist().replaceAll(",", "\n");
			
			context.reply("**Blocked Meme Templates:**\n" + blocklistString);
		} else {
			context.reply("**There is no blocklist yet!**");
		}
	}

	private void unblockMeme(CommandContext context, List<String> args) {
		if (context.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
			if (blocklistExists(serverService.getServer(context.getGuild().getId()).getId())) {
				MemeBlocklist blocklistEntity = getBlocklist(serverService.getServer(context.getGuild().getId()).getId());
				String blocklistString = blocklistEntity.getBlocklist();
				List<String> blocklist = new ArrayList<String>(Arrays.asList(blocklistString.split(",")));
				
				List<String> modifiableArgs = args.subList(1, args.size());
				String templateToRemove = String.join(" ", modifiableArgs);
				
				if (templateToRemove.equals("")) {
					context.reply("**You need to specify a template!**");
				} else {
					if (blocklist.contains(templateToRemove)) {
						blocklist.remove(templateToRemove);
						blocklistString = String.join(",", blocklist);
						updateBlocklist(blocklistString, blocklistEntity);
						context.reply("'" + templateToRemove + "' **has been removed from the blocklist**");
					} else {
						context.reply("**This template is not blocked!**");
					}	
				}
			} else {
				context.reply("**There is no blocklist yet!**");
			}
		}
	}

	private void blockMeme(CommandContext context, List<String> args) {
		if (context.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
			MemeBlocklist blocklistEntity = getOrCreateBlocklist(serverService.getServer(context.getGuild().getId()).getId());
			String blocklistString = blocklistEntity.getBlocklist();
			List<String> blocklist = new ArrayList<String>(Arrays.asList(blocklistString.split(",")));
			
			List<String> modifiableArgs = args.subList(1, args.size());
			String templateToAdd = String.join(" ", modifiableArgs);
			
			if (templateToAdd.equals("")) {
				context.reply("**You need to specify a template!**");
			} else {
				if (blocklist.contains(templateToAdd)) {
					context.reply("**This template is already blocked!**");
				} else {
					blocklist.add(templateToAdd);
					blocklistString = String.join(",", blocklist);
					updateBlocklist(blocklistString, blocklistEntity);
					context.reply("'" + templateToAdd + "' **has been added to the blocklist**");
				}		
			}
		}		
	}

	@Override
	public void executeInternal(CommandContext context, List<String> args) {
		switch (args.get(0)) {
		case "block":
			blockMeme(context, args);
			break;
		case "unblock":
			unblockMeme(context, args);
			break;
		case "blocklist":
			listBlockedMemes(context);
			break;
		default:
			sendMeme(context, args);
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
		return new String[]{"mememaker", "makememe"};
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