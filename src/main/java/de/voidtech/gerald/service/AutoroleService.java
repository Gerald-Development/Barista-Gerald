package main.java.de.voidtech.gerald.service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.entities.AutoroleConfig;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

@Service
public class AutoroleService {
	
	@Autowired
	private SessionFactory sessionFactory;
	
    @SuppressWarnings("unchecked")
	public List<AutoroleConfig> getAutoroleConfigs(long serverID) {
    	try(Session session = sessionFactory.openSession())
		{

            return (List<AutoroleConfig>) session.createQuery("FROM AutoroleConfig WHERE serverID = :serverID")
            		.setParameter("serverID", serverID)
            		.list();
		}
	}
    
    public AutoroleConfig getAutoroleConfigByRoleID(String roleID) {
    	try(Session session = sessionFactory.openSession())
		{
            return (AutoroleConfig) session.createQuery("FROM AutoroleConfig WHERE roleID = :roleID")
            		.setParameter("roleID", roleID)
            		.uniqueResult();
		}
    }
    
    public void deleteAutoroleConfig(String roleID) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			session.createQuery("DELETE FROM AutoroleConfig WHERE roleID = :roleID")
				.setParameter("roleID", roleID)
				.executeUpdate();
			session.getTransaction().commit();
		}
	}

	public void saveAutoroleConfig(AutoroleConfig config) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();	
			session.saveOrUpdate(config);
			session.getTransaction().commit();
		}
	}

	public void addRolesToMember(GuildMemberJoinEvent event, List<AutoroleConfig> configs) {
		EnumSet<Permission> perms = event.getGuild().getSelfMember().getPermissions();
		List<AutoroleConfig> discardedConfigs = new ArrayList<AutoroleConfig>();
		
		if (perms.contains(Permission.MANAGE_ROLES)) {
			for (AutoroleConfig config : configs) {
				Role role = event.getJDA().getRoleById(config.getRoleID());
				
				if (role != null) {
					if (config.isAvailableForBots() && event.getUser().isBot())
						event.getGuild().addRoleToMember(event.getMember(), role).queue();
					if (config.isAvailableForHumans() && !event.getUser().isBot())
						event.getGuild().addRoleToMember(event.getMember(), role).queue();	
				} else discardedConfigs.add(config);
			}
			if (!discardedConfigs.isEmpty()) {
				for (AutoroleConfig config : discardedConfigs) {
					deleteAutoroleConfig(config.getRoleID());
				}
			}
		}
	}

	public void removeAllGuildConfigs(long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			session.createQuery("DELETE FROM AutoroleConfig WHERE serverID = :serverID")
				.setParameter("serverID", serverID)
				.executeUpdate();
			session.getTransaction().commit();
		}
	}
}