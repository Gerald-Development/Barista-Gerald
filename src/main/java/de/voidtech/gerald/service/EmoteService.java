package main.java.de.voidtech.gerald.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.entities.NitroliteEmote;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;

@Service
public class EmoteService {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	private List<NitroliteEmote> getPersistentEmotes(String name) {
		try(Session session = sessionFactory.openSession())
		{
			return session.createQuery("FROM NitroliteEmote WHERE LOWER(name) LIKE :name", NitroliteEmote.class)
                    .setParameter("name", "%" + name.toLowerCase() + "%")
                    .list();
		}
	}
	
	private NitroliteEmote getPersistentEmoteById(String id) {
		try(Session session = sessionFactory.openSession())
		{
			return (NitroliteEmote) session.createQuery("FROM NitroliteEmote WHERE emoteID = :id")
                    .setParameter("id", id)
                    .setFirstResult(0)
                    .setMaxResults(1)
                    .uniqueResult();
		}
	}
	
	private NitroliteEmote getPersistentEmoteByName(String name) {
		try(Session session = sessionFactory.openSession())
		{
			return (NitroliteEmote) session.createQuery("FROM NitroliteEmote WHERE LOWER(name) = :name")
                    .setParameter("name", name.toLowerCase())
                    .setFirstResult(0)
                    .setMaxResults(1)
                    .uniqueResult();
		}
	}
	
	public NitroliteEmote getEmoteById(String id, JDA jda) {
		if (jda.getEmoteById(id) != null) {
			return new NitroliteEmote(
					jda.getEmoteById(id).getName(),
					jda.getEmoteById(id).getId(),
					jda.getEmoteById(id).isAnimated());
		} else {
			return getPersistentEmoteById(id);
		}
	}
	
	public NitroliteEmote getEmoteByName(String searchWord, JDA jda) {
        List<Emote> emoteList = jda.getEmoteCache()
                .stream()
                .collect(Collectors.toList());
        
        Emote emoteOpt = emoteList//
                .stream()//
                .filter(emote -> emote.getName().equalsIgnoreCase(searchWord))
                .findFirst().orElse(null);
        if (emoteOpt != null) {
			return new NitroliteEmote(
					emoteOpt.getName(),
					emoteOpt.getId(),
					emoteOpt.isAnimated());
        } else {
        	return getPersistentEmoteByName(searchWord);
        }
	}
	
	public List<NitroliteEmote> getEmotes(String name, JDA jda) {
		List<Emote> emoteList = jda
                .getEmoteCache()
                .stream()
                .collect(Collectors.toList());
		
		List<NitroliteEmote> finalResult = new ArrayList<NitroliteEmote>();
		
        List<Emote> jdaCacheResult = emoteList.stream()//
                .filter(emote -> emote.getName().equalsIgnoreCase(name)).collect(Collectors.toList());
        List<NitroliteEmote> persistentResult = getPersistentEmotes(name);
       
        if (jdaCacheResult.size() > 0) {
        	jdaCacheResult.forEach(emote -> {
        		NitroliteEmote newEmote = new NitroliteEmote(emote.getName(), emote.getId(), emote.isAnimated());
        		finalResult.add(newEmote);
        	});
        }
        
        if (persistentResult.size() > 0) {
			finalResult.addAll(persistentResult);
        }
		
        return finalResult;
	}
}