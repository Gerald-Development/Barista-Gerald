package main.java.de.voidtech.gerald.service;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import javax.xml.bind.DatatypeConverter;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.entities.Experience;
import main.java.de.voidtech.gerald.entities.LevelUpRole;
import main.java.de.voidtech.gerald.entities.Server;
import main.java.de.voidtech.gerald.entities.ServerExperienceConfig;
import main.java.de.voidtech.gerald.util.GeraldLogger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;

@Service
public class ExperienceService {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private ServerService serverService;
	
	@Autowired
	private GeraldConfig config;
	
	private static final GeraldLogger LOGGER = LogService.GetLogger(ExperienceService.class.getSimpleName());
	private static final int EXPERIENCE_DELAY = 60; //Delay between incrementing XP in seconds

	private static final String BAR_FROM = "#F24548";
	private static final String BAR_TO = "#3B43D5";
	private static final String BACKGROUND = "#2F3136";
	
	public byte[] getExperienceCard(String avatarURL, long xpAchieved, long xpNeeded,
			long level, long rank, String username, String discriminator) {
		try {
			String cardURL = config.getExperienceCardApiURL() + "xpcard/?avatar_url=" + avatarURL +
					"&xp=" + xpAchieved + "&xp_needed=" + xpNeeded + "&level=" + level + "&rank=" + rank
					+ "&username=" + URLEncoder.encode(username, StandardCharsets.UTF_8.toString())
					+ "&discriminator=" + URLEncoder.encode(discriminator, StandardCharsets.UTF_8.toString())
					+ "&bar_colour_from=" + URLEncoder.encode(BAR_FROM, StandardCharsets.UTF_8.toString())
					+ "&bar_colour_to=" + URLEncoder.encode(BAR_TO, StandardCharsets.UTF_8.toString())
					+ "&bg_colour=" + URLEncoder.encode(BACKGROUND, StandardCharsets.UTF_8.toString());
			URL url = new URL(cardURL);
			//Remove the data:image/png;base64 part
			String response = Jsoup.connect(url.toString()).get().toString().split(",")[1];
			return DatatypeConverter.parseBase64Binary(response);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage());
		}
		return null;
	}
	
	public Experience getUserExperience(String userID, long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			return (Experience) session.createQuery("FROM Experience WHERE userID = :userID AND serverID = :serverID")
					.setParameter("userID", userID)
					.setParameter("serverID", serverID)
					.uniqueResult();
		}
	}
	
	public List<String> getNoExperienceChannelsForServer(long serverID, JDA jda) {
		List<String> channels;
		ServerExperienceConfig config = getServerExperienceConfig(serverID);
		config.getNoXPChannels()
				.stream()
				.filter(channel -> jda.getTextChannelById(channel) == null)
				.forEach(config::removeNoExperienceChannel);
		channels = new ArrayList<>(config.getNoXPChannels());
		return channels;
	}
	
	private ServerExperienceConfig getServerExperienceConfig(long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			ServerExperienceConfig xpConf = (ServerExperienceConfig) session.createQuery("FROM ServerExperienceConfig WHERE serverID = :serverID")
					.setParameter("serverID", serverID)
					.uniqueResult();
			
			if (xpConf == null) {
				xpConf = new ServerExperienceConfig(serverID);
				saveServerExperienceConfig(xpConf);
			}
			return xpConf;
		}
	}
	
	public List<Experience> getServerLeaderboard(long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			@SuppressWarnings("unchecked")
			List<Experience> leaderboard = session.createQuery("FROM Experience WHERE serverID = :serverID ORDER BY totalExperience DESC")
					.setParameter("serverID", serverID)
					.list();
			return leaderboard;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<Experience> getServerLeaderboardChunk(long serverID, int limit, int offset) {
		try(Session session = sessionFactory.openSession())
		{
			@SuppressWarnings("unchecked")
			List<Experience> leaderboard = session.createQuery("FROM Experience WHERE serverID = :serverID ORDER BY totalExperience DESC")
					.setParameter("serverID", serverID)
					.setMaxResults(limit)
					.setFirstResult(offset)
					.list();
			return leaderboard;
		}
	}
	
	public int getUserLeaderboardPosition(long serverID, String userID) {
		List<Experience> leaderboard = getServerLeaderboard(serverID);
		int position = 0;
		
		for (Experience xp : leaderboard) {
			position++;
			if (xp.getUserID().equals(userID)) break;
		}

		return position;
	}
	
	private List<LevelUpRole> getRolesForLevelFromServer(long id, long level) {
		try(Session session = sessionFactory.openSession())
		{
			@SuppressWarnings("unchecked")
			List<LevelUpRole> roles = (List<LevelUpRole>) session.createQuery("FROM LevelUpRole WHERE serverID = :serverID AND level <= :level")
					.setParameter("serverID", id)
					.setParameter("level", level)
					.list();
			return roles;
		}
	}
	
	public List<LevelUpRole> getAllLevelUpRolesForServer(long id) {
		try(Session session = sessionFactory.openSession())
		{
			@SuppressWarnings("unchecked")
			List<LevelUpRole> roles = (List<LevelUpRole>) session.createQuery("FROM LevelUpRole WHERE serverID = :serverID")
					.setParameter("serverID", id)
					.list();
			return roles;
		}
	}
	
	public boolean serverHasRoleForLevel(long id, long level) {
		try(Session session = sessionFactory.openSession())
		{
			LevelUpRole role = (LevelUpRole) session.createQuery("FROM LevelUpRole WHERE serverID = :serverID AND level = :level")
				.setParameter("serverID", id)
				.setParameter("level", level)
				.uniqueResult();
			return role != null;
		}
	}
	
	public void saveUserExperience(Experience userXP) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();	
			session.saveOrUpdate(userXP);
			session.getTransaction().commit();
		}
	}
	
	private void saveServerExperienceConfig(ServerExperienceConfig config) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();	
			session.saveOrUpdate(config);
			session.getTransaction().commit();
		}
	}
	
	public void saveLevelUpRole(LevelUpRole role) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();	
			session.saveOrUpdate(role);
			session.getTransaction().commit();
		}
	}
	

	public void removeLevelUpRole(long level, long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			session.createQuery("DELETE FROM LevelUpRole WHERE level = :level AND serverID = :serverID")
				.setParameter("level", level)
				.setParameter("serverID", serverID)
				.executeUpdate();
			session.getTransaction().commit();
		}
	}

	private void removeAllLevelUpRoles(long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			session.createQuery("DELETE FROM LevelUpRole WHERE serverID = :serverID")
					.setParameter("serverID", serverID)
					.executeUpdate();
			session.getTransaction().commit();
		}
	}

	private void removeAllUserExperience(long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			session.createQuery("DELETE FROM Experience WHERE serverID = :serverID")
					.setParameter("serverID", serverID)
					.executeUpdate();
			session.getTransaction().commit();
		}
	}

	private void deleteServerExperienceConfig(long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			session.createQuery("DELETE FROM ServerExperienceConfig WHERE serverID = :serverID")
					.setParameter("serverID", serverID)
					.executeUpdate();
			session.getTransaction().commit();
		}
	}
	
	public void deleteNoXpChannel(String channelID, long serverID) {
		ServerExperienceConfig config = getServerExperienceConfig(serverID);
		config.removeNoExperienceChannel(channelID);
		saveServerExperienceConfig(config);
	}
	
	public void clearNoXpChannels(long serverID) {
		ServerExperienceConfig config = getServerExperienceConfig(serverID);
		config.clearNoExperienceChannels();
		saveServerExperienceConfig(config);
	}
	
	public void addNoXpChannel(String channelID, long serverID) {
		ServerExperienceConfig config = getServerExperienceConfig(serverID);
		config.addNoExperienceChannel(channelID);
		saveServerExperienceConfig(config);
	}
	
	public long totalXpNeededForLevel(long level) {
		return (long) Math.ceil(((double)5 / (double) 6) * (level * ((2 * Math.pow(level, 2)) + (27 * level) + 91)));
	}

	public long xpNeededForLevelWithoutPreviousLevels(long level) {
		return level == 0 ? totalXpNeededForLevel(level) : totalXpNeededForLevel(level) - totalXpNeededForLevel(level - 1);
	}

	public long xpGainedToNextLevelWithoutPreviousLevels(long level, long currentXp) {
		long excess = level == 0 ? 0 : totalXpNeededForLevel(level - 1);
		return currentXp - excess;
	}
	
	private long xpToNextLevel(long nextLevel, long currentXP) {
		return totalXpNeededForLevel(nextLevel) - currentXP;
	}
	
	public void updateUserExperience(Member member, String guildID, String channelID) {
		Server server = serverService.getServer(guildID);
		ServerExperienceConfig config = getServerExperienceConfig(server.getId());
		Experience userXP = getUserExperience(member.getId(), server.getId());
		
		if (userXP == null) {
			userXP = new Experience(member.getId(), server.getId());
		}
		
		userXP.incrementMessageCount();
		
		if ((userXP.getLastMessageTime() + EXPERIENCE_DELAY) > Instant.now().getEpochSecond()) {
			saveUserExperience(userXP);
			return; 
		}
		
		userXP.incrementExperience(config.getExperienceIncrement());
		long currentExperience = userXP.getTotalExperience();
		long xpToNextLevel = xpToNextLevel(userXP.getNextLevel(), currentExperience);
		if (xpToNextLevel <= 0) {
			userXP.setLevel(userXP.getNextLevel());
			performLevelUpActions(userXP, server, member, channelID);
		}
		
		userXP.setLastMessageTime(Instant.now().getEpochSecond());
		
		saveUserExperience(userXP);
	}

	private void performLevelUpActions(Experience userXP, Server server, Member member, String channelID) {
		ServerExperienceConfig config = getServerExperienceConfig(server.getId());
		
		List<LevelUpRole> roles = getRolesForLevelFromServer(server.getId(), userXP.getCurrentLevel());
		if (roles.isEmpty()) return;
		
		List<Role> memberRoles = member.getRoles();
		
		for (LevelUpRole role : roles) {
			Role roleToBeGiven = member.getGuild().getRoleById(role.getRoleID());
			if (roleToBeGiven == null) removeLevelUpRole(role.getLevel(), role.getServerID());
			else {
				if (!memberRoles.contains(roleToBeGiven)) {
					member.getGuild().addRoleToMember(member, roleToBeGiven).complete();
					if (config.levelUpMessagesEnabled()) sendLevelUpMessage(role, member, roleToBeGiven, channelID);
				}
			}
		}
	}
	
	public void addRolesOnServerJoin(Server server, Member member) {
		Experience userXP = getUserExperience(member.getId(), server.getId());
		if (userXP == null) return;
		
		List<LevelUpRole> roles = getRolesForLevelFromServer(server.getId(), userXP.getCurrentLevel());
		if (roles.isEmpty()) return;
		
		List<Role> memberRoles = member.getRoles();
		for (LevelUpRole role : roles) {
			Role roleToBeGiven = member.getGuild().getRoleById(role.getRoleID());
			if (roleToBeGiven == null) removeLevelUpRole(role.getLevel(), role.getServerID());
			else if (!memberRoles.contains(roleToBeGiven)) member.getGuild().addRoleToMember(member, roleToBeGiven).complete();
		}
	}

	private void sendLevelUpMessage(LevelUpRole role, Member member, Role roleToBeGiven, String channelID) {
		MessageEmbed levelUpEmbed = new EmbedBuilder()
				.setColor(roleToBeGiven.getColor())
				.setTitle(member.getUser().getName() + " levelled up!")
				.setDescription(member.getAsMention() + " reached level `" + role.getLevel()
					+ "` and received the role " + roleToBeGiven.getAsMention())
				.build();
		member.getGuild().getTextChannelById(channelID).sendMessageEmbeds(levelUpEmbed).queue();
	}

	public boolean toggleLevelUpMessages(long id) {
		ServerExperienceConfig config = getServerExperienceConfig(id);
		boolean nowEnabled = !config.levelUpMessagesEnabled();
		config.setLevelUpMessagesEnabled(nowEnabled);
		saveServerExperienceConfig(config);
		return nowEnabled;
	}

	public void resetServer(long id) {
		removeAllLevelUpRoles(id);
		removeAllUserExperience(id);
		deleteServerExperienceConfig(id);
	}
}
