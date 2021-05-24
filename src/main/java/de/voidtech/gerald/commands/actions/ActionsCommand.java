package main.java.de.voidtech.gerald.commands.actions;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.commands.AbstractCommand;
import main.java.de.voidtech.gerald.entities.ActionStats;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

public abstract class ActionsCommand extends AbstractCommand {
	
	private static final String API_URL = "http://api.nekos.fun:8080/api/";
	private static final Logger LOGGER = Logger.getLogger(ActionsCommand.class.getName());

	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private ServerService serverService;
	
	private ActionStats getStatsProfile(String id, ActionType action, long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			ActionStats stats = (ActionStats) session.createQuery("FROM ActionStats WHERE memberID = :member AND type = :type AND serverID = :serverID")
                    .setParameter("member", id)
                    .setParameter("type", action.getType())
                    .setParameter("serverID", serverID)
                    .uniqueResult();
			return stats;
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

	private void updateStatsProfile(ActionStats stats) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			session.saveOrUpdate(stats);
			session.getTransaction().commit();
		}
	}
	
	private void updateActionStats(String giver, String receiver, ActionType action, Message message) {
		
		long serverID = serverService.getServer(message.getGuild().getId()).getId();
		
		if (!giver.equals(receiver)) {
			ActionStats giverStats = getOrCreateProfile(giver, action, serverID);
			ActionStats receiverStats = getOrCreateProfile(receiver, action, serverID);
			
			giverStats.setGivenCount(giverStats.getGivenCount() + 1);
			receiverStats.setReceivedCount(receiverStats.getReceivedCount() + 1);
			
			updateStatsProfile(giverStats);
			updateStatsProfile(receiverStats);	
		}
	}
	
	private String getStatsString(String giver, String receiver, ActionType action, Message message) {
		long serverID = serverService.getServer(message.getGuild().getId()).getId();
		
		ActionStats giverStats = getOrCreateProfile(giver, action, serverID);
		ActionStats receiverStats = getOrCreateProfile(receiver, action, serverID);
		
		String giverTag = message.getJDA().retrieveUserById(giver).complete().getAsTag();
		String receiverTag = message.getJDA().retrieveUserById(receiver).complete().getAsTag();
		
		String statsString = giverTag + " has " + conjugateAction(action.getType()) + " people " + giverStats.getGivenCount() + " times\n"
				+ receiverTag + " has been " + conjugateAction(action.getType()) + " by people " + receiverStats.getReceivedCount() + " times";
		
		return statsString;
	}

	public void sendAction(Message message, ActionType action) {
		if(message.getMentionedMembers().isEmpty()) {
            message.getChannel().sendMessage("You need to mention someone to " + action.getType() + "!").queue();
        } else {
            String gifURL = getActionGif(action.getType());
            if (gifURL != null)
            {
            	updateActionStats(message.getAuthor().getId(), message.getMentionedMembers().get(0).getId(), action, message);
            	String phrase = String.format("%s %s %s", message.getMember().getEffectiveName(), conjugateAction(action.getType()), 
            			message.getMentionedMembers().get(0).getId().equals(message.getAuthor().getId()) ? "themself" : message.getMentionedMembers().get(0).getEffectiveName());
            	
                EmbedBuilder actionEmbedBuilder = new EmbedBuilder();
                actionEmbedBuilder.setTitle(phrase);
                actionEmbedBuilder.setColor(Color.ORANGE);
                if (!gifURL.equals("")) {
                	actionEmbedBuilder.setImage(gifURL);	
                }
                actionEmbedBuilder.setFooter(getStatsString(message.getAuthor().getId(), message.getMentionedMembers().get(0).getId(), action, message));
                MessageEmbed actionEmbed = actionEmbedBuilder.build();
                message.getChannel().sendMessage(actionEmbed).queue();
            }
        }
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
