package main.java.de.voidtech.gerald.routines.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Routine;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.routines.RoutineCategory;
import main.java.de.voidtech.gerald.service.NitroliteService;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;

@Routine
public class NitroliteRoutine extends AbstractRoutine {
    
	@Autowired
	private NitroliteService nitroliteService;
	
	@Override
    public void executeInternal(Message message) {
        List<String> messageTokens = Arrays.asList(message.getContentRaw().split(" "));
        List<Emote> emoteList = message.getJDA()//
                .getEmoteCache()//
                .stream()//
                .collect(Collectors.toList());
        
        boolean foundOne = false;

        for (int i = 0; i < messageTokens.size(); i++) {
            String token = messageTokens.get(i);

            if (token.matches("\\[:[^:]*:]")) {
                Emote emoteOpt = emoteList//
                        .stream()//
                        .filter(emote -> emote.getName().equals(token.substring(2, token.length() - 2)))
                        .findFirst().orElse(null);

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
