package main.java.de.voidtech.gerald.service;

import main.java.de.voidtech.gerald.GlobalConstants;
import main.java.de.voidtech.gerald.persistence.entity.GlobalConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.security.auth.login.LoginException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class JdaService {

    @Autowired
    private GlobalConfigService globalConfigService;

    @Autowired
    private GeraldConfigService configService;

    @Bean("JDA")
    @Order(3)
    public JDA getJDA(@Autowired List<ListenerAdapter> listeners) throws LoginException, InterruptedException {
        GlobalConfig globalConf = globalConfigService.getGlobalConfig();
        return JDABuilder.createDefault(configService.getToken())
                .enableIntents(getApprovedIntents())
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setBulkDeleteSplittingEnabled(false)
                .setStatus(OnlineStatus.ONLINE)
                .setCompression(Compression.NONE)
                .addEventListeners(listeners.toArray(new ListenerAdapter[0]))
                .setActivity(EntityBuilder.createActivity(globalConf.getStatus(),
                        GlobalConstants.STREAM_URL,
                        globalConf.getActivity()))
                .build()
                .awaitReady();
    }

    private Set<GatewayIntent> getApprovedIntents() {
        Set<GatewayIntent> approvedIntents = new HashSet<>();

        approvedIntents.add(GatewayIntent.GUILD_MEMBERS);
        approvedIntents.add(GatewayIntent.GUILD_EMOJIS_AND_STICKERS);
        approvedIntents.add(GatewayIntent.GUILD_WEBHOOKS);
        approvedIntents.add(GatewayIntent.GUILD_INVITES);
        approvedIntents.add(GatewayIntent.GUILD_VOICE_STATES);
        approvedIntents.add(GatewayIntent.GUILD_MESSAGES);
        approvedIntents.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);
        approvedIntents.add(GatewayIntent.GUILD_MESSAGE_TYPING);
        approvedIntents.add(GatewayIntent.DIRECT_MESSAGES);
        approvedIntents.add(GatewayIntent.DIRECT_MESSAGE_REACTIONS);
        approvedIntents.add(GatewayIntent.DIRECT_MESSAGE_TYPING);
        approvedIntents.add(GatewayIntent.MESSAGE_CONTENT);

        return approvedIntents;
    }

}
