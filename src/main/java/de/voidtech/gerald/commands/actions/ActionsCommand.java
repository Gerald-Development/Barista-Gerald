package main.java.de.voidtech.gerald.commands.actions;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.persistence.entity.ActionStats;
import main.java.de.voidtech.gerald.persistence.entity.Server;
import main.java.de.voidtech.gerald.persistence.repository.ActionStatsRepository;
import main.java.de.voidtech.gerald.service.HttpClientService;
import main.java.de.voidtech.gerald.service.ServerService;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.Result;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;

public abstract class ActionsCommand extends AbstractCommand {
	private static final String API_URL = "http://api.nekos.fun:8080/api/";

	@Autowired
	private ActionStatsRepository repository;
	
	@Autowired
	private ServerService serverService;

	@Autowired
	private HttpClientService httpClientService;
	
	private ActionStats getStatsProfile(String id, ActionType action, long serverID) {
		return repository.getActionStatsProfile(id, action.getType(), serverID);
	}
	
	private void createStatsProfile(String id, ActionType action, long serverID) {
		repository.save(new ActionStats(action.getType(), id, 0, 0, serverID));
	}
	
	private ActionStats getOrCreateProfile(String id, ActionType action, long serverID) {
		ActionStats stats = getStatsProfile(id, action, serverID);
		if (stats == null) {
			createStatsProfile(id, action, serverID);
			stats = getStatsProfile(id, action, serverID);
		}
		return stats;
	}

	private List<ActionStats> getTopGivenInServer(ActionType type, long serverID) {
		return repository.getTopGivenByTypeInServer(type.getType(), serverID);
	}

	private List<ActionStats> getTopReceivedInServer(ActionType type, long serverID) {
		return repository.getTopReceivedByTypeInServer(type.getType(), serverID);
	}

	private void updateStatsProfile(ActionStats stats) {
		repository.save(stats);
	}
	
	private void updateActionStats(String giver, String receiver, ActionType action, CommandContext context) {
		
		long serverID = serverService.getServer(context.getGuild().getId()).getId();
		
		if (!giver.equals(receiver)) {
			ActionStats giverStats = getOrCreateProfile(giver, action, serverID);
			ActionStats receiverStats = getOrCreateProfile(receiver, action, serverID);
			
			giverStats.setGivenCount(giverStats.getGivenCount() + 1);
			receiverStats.setReceivedCount(receiverStats.getReceivedCount() + 1);
			
			updateStatsProfile(giverStats);
			updateStatsProfile(receiverStats);	
		}
	}
	
	private String getStatsString(String giver, String receiver, ActionType action, CommandContext context) {
		long serverID = serverService.getServer(context.getGuild().getId()).getId();
		
		ActionStats giverStats = getOrCreateProfile(giver, action, serverID);
		ActionStats receiverStats = getOrCreateProfile(receiver, action, serverID);
		
		String giverTag = context.getMember().getJDA().retrieveUserById(giver).complete().getEffectiveName();
		String receiverTag = context.getMember().getJDA().retrieveUserById(receiver).complete().getEffectiveName();

		return giverTag + " has " + conjugateAction(action.getType()) + " people " + giverStats.getGivenCount() + " times\n"
				+ receiverTag + " has been " + conjugateAction(action.getType()) + " by people " + receiverStats.getReceivedCount() + " times";
	}

