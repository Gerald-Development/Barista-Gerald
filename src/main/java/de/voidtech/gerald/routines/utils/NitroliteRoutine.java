package main.java.de.voidtech.gerald.routines.utils;

import main.java.de.voidtech.gerald.routines.AbstractRoutine;
import main.java.de.voidtech.gerald.service.NitroliteService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class NitroliteRoutine extends AbstractRoutine {
    @Override
    public void executeInternal(Message message) {
        NitroliteService nls = NitroliteService.getInstance();

        EnumSet<Permission> perms = message.getGuild().getSelfMember().getPermissions((GuildChannel) message.getChannel());

        List<String> messageTokens = Arrays.asList(message.getContentRaw().split(" "));

        boolean foundOne = false;

        for (int i = 0; i < messageTokens.size(); i++) {
            String token = messageTokens.get(i);

            if (token.matches("\\[:[^:]*:]")) {
                List<Emote> emoteList = message.getJDA()//
                        .getEmoteCache()//
                        .stream()//
                        .filter(emote -> emote.getName().equals(token.substring(2, token.length() - 2)))
                        .collect(Collectors.toList());

                if (!emoteList.isEmpty()) {
                    foundOne = true;
                    messageTokens.set(i, nls.constructEmoteString(emoteList.get(0)));
                }
            }
        }

        if (foundOne) {
            final String content = StringUtils.join(messageTokens, " ");

            nls.sendMessage(message, content, perms);
        }
    }

    @Override
    public String getDescription() {
        // TODO: description
        return "n/a";
    }

}
