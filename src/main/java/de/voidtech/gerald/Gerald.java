package main.java.de.voidtech.gerald;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.LoginException;


import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import main.java.de.voidtech.gerald.entities.GlobalConfig;
import main.java.de.voidtech.gerald.listeners.MessageListener;
import main.java.de.voidtech.gerald.listeners.ReadyListener;
import main.java.de.voidtech.gerald.service.ConfigService;
import main.java.de.voidtech.gerald.service.DatabaseService;
import main.java.de.voidtech.gerald.service.GlobalConfigService;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.entities.EntityBuilder;

public class Gerald {

	private static final Logger LOGGER = Logger.getLogger(Gerald.class.getName());

	private Gerald() throws LoginException, InterruptedException {
		ConfigService config = ConfigService.getInstance();
		DatabaseService dbService = DatabaseService.getInstance();
		GlobalConfigService globalConfService = GlobalConfigService.getInstance();

		dbService.exportSchema();
		GlobalConfig globalConf = globalConfService.getGlobalConfig();

		JDABuilder.createDefault(config.getToken()).enableCache(CacheFlag.CLIENT_STATUS)//
				.enableIntents(Arrays.asList(GatewayIntent.values()))//
				.setMemberCachePolicy(MemberCachePolicy.ALL)//
				.setBulkDeleteSplittingEnabled(false)//
				.setCompression(Compression.NONE)//
				.addEventListeners(new EventWaiter(), new ReadyListener(), new MessageListener())//
				.setActivity(EntityBuilder.createActivity(globalConf.getStatus(), GlobalConstants.STREAM_URL, globalConf.getActivity()))
				.build()//
				.awaitReady();

	}

	public static void main(String[] args) {
		try {
			new Gerald();
		} catch (LoginException | InterruptedException e) {
			LOGGER.log(Level.SEVERE, "An error has occurred while initilizing Gerald\n" + e.getMessage());
		}
	}
}