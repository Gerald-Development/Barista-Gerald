package main.java.de.voidtech.gerald;

import java.util.Arrays;
import java.util.Properties;

import javax.security.auth.login.LoginException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import main.java.de.voidtech.gerald.entities.GlobalConfig;
import main.java.de.voidtech.gerald.listeners.ChannelDeleteListener;
import main.java.de.voidtech.gerald.listeners.GuildGoneListener;
import main.java.de.voidtech.gerald.listeners.MessageListener;
import main.java.de.voidtech.gerald.listeners.ReadyListener;
import main.java.de.voidtech.gerald.service.GeraldConfig;
import main.java.de.voidtech.gerald.service.GlobalConfigService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.entities.EntityBuilder;

@SpringBootApplication
public class Gerald {
	
	@Bean
	@DependsOn(value = "sessionFactory")
	@Autowired
	public JDA getJDA(MessageListener msgListener, GuildGoneListener guildGoneListener, ChannelDeleteListener channelDeleteListener,  GeraldConfig configService, GlobalConfigService globalConfService, EventWaiter eventWaiter) throws LoginException, InterruptedException
	{
		GlobalConfig globalConf = globalConfService.getGlobalConfig();
		
		
		return JDABuilder.createDefault(configService.getToken()).enableCache(CacheFlag.CLIENT_STATUS)//
				.enableIntents(Arrays.asList(GatewayIntent.values()))//
				.setMemberCachePolicy(MemberCachePolicy.ALL)//
				.setBulkDeleteSplittingEnabled(false)//
				.setCompression(Compression.NONE)//
				.addEventListeners(eventWaiter, msgListener, new ReadyListener(), guildGoneListener, channelDeleteListener)//
				 .setActivity(EntityBuilder.createActivity(globalConf.getStatus(),
						 GlobalConstants.STREAM_URL, globalConf.getActivity()))
				.build()//
				.awaitReady();
	}
	
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
		properties.put("spring.datasource.url", configService.getConnectionURL());
		properties.put("spring.jpa.properties.hibernate.dialect", configService.getHibernateDialect());
		properties.put("jdbc.driver", configService.getDriver());
		properties.put("spring.jpa.hibernate.ddl-auto", "update");
		
//		exportSchema(configService);
		
		springApp.setDefaultProperties(properties);		
		springApp.run(args);
	}

//	private static void exportSchema(GeraldConfig configService) {
//		Properties hbnProperties = getHibernateProperties(configService);
//		
//		MetadataSources metadataSources = new MetadataSources(new StandardServiceRegistryBuilder().applySettings(hbnProperties).build());
//		
//		Set<Class<?>> annotated = new Reflections("main.java.de.voidtech.gerald").getTypesAnnotatedWith(Entity.class);
//		annotated.forEach(metadataSources::addAnnotatedClass);
//		
//		//TODO: This is highly not good. Better export to a migration file and migrate the DB after it
//		new SchemaUpdate()
//			.setFormat(true)
//			.execute(EnumSet.of(TargetType.DATABASE), metadataSources.buildMetadata());
//	}
//	
//	private static Properties getHibernateProperties(GeraldConfig configService)
//	{
//		Properties properties = new Properties();
//		properties.put(Environment.DRIVER, configService.getDriver());
//		properties.put(Environment.URL, configService.getConnectionURL());
//		properties.put(Environment.USER, configService.getDBUser());
//		properties.put(Environment.PASS, configService.getDBPassword());
//		properties.put(Environment.DIALECT, configService.getHibernateDialect());
//		
//		return properties;
//	}
}