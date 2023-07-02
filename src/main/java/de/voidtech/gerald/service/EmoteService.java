package main.java.de.voidtech.gerald.service;

import main.java.de.voidtech.gerald.persistence.entity.NitroliteEmote;
import main.java.de.voidtech.gerald.persistence.repository.NitroliteEmoteRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmoteService {

	@Autowired
	private NitroliteEmoteRepository repository;
	
	//private List<NitroliteEmote> getPersistentEmotes(String name) {
		//return repository.getEmoteListWithSimilarNames("%" + name.toLowerCase() + "%");
	//}
	
	//private NitroliteEmote getPersistentEmoteById(String id) {
		//return repository.getEmoteByEmoteId(id);
	//}
	
	//private NitroliteEmote getPersistentEmoteByName(String name) {
		//return repository.getOneEmoteByName(name);
	//}

	//I wanna bring this functionality back one day. Just not today. (Seb)

	public NitroliteEmote getEmoteById(String id, JDA jda) {
		/*
		if (jda.getEmoteById(id) != null) {
			if (jda.getEmoteById(id).isAvailable()) {
				return new NitroliteEmote(
						jda.getEmoteById(id).getName(),
						jda.getEmoteById(id).getId(),
						jda.getEmoteById(id).isAnimated());
			} else return getPersistentEmoteById(id);
		} else return getPersistentEmoteById(id);
		 */
		if (jda.getEmojiById(id) == null) return null;
		else {
			return new NitroliteEmote(
					jda.getEmojiById(id).getName(),
					jda.getEmojiById(id).getId(),
					jda.getEmojiById(id).isAnimated());
		}
	}
	
	public NitroliteEmote getEmoteByName(String searchWord, JDA jda) {
        List<RichCustomEmoji> emoteList = jda.getEmojiCache()
                .stream()
                .collect(Collectors.toList());
        
        RichCustomEmoji emoteOpt = emoteList//
                .stream()//
                .filter(emote -> emote.getName().equalsIgnoreCase(searchWord))
                .findFirst().orElse(null);

		/*
        if (emoteOpt != null) {
        	if (emoteOpt.isAvailable()) {
        		return new NitroliteEmote(
    					emoteOpt.getName(),
    					emoteOpt.getId(),
    					emoteOpt.isAnimated());
        	} else return getPersistentEmoteByName(searchWord);
        } else return getPersistentEmoteByName(searchWord);
		 */
		if (emoteOpt == null) return null;
		else {
			return new NitroliteEmote(
					emoteOpt.getName(),
					emoteOpt.getId(),
					emoteOpt.isAnimated());
		}
	}
	
	public List<NitroliteEmote> getEmotes(String name, JDA jda) {
		List<RichCustomEmoji> emoteList = jda
                .getEmojiCache()
                .stream()
                .collect(Collectors.toList());
		
		List<NitroliteEmote> finalResult = new ArrayList<>();
		
        List<RichCustomEmoji> jdaCacheResult = emoteList.stream()//
                .filter(emote -> emote.getName().equalsIgnoreCase(name) && emote.isAvailable()).collect(Collectors.toList());
        //List<NitroliteEmote> persistentResult = getPersistentEmotes(name);
       
        if (jdaCacheResult.size() > 0) {
        	jdaCacheResult.forEach(emote -> {
        		NitroliteEmote newEmote = new NitroliteEmote(emote.getName(), emote.getId(), emote.isAnimated());
        		finalResult.add(newEmote);
        	});
        }
        //if (persistentResult.size() > 0) finalResult.addAll(persistentResult);
        return finalResult;
	}
}