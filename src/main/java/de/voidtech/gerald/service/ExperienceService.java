package main.java.de.voidtech.gerald.service;

import java.time.Instant;
import java.util.List;
import java.util.Random;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
	
	private static final int EXPERIENCE_DELAY = 0; //Delay between incrementing XP in seconds
	
	private Experience getUserExperience(String userID, long serverID) {
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
	
	private List<LevelUpRole> getRolesForLevel(long id, long level) {
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
	

	private void removeLevelUpRole(LevelUpRole role) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			session.createQuery("DELETE FROM LevelUpRole WHERE roleID = :roleID AND serverID = :serverID")
				.setParameter("roleID", role.getRoleID())
				.setParameter("serverID", role.getServerID())
				.executeUpdate();
			session.getTransaction().commit();
		}
	}
	
	private long xpToNextLevel(long currentLevel, long currentXP) {
		long nextLevel = currentLevel + 1;
		return 5 * (nextLevel ^ 2) + (50 * nextLevel) + 100 - currentXP;
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
		
		long currentExperience = userXP.getCurrentExperience() + generateExperience();
		long xpToNextLevel = xpToNextLevel(userXP.getLevel(), currentExperience);
		
		if (xpToNextLevel < 0) {
			userXP.setLevel(userXP.getLevel() + 1);
			userXP.setCurrentXP(-1 * xpToNextLevel);
			performLevelUpActions(userXP, server, member, channelID);
		} else userXP.setCurrentXP(currentExperience);
		
		userXP.setLastMessageTime(Instant.now().getEpochSecond());
		
		saveUserExperience(userXP);
	}

	private void performLevelUpActions(Experience userXP, Server server, Member member, String channelID) {
		ServerExperienceConfig config = getServerExperienceConfig(server.getId());
		
		List<LevelUpRole> roles = getRolesForLevel(server.getId(), userXP.getLevel());
		if (roles.isEmpty()) return;
		
		List<Role> memberRoles = member.getRoles();
		
		for (LevelUpRole role : roles) {
			Role roleToBeGiven = member.getGuild().getRoleById(role.getRoleID());
			if (roleToBeGiven == null) removeLevelUpRole(role);
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
