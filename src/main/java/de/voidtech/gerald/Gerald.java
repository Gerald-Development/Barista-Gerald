package main.java.de.voidtech.gerald;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.LoginException;

import main.java.de.voidtech.gerald.listeners.MessageListener;
import main.java.de.voidtech.gerald.listeners.ReadyListener;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Gerald {

	private static final Logger LOGGER = Logger.getLogger(Gerald.class.getName());

	private Gerald() throws LoginException {
		//TODO: read token from a config file
		JDABuilder.createDefault("NzkyODAyMjYwMTUyMjIxNzQ3.X-jApA.FyVQ_j_LCLFrl8-iT6TjFtX1Qis")
				.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)//
				.setBulkDeleteSplittingEnabled(false)//
				.setCompression(Compression.NONE)//
				.addEventListeners(new ReadyListener(), new MessageListener())//
				//TODO: store activity in database
				.setActivity(Activity.listening("to the coffee machine"))//
				.build();
	}

	public static void main(String[] args) {
		try {
			new Gerald();
		} catch (LoginException e) {
			LOGGER.log(Level.SEVERE, "An error has occurred while initilizing Gerald\n" + e.getMessage());
		}
	}
}