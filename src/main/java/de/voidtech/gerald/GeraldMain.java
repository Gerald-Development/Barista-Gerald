package main.java.de.voidtech.gerald;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class GeraldMain {

	public static void main(String[] args) {
		try {
			JDA jda = JDABuilder.createDefault("Use your own token here ;3")
					.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
					.setBulkDeleteSplittingEnabled(false)
					.setCompression(Compression.NONE)
					.setActivity(Activity.watching("My coffee being imported"))
					.build();
			
		} catch (LoginException e) {
			e.printStackTrace();
		}
	}

}
