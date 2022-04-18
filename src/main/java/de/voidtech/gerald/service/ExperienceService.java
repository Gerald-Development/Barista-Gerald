package main.java.de.voidtech.gerald.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Random;

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
import net.dv8tion.jda.api.EmbedBuilder;
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
	
	private static final int EXPERIENCE_DELAY = 0; //Delay between incrementing XP in seconds
	
	public byte[] getExperienceCard(String avatarURL, long xpAchieved, long xpNeeded,
			long level, long rank, String username, String barColour, String background) {
		try {
			String cardURL = config.getExperienceCardApiURL() + "?avatar_url=" + avatarURL +
					"&xp=" + xpAchieved + "&xp_needed=" + xpNeeded + "&level=" + level + "&rank=" + rank
					+ "&username=" + URLEncoder.encode(username, StandardCharsets.UTF_8.toString())
					+ "&bar_colour=" + URLEncoder.encode(barColour, StandardCharsets.UTF_8.toString())
					+ "&bg_colour=" + URLEncoder.encode(background, StandardCharsets.UTF_8.toString());
			URL url = new URL(cardURL);
			//Remove the data:image/png;base64 part
			String response = Jsoup.connect(url.toString()).get().toString().split(",")[1];
			byte[] imageBytes = DatatypeConverter.parseBase64Binary(response);
			return imageBytes;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public Experience getUserExperience(String userID, long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			Experience xp = (Experience) session.createQuery("FROM Experience WHERE userID = :userID AND serverID = :serverID")
					.setParameter("userID", userID)
					.setParameter("serverID", serverID)
					.uniqueResult();
			
			if (xp == null) xp = new Experience(userID, serverID);
			return xp;
		}
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
	
	private void saveUserExperience(Experience userXP) {
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
	
	public long xpNeededForLevel(long level) {
		return 5 * (level ^ 2) + (50 * level) + 100;
	}
	
	private long xpToNextLevel(long nextLevel, long currentXP) {
		return xpNeededForLevel(nextLevel) - currentXP;
	}
	
	private int generateExperience() {
		return new Random().nextInt(16);
	}
	
	public void updateUserExperience(Member member, String guildID, String channelID) {
		Server server = serverService.getServer(guildID);
		Experience userXP = getUserExperience(member.getId(), server.getId());
		userXP.incrementMessageCount();
		
		if ((userXP.getLastMessageTime() + EXPERIENCE_DELAY) > Instant.now().getEpochSecond()) {
			saveUserExperience(userXP);
			return; 
		}
		
		userXP.incrementExperience(generateExperience());
		long currentExperience = userXP.getCurrentExperience();
		long xpToNextLevel = xpToNextLevel(userXP.getNextLevel(), currentExperience);
		
		if (xpToNextLevel <= 0) {
			userXP.setLevel(userXP.getNextLevel());
			userXP.setCurrentXP(-1 * xpToNextLevel);
			performLevelUpActions(userXP, server, member, channelID);
		} else userXP.setCurrentXP(currentExperience);
		
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

	private void sendLevelUpMessage(LevelUpRole role, Member member, Role roleToBeGiven, String channelID) {
		MessageEmbed levelUpEmbed = new EmbedBuilder()
				.setColor(roleToBeGiven.getColor())
				.setTitle(member.getUser().getName() + " levelled up!")
				.setDescription(member.getAsMention() + " reached level `" + role.getLevel()
					+ "` and received the role " + roleToBeGiven.getAsMention())
				.build();
		member.getGuild().getTextChannelById(channelID).sendMessageEmbeds(levelUpEmbed).queue();
	}
}
