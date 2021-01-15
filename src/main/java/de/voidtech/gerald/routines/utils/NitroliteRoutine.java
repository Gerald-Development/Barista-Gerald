package main.java.de.voidtech.gerald.routines.utils;

import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.service.NitroliteService;
import net.dv8tion.jda.api.entities.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NitroliteRoutine extends AbstractRoutine {
    @Override
    public void executeInternal(Message message) {
        NitroliteService nls = NitroliteService.getInstance();

        List<String> messageTokens = Arrays.asList(message.getContentRaw().split(" "));
        List<Emote> emoteList = message.getJDA()//
                .getEmoteCache()//
                .stream()//
                .collect(Collectors.toList());
        
        boolean foundOne = false;

        for (int i = 0; i < messageTokens.size(); i++) {
            String token = messageTokens.get(i);

            if (token.matches("\\[:[^:]*:]")) {
                boolean emoteFound = emoteList//
                        .stream()//
                        .anyMatch(emote -> emote.getName().equals(token.substring(2, token.length() - 2)));

                if (emoteFound) {
                    foundOne = true;
                    messageTokens.set(i, nls.constructEmoteString(emoteList.get(0)));
                }
            }
        }
        if (foundOne) {
            final String content = StringUtils.join(messageTokens, " ");

            nls.sendMessage(message, content);
        }
    }

    @Override
    public String getDescription() {
        // TODO: description
        return "n/a";
    }

}
