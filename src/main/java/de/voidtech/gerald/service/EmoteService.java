package main.java.de.voidtech.gerald.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import main.java.de.voidtech.gerald.entities.NitroliteEmoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import main.java.de.voidtech.gerald.entities.NitroliteEmote;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;

@Service
public class EmoteService {

	@Autowired
	private NitroliteEmoteRepository repository;
	
	private List<NitroliteEmote> getPersistentEmotes(String name) {
		return repository.getEmoteListWithSimilarNames("%" + name.toLowerCase() + "%");
	}
	
	private NitroliteEmote getPersistentEmoteById(String id) {
		return repository.getEmoteByEmoteId(id);
	}
	
	private NitroliteEmote getPersistentEmoteByName(String name) {
		return repository.getOneEmoteByName(name);
	}
	
	public NitroliteEmote getEmoteById(String id, JDA jda) {
		if (jda.getEmoteById(id) != null) {
			if (Objects.requireNonNull(jda.getEmoteById(id)).isAvailable()) {
				return new NitroliteEmote(
						Objects.requireNonNull(jda.getEmoteById(id)).getName(),
						Objects.requireNonNull(jda.getEmoteById(id)).getId(),
						Objects.requireNonNull(jda.getEmoteById(id)).isAnimated());
			} else return getPersistentEmoteById(id);
		} else return getPersistentEmoteById(id);
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
        	if (emoteOpt.isAvailable()) {
        		return new NitroliteEmote(
    					emoteOpt.getName(),
    					emoteOpt.getId(),
    					emoteOpt.isAnimated());	
        	} else return getPersistentEmoteByName(searchWord);	
        } else return getPersistentEmoteByName(searchWord);
	}
	
	public List<NitroliteEmote> getEmotes(String name, JDA jda) {
		List<Emote> emoteList = jda
                .getEmoteCache()
                .stream()
                .collect(Collectors.toList());
		
		List<NitroliteEmote> finalResult = new ArrayList<NitroliteEmote>();
		
        List<Emote> jdaCacheResult = emoteList.stream()//
                .filter(emote -> emote.getName().equalsIgnoreCase(name) && emote.isAvailable()).collect(Collectors.toList());
        List<NitroliteEmote> persistentResult = getPersistentEmotes(name);
       
        if (jdaCacheResult.size() > 0) {
        	jdaCacheResult.forEach(emote -> {
        		NitroliteEmote newEmote = new NitroliteEmote(emote.getName(), emote.getId(), emote.isAnimated());
        		finalResult.add(newEmote);
        	});
        }
        if (persistentResult.size() > 0) finalResult.addAll(persistentResult);
        return finalResult;
	}
}