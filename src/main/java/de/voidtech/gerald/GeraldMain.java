package main.java.de.voidtech.gerald;

import javax.security.auth.login.LoginException;

import main.java.de.voidtech.gerald.listeners.ReadyListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class GeraldMain {

	public static void main(String[] args) {
		try {
			JDA jda = JDABuilder.createDefault("NzkyODAyMjYwMTUyMjIxNzQ3.X-jApA.2c90ENBqvv0IrcSR_-0F5LaYwFs")
					.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
					.setBulkDeleteSplittingEnabled(false).setCompression(Compression.NONE)
					.setActivity(Activity.listening("to the coffee machine"))//
					.addEventListeners(new ReadyListener())
					.build();
			
		} catch (LoginException e) {
			e.printStackTrace();
		}
	}
}