package main.java.de.voidtech.gerald.routines.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Routine;
import main.java.de.voidtech.gerald.entities.NitroliteAlias;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.routines.RoutineCategory;
import main.java.de.voidtech.gerald.service.NitroliteService;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;

@Routine
public class NitroliteRoutine extends AbstractRoutine {
    
	@Autowired
	private NitroliteService nitroliteService;
	
	@Autowired
	private ServerService serverService;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	private boolean aliasExists(String name, long serverID) {
		try(Session session = sessionFactory.openSession())
		{
			NitroliteAlias alias = (NitroliteAlias) session.createQuery("FROM NitroliteAlias WHERE ServerID = :serverID AND aliasName = :aliasName")
                    .setParameter("serverID", serverID)
                    .setParameter("aliasName", name)
                    .uniqueResult();
			return alias != null;
		}
	}
	
    private Emote getEmoteFromAlias(String name, long serverID, Message message) {
    	try(Session session = sessionFactory.openSession())
		{
			NitroliteAlias alias = (NitroliteAlias) session.createQuery("FROM NitroliteAlias WHERE ServerID = :serverID AND aliasName = :aliasName")
                    .setParameter("serverID", serverID)
                    .setParameter("aliasName", name)
                    .uniqueResult();
			
			return message.getJDA().getEmoteById(alias.getEmoteID());
		}
	}
	
	@Override
    public void executeInternal(Message message) {
        List<String> messageTokens = Arrays.asList(message.getContentRaw().split(" "));
        List<Emote> emoteList = message.getJDA()//
                .getEmoteCache()//
                .stream()//
                .collect(Collectors.toList());
        
        long serverID = serverService.getServer(message.getGuild().getId()).getId();
        boolean foundOne = false;

        for (int i = 0; i < messageTokens.size(); i++) {
            String token = messageTokens.get(i);
            Emote emoteOpt = null;
            
            if (token.matches("\\[:[^:]*:]")) {
                String searchWord = token.substring(2, token.length() - 2);
            	
                if (aliasExists(searchWord, serverID)) {
            		emoteOpt = getEmoteFromAlias(searchWord, serverID, message);
            	} else {
                	emoteOpt = emoteList//
                            .stream()//
                            .filter(emote -> emote.getName().equals(searchWord))
                            .findFirst().orElse(null);	
            	}

                if (emoteOpt != null) {
                    foundOne = true;
                    messageTokens.set(i, nitroliteService.constructEmoteString(emoteOpt));
                }
            }
        }
        if (foundOne) {
            final String content = StringUtils.join(messageTokens, " ");
            nitroliteService.sendMessage(message, content);
        }
    }

	@Override
    public String getDescription() {
        return "Service for sending emotes without nitro";
    }
    
	@Override
	public boolean allowsBotResponses() {
		return false;
	}

	@Override
	public String getName() {
		return "Nitrolite";
	}
	
	@Override
	public RoutineCategory getRoutineCategory() {
		return RoutineCategory.UTILS;
	}

}