	public void sendAction(CommandContext context, ActionType action) {
		if (context.getArgs().get(0).equals("leaderboard")) {
			sendActionLeaderboard(context, action);
			return;
		}
		if (context.getMentionedMembers().isEmpty()) {
			context.reply("You need to mention someone to " + action.getType() + "!");
			return;
		}
		String gifURL = getActionGif(action.getType());
        if (gifURL != null) {
            updateActionStats(context.getAuthor().getId(), context.getMentionedMembers().get(0).getId(), action, context);
            String phrase = String.format("%s %s %s",
            		context.getMember().getEffectiveName(),
            		conjugateAction(action.getType()),
            		context.getMentionedMembers().get(0).getId().equals(context.getAuthor().getId()) ?
            				"themself" : context.getMentionedMembers().get(0).getEffectiveName());
            //Wow that was a big one
            EmbedBuilder actionEmbedBuilder = new EmbedBuilder();
            actionEmbedBuilder.setTitle(phrase);
            actionEmbedBuilder.setColor(Color.ORANGE);
            if (!gifURL.equals("")) actionEmbedBuilder.setImage(gifURL);
            actionEmbedBuilder.setFooter(getStatsString(context.getAuthor().getId(), context.getMentionedMembers().get(0).getId(), action, context));
            MessageEmbed actionEmbed = actionEmbedBuilder.build();
			context.reply(actionEmbed);
        }
	}
	
	private void sendActionLeaderboard(CommandContext context, ActionType action) {
		Server server = serverService.getServer(context.getGuild().getId());
		List<ActionStats> topGiven = getTopGivenInServer(action, server.getId());
		List<ActionStats> topReceived = getTopReceivedInServer(action, server.getId());
		
		StringBuilder leaderboardBuilder = new StringBuilder();
		int i = 1;
		leaderboardBuilder.append("**Top 5 ").append(action.getType()).append(" givers**\n\n");
		for (ActionStats stat : topGiven) {
			Result<Member> memberGet = context.getGuild().retrieveMemberById(stat.getMember()).mapToResult().complete();
			if (memberGet.isSuccess()) {
				leaderboardBuilder.append(ParsingUtils.convertSingleDigitToEmoji(String.valueOf(i)));
				leaderboardBuilder.append(" ");
				leaderboardBuilder.append(memberGet.get().getAsMention()); 
				leaderboardBuilder.append(" - `");
				leaderboardBuilder.append(stat.getGivenCount());
				leaderboardBuilder.append("`\n");
				i++;				
			}
			if (i == 6) break;
		}
		if (i == 1) leaderboardBuilder.append("Nobody to show! Go ").append(action.getType()).append(" someone!\n");
		i = 1;
		leaderboardBuilder.append("**\nTop 5 ").append(action.getType()).append(" receivers**\n\n");
		for (ActionStats stat : topReceived) {
			Result<Member> memberGet = context.getGuild().retrieveMemberById(stat.getMember()).mapToResult().complete();
			if (memberGet.isSuccess()) {
				leaderboardBuilder.append(ParsingUtils.convertSingleDigitToEmoji(String.valueOf(i)));
				leaderboardBuilder.append(" ");
				leaderboardBuilder.append(memberGet.get().getAsMention());
				leaderboardBuilder.append(" - `");
				leaderboardBuilder.append(stat.getReceivedCount());
				leaderboardBuilder.append("`\n");
				i++;
			}
			if (i == 6) break;
		}
		if (i == 1) leaderboardBuilder.append("Nobody to show! Go ").append(action.getType()).append(" someone!");
		MessageEmbed leaderboardEmbed = new EmbedBuilder()
				.setColor(Color.ORANGE)
				.setTitle(context.getGuild().getName() + "'s " + action.getType() + " leaderboard")
				.setDescription(leaderboardBuilder.toString())
				.build();
		context.reply(leaderboardEmbed);
	}

	private String conjugateAction(String action) {
		String conjugatedAction = action;
		
		if (action.charAt(action.length() - 1) == action.charAt(action.length() - 2)) {
			conjugatedAction += "ed";
		}
		else if (action.charAt(action.length() - 1) == 'e') {
			conjugatedAction += "d";
		}
		else conjugatedAction += action.charAt(action.length()-1) + "ed";

		return conjugatedAction;
	}
	
	private String getActionGif(String action) {
		JSONObject response = httpClientService.getAndReturnJson(API_URL + action);
		return response.getString("image");
	}
}