/*
	BaristaGerald A General Purpose Discord Bot
    Copyright (C) 2020-2023  Barista Gerald Dev Team (https://github.com/Gerald-Development/Barista-Gerald)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package main.java.de.voidtech.gerald;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import main.java.de.voidtech.gerald.persistence.entity.GlobalConfig;
import main.java.de.voidtech.gerald.listeners.*;
import main.java.de.voidtech.gerald.service.GeraldConfig;
import main.java.de.voidtech.gerald.service.GlobalConfigService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.internal.entities.EntityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

import javax.security.auth.login.LoginException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

@SpringBootApplication
public class Gerald {
	
	@Bean("JDA")
	@Order(3)
	@Autowired
	//TODO (from: Franziska): Just get all @Listener annotated classes or rather all EventListener implented classes?!
	public JDA getJDA(MessageListener msgListener, GuildGoneListener guildGoneListener,
			ChannelDeleteListener channelDeleteListener, GeraldConfig configService,
			GlobalConfigService globalConfService,	EventWaiter eventWaiter,
			MemberListener memberListener,	ReadyListener readyListener,
			StarboardListener starboardListener, AutoroleListener autoroleListener,
			CountingMessageDeleteListener countingListener, SlashCommandListener slashCommandListener, VoteListener voteListener) throws LoginException, InterruptedException
	{
		GlobalConfig globalConf = globalConfService.getGlobalConfig();

		//TODO (from: Franziska): WIP, DO NOT USE IN PROD
		//upsertSlashCommands(jda);
		return JDABuilder.createDefault(configService.getToken())
				.enableIntents(getApprovedIntents())
				.setMemberCachePolicy(MemberCachePolicy.ALL)
				.setBulkDeleteSplittingEnabled(false)
				.setStatus(OnlineStatus.ONLINE)
				.setCompression(Compression.NONE)
				.addEventListeners(eventWaiter,	msgListener, readyListener,	guildGoneListener, channelDeleteListener, memberListener, starboardListener, autoroleListener, countingListener, slashCommandListener, voteListener)
				.setActivity(EntityBuilder.createActivity(globalConf.getStatus(),
							 GlobalConstants.STREAM_URL,
						     globalConf.getActivity()))
				.build()
				.awaitReady();
	}
	
	private Set<GatewayIntent> getApprovedIntents()
	{
		Set<GatewayIntent> approvedIntents = new HashSet<GatewayIntent>();
		
		approvedIntents.add(GatewayIntent.GUILD_MEMBERS);
		approvedIntents.add(GatewayIntent.GUILD_BANS);
		approvedIntents.add(GatewayIntent.GUILD_EMOJIS);
		approvedIntents.add(GatewayIntent.GUILD_WEBHOOKS);
		approvedIntents.add(GatewayIntent.GUILD_INVITES);
		approvedIntents.add(GatewayIntent.GUILD_VOICE_STATES);
		approvedIntents.add(GatewayIntent.GUILD_MESSAGES);
		approvedIntents.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);
		approvedIntents.add(GatewayIntent.GUILD_MESSAGE_TYPING);
		approvedIntents.add(GatewayIntent.DIRECT_MESSAGES);
		approvedIntents.add(GatewayIntent.DIRECT_MESSAGE_REACTIONS);
		approvedIntents.add(GatewayIntent.DIRECT_MESSAGE_TYPING);
		
		return approvedIntents;
	}
	//TODO (from: Franziska): WIP, DO NOT USE IN PROD
	//private void upsertSlashCommands(JDA jda) {
		//jda.upsertCommand("ping", "Check the ping.").queue();
		//jda.upsertCommand("fact", "Get a cool fact.").queue();
		//jda.upsertCommand("slap", "Slap someone.").addOption(OptionType.USER, "user", "User to slap").queue();
	//}

	@Bean
	public EventWaiter getEventWaiter()
    {
		return new EventWaiter();
    }
	
	public static void main(String[] args) {
		SpringApplication springApp = new SpringApplication(Gerald.class);
		
		GeraldConfig configService = new GeraldConfig();
		Properties properties = new Properties();
		
		properties.put("spring.datasource.url", configService.getConnectionURL());
		properties.put("spring.datasource.username", configService.getDBUser());
		properties.put("spring.datasource.password", configService.getDBPassword());
		properties.put("spring.jpa.properties.hibernate.dialect", configService.getHibernateDialect());
		properties.put("jdbc.driver", configService.getDriver());
		properties.put("spring.jpa.hibernate.ddl-auto", "update");
		properties.put("spring.jpa.hibernate.naming.physical-strategy", "main.java.de.voidtech.gerald.persistence.CustomPhysicalNamingStrategy");
		
		springApp.setDefaultProperties(properties);		
		springApp.run(args);
	}
}