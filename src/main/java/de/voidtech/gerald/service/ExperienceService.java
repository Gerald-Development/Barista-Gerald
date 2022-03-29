package main.java.de.voidtech.gerald.service;

import java.time.Instant;
import java.util.Random;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.entities.Experience;
import main.java.de.voidtech.gerald.entities.Server;

@Service
public class ExperienceService {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private ServerService serverService;
	
	private static final int EXPERIENCE_DELAY = 60; //Delay between incrementing XP in seconds
	
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
	
	private void saveUserExperience(Experience userXP) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();	
			session.saveOrUpdate(userXP);
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
	
	public void updateUserExperience(String userID, String guildID, String channelID) {
		Server server = serverService.getServer(guildID);
		Experience userXP = getUserExperience(userID, server.getId());
		userXP.incrementMessageCount();
		
		if ((userXP.getLastMessageTime() + EXPERIENCE_DELAY) > Instant.now().getEpochSecond()) return; 
		
		long currentExperience = userXP.getCurrentExperience() + generateExperience();
		long xpToNextLevel = xpToNextLevel(userXP.getLevel(), currentExperience);
		
		if (xpToNextLevel < 0) {
			userXP.setLevel(userXP.getLevel() + 1);
			userXP.setCurrentXP(-1 * xpToNextLevel);
		} else {
			userXP.setCurrentXP(currentExperience);
		}
		
		userXP.setLastMessageTime(Instant.now().getEpochSecond());
		
		saveUserExperience(userXP);
	}

}
