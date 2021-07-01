package main.java.de.voidtech.gerald.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
	
	@Autowired
	private NitroliteService nitroliteService;
	
	private static final String DISCORD_EMOJI_CDN_URL = "https://cdn.discordapp.com/emojis/";
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.2; WOW64; Trident/7.0; rv:11.0) like Gecko";
	
	private static final Logger LOGGER = Logger.getLogger(EmoteService.class.getName());
	
	private List<NitroliteEmote> getPersistentEmotes(String name) {
		try(Session session = sessionFactory.openSession())
		{
			List<NitroliteEmote> emotes = (List<NitroliteEmote>) session.createQuery("FROM NitroliteEmote WHERE LOWER(name) LIKE :name", NitroliteEmote.class)
                    .setParameter("name", "%" + name.toLowerCase() + "%")
                    .list();
			if (!emotes.isEmpty()) {
				List<NitroliteEmote> checkedEmotesList = new ArrayList<NitroliteEmote>();
				for (NitroliteEmote emote : emotes) {
					if (emoteStillExists(emote)) checkedEmotesList.add(emote);
					else deletePersistentEmoteById(emote.getID());
				}
				return checkedEmotesList;
			} else return emotes;	
		}
	}
	
	private NitroliteEmote getPersistentEmoteById(String id) {
		try(Session session = sessionFactory.openSession())
		{
			NitroliteEmote emote = (NitroliteEmote) session.createQuery("FROM NitroliteEmote WHERE emoteID = :id")
                    .setParameter("id", id)
                    .setFirstResult(0)
                    .setMaxResults(1)
                    .uniqueResult();
			
			if (emote != null) {
				if (!emoteStillExists(emote))
					deletePersistentEmoteById(emote.getID());
			}
			return emote;
		}
	}
	
	private NitroliteEmote getPersistentEmoteByName(String name) {
		try(Session session = sessionFactory.openSession())
		{
			NitroliteEmote emote = (NitroliteEmote) session.createQuery("FROM NitroliteEmote WHERE LOWER(name) = :name")
                    .setParameter("name", name.toLowerCase())
                    .setFirstResult(0)
                    .setMaxResults(1)
                    .uniqueResult();
			
			if (emote != null) {
				if (!emoteStillExists(emote))
					deletePersistentEmoteById(emote.getID());
			}
			return emote;
		}
	}
	
	private void deletePersistentEmoteById(String emoteID) {
		try(Session session = sessionFactory.openSession())
		{
			session.getTransaction().begin();
			session.createQuery("DELETE FROM NitroliteEmote WHERE emoteID = :emoteID")
				.setParameter("emoteID", emoteID)
				.executeUpdate();
			session.getTransaction().commit();
		}
		nitroliteService.deleteAliasesUsingEmote(emoteID);
	}
	
	public NitroliteEmote getEmoteById(String id, JDA jda) {
		if (jda.getEmoteById(id) != null) {
			NitroliteEmote returnedEmote = new NitroliteEmote(
					jda.getEmoteById(id).getName(),
					jda.getEmoteById(id).getId(),
					jda.getEmoteById(id).isAnimated());
			return returnedEmote;
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
                .filter(emote -> emote.getName().toLowerCase().equals(searchWord.toLowerCase()))
                .findFirst().orElse(null);
        if (emoteOpt != null) {
			NitroliteEmote returnedEmote = new NitroliteEmote(
					emoteOpt.getName(),
					emoteOpt.getId(),
					emoteOpt.isAnimated());
			return returnedEmote;
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
                .filter(emote -> emote.getName().toLowerCase().equals(name.toLowerCase())).collect(Collectors.toList());
        List<NitroliteEmote> persistentResult = getPersistentEmotes(name);
       
        if (jdaCacheResult.size() > 0) {
        	jdaCacheResult.forEach(emote -> {
        		NitroliteEmote newEmote = new NitroliteEmote(emote.getName(), emote.getId(), emote.isAnimated());
        		finalResult.add(newEmote);
        	});
        }
        
        if (persistentResult.size() > 0) {
        	persistentResult.forEach(emote -> {
        		finalResult.add(emote);
        	});
        }
        
        List<NitroliteEmote> finalResultChecked = new ArrayList<NitroliteEmote>();
        
        for (NitroliteEmote emote : finalResult) {
        	if (emoteStillExists(emote)) finalResultChecked.add(emote);
        	else deletePersistentEmoteById(emote.getID());
        }
		
        return finalResultChecked;
	}
	
	private boolean emoteStillExists(NitroliteEmote emote) {
		try {
			URL url = new URL(DISCORD_EMOJI_CDN_URL + emote.getID() + (emote.isEmoteAnimated() ? ".gif" : ".png"));
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestMethod("HEAD");
			httpConnection.setRequestProperty("User-Agent", USER_AGENT);

			return (httpConnection.getResponseCode() != HttpURLConnection.HTTP_NOT_FOUND);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error during ServiceExecution: " + e.getMessage());
		}
		return true;
	}
}