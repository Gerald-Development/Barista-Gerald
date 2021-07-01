package main.java.de.voidtech.gerald.routines.utils;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import main.java.de.voidtech.gerald.annotations.Routine;
import main.java.de.voidtech.gerald.entities.NitroliteEmote;
import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.routines.RoutineCategory;
import main.java.de.voidtech.gerald.service.EmoteService;
import main.java.de.voidtech.gerald.service.NitroliteService;
import main.java.de.voidtech.gerald.service.ServerService;
import net.dv8tion.jda.api.entities.Message;

@Routine
public class NitroliteRoutine extends AbstractRoutine {
    
	@Autowired
	private NitroliteService nitroliteService;
	
	@Autowired
	private ServerService serverService;
	
	@Autowired
	private EmoteService emoteService;
	
	@Override
    public void executeInternal(Message message) {
        List<String> messageTokens = Arrays.asList(message.getContentRaw().split(" "));
        
        long serverID = serverService.getServer(message.getGuild().getId()).getId();
        boolean foundOne = false;

        for (int i = 0; i < messageTokens.size(); i++) {
            String token = messageTokens.get(i);
            NitroliteEmote emoteOpt = null;
            
            if (token.matches("\\[:[^:]*:]")) {
                String searchWord = token.substring(2, token.length() - 2);
            	
                if (nitroliteService.aliasExists(searchWord, serverID)) {
            		emoteOpt = nitroliteService.getEmoteFromAlias(searchWord, serverID, message);
            	} else {
                	emoteOpt = emoteService.getEmoteByName(searchWord, message.getJDA());
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
        return "Allows nitrolite messages to be detected and handled";
    }
    
	@Override
	public boolean allowsBotResponses() {
		return false;
	}

	@Override
	public boolean canBeDisabled() {
		return true;
	}

	@Override
	public String getName() {
		return "r-nitrolite";
	}
	
	@Override
	public RoutineCategory getRoutineCategory() {
		return RoutineCategory.UTILS;
	}

}
