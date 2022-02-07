package main.java.de.voidtech.gerald.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.commands.CommandContext;
import main.java.de.voidtech.gerald.entities.SuggestionChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

@Service
public class SuggestionService {
	
    @Autowired
    private SessionFactory sessionFactory;
	
    public SuggestionChannel getSuggestionChannel(long serverID) {
        try (Session session = sessionFactory.openSession()) {
            SuggestionChannel suggestionChannel = (SuggestionChannel) session.createQuery("FROM SuggestionChannel WHERE ServerID = :serverID")
                    .setParameter("serverID", serverID)
                    .uniqueResult();
            return suggestionChannel;
        }
    }
    
    public void deleteSuggestionChannel(long guildID) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            session.createQuery("DELETE FROM SuggestionChannel WHERE ServerID = :guildID")
                    .setParameter("guildID", guildID)
                    .executeUpdate();
            session.getTransaction().commit();
        }
    }
    
    public void saveSuggestionChannel(SuggestionChannel config) {
    	try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            session.saveOrUpdate(config);
            session.getTransaction().commit();
        }
    }
    
    public boolean isGuildChannel(String channelID, CommandContext context) {
        return context.getGuild().getTextChannelById(channelID) != null;
    }
    
    public boolean isRole(String roleID, CommandContext context) {
    	return context.getGuild().getRoleById(roleID) != null;
    }

	public boolean memberHasRole(Member member, String roleID) {
		Role role = member.getRoles().stream()
				.filter(searchRole -> searchRole.getId().equals(roleID))
				.findFirst()
				.orElse(null);
		return role != null;
	}
	
}
