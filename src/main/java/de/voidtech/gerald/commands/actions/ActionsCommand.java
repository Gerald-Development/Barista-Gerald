package main.java.de.voidtech.gerald.commands.actions;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.entities.ActionStats;
import main.java.de.voidtech.gerald.entities.GeraldLogger;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.service.LogService;
import main.java.de.voidtech.gerald.service.ServerService;
import main.java.de.voidtech.gerald.util.ParsingUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.Result;

public abstract class ActionsCommand extends AbstractCommand {
	
	private static final String API_URL = "http://api.nekos.fun:8080/api/";
	private static final GeraldLogger LOGGER = LogService.GetLogger(ActionsCommand.class.getSimpleName());

	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private ServerService serverService;
	
	private ActionStats getStatsProfile(String id, ActionType action, long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			return (ActionStats) session.createQuery("FROM ActionStats WHERE memberID = :member AND type = :type AND serverID = :serverID")
                    .setParameter("member", id)
                    .setParameter("type", action.getType())
                    .setParameter("serverID", serverID)
                    .uniqueResult();
		}
	}
	
	private void createStatsProfile(String id, ActionType action, long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			
			ActionStats stats = new ActionStats(action.getType(), id, 0, 0, serverID);
			session.saveOrUpdate(stats);
			session.getTransaction().commit();
		}
	}
	
	private ActionStats getOrCreateProfile(String id, ActionType action, long serverID) {
		ActionStats stats = getStatsProfile(id, action, serverID);
		if (stats == null) {
			createStatsProfile(id, action, serverID);
			stats = getStatsProfile(id, action, serverID);
		}
		return stats;
	}
	
	@SuppressWarnings("unchecked")
	private List<ActionStats> getTopGivenInServer(ActionType type, long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			return (List<ActionStats>) session
					.createQuery("FROM ActionStats WHERE type = :type AND serverID = :serverID AND givenCount > 0 ORDER BY givenCount DESC")
					.setParameter("type", type.getType())
					.setParameter("serverID", serverID)
					.list();
		}
	}
	
	@SuppressWarnings("unchecked")
	private List<ActionStats> getTopReceivedInServer(ActionType type, long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			return (List<ActionStats>) session
					.createQuery("FROM ActionStats WHERE type = :type AND serverID = :serverID AND receivedCount > 0 ORDER BY receivedCount DESC")
					.setParameter("type", type.getType())
					.setParameter("serverID", serverID)
					.list();
		}
	}

	private void updateStatsProfile(ActionStats stats) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			session.saveOrUpdate(stats);
			session.getTransaction().commit();
		}
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
		
		String giverTag = context.getMember().getJDA().retrieveUserById(giver).complete().getAsTag();
		String receiverTag = context.getMember().getJDA().retrieveUserById(receiver).complete().getAsTag();

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
		leaderboardBuilder.append("**Top 5 " + action.getType() + " givers**\n\n");
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
		if (i == 1) leaderboardBuilder.append("Nobody to show! Go " + action.getType() + " someone!\n");
		i = 1;
		leaderboardBuilder.append("**\nTop 5 " + action.getType() + " receivers**\n\n");
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
		if (i == 1) leaderboardBuilder.append("Nobody to show! Go " + action.getType() + " someone!");
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
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(API_URL + action).openConnection();
			con.setRequestMethod("GET");

			if (con.getResponseCode() == 200) {
				try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
					String content = in.lines().collect(Collectors.joining());
					JSONObject json = new JSONObject(content);
					return json.getString("image");
				}
			}
			con.disconnect();
		} catch (IOException | JSONException e) {
			LOGGER.log(Level.SEVERE, "Error during CommandExecution: " + e.getMessage());
		}
		return "";
	}
}
