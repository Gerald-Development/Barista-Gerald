package main.java.de.voidtech.gerald.service;

import main.java.de.voidtech.gerald.persistence.entity.NitroliteEmote;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmoteService {

    public NitroliteEmote getEmoteByName(String searchWord, JDA jda) {
        List<RichCustomEmoji> emoteList = jda.getEmojiCache()
                .stream()
                .collect(Collectors.toList());

        RichCustomEmoji emoteOpt = emoteList//
                .stream()//
                .filter(emote -> emote.getName().equalsIgnoreCase(searchWord))
                .findFirst().orElse(null);

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

        if (!jdaCacheResult.isEmpty()) {
            jdaCacheResult.forEach(emote -> {
                NitroliteEmote newEmote = new NitroliteEmote(emote.getName(), emote.getId(), emote.isAnimated());
                finalResult.add(newEmote);
            });
        }
        return finalResult;
    }
}